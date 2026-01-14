package ee.tu.eewn.controller;

import ee.tu.eewn.dto.WordDetailsDto;
import ee.tu.eewn.dto.WordDto;
import ee.tu.eewn.service.WordDetailsService;
import ee.tu.eewn.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WordController {
    private final WordService wordService;
    private final WordDetailsService wordDetailsService;

    @GetMapping("/search")
    public ResponseEntity<List<WordDto>> searchWords(@RequestParam String query) {
        return ResponseEntity.ok(wordService.searchWords(query));
    }

    @GetMapping("/word/{id}")
    public ResponseEntity<WordDetailsDto> getWordDetails(@PathVariable Integer id) {
        WordDetailsDto details = wordDetailsService.getWordDetails(id);
        if (details == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(details);
    }
}
