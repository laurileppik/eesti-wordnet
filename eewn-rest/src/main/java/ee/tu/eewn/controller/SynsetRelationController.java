package ee.tu.eewn.controller;

import ee.tu.eewn.service.SynsetRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/synset")
@RequiredArgsConstructor
public class SynsetRelationController {
    private final SynsetRelationService synsetRelationService;

    @GetMapping("/{id}/relations")
    public String getSynsetRelations(@PathVariable Integer id) {
        return synsetRelationService.getRelationsJsonJooq(id);
    }
}
