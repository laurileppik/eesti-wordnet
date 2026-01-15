package ee.tu.eewn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AutocompleteWordDto {
    private Integer id;
    private String lemma;
}
