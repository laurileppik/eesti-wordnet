package ee.tu.eewn.service;

import ee.tu.eewn.dto.ExternalReferenceDto;
import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.external.WnwbExternalref;
import ee.tu.eewn.repository.WnwbExternalrefRepository;
import ee.tu.eewn.repository.WnwbSynsetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
//TODO vt üle ma arvan, et siin on osad db query-d ebavajalikud ja saame vähemaga
public class ExternalReferenceService {
    private final WnwbExternalrefRepository externalrefRepository;
    private final WnwbSynsetRepository synsetRepository;
    private final DataFetchService dataFetchService;
    public List<ExternalReferenceDto> getExternalReferences(WnwbSynset synset) {
        List<WnwbExternalref> externalRefs = externalrefRepository.findBySynset(synset);
        Set<String> references = findReferences(externalRefs);
        if (references.isEmpty()) {
            //TODO pigem pole vaja ju, tühi list vms
            return buildSimpleReferenceDtos(externalRefs);
        }

        Map<String, WnwbSynset> synsetsByLabel = findExternalSynsets(references);
        Set<Integer> synsetIds = new HashSet<>();
        for (WnwbSynset s : synsetsByLabel.values()) {
            synsetIds.add(s.getId());
        }

        Map<Integer, List<WnwbSense>> sensesBySynsetId = dataFetchService.groupSensesBySynsetId(synsetIds);
        Map<Integer, String> definitionsBySynsetId = dataFetchService.getDefinitionsForSynsets(synsetIds, "eng");

        return buildExternalReferences(externalRefs, synsetsByLabel, sensesBySynsetId, definitionsBySynsetId);
    }

    private Set<String> findReferences(List<WnwbExternalref> externalRefs) {
        Set<String> references = new HashSet<>();
        for (WnwbExternalref ref : externalRefs) {
            if (ref.getReference() != null && !ref.getReference().isBlank()) {
                references.add(ref.getReference());
            }
        }
        return references;
    }

    private List<ExternalReferenceDto> buildSimpleReferenceDtos(List<WnwbExternalref> externalRefs) {
        List<ExternalReferenceDto> dtos = new ArrayList<>();
        for (WnwbExternalref ref : externalRefs) {
            String systemName = ref.getSysId() != null ? ref.getSysId().getName() : "Unknown";
            String relationType = ref.getRelType() != null ? ref.getRelType().getName() : "";
            dtos.add(new ExternalReferenceDto(systemName, relationType, ref.getReference(),
                Collections.singletonList(ref.getReference()), ""));
        }
        return dtos;
    }

    private Map<String, WnwbSynset> findExternalSynsets(Set<String> references) {
        //TODO est ja eng jaoks mingi ENUM
        //Leia labeli põhised ik synsetid
        List<WnwbSynset> englishSynsets = synsetRepository.findByLabelInAndLanguage(references, "eng");
        Map<String, WnwbSynset> labelToSynsetMap = new HashMap<>();
        for (WnwbSynset synset : englishSynsets) {
            labelToSynsetMap.put(synset.getLabel(), synset);
        }
        return labelToSynsetMap;
    }


    private List<ExternalReferenceDto> buildExternalReferences(
            List<WnwbExternalref> externalRefs,
            Map<String, WnwbSynset> synsetsByLabel,
            Map<Integer, List<WnwbSense>> sensesBySynsetId,
            Map<Integer, String> definitionsBySynsetId) {
        List<ExternalReferenceDto> dtos = new ArrayList<>();
        for (WnwbExternalref ref : externalRefs) {
            String systemName = ref.getSysId() != null ? ref.getSysId().getName() : "Unknown";
            String relationType = ref.getRelType() != null ? ref.getRelType().getName() : "";
            String reference = ref.getReference();
            List<String> words = extractWords(reference, synsetsByLabel, sensesBySynsetId);
            String definition = extractDefinition(reference, synsetsByLabel, definitionsBySynsetId);
            dtos.add(new ExternalReferenceDto(systemName, relationType, reference, words, definition));
        }
        return dtos;
    }

    private List<String> extractWords(String reference, Map<String, WnwbSynset> synsetsByLabel,
                                      Map<Integer, List<WnwbSense>> sensesBySynsetId) {
        List<String> words = new ArrayList<>();
        if (reference == null || reference.isBlank()) {
            return words;
        }
        WnwbSynset englishSynset = synsetsByLabel.get(reference);
        if (englishSynset == null) {
            words.add(reference);
            return words;
        }
        List<WnwbSense> senses = sensesBySynsetId.get(englishSynset.getId());
        if (senses != null) {
            for (WnwbSense sense : senses) {
                words.add(formatSenseLabel(sense));
            }
        }

        if (words.isEmpty()) {
            words.add(reference);
        }
        return words;
    }

    private String formatSenseLabel(WnwbSense sense) {
        String lemma = sense.getLexicalEntry().getLemma();
        String label = sense.getLabel();
        String pos = sense.getLexicalEntry().getPartOfSpeech();

        if (label != null && !label.isBlank()) {
            return lemma + " " + label + "(" + pos + ")";
        }
        return lemma + " (" + pos + ")";
    }

    private String extractDefinition(String reference, Map<String, WnwbSynset> synsetsByLabel,
                                     Map<Integer, String> definitionsBySynsetId) {
        if (reference == null || reference.isBlank()) {
            return "";
        }

        WnwbSynset synset = synsetsByLabel.get(reference);
        if (synset == null) {
            return "";
        }

        String definition = definitionsBySynsetId.get(synset.getId());
        return definition != null ? definition : "";
    }

    public Map<Integer, List<ExternalReferenceDto>> getExternalReferencesForSynsets(Collection<WnwbSynset> synsets) {
        if (synsets == null || synsets.isEmpty()) {
            return Collections.emptyMap();
        }
        List<WnwbExternalref> externalRefs = externalrefRepository.findBySynsetIn(synsets);
        Map<Integer, List<WnwbExternalref>> refsBySynsetId = new HashMap<>();
        Set<String> allReferences = new HashSet<>();
        for (WnwbExternalref ref : externalRefs) {
            if (ref.getSynset() != null) {
                refsBySynsetId.computeIfAbsent(ref.getSynset().getId(), k -> new ArrayList<>()).add(ref);
            }
            if (ref.getReference() != null && !ref.getReference().isBlank()) {
                allReferences.add(ref.getReference());
            }
        }
        Map<String, WnwbSynset> synsetsByLabel = findExternalSynsets(allReferences);
        Set<Integer> extSynsetIds = new HashSet<>();
        for (WnwbSynset s : synsetsByLabel.values()) {
            extSynsetIds.add(s.getId());
        }
        Map<Integer, List<WnwbSense>> sensesBySynsetId = dataFetchService.groupSensesBySynsetId(extSynsetIds);
        Map<Integer, String> definitionsBySynsetId = dataFetchService.getDefinitionsForSynsets(extSynsetIds, "eng");
        Map<Integer, List<ExternalReferenceDto>> result = new HashMap<>();
        for (Map.Entry<Integer, List<WnwbExternalref>> entry : refsBySynsetId.entrySet()) {
            List<ExternalReferenceDto> dtos = buildExternalReferences(entry.getValue(), synsetsByLabel, sensesBySynsetId, definitionsBySynsetId);
            result.put(entry.getKey(), dtos);
        }
        return result;
    }
}
