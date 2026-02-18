package ee.tu.eewn.service;

import ee.tu.eewn.dto.ExternalReferenceDto;
import ee.tu.eewn.dto.WordWithRelationsDto;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.relation.WnwbSynsetRelation;
import ee.tu.eewn.repository.SynsetRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
//TODO vt üle ma arvan, et siin on osad db query-d ebavajalikud ja saame vähemaga
public class SynsetRelationService {
    private final SynsetRelationRepository synsetRelationRepository;
    private final DataFetchService dataFetchService;
    private final ExternalReferenceService externalReferenceService;

    /**
     * includeDefinitions - kui on true, siis definitsioonid fetchitakse ka
     * */
    public Map<String, List<WordWithRelationsDto>> getSynsetRelationsData(WnwbSynset synset, Integer id, boolean includeDefinitions) {
        if (synset == null) {
            return Collections.emptyMap();
        }
        List<WnwbSynsetRelation> relations = synsetRelationRepository.findAllBySynset(synset);
        log.info("Found {} relations for synset id {}", relations, id);
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, String> relTypesBySynsetId = findRelatedSynsets(synset, relations);
        if (relTypesBySynsetId.isEmpty()) {
            return Collections.emptyMap();
        }
        log.info("Found {} related synsets for synset id {}", relTypesBySynsetId, id);

        Map<Integer, List<WnwbSense>> sensesBySynsetId = dataFetchService.groupSensesBySynsetId(
            relTypesBySynsetId.keySet()
        );
        Map<Integer, String> definitionsBySynsetId = Collections.emptyMap();
        if (includeDefinitions) {
            String language = getLanguageFromSenses(sensesBySynsetId);
            definitionsBySynsetId = dataFetchService.getDefinitionsForSynsets(relTypesBySynsetId.keySet(), language);
        }

        Map<Integer, List<ExternalReferenceDto>> externalReferencesBySynsetId = externalReferenceService.getExternalReferencesForSynsets(
            sensesBySynsetId.keySet().stream()
                .map(sid -> sensesBySynsetId.get(sid).getFirst().getSynset())
                .filter(Objects::nonNull)
                .toList()
        );

        return buildWordWithRelations(relTypesBySynsetId, sensesBySynsetId, definitionsBySynsetId, externalReferencesBySynsetId);
    }

    //TODO see on veits bs, kas ei ole alsti est?
    private String getLanguageFromSenses(Map<Integer, List<WnwbSense>> sensesBySynsetId) {
        if (sensesBySynsetId.isEmpty()) {
            return "est";
        }
        List<WnwbSense> firstSenseList = sensesBySynsetId.values().iterator().next();
        if (firstSenseList.isEmpty()) {
            return "est";
        }
        return firstSenseList.getFirst().getLexicalEntry().getLexicon().getLanguage();
    }

    private Map<Integer, String> findRelatedSynsets(WnwbSynset synset, List<WnwbSynsetRelation> relations) {
        Map<Integer, String> synsetIdToRelationType = new HashMap<>();
        for (WnwbSynsetRelation rel : relations) {
            String type = rel.getRelType().getName();
            WnwbSynset related = rel.getASynset().equals(synset) ? rel.getBSynset() : rel.getASynset();
            if (related != null) {
                synsetIdToRelationType.put(related.getId(), type);
            }
        }
        return synsetIdToRelationType;
    }

    private Map<String, List<WordWithRelationsDto>> buildWordWithRelations(
        Map<Integer, String> relTypesBySynsetId,
        Map<Integer, List<WnwbSense>> sensesBySynsetId,
        Map<Integer, String> definitions,
        Map<Integer, List<ExternalReferenceDto>> externalReferencesBySynsetId
    ) {
        Map<String, List<WordWithRelationsDto>> result = new HashMap<>();
        for (Map.Entry<Integer, String> entry : relTypesBySynsetId.entrySet()) {
            Integer synsetId = entry.getKey();
            String relationType = entry.getValue();
            List<WnwbSense> senses = sensesBySynsetId.get(synsetId);
            if (senses == null || senses.isEmpty()) {
                continue;
            }

            WordWithRelationsDto dto = createWordDto(senses, definitions, externalReferencesBySynsetId);
            List<String> relevantWords = new ArrayList<>();
            for (WnwbSense sense : senses) {
                String lemma = sense.getLexicalEntry().getLemma();
                if (!relevantWords.contains(lemma)) {
                    relevantWords.add(lemma);
                }
            }
            dto.setRelevantWords(relevantWords);
            result.computeIfAbsent(relationType, k -> new ArrayList<>()).add(dto);
        }
        return result;
    }

    private WordWithRelationsDto createWordDto(List<WnwbSense> senses, Map<Integer, String> definitions, Map<Integer, List<ExternalReferenceDto>> externalReferencesBySynsetId) {
        for (WnwbSense sense : senses) {
            String def = definitions.get(sense.getSynset().getId());
            if (def != null) {
                return buildWordDto(sense, def, externalReferencesBySynsetId);
            }
        }
        return buildWordDto(senses.getFirst(), null, externalReferencesBySynsetId);
    }

    //TODO äkki saaks definitioni mõistlikumalt?
    private WordWithRelationsDto buildWordDto(WnwbSense sense, String definition, Map<Integer, List<ExternalReferenceDto>> externalReferencesBySynsetId) {
        WordWithRelationsDto dto = new WordWithRelationsDto();
        dto.setId(sense.getId());
        dto.setLemma(sense.getLexicalEntry().getLemma());
        dto.setPartOfSpeech(sense.getLexicalEntry().getPartOfSpeech());
        dto.setDefinition(definition);
        dto.setLabel(sense.getLabel());
        if (sense.getSynset() != null) {
            dto.setSynsetId(sense.getSynset().getId());
            List<ExternalReferenceDto> externalRefs = externalReferencesBySynsetId.get(sense.getSynset().getId());
            dto.setExternalReferences(externalRefs);
        }
        return dto;
    }
}
