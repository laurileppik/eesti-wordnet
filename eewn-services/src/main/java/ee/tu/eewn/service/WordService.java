package ee.tu.eewn.service;


import ee.tu.eewn.dto.WordWithRelationsDto;
import ee.tu.eewn.dto.AutocompleteWordDto;
import ee.tu.eewn.repository.WordRepository;
import ee.tu.eewn.repository.SenseRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WordService implements InitializingBean {
    private final WordRepository wordRepository;
    private final SenseRepository senseRepository;
    private final DataFetchService dataFetchService;
    private Cache<String, List<WordWithRelationsDto>> searchCache;

    @Override
    public void afterPropertiesSet() {
        searchCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    public List<WordWithRelationsDto> searchWords(String query) {
        return searchCache.get(query, q -> senseRepository.findByLemmaAndLanguage(q, "est").stream()
                .map(sense -> {
                    String language = sense.getLexicalEntry().getLexicon().getLanguage();
                    String definition = dataFetchService.getDefinitionForSense(sense, language);
                    WordWithRelationsDto dto = new WordWithRelationsDto();
                    dto.setId(sense.getId());
                    dto.setLemma(sense.getLexicalEntry().getLemma());
                    dto.setPartOfSpeech(sense.getLexicalEntry().getPartOfSpeech());
                    dto.setDefinition(definition);
                    dto.setLabel(sense.getLabel());
                    if (sense.getSynset() != null) {
                        dto.setSynsetId(sense.getSynset().getId());
                        dto.setRelevantWords(dataFetchService.getLemmasForSynset(sense.getSynset().getId()));
                    } else {
                        dto.setRelevantWords(List.of());
                    }
                    return dto;
                })
                .toList());
    }

    public List<String> getRelevantWordsForSense(Integer senseId) {
        var senseOpt = senseRepository.findById(senseId);
        if (senseOpt.isEmpty() || senseOpt.get().getSynset() == null) {
            return List.of();
        }
        return dataFetchService.getLemmasForSynset(senseOpt.get().getSynset().getId());
    }

    public List<AutocompleteWordDto> autocompleteWords(String query) {
        var results = wordRepository.findTop10ByLemmaStartingWithIgnoreCaseAndLanguage(query, "est", PageRequest.of(0, 10));
        return results.stream()
                .map(w -> new AutocompleteWordDto(w.getId(), w.getLemma()))
                .toList();
    }
}
