package ee.tu.eewn.controller;

import ee.tu.eewn.dto.WordWithDefinitionDto;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.relation.WnwbSynsetRelation;
import ee.tu.eewn.repository.DefinitionRepository;
import ee.tu.eewn.repository.SenseRepository;
import ee.tu.eewn.repository.SynsetRelationRepository;
import ee.tu.eewn.repository.WnwbSynsetRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/synset")
@RequiredArgsConstructor
public class SynsetRelationController {
    private final SynsetRelationRepository synsetRelationRepository;
    private final WnwbSynsetRepository synsetRepository;
    private final SenseRepository senseRepository;
    private final DefinitionRepository definitionRepository;

    @GetMapping("/{id}/relations")
    public Map<String, List<WordWithDefinitionDto>> getSynsetRelations(@PathVariable Integer id) {
        Optional<WnwbSynset> synsetOpt = synsetRepository.findById(id);
        if (synsetOpt.isEmpty()) {
            return Collections.emptyMap();
        }
        WnwbSynset synset = synsetOpt.get();
        List<WnwbSynsetRelation> relations = synsetRelationRepository.findAllBySynset(synset);
        Map<String, List<WordWithDefinitionDto>> result = new HashMap<>();
        for (WnwbSynsetRelation rel : relations) {
            String type = rel.getRelType().getName();
            WnwbSynset related = null;
            if ("has_hypernym".equals(type)) {
                if (rel.getASynset().equals(synset)) {
                    related = rel.getBSynset();
                }
            } else if ("has_hyponym".equals(type)) {
                if (rel.getASynset().equals(synset)) {
                    related = rel.getBSynset();
                }
            } else {
                related = rel.getASynset().equals(synset) ? rel.getBSynset() : rel.getASynset();
            }
            if (related == null) continue;
            List<WnwbSense> senses = senseRepository.findBySynsetId(related.getId());
            WordWithDefinitionDto dto = null;
            for (WnwbSense sense : senses) {
                String definition = null;
                var defs = definitionRepository.findBySenseIdAndLang(sense.getId(), sense.getLexicalEntry().getLexicon().getLanguage());
                if (!defs.isEmpty() && defs.get(0).getText() != null && !defs.get(0).getText().isBlank()) {
                    definition = defs.get(0).getText();
                } else if (sense.getSynset() != null) {
                    var synsetDefs = definitionRepository.findBySynsetIdAndLang(sense.getSynset().getId(), sense.getLexicalEntry().getLexicon().getLanguage());
                    if (!synsetDefs.isEmpty() && synsetDefs.get(0).getText() != null && !synsetDefs.get(0).getText().isBlank()) {
                        definition = synsetDefs.get(0).getText();
                    }
                }
                if (definition != null) {
                    dto = new WordWithDefinitionDto();
                    dto.setId(sense.getId());
                    dto.setLemma(sense.getLexicalEntry().getLemma());
                    dto.setPartOfSpeech(sense.getLexicalEntry().getPartOfSpeech());
                    dto.setDefinition(definition);
                    dto.setLabel(sense.getLabel());
                    if (sense.getSynset() != null) {
                        dto.setSynsetId(sense.getSynset().getId());
                    }
                    break;
                }
            }
            if (dto == null && !senses.isEmpty()) {
                WnwbSense sense = senses.get(0);
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
            if (dto != null) {
                result.computeIfAbsent(type, k -> new ArrayList<>()).add(dto);
            }
        }
        return result;
    }
}
