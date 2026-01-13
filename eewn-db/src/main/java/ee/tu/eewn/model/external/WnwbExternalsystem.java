package ee.tu.eewn.model.external;

import ee.tu.eewn.model.core.WnwbLexicon;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_externalsystem")
public class WnwbExternalsystem {
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

    @Column(name = "description")
    private String description;

    @Column(name = "url_pattern")
    private String urlPattern;

    @Column(name = "reference_pattern")
    private String referencePattern;

    @ManyToOne
    @JoinColumn(name = "local_lexicon_id")
    private WnwbLexicon localLexicon;

    @Column(name = "dc_source")
    private String dcSource;

    @Column(name = "note")
    private String note;
}
