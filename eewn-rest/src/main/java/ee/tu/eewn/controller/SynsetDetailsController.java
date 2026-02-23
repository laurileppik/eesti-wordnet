package ee.tu.eewn.controller;

import ee.tu.eewn.service.SynsetDetailsService;
import ee.tu.eewn.dto.SynsetDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/synsets")
@RequiredArgsConstructor
@Slf4j
public class SynsetDetailsController {
    private final SynsetDetailsService synsetDetailsService;

    @GetMapping(value = "/{id}")
    public ResponseEntity<SynsetDetailsDto> getSynsetDetails(@PathVariable Integer id) {
        log.debug("[SynsetDetailsController] Received request for synset details with id: {}", id);
        SynsetDetailsDto dto = synsetDetailsService.getSynsetDetails(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dto);
    }
}
