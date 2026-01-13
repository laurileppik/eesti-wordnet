package ee.tu.eewn.model.relation;

import ee.tu.eewn.model.core.WnwbSynset;
import ee.tu.eewn.model.meta.WnwbTag;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_synsettag")
public class WnwbSynsettag {
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

    @ManyToOne
    @JoinColumn(name = "synset_id", nullable = false)
    private WnwbSynset synset;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private WnwbTag tag;
}
