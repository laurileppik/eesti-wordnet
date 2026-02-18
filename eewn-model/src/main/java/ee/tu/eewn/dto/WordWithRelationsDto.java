package ee.tu.eewn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
// TODO vb lihtsalt WordDto-ks ja WordDto refactoda.
public class WordWithRelationsDto {
    private Integer id;
    private String lemma;
    private String partOfSpeech;
    private String definition;
    private String label;
    private Integer synsetId;
    private List<String> relevantWords;
    private List<ExternalReferenceDto> externalReferences;
}
