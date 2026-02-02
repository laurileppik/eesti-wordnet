package ee.tu.eewn.service;

import ee.tu.eewn.dto.*;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.core.WnwbDefinition;
import ee.tu.eewn.entity.relation.WnwbSynsettag;
import ee.tu.eewn.repository.WnwbSynsetRepository;
import ee.tu.eewn.repository.SenseRepository;
import ee.tu.eewn.repository.DefinitionRepository;
import ee.tu.eewn.repository.WnwbSynsettagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SynsetDetailsService {
    private final WnwbSynsetRepository synsetRepository;
    private final SenseRepository senseRepository;
    private final DefinitionRepository definitionRepository;
    private final WnwbSynsettagRepository synsettagRepository;
    private final SynsetRelationService synsetRelationService;

    public SynsetDetailsDto getSynsetDetails(Integer id) {
        Optional<WnwbSynset> synsetOpt = synsetRepository.findById(id);
        if (synsetOpt.isEmpty()) return null;
        WnwbSynset synset = synsetOpt.get();
        List<WnwbDefinition> definitions = definitionRepository.findBySynsetIdAndLang(id, "est");
        List<WnwbSense> senses = senseRepository.findBySynsetId(id);
        List<WnwbSynsettag> tags = synsettagRepository.findBySynset(synset);
        Map<String, List<WordWithDefinitionDto>> relationsMap = synsetRelationService.getSynsetRelationsData(id);
        List<SynsetRelationDto> relationDtos = new ArrayList<>();
        for (Map.Entry<String, List<WordWithDefinitionDto>> entry : relationsMap.entrySet()) {
            String type = entry.getKey();
            for (WordWithDefinitionDto dto : entry.getValue()) {
                relationDtos.add(new SynsetRelationDto(type, dto.getSynsetId(), dto.getLemma(), dto.getRelevantWords()));
            }
        }

        return new SynsetDetailsDto(
            synset.getId(),
            synset.getLabel(),
            synset.getSynsetType(),
            synset.getStatus(),
            synset.getComment(),
            definitions.stream().map(WnwbDefinition::getText).toList(),
            senses.stream().map(s -> new SenseDto(
                s.getId(),
                s.getLexicalEntry().getLemma(),
                s.getLexicalEntry().getPartOfSpeech(),
                s.getStatus(),
                s.getComment(),
                s.getLabel()
            )).toList(),
            relationDtos,
            tags.stream().map(t -> new TagDto(t.getTag().getId(), t.getTag().getCategory(), t.getTag().getValue())).toList()
        );
    }
}
