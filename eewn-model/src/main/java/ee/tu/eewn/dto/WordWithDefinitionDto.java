package ee.tu.eewn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordWithDefinitionDto {
    private Integer id;
    private String lemma;
    private String partOfSpeech;
    private String definition;
    private String label;
    private Integer synsetId;
    private List<String> relevantWords;
}
