package ee.tu.eewn.controller;

import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.relation.WnwbSynsetRelation;
import ee.tu.eewn.repository.WnwbSynsetRepository;
import ee.tu.eewn.repository.SynsetRelationRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/synset")
@RequiredArgsConstructor
public class SynsetRelationController {
    private final SynsetRelationRepository synsetRelationRepository;
    private final WnwbSynsetRepository synsetRepository;

    @GetMapping("/{id}/relations")
    public Map<String, List<WnwbSynset>> getSynsetRelations(@PathVariable Integer id) {
        Optional<WnwbSynset> synsetOpt = synsetRepository.findById(id);
        if (synsetOpt.isEmpty()) {
            return Collections.emptyMap();
        }
        WnwbSynset synset = synsetOpt.get();
        List<WnwbSynsetRelation> relations = synsetRelationRepository.findAllBySynset(synset);
        Map<String, List<WnwbSynset>> result = new HashMap<>();
        for (WnwbSynsetRelation rel : relations) {
            String type = rel.getRelType().getName();
            WnwbSynset related = rel.getASynset().equals(synset) ? rel.getBSynset() : rel.getASynset();
            result.computeIfAbsent(type, k -> new ArrayList<>()).add(related);
        }
        return result;
    }
}
