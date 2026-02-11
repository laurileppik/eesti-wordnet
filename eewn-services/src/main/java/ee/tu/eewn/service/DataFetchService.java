package ee.tu.eewn.service;

import ee.tu.eewn.entity.core.WnwbDefinition;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.repository.DefinitionRepository;
import ee.tu.eewn.repository.SenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DataFetchService {
    private final SenseRepository senseRepository;
    private final DefinitionRepository definitionRepository;

    public String getDefinitionForSense(WnwbSense sense, String language) {
        List<WnwbDefinition> defs = definitionRepository.findBySenseIdAndLang(sense.getId(), language);
        if (!defs.isEmpty()) {
            return defs.getFirst().getText();
        }
        if (sense.getSynset() != null) {
            List<WnwbDefinition> synsetDefs = definitionRepository.findBySynsetIdAndLang(
                sense.getSynset().getId(), language
            );
            if (!synsetDefs.isEmpty()) {
                return synsetDefs.getFirst().getText();
            }
        }
        return null;
    }

    public List<String> getLemmasForSynset(Integer synsetId) {
        return senseRepository.findBySynsetId(synsetId).stream()
            .map(s -> s.getLexicalEntry().getLemma())
            .distinct()
            .toList();
    }

    public Map<Integer, List<WnwbSense>> groupSensesBySynsetId(Set<Integer> synsetIds) {
        if (synsetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<WnwbSense> senses = senseRepository.findBySynsetIdIn(synsetIds);
        Map<Integer, List<WnwbSense>> grouped = new HashMap<>();
        for (WnwbSense sense : senses) {
            if (sense.getSynset() != null) {
                grouped.computeIfAbsent(sense.getSynset().getId(), k -> new ArrayList<>()).add(sense);
            }
        }
        return grouped;
    }

    public Map<Integer, String> getDefinitionsForSynsets(Set<Integer> synsetIds, String language) {
        if (synsetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<WnwbDefinition> definitions = definitionRepository.findBySynsetIdInAndLang(synsetIds, language);
        Map<Integer, String> definitionMap = new HashMap<>();
        for (WnwbDefinition def : definitions) {
            if (def.getSynset() != null && def.getText() != null && !def.getText().isBlank()) {
                definitionMap.putIfAbsent(def.getSynset().getId(), def.getText());
            }
        }
        return definitionMap;
    }
}
