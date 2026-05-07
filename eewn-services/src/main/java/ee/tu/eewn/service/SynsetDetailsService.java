package ee.tu.eewn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jooq.*;
import org.jooq.conf.Settings;
import org.springframework.stereotype.Service;

import static org.jooq.impl.DSL.*;
import static nu.studer.sample.tables.WnwbSynset.WNWB_SYNSET;
import static nu.studer.sample.tables.WnwbDefinition.WNWB_DEFINITION;
import static nu.studer.sample.tables.WnwbSense.WNWB_SENSE;
import static nu.studer.sample.tables.WnwbLexicalentry.WNWB_LEXICALENTRY;
import static nu.studer.sample.tables.WnwbSenseexample.WNWB_SENSEEXAMPLE;
import static nu.studer.sample.tables.WnwbSynsetrelation.WNWB_SYNSETRELATION;
import static nu.studer.sample.tables.WnwbSynsetrelationtype.WNWB_SYNSETRELATIONTYPE;
import static nu.studer.sample.tables.WnwbSynsettag.WNWB_SYNSETTAG;
import static nu.studer.sample.tables.WnwbTag.WNWB_TAG;
import static nu.studer.sample.tables.WnwbExternalref.WNWB_EXTERNALREF;
import static nu.studer.sample.tables.WnwbExternalsystem.WNWB_EXTERNALSYSTEM;
import static nu.studer.sample.tables.WnwbExternalrelationtype.WNWB_EXTERNALRELATIONTYPE;
import ee.tu.eewn.dto.SynsetDetailsDto;
import ee.tu.eewn.dto.SenseDto;
import ee.tu.eewn.dto.SynsetRelationForDetailsDto;
import ee.tu.eewn.dto.TagDto;
import ee.tu.eewn.dto.ExternalReferenceDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.InitializingBean;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SynsetDetailsService implements InitializingBean {
    private final DSLContext dsl;
    private final ExternalReferenceService externalReferenceService;
    private Cache<Integer, SynsetDetailsDto> synsetDetailsCache;

    @Override
    public void afterPropertiesSet() {
        synsetDetailsCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(500)
                .build();
    }

    public SynsetDetailsDto getSynsetDetails(Integer id) {
        SynsetDetailsDto cached = synsetDetailsCache.getIfPresent(id);
        if (cached != null) {
            log.debug("[SYNSET DETAILS CACHE HIT] id={}", id);
            return cached;
        }
        var origSyn = WNWB_SYNSET.as("orig_syn");
        var origSynDef = WNWB_DEFINITION.as("orig_syn_def");
        var origSense = WNWB_SENSE.as("orig_sense");
        var origLex = WNWB_LEXICALENTRY.as("orig_lex_entry");
        var origSynTag = WNWB_SYNSETTAG.as("orig_syn_tag");
        var origTag = WNWB_TAG.as("orig_tag");
        var senseExample = WNWB_SENSEEXAMPLE.as("sense_example");

        var synRel = WNWB_SYNSETRELATION.as("syn_rel");
        var synRelType = WNWB_SYNSETRELATIONTYPE.as("syn_rel_type");
        var relSyn = WNWB_SYNSET.as("rel_syn");
        var relSense = WNWB_SENSE.as("rel_sense");
        var relLex = WNWB_LEXICALENTRY.as("rel_lex_entry");

        var extRef = WNWB_EXTERNALREF.as("ext_ref");
        var extSys = WNWB_EXTERNALSYSTEM.as("ext_sys");
        var extRelType = WNWB_EXTERNALRELATIONTYPE.as("ext_rel_type");
        var extSyn = WNWB_SYNSET.as("ext_syn");
        var extDef = WNWB_DEFINITION.as("ext_def");
        Field<JSONB> emptyJsonArray = cast(inline("[]"), JSONB.class);

        Field<JSONB> definitions =
            coalesce(
                field(
                    select(jsonbArrayAgg(origSynDef.TEXT))
                        .from(origSynDef)
                        .where(origSynDef.SYNSET_ID.eq(origSyn.ID))
                        .and(origSynDef.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> examples =
            coalesce(
                field(
                    select(jsonbArrayAgg(senseExample.TEXT))
                        .from(senseExample)
                        .where(senseExample.SENSE_ID.eq(origSense.ID))
                        .and(senseExample.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> senses =
            coalesce(
                field(
                    select(jsonbArrayAgg(
                        jsonbObject(
                            key("id").value(origSense.ID),
                            key("lemma").value(origLex.LEMMA),
                            key("partOfSpeech").value(origLex.PART_OF_SPEECH),
                            key("status").value(origSense.STATUS),
                            key("comment").value(origSense.COMMENT),
                            key("label").value(origSense.LABEL),
                            key("examples").value(examples)
                        )
                    ))
                        .from(origSense)
                        .join(origLex)
                        .on(origLex.ID.eq(origSense.LEXICAL_ENTRY_ID))
                        .and(origLex.IS_DELETED.isFalse())
                        .where(origSense.SYNSET_ID.eq(origSyn.ID))
                        .and(origSense.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> relevantWords =
            coalesce(
                field(
                    select(jsonbArrayAgg(relSense.LABEL))
                        .from(relSense)
                        .join(relLex)
                        .on(relLex.ID.eq(relSense.LEXICAL_ENTRY_ID))
                        .and(relLex.IS_DELETED.isFalse())
                        .where(relSense.SYNSET_ID.eq(relSyn.ID))
                        .and(relSense.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> relations =
            coalesce(
                field(
                    select(jsonbArrayAgg(
                        jsonbObject(
                            key("type").value(synRelType.NAME),
                            key("relatedSynsetId").value(relSyn.ID),
                            key("relatedLabel").value(relSyn.LABEL),
                            key("relevantWords").value(relevantWords)
                        )
                    ))
                        .from(synRel)
                        .join(synRelType)
                        .on(synRelType.ID.eq(synRel.REL_TYPE_ID))
                        .and(synRelType.IS_DELETED.isFalse())
                        .join(relSyn)
                        .on(relSyn.ID.eq(synRel.B_SYNSET_ID))
                        .where(synRel.A_SYNSET_ID.eq(origSyn.ID))
                        .and(synRel.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> tags =
            coalesce(
                field(
                    select(jsonbArrayAgg(origTag.VALUE))
                        .from(origSynTag)
                        .join(origTag)
                        .on(origTag.ID.eq(origSynTag.TAG_ID))
                        .and(origTag.IS_DELETED.isFalse())
                        .where(origSynTag.SYNSET_ID.eq(origSyn.ID))
                        .and(origSynTag.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> externalReferences = externalReferenceService.getExternalReferences(origSense, extSyn, origLex, extRef, emptyJsonArray, extDef, extSys, extRelType, origSyn);


        DSLContext prettyDsl = dsl.configuration()
                                  .derive(new Settings().withRenderFormatted(true))
                                  .dsl();


        var query = dsl.select(
                           origSyn.ID,
                           origSyn.LABEL,
                           origSyn.SYNSET_TYPE,
                           origSyn.STATUS,
                           origSyn.COMMENT,
                           definitions,
                           senses,
                           relations,
                           tags,
                           externalReferences
                       )
                       .from(origSyn)
                       .where(origSyn.ID.eq(id))
                       .and(origSyn.IS_DELETED.isFalse());

        log.debug("[SYNSET DETAILS SQL]\n{}", prettyDsl.renderInlined(query));
        var synsetDetailsRecord = query.fetchOne();

        if (synsetDetailsRecord == null) return null;

        SynsetDetailsDto dto = new SynsetDetailsDto();
        dto.setId(synsetDetailsRecord.get(origSyn.ID));
        dto.setLabel(synsetDetailsRecord.get(origSyn.LABEL));
        dto.setSynsetType(synsetDetailsRecord.get(origSyn.SYNSET_TYPE));
        dto.setStatus(synsetDetailsRecord.get(origSyn.STATUS));
        dto.setComment(synsetDetailsRecord.get(origSyn.COMMENT));
        dto.setDefinitions(jsonbToStringList(synsetDetailsRecord.get(definitions)));
        JSONB sensesJson = synsetDetailsRecord.get(senses);
        dto.setSenses(jsonbToSenseDtoList(sensesJson));
        dto.setRelations(jsonbToSynsetRelationDtoList(synsetDetailsRecord.get(relations)));
        dto.setTags(jsonbToStringList(synsetDetailsRecord.get(tags)));
        dto.setExternalReferences(jsonbToExternalReferenceList(synsetDetailsRecord.get(externalReferences)));
        log.debug("[SYNSET DETAILS] {}", dto);
        synsetDetailsCache.put(id, dto);
        return dto;
    }

    private static <T> List<T> jsonbToList(JSONB jsonb, TypeReference<List<T>> typeRef) {
        ObjectMapper mapper = new ObjectMapper();
        if (jsonb == null) return List.of();
        try {
            return mapper.readValue(jsonb.data(), typeRef);
        } catch (Exception e) {
            log.error("[JSONB MAPPING ERROR] {}", e.getMessage());
            return List.of();
        }
    }

    private static List<SenseDto> jsonbToSenseDtoList(JSONB jsonb) {
        return jsonbToList(jsonb, new TypeReference<>() {});
    }
    private static List<SynsetRelationForDetailsDto> jsonbToSynsetRelationDtoList(JSONB jsonb) {
        return jsonbToList(jsonb, new TypeReference<>() {});
    }
    private static List<TagDto> jsonbToTagDtoList(JSONB jsonb) {
        return jsonbToList(jsonb, new TypeReference<>() {});
    }
    private static List<String> jsonbToStringList(JSONB jsonb) {
        return jsonbToList(jsonb, new TypeReference<>() {});
    }
    private static List<ExternalReferenceDto> jsonbToExternalReferenceList(JSONB jsonb) {
        return jsonbToList(jsonb, new TypeReference<>() {});
    }
}
