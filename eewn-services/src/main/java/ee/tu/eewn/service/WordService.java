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
        return searchCache.get(query, q -> senseRepository.findByLemmaAndLanguage(q, "est").stream()
                .map(sense -> {
                    String definition = null;
                    var defs = definitionRepository.findBySenseIdAndLang(sense.getId(), sense.getLexicalEntry().getLexicon().getLanguage());
                    if (!defs.isEmpty()) {
                        definition = defs.get(0).getText();
                    } else if (sense.getSynset() != null) {
                        var synsetDefs = definitionRepository.findBySynsetIdAndLang(sense.getSynset().getId(), sense.getLexicalEntry().getLexicon().getLanguage());
                        if (!synsetDefs.isEmpty()) {
                            definition = synsetDefs.get(0).getText();
                        }
                    }
                    WordWithDefinitionDto dto = new WordWithDefinitionDto();
                    dto.setId(sense.getId());
                    dto.setLemma(sense.getLexicalEntry().getLemma());
                    dto.setPartOfSpeech(sense.getLexicalEntry().getPartOfSpeech());
                    dto.setDefinition(definition);
                    dto.setLabel(sense.getLabel());
                    if (sense.getSynset() != null) {
                        dto.setSynsetId(sense.getSynset().getId());
                    }
                    return dto;
                })
                .toList());
    }
}
