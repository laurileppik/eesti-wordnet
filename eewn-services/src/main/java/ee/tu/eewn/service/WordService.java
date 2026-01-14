package ee.tu.eewn.service;

import ee.tu.eewn.dto.WordDto;
import ee.tu.eewn.repository.WordRepository;
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
    private Cache<String, List<WordDto>> searchCache;

    @Override
    public void afterPropertiesSet() {
        searchCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    public List<WordDto> searchWords(String query) {
        return searchCache.get(query, q -> wordRepository.searchByLemma(q).stream()
                .map(word -> new WordDto(word.getId(), word.getLemma(), word.getPartOfSpeech()))
                .toList());
    }
}
