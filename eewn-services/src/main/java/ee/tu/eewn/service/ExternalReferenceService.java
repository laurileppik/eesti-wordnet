package ee.tu.eewn.service;

import ee.tu.eewn.dto.ExternalReferenceDto;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.core.WnwbDefinition;
import ee.tu.eewn.entity.external.WnwbExternalref;
import ee.tu.eewn.repository.WnwbExternalrefRepository;
import ee.tu.eewn.repository.WnwbSynsetRepository;
import ee.tu.eewn.repository.SenseRepository;
import ee.tu.eewn.repository.DefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ExternalReferenceService {
    private final WnwbExternalrefRepository externalrefRepository;
    private final WnwbSynsetRepository synsetRepository;
    private final SenseRepository senseRepository;
    private final DefinitionRepository definitionRepository;

    public List<ExternalReferenceDto> getExternalReferences(WnwbSynset synset) {
        List<WnwbExternalref> externalRefs = externalrefRepository.findBySynset(synset);
        List<ExternalReferenceDto> result = new ArrayList<>();

        for (WnwbExternalref ref : externalRefs) {
            String systemName = ref.getSysId() != null ? ref.getSysId().getName() : "Unknown";
            String relationType = ref.getRelType() != null ? ref.getRelType().getName() : "";
            String reference = ref.getReference();
            List<String> words = new ArrayList<>();
            String definition = "";
            if (reference != null && !reference.isBlank()) {
                List<WnwbSynset> englishSynsets = synsetRepository.findByLabelAndLanguage(reference, "eng");
                if (!englishSynsets.isEmpty()) {
                    WnwbSynset englishSynset = englishSynsets.get(0);
                    List<WnwbSense> senses = senseRepository.findBySynsetId(englishSynset.getId());
                    for (WnwbSense sense : senses) {
                        String lemma = sense.getLexicalEntry().getLemma();
                        String label = sense.getLabel();
                        String pos = sense.getLexicalEntry().getPartOfSpeech();

                        if (label != null && !label.isBlank()) {
                            words.add(lemma + " " + label + "(" + pos + ")");
                        } else {
                            words.add(lemma + " (" + pos + ")");
                        }
                    }
                    List<WnwbDefinition> definitions = definitionRepository.findBySynsetIdAndLang(
                        englishSynset.getId(),
                        "eng"
                    );
                    if (!definitions.isEmpty() && definitions.get(0).getText() != null) {
                        definition = definitions.get(0).getText();
                    }
                }
            }
            if (words.isEmpty() && reference != null && !reference.isBlank()) {
                words.add(reference);
            }

            result.add(new ExternalReferenceDto(systemName, relationType, reference, words, definition));
        }

        return result;
    }
}
