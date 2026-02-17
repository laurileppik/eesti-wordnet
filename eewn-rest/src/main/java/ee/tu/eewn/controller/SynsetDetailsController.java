package ee.tu.eewn.controller;

import ee.tu.eewn.service.SynsetDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/synsets")
@RequiredArgsConstructor
public class SynsetDetailsController {
    private final SynsetDetailsService synsetDetailsService;

    @GetMapping(value = "/{id}")
    public ResponseEntity<String> getSynsetDetails(@PathVariable Integer id) {
        String json = synsetDetailsService.getSynsetDetails(id);
        if (json == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
    }
}
