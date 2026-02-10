package ee.tu.eewn.service;

import ee.tu.eewn.dto.WordWithDefinitionDto;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.relation.WnwbSynsetRelation;
import ee.tu.eewn.repository.DefinitionRepository;
import ee.tu.eewn.repository.SenseRepository;
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
    private final SenseRepository senseRepository;
    private final DefinitionRepository definitionRepository;

    //TODO tee seda kohutavat meetodit lühemaks
    public Map<String, List<WordWithDefinitionDto>> getSynsetRelationsData(Integer id) {
        Optional<WnwbSynset> synsetOpt = synsetRepository.findByIdWithLexicon(id);
        if (synsetOpt.isEmpty()) {
            return Collections.emptyMap();
        }
        WnwbSynset synset = synsetOpt.get();
        List<WnwbSynsetRelation> relations = synsetRelationRepository.findAllBySynset(synset);
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Integer> relatedSynsetIds = new HashSet<>();
        Map<Integer, String> synsetIdToRelationType = new HashMap<>();

        for (WnwbSynsetRelation rel : relations) {
            String type = rel.getRelType().getName();
            WnwbSynset related = null;
            if ("has_hypernym".equals(type) || "has_hyponym".equals(type)) {
                if (rel.getASynset().equals(synset)) {
                    related = rel.getBSynset();
                }
            } else {
                related = rel.getASynset().equals(synset) ? rel.getBSynset() : rel.getASynset();
            }

            if (related != null) {
                relatedSynsetIds.add(related.getId());
                synsetIdToRelationType.put(related.getId(), type);
            }
        }

        if (relatedSynsetIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<WnwbSense> allSenses = senseRepository.findBySynsetIdIn(relatedSynsetIds);
        Map<Integer, List<WnwbSense>> synsetIdToSensesMap = new HashMap<>();
        Set<Integer> senseIds = new HashSet<>();
        Set<Integer> synsetIdsForDefs = new HashSet<>();
        groupSensesBySynset(allSenses, synsetIdToSensesMap, senseIds, synsetIdsForDefs);

        Map<Integer, String> senseIdToDefinitionMap = new HashMap<>();
        //TODO midagi cachemiseks?  15min evictioniga nt?
        Map<Integer, String> synsetIdToDefinitionMap = new HashMap<>();
        loadDefinitionsForSynsets(allSenses, synsetIdsForDefs, synsetIdToDefinitionMap);

        Map<String, Set<WordWithDefinitionDto>> resultSet = new HashMap<>();
        for (Integer relatedSynsetId : relatedSynsetIds) {
            String type = synsetIdToRelationType.get(relatedSynsetId);
            List<WnwbSense> senses = synsetIdToSensesMap.get(relatedSynsetId);

            if (senses == null || senses.isEmpty()) {
                continue;
            }
            WordWithDefinitionDto dto = null;
            for (WnwbSense sense : senses) {
                String definition = synsetIdToDefinitionMap.get(sense.getSynset().getId());

                if (definition != null) {
                    dto = new WordWithDefinitionDto();
                    dto.setId(sense.getId());
                    dto.setLemma(sense.getLexicalEntry().getLemma());
                    dto.setPartOfSpeech(sense.getLexicalEntry().getPartOfSpeech());
                    dto.setDefinition(definition);
                    dto.setLabel(sense.getLabel());
                    dto.setSynsetId(sense.getSynset().getId());
                    break;
                }
            }
            if (dto == null) {
                WnwbSense sense = senses.getFirst();
                dto = new WordWithDefinitionDto();
                dto.setId(sense.getId());
                dto.setLemma(sense.getLexicalEntry().getLemma());
                dto.setPartOfSpeech(sense.getLexicalEntry().getPartOfSpeech());
                dto.setDefinition(null);
                dto.setLabel(sense.getLabel());
                if (sense.getSynset() != null) {
                    dto.setSynsetId(sense.getSynset().getId());
                }
            }

            if (dto.getSynsetId() != null) {
                List<WnwbSense> synsetSenses = synsetIdToSensesMap.get(dto.getSynsetId());
                if (synsetSenses != null) {
                    List<String> relevantWords = synsetSenses.stream()
                        .map(s -> s.getLexicalEntry().getLemma())
                        .distinct()
                        .toList();
                    dto.setRelevantWords(relevantWords);
                } else {
                    dto.setRelevantWords(List.of());
                }
            } else {
                dto.setRelevantWords(List.of());
            }
            resultSet.computeIfAbsent(type, k -> new HashSet<>()).add(dto);
        }
        Map<String, List<WordWithDefinitionDto>> result = new HashMap<>();
        for (Map.Entry<String, Set<WordWithDefinitionDto>> entry : resultSet.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }

    private void loadDefinitionsForSynsets(List<WnwbSense> allSenses, Set<Integer> synsetIdsForDefs, Map<Integer, String> synsetIdToDefinitionMap) {
        if (!allSenses.isEmpty()) {
            String language = allSenses.getFirst().getLexicalEntry().getLexicon().getLanguage();
            if (!synsetIdsForDefs.isEmpty()) {
                var synsetDefs = definitionRepository.findBySynsetIdInAndLang(synsetIdsForDefs, language);
                for (var def : synsetDefs) {
                    if (def.getSynset() != null && def.getText() != null && !def.getText().isBlank()) {
                        synsetIdToDefinitionMap.putIfAbsent(def.getSynset().getId(), def.getText());
                    }
                }
            }
        }
    }

    private static void groupSensesBySynset(List<WnwbSense> allSenses, Map<Integer, List<WnwbSense>> synsetIdToSensesMap, Set<Integer> senseIds,
                                  Set<Integer> synsetIdsForDefs) {
        for (WnwbSense sense : allSenses) {
            if (sense.getSynset() != null) {
                synsetIdToSensesMap.computeIfAbsent(sense.getSynset().getId(), k -> new ArrayList<>()).add(sense);
                senseIds.add(sense.getId());
                synsetIdsForDefs.add(sense.getSynset().getId());
            }
        }
    }
}
