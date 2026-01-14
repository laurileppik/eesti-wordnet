package ee.tu.eewn.entity.relation;

import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.meta.WnwbTag;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_sensetag")
public class WnwbSensetag {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sense_id", nullable = false)
    private WnwbSense sense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private WnwbTag tag;
}
