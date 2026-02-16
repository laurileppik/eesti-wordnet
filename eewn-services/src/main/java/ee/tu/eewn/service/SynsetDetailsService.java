package ee.tu.eewn.service;

import ee.tu.eewn.dto.*;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.core.WnwbDefinition;
import ee.tu.eewn.entity.relation.WnwbSynsettag;
import ee.tu.eewn.repository.DefinitionRepository;
import ee.tu.eewn.repository.WnwbSynsetRepository;
import ee.tu.eewn.repository.SenseRepository;
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
    private final ExternalReferenceService externalReferenceService;

    public SynsetDetailsDto getSynsetDetails(Integer id) {
        WnwbSynset synset = synsetRepository.findByIdWithLexiconAndDefinitions(id).orElse(null);
        if (synset == null) {
            return null;
        }

        List<WnwbDefinition> definitions = definitionRepository.findBySynsetIdAndLang(id, "est");
        List<WnwbSynsettag> tags = synsettagRepository.findBySynset(synset);
        List<WnwbSense> senses = senseRepository.findBySynsetId(id);
        Map<String, List<WordWithRelationsDto>> relationsMap = synsetRelationService.getSynsetRelationsData(synset, id, false);
        List<SynsetRelationDto> relationDtos = new ArrayList<>();
        for (Map.Entry<String, List<WordWithRelationsDto>> entry : relationsMap.entrySet()) {
            String type = entry.getKey();
            for (WordWithRelationsDto dto : entry.getValue()) {
                relationDtos.add(new SynsetRelationDto(type, dto.getSynsetId(), dto.getLemma(), dto.getRelevantWords()));
            }
        }

        List<ExternalReferenceDto> externalRefs = externalReferenceService.getExternalReferences(synset);

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
            tags.stream().map(t -> new TagDto(t.getTag().getId(), t.getTag().getCategory(), t.getTag().getValue())).toList(),
            externalRefs
        );
    }
}
