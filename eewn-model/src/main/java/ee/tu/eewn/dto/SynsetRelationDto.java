package ee.tu.eewn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SynsetRelationDto {
    private String type;
    private Integer relatedSynsetId;
    private String relatedLabel;
}
