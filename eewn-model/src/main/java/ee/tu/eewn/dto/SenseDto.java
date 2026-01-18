package ee.tu.eewn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SenseDto {
    private Integer id;
    private String lemma;
    private String partOfSpeech;
    private String status;
    private String comment;
}
