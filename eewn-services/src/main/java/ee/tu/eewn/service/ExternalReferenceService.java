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

    //TODO tee lühemaks
    public List<ExternalReferenceDto> getExternalReferences(WnwbSynset synset) {
        List<WnwbExternalref> externalRefs = externalrefRepository.findBySynset(synset);
        List<ExternalReferenceDto> result = new ArrayList<>();

        Set<String> references = new HashSet<>();
        for (WnwbExternalref ref : externalRefs) {
            if (ref.getReference() != null && !ref.getReference().isBlank()) {
                references.add(ref.getReference());
            }
        }

        if (references.isEmpty()) {
            for (WnwbExternalref ref : externalRefs) {
                String systemName = ref.getSysId() != null ? ref.getSysId().getName() : "Unknown";
                String relationType = ref.getRelType() != null ? ref.getRelType().getName() : "";
                result.add(new ExternalReferenceDto(systemName, relationType, ref.getReference(),
                    Collections.singletonList(ref.getReference()), ""));
            }
            return result;
        }

        List<WnwbSynset> englishSynsets = synsetRepository.findByLabelInAndLanguage(references, "eng");
        Map<String, WnwbSynset> labelToSynsetMap = new HashMap<>();
        Set<Integer> synsetIds = new HashSet<>();
        for (WnwbSynset s : englishSynsets) {
            labelToSynsetMap.put(s.getLabel(), s);
            synsetIds.add(s.getId());
        }

        List<WnwbSense> allSenses = synsetIds.isEmpty() ? Collections.emptyList() :
            senseRepository.findBySynsetIdIn(synsetIds);
        Map<Integer, List<WnwbSense>> synsetIdToSensesMap = new HashMap<>();
        for (WnwbSense sense : allSenses) {
            if (sense.getSynset() != null) {
                synsetIdToSensesMap.computeIfAbsent(sense.getSynset().getId(), k -> new ArrayList<>()).add(sense);
            }
        }

        List<WnwbDefinition> allDefinitions = synsetIds.isEmpty() ? Collections.emptyList() :
            definitionRepository.findBySynsetIdInAndLang(synsetIds, "eng");
        Map<Integer, String> synsetIdToDefinitionMap = new HashMap<>();
        for (WnwbDefinition def : allDefinitions) {
            if (def.getSynset() != null && def.getText() != null && !def.getText().isBlank()) {
                synsetIdToDefinitionMap.putIfAbsent(def.getSynset().getId(), def.getText());
            }
        }

        for (WnwbExternalref ref : externalRefs) {
            String systemName = ref.getSysId() != null ? ref.getSysId().getName() : "Unknown";
            String relationType = ref.getRelType() != null ? ref.getRelType().getName() : "";
            String reference = ref.getReference();
            List<String> words = new ArrayList<>();
            String definition = "";
            if (reference != null && !reference.isBlank()) {
                WnwbSynset englishSynset = labelToSynsetMap.get(reference);
                if (englishSynset != null) {
                    List<WnwbSense> senses = synsetIdToSensesMap.get(englishSynset.getId());
                    if (senses != null) {
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
                    }
                    String def = synsetIdToDefinitionMap.get(englishSynset.getId());
                    if (def != null) {
                        definition = def;
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
