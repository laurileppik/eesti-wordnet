package ee.tu.eewn.service;


import ee.tu.eewn.dto.WordWithDefinitionDto;
import ee.tu.eewn.repository.WordRepository;
import ee.tu.eewn.repository.SenseRepository;
import ee.tu.eewn.repository.DefinitionRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WordService implements InitializingBean {
    private final WordRepository wordRepository;
    private final SenseRepository senseRepository;
    private final DefinitionRepository definitionRepository;
    private Cache<String, List<WordWithDefinitionDto>> searchCache;

    @Override
    public void afterPropertiesSet() {
        searchCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    public List<WordWithDefinitionDto> searchWords(String query) {
        return searchCache.get(query, q -> wordRepository.searchByLemma(q).stream()
                .map(word -> {
                    var senses = senseRepository.findByLexicalEntryId(word.getId());
                    String definition = null;
                    for (var sense : senses) {
                        var defs = definitionRepository.findBySenseIdAndLang(sense.getId(), sense.getLexicalEntry().getLexicon().getLanguage());
                        if (!defs.isEmpty()) {
                            definition = defs.get(0).getText();
                            break;
                        }
                    }
                    return new WordWithDefinitionDto(word.getId(), word.getLemma(), word.getPartOfSpeech(), definition);
                })
                .toList());
    }
}
