package ee.tu.eewn.service;


import ee.tu.eewn.dto.ExternalReferenceDto;
import ee.tu.eewn.dto.WordWithRelationsDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.jooq.*;
import org.jooq.JSONB;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.jooq.impl.DSL.*;
import static nu.studer.sample.tables.WnwbSynset.WNWB_SYNSET;
import static nu.studer.sample.tables.WnwbDefinition.WNWB_DEFINITION;
import static nu.studer.sample.tables.WnwbSense.WNWB_SENSE;
import static nu.studer.sample.tables.WnwbLexicalentry.WNWB_LEXICALENTRY;
import static nu.studer.sample.tables.WnwbExternalref.WNWB_EXTERNALREF;
import static nu.studer.sample.tables.WnwbExternalsystem.WNWB_EXTERNALSYSTEM;
import static nu.studer.sample.tables.WnwbExternalrelationtype.WNWB_EXTERNALRELATIONTYPE;
import static nu.studer.sample.tables.WnwbLexicon.WNWB_LEXICON;

@Service
@RequiredArgsConstructor
public class WordService implements InitializingBean {
    private final ExternalReferenceService externalReferenceService;
    private final DSLContext dsl;
    private Cache<String, List<WordWithRelationsDto>> searchCache;

    @Override
    public void afterPropertiesSet() {
        searchCache = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    public List<WordWithRelationsDto> searchWords(String query) {
        var origSense = WNWB_SENSE.as("orig_sense");
        var origLex = WNWB_LEXICALENTRY.as("orig_lex_entry");
        var origSyn = WNWB_SYNSET.as("orig_syn");
        var origDef = WNWB_DEFINITION.as("orig_def");
        var extRef = WNWB_EXTERNALREF.as("ext_ref");
        var extSys = WNWB_EXTERNALSYSTEM.as("ext_sys");
        var extRelType = WNWB_EXTERNALRELATIONTYPE.as("ext_rel_type");
        var lexicon = WNWB_LEXICON.as("lexicon");
        Field<JSONB> emptyJsonArray = cast(inline("[]"), JSONB.class);
        Field<JSONB> relevantWords = getRelevantWordsField(origSense, origSyn, emptyJsonArray);

        Field<String> definition = coalesce(
            field(
                select(origDef.TEXT)
                    .from(origDef)
                    .where(origDef.SENSE_ID.eq(origSense.ID))
                    .limit(1)),
            field(
                select(origDef.TEXT)
                    .from(origDef)
                    .where(origDef.SYNSET_ID.eq(origSyn.ID))
                    .limit(1))
        );

        Field<JSONB> externalReferences = externalReferenceService.getExternalReferences(
            origSense, origSyn, origLex, extRef, emptyJsonArray, origDef, extSys, extRelType, origSyn
        );

        Field<Integer> idField = origSense.ID.as("id");
        Field<String> lemmaField = origLex.LEMMA.as("lemma");
        Field<String> posField = origLex.PART_OF_SPEECH.as("partOfSpeech");
        Field<String> labelField = origSense.LABEL.as("label");
        Field<Integer> synsetIdField = origSyn.ID.as("synsetId");
        Field<JSONB> relevantWordsField = relevantWords.as("relevantWords");
        Field<JSONB> externalReferencesField = externalReferences.as("externalReferences");
        Field<String> definitionField = definition.as("definition");

        var result = dsl.select(
                idField,
                lemmaField,
                posField,
                definitionField,
                labelField,
                synsetIdField,
                relevantWordsField,
                externalReferencesField
            )
            .from(origSense)
            .join(origLex).on(origLex.ID.eq(origSense.LEXICAL_ENTRY_ID)).and(origLex.IS_DELETED.isFalse())
            .join(origSyn).on(origSyn.ID.eq(origSense.SYNSET_ID)).and(origSyn.IS_DELETED.isFalse())
            .join(lexicon).on(origLex.LEXICON_ID.eq(lexicon.ID)).and(lexicon.IS_DELETED.isFalse())
            .where(lower(origLex.LEMMA).eq(query.toLowerCase()))
            .and(lexicon.LANGUAGE.eq("est"))
            .and(origSense.IS_DELETED.isFalse())
            .fetch();

        return result.stream().map(r -> {
            WordWithRelationsDto dto = new WordWithRelationsDto();
            dto.setId(r.get(idField));
            dto.setLemma(r.get(lemmaField));
            dto.setPartOfSpeech(r.get(posField));
            dto.setDefinition(r.get(definitionField));
            dto.setLabel(r.get(labelField));
            dto.setSynsetId(r.get(synsetIdField));
            dto.setRelevantWords(jsonbToStringList(r.get(relevantWordsField)));
            dto.setExternalReferences(jsonbToExternalReferenceList(r.get(externalReferencesField)));
            return dto;
        }).toList();
    }

    private static Field<JSONB> getRelevantWordsField(
            nu.studer.sample.tables.WnwbSense origSense,
            nu.studer.sample.tables.WnwbSynset origSyn,
            Field<JSONB> emptyJsonArray
    ) {
        return coalesce(
            field(
                select(jsonbArrayAgg(origSense.LABEL))
                    .from(origSense)
                    .where(origSense.SYNSET_ID.eq(origSyn.ID))
                    .and(origSense.IS_DELETED.isFalse())
            ),
            emptyJsonArray
        );
    }

    private static List<String> jsonbToStringList(JSONB jsonb) {
        if (jsonb == null) return List.of();
        try {
            return new ObjectMapper()
                .readValue(jsonb.data(), new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private static List<ExternalReferenceDto> jsonbToExternalReferenceList(JSONB jsonb) {
        if (jsonb == null) return List.of();
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> list = mapper.readValue(jsonb.data(), new TypeReference<>() {});
            return list.stream().map(map -> {
                ExternalReferenceDto dto = new ExternalReferenceDto();
                dto.setSystemName((String) map.get("systemName"));
                dto.setRelationType((String) map.get("relationType"));
                dto.setReference((String) map.get("reference"));
                Object wordsObj = map.get("words");
                List<String> words = (wordsObj instanceof List)
                    ? ((List<?>) wordsObj).stream().filter(String.class::isInstance).map(x -> (String)x).toList()
                    : List.of();
                dto.setWords(words);
                dto.setDefinition((String) map.get("definition"));
                return dto;
            }).toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
