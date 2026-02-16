package ee.tu.eewn.controller;

import ee.tu.eewn.dto.AutocompleteWordDto;
import ee.tu.eewn.dto.WordWithRelationsDto;
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

    @GetMapping("/search")
    public ResponseEntity<List<WordWithRelationsDto>> searchWords(@RequestParam String query) {
        return ResponseEntity.ok(wordService.searchWords(query));
    }

    @GetMapping("/word/{id}/relevant-words")
    public ResponseEntity<List<String>> getRelevantWords(@PathVariable Integer id) {
        List<String> relevantWords = wordService.getRelevantWordsForSense(id);
        return ResponseEntity.ok(relevantWords);
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<AutocompleteWordDto>> autocompleteWords(@RequestParam String query) {
        return ResponseEntity.ok(wordService.autocompleteWords(query));
    }
}
