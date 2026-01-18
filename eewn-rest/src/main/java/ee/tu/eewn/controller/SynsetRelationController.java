package ee.tu.eewn.controller;

import ee.tu.eewn.dto.WordWithDefinitionDto;
import ee.tu.eewn.service.SynsetRelationService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/synset")
@RequiredArgsConstructor
public class SynsetRelationController {
    private final SynsetRelationService synsetRelationService;

    @GetMapping("/{id}/relations")
    public Map<String, List<WordWithDefinitionDto>> getSynsetRelations(@PathVariable Integer id) {
        return synsetRelationService.getSynsetRelationsData(id);
    }
}
