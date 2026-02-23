package ee.tu.eewn.service;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.jsonbArrayAgg;
import static org.jooq.impl.DSL.jsonbObject;
import static org.jooq.impl.DSL.key;
import static org.jooq.impl.DSL.select;

import ee.tu.eewn.dto.ExternalReferenceDto;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.external.WnwbExternalref;
import ee.tu.eewn.repository.WnwbExternalrefRepository;
import ee.tu.eewn.repository.WnwbSynsetRepository;
import lombok.RequiredArgsConstructor;
import nu.studer.sample.tables.WnwbDefinition;
import nu.studer.sample.tables.WnwbExternalrelationtype;
import nu.studer.sample.tables.WnwbExternalsystem;
import nu.studer.sample.tables.WnwbLexicalentry;

import org.jooq.Field;
import org.jooq.JSONB;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
//TODO vt üle ma arvan, et siin on osad db query-d ebavajalikud ja saame vähemaga
public class ExternalReferenceService {
    private final WnwbExternalrefRepository externalrefRepository;
    private final WnwbSynsetRepository synsetRepository;
    private final DataFetchService dataFetchService;
    public List<ExternalReferenceDto> getExternalReferences(WnwbSynset synset) {
        List<WnwbExternalref> externalRefs = externalrefRepository.findBySynset(synset);
        Set<String> references = findReferences(externalRefs);
        if (references.isEmpty()) {
            //TODO pigem pole vaja ju, tühi list vms
            return buildSimpleReferenceDtos(externalRefs);
        }

        Map<String, WnwbSynset> synsetsByLabel = findExternalSynsets(references);
        Set<Integer> synsetIds = new HashSet<>();
        for (WnwbSynset s : synsetsByLabel.values()) {
            synsetIds.add(s.getId());
        }

        Map<Integer, List<WnwbSense>> sensesBySynsetId = dataFetchService.groupSensesBySynsetId(synsetIds);
        Map<Integer, String> definitionsBySynsetId = dataFetchService.getDefinitionsForSynsets(synsetIds, "eng");

        return buildExternalReferences(externalRefs, synsetsByLabel, sensesBySynsetId, definitionsBySynsetId);
    }

    private Set<String> findReferences(List<WnwbExternalref> externalRefs) {
        Set<String> references = new HashSet<>();
        for (WnwbExternalref ref : externalRefs) {
            if (ref.getReference() != null && !ref.getReference().isBlank()) {
                references.add(ref.getReference());
            }
        }
        return references;
    }

    private List<ExternalReferenceDto> buildSimpleReferenceDtos(List<WnwbExternalref> externalRefs) {
        List<ExternalReferenceDto> dtos = new ArrayList<>();
        for (WnwbExternalref ref : externalRefs) {
            String systemName = ref.getSysId() != null ? ref.getSysId().getName() : "Unknown";
            String relationType = ref.getRelType() != null ? ref.getRelType().getName() : "";
            dtos.add(new ExternalReferenceDto(systemName, relationType, ref.getReference(),
                Collections.singletonList(ref.getReference()), ""));
        }
        return dtos;
    }

    private Map<String, WnwbSynset> findExternalSynsets(Set<String> references) {
        //TODO est ja eng jaoks mingi ENUM
        //Leia labeli põhised ik synsetid
        List<WnwbSynset> englishSynsets = synsetRepository.findByLabelInAndLanguage(references, "eng");
        Map<String, WnwbSynset> labelToSynsetMap = new HashMap<>();
        for (WnwbSynset synset : englishSynsets) {
            labelToSynsetMap.put(synset.getLabel(), synset);
        }
        return labelToSynsetMap;
    }


    private List<ExternalReferenceDto> buildExternalReferences(
            List<WnwbExternalref> externalRefs,
            Map<String, WnwbSynset> synsetsByLabel,
            Map<Integer, List<WnwbSense>> sensesBySynsetId,
            Map<Integer, String> definitionsBySynsetId) {
        List<ExternalReferenceDto> dtos = new ArrayList<>();
        for (WnwbExternalref ref : externalRefs) {
            String systemName = ref.getSysId() != null ? ref.getSysId().getName() : "Unknown";
            String relationType = ref.getRelType() != null ? ref.getRelType().getName() : "";
            String reference = ref.getReference();
            List<String> words = extractWords(reference, synsetsByLabel, sensesBySynsetId);
            String definition = extractDefinition(reference, synsetsByLabel, definitionsBySynsetId);
            dtos.add(new ExternalReferenceDto(systemName, relationType, reference, words, definition));
        }
        return dtos;
    }

    private List<String> extractWords(String reference, Map<String, WnwbSynset> synsetsByLabel,
                                      Map<Integer, List<WnwbSense>> sensesBySynsetId) {
        List<String> words = new ArrayList<>();
        if (reference == null || reference.isBlank()) {
            return words;
        }
        WnwbSynset englishSynset = synsetsByLabel.get(reference);
        if (englishSynset == null) {
            words.add(reference);
            return words;
        }
        List<WnwbSense> senses = sensesBySynsetId.get(englishSynset.getId());
        if (senses != null) {
            for (WnwbSense sense : senses) {
                words.add(formatSenseLabel(sense));
            }
        }

        if (words.isEmpty()) {
            words.add(reference);
        }
        return words;
    }

    private String formatSenseLabel(WnwbSense sense) {
        String label = sense.getLabel();
        return label != null && !label.isBlank() ? label : "";
    }

    private String extractDefinition(String reference, Map<String, WnwbSynset> synsetsByLabel,
                                     Map<Integer, String> definitionsBySynsetId) {
        if (reference == null || reference.isBlank()) {
            return "";
        }

        WnwbSynset synset = synsetsByLabel.get(reference);
        if (synset == null) {
            return "";
        }

        String definition = definitionsBySynsetId.get(synset.getId());
        return definition != null ? definition : "";
    }

    public Field<JSONB> getExternalReferences(nu.studer.sample.tables.WnwbSense origSense, nu.studer.sample.tables.WnwbSynset extSyn, WnwbLexicalentry origLex, nu.studer.sample.tables.WnwbExternalref extRef,
                                                     Field<JSONB> emptyJsonArray, WnwbDefinition extDef, WnwbExternalsystem extSys,
                                                     WnwbExternalrelationtype extRelType, nu.studer.sample.tables.WnwbSynset relSyn) {
        Field<JSONB> extWords =
            coalesce(
                field(
                    select(jsonbArrayAgg(origSense.LABEL))
                        .from(extSyn)
                        .join(origSense)
                        .on(origSense.SYNSET_ID.eq(extSyn.ID))
                        .and(origSense.IS_DELETED.isFalse())
                        .join(origLex)
                        .on(origLex.ID.eq(origSense.LEXICAL_ENTRY_ID))
                        .and(origLex.IS_DELETED.isFalse())
                        .where(extSyn.LABEL.eq(extRef.REFERENCE))
                ), emptyJsonArray
            );

        Field<String> extDefinition =
            field(
                select(extDef.TEXT)
                    .from(extSyn)
                    .join(extDef)
                    .on(extDef.SYNSET_ID.eq(extSyn.ID))
                    .and(extDef.IS_DELETED.isFalse())
                    .where(extSyn.LABEL.eq(extRef.REFERENCE))
                    .limit(1)
            );

        return coalesce(
            field(
                select(jsonbArrayAgg(
                    jsonbObject(
                        key("systemName").value(extSys.NAME),
                        key("relationType").value(extRelType.NAME),
                        key("reference").value(extRef.REFERENCE),
                        key("words").value(extWords),
                        key("definition").value(extDefinition)
                    )
                ))
                    .from(extRef)
                    .join(extSys)
                    .on(extSys.ID.eq(extRef.SYS_ID_ID))
                    .join(extRelType)
                    .on(extRelType.ID.eq(extRef.REL_TYPE_ID))
                    .where(extRef.SYNSET_ID.eq(relSyn.ID))
            ),
            emptyJsonArray
        );
    }
}
