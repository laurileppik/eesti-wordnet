package ee.tu.eewn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordDetailsDto {
    private Integer id;
    private String lemma;
    private String partOfSpeech;
    private List<RelationDto> relations;
}

