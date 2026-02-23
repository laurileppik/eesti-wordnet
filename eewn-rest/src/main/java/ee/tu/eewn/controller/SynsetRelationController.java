package ee.tu.eewn.controller;

import ee.tu.eewn.dto.SynsetRelationDto;
import ee.tu.eewn.service.SynsetRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/synset")
@RequiredArgsConstructor
@Slf4j
public class SynsetRelationController {
    private final SynsetRelationService synsetRelationService;

    @GetMapping("/{id}/relations")
    public Map<String, List<SynsetRelationDto>> getSynsetRelations(@PathVariable Integer id) {
        log.debug("[SynsetRelationController] Received request for synset relations with id: {}", id);
        return synsetRelationService.getRelationsJsonJooq(id);
    }
}
