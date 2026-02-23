package ee.tu.eewn.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import ee.tu.eewn.dto.AutocompleteWordDto;
import ee.tu.eewn.repository.SenseRepository;
import ee.tu.eewn.repository.WordRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutoCompleteService {
    private final WordRepository wordRepository;
    private final SenseRepository senseRepository;

    public List<String> getRelevantWordsForSense(Integer senseId) {
        var senseOpt = senseRepository.findById(senseId);
        if (senseOpt.isEmpty() || senseOpt.get().getSynset() == null) {
            return List.of();
        }
        return getLemmasForSynset(senseOpt.get().getSynset().getId());
    }

    public List<AutocompleteWordDto> autocompleteWords(String query) {
        var results = wordRepository.findTop10ByLemmaStartingWithIgnoreCaseAndLanguage(query, "est", PageRequest.of(0, 10));
        return results.stream()
                      .map(w -> new AutocompleteWordDto(w.getId(), w.getLemma()))
                      .toList();
    }

    public List<String> getLemmasForSynset(Integer synsetId) {
        return senseRepository.findBySynsetId(synsetId).stream()
                              .map(s -> s.getLexicalEntry().getLemma())
                              .distinct()
                              .toList();
    }
}
