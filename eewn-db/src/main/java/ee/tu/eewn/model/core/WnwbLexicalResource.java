package ee.tu.eewn.model.core;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_lexicalresource")
public class WnwbLexicalResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "date_created", nullable = false)
    private OffsetDateTime dateCreated;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "date_updated")
    private OffsetDateTime dateUpdated;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "language_coding", nullable = false)
    private String languageCoding;

    @Column(name = "script_coding", nullable = false)
    private String scriptCoding;

    @Column(name = "char_coding", nullable = false)
    private String charCoding;

    @Column(name = "short", nullable = false)
    private String shortName;
}
