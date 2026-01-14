package ee.tu.eewn.service;

import ee.tu.eewn.dto.WordDetailsDto;
import ee.tu.eewn.dto.RelationDto;
import ee.tu.eewn.dto.WordDto;
import ee.tu.eewn.entity.core.WnwbLexicalentry;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.relation.WnwbSenserelation;
import ee.tu.eewn.repository.WordRepository;
import ee.tu.eewn.repository.SenseRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WordDetailsService {
    private final WordRepository wordRepository;
    private final SenseRelationRepository senseRelationRepository;

    public WordDetailsDto getWordDetails(Integer wordId) {
        WnwbLexicalentry word = wordRepository.findById(wordId).orElse(null);
        if (word == null) return null;

        List<WnwbSense> senses = word.getSenses();
        List<RelationDto> relations = new ArrayList<>();

        for (WnwbSense sense : senses) {
            List<WnwbSenserelation> senseRelations = senseRelationRepository.findAllBySense(sense);
            Map<String, List<WordDto>> typeToWords = new HashMap<>();
            for (WnwbSenserelation sr : senseRelations) {
                String type = sr.getRelType().getName();
                WnwbSense relatedSense = sr.getASense().equals(sense) ? sr.getBSense() : sr.getASense();
                WnwbLexicalentry relatedWord = relatedSense.getLexicalEntry();
                WordDto relatedDto = new WordDto(relatedWord.getId(), relatedWord.getLemma(), relatedWord.getPartOfSpeech());
                typeToWords.computeIfAbsent(type, k -> new ArrayList<>()).add(relatedDto);
            }
            for (Map.Entry<String, List<WordDto>> entry : typeToWords.entrySet()) {
                relations.add(new RelationDto(entry.getKey(), entry.getValue()));
            }
        }

        return new WordDetailsDto(word.getId(), word.getLemma(), word.getPartOfSpeech(), relations);
    }
}
