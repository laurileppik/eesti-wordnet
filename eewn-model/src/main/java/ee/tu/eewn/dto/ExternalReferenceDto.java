package ee.tu.eewn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalReferenceDto {
    private String systemName;
    private String relationType;
    private String reference;
    private List<String> words;
    private String definition;
}
