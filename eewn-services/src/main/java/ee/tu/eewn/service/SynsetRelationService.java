package ee.tu.eewn.service;

import ee.tu.eewn.dto.WordWithDefinitionDto;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.relation.WnwbSynsetRelation;
import ee.tu.eewn.repository.SynsetRelationRepository;
import ee.tu.eewn.repository.WnwbSynsetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SynsetRelationService {
    private final SynsetRelationRepository synsetRelationRepository;
    private final WnwbSynsetRepository synsetRepository;
    private final DataFetchService dataFetchService;

    public Map<String, List<WordWithDefinitionDto>> getSynsetRelationsData(Integer id) {
        WnwbSynset synset = synsetRepository.findByIdWithLexicon(id).orElse(null);
        if (synset == null) {
            return Collections.emptyMap();
        }
        List<WnwbSynsetRelation> relations = synsetRelationRepository.findAllBySynset(synset);
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, String> relTypesBySynsetId = findRelatedSynsets(synset, relations);
        if (relTypesBySynsetId.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, List<WnwbSense>> sensesBySynsetId = dataFetchService.groupSensesBySynsetId(
            relTypesBySynsetId.keySet()
        );
        String language = getLanguageFromSenses(sensesBySynsetId);
        Map<Integer, String> definitionsBySynsetId = dataFetchService.getDefinitionsForSynsets(
            relTypesBySynsetId.keySet(), language
        );

        return buildWordWithRelations(relTypesBySynsetId, sensesBySynsetId, definitionsBySynsetId);
    }

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

    private Map<String, List<WordWithDefinitionDto>> buildWordWithRelations(
        Map<Integer, String> relTypesBySynsetId,
        Map<Integer, List<WnwbSense>> sensesBySynsetId,
        Map<Integer, String> definitions
    ) {
        Map<String, List<WordWithDefinitionDto>> result = new HashMap<>();
        for (Map.Entry<Integer, String> entry : relTypesBySynsetId.entrySet()) {
            Integer synsetId = entry.getKey();
            String relationType = entry.getValue();
            List<WnwbSense> senses = sensesBySynsetId.get(synsetId);
            if (senses == null || senses.isEmpty()) {
                continue;
            }

            WordWithDefinitionDto dto = createWordDto(senses, definitions);
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

    private WordWithDefinitionDto createWordDto(List<WnwbSense> senses, Map<Integer, String> definitions) {
        for (WnwbSense sense : senses) {
            String def = definitions.get(sense.getSynset().getId());
            if (def != null) {
                return buildWordDto(sense, def);
            }
        }
        return buildWordDto(senses.getFirst(), null);
    }

    //TODO äkki saaks definitioni mõistlikumalt?
    private WordWithDefinitionDto buildWordDto(WnwbSense sense, String definition) {
        WordWithDefinitionDto dto = new WordWithDefinitionDto();
        dto.setId(sense.getId());
        dto.setLemma(sense.getLexicalEntry().getLemma());
        dto.setPartOfSpeech(sense.getLexicalEntry().getPartOfSpeech());
        dto.setDefinition(definition);
        dto.setLabel(sense.getLabel());
        if (sense.getSynset() != null) {
            dto.setSynsetId(sense.getSynset().getId());
        }
        return dto;
    }
}
