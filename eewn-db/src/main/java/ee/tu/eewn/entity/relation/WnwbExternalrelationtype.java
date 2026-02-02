package ee.tu.eewn.entity.relation;

import ee.tu.eewn.entity.core.WnwbLexicon;
import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_externalrelationtype")
@Data
public class WnwbExternalrelationtype {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lexicon_id")
    private WnwbLexicon lexicon;

    @Column(name = "dc_source")
    private String dcSource;

    @Column(name = "note")
    private String note;

    @Column(name = "priority")
    private Integer priority;
}
