package ee.tu.eewn.controller;

import ee.tu.eewn.dto.SynsetDetailsDto;
import ee.tu.eewn.service.SynsetDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/synsets")
@RequiredArgsConstructor
public class SynsetDetailsController {
    private final SynsetDetailsService synsetDetailsService;

    @GetMapping("/{id}")
    public ResponseEntity<SynsetDetailsDto> getSynsetDetails(@PathVariable Integer id) {
        SynsetDetailsDto details = synsetDetailsService.getSynsetDetails(id);
        if (details == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(details);
    }
}
