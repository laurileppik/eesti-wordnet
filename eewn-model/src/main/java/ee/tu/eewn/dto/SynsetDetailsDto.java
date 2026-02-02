package ee.tu.eewn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SynsetDetailsDto {
    private Integer id;
    private String label;
    private String synsetType;
    private String status;
    private String comment;
    private List<String> definitions;
    private List<SenseDto> senses;
    private List<SynsetRelationDto> relations;
    private List<TagDto> tags;
    private List<ExternalReferenceDto> externalReferences;
}
