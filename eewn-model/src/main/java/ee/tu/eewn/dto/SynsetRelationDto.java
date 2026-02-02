package ee.tu.eewn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SynsetRelationDto {
    private String type;
    private Integer relatedSynsetId;
    private String relatedLabel;
    private List<String> relevantWords;
}
