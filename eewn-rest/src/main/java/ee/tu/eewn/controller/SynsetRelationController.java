package ee.tu.eewn.controller;

import ee.tu.eewn.dto.WordWithRelationsDto;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.repository.WnwbSynsetRepository;
import ee.tu.eewn.service.SynsetRelationService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/synset")
@RequiredArgsConstructor
public class SynsetRelationController {
    private final SynsetRelationService synsetRelationService;
    private final WnwbSynsetRepository synsetRepository;

    @GetMapping("/{id}/relations")
    public Map<String, List<WordWithRelationsDto>> getSynsetRelations(@PathVariable Integer id) {
        //TODO ei ole leksikon ju enam, muudatustega ebavajalik vist
        WnwbSynset synset = synsetRepository.findByIdWithLexicon(id).orElse(null);
        // keep fetching definitions for controller responses
        return synsetRelationService.getSynsetRelationsData(synset, id, true);
    }
}
