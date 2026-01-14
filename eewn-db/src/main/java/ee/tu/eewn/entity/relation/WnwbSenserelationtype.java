package ee.tu.eewn.entity.relation;

import ee.tu.eewn.entity.core.WnwbLexicon;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wnwb_senserelationtype")
public class WnwbSenserelationtype {
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

    @Column(name = "direction", nullable = false)
    private String direction;

    @Column(name = "description")
    private String description;

    @Column(name = "other")
    private Integer other;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lexicon_id")
    private WnwbLexicon lexicon;

    @Column(name = "omw_name")
    private String omwName;

    @Column(name = "priority")
    private Integer priority;
}
