package ee.tu.eewn.model.core;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_statement")
public class WnwbStatement {
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

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "source")
    private String source;

    @Column(name = "language")
    private String language;

    @ManyToOne
    @JoinColumn(name = "definition_id", nullable = false)
    private WnwbDefinition definition;
}
