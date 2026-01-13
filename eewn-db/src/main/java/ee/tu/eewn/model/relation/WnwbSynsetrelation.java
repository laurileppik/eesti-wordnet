package ee.tu.eewn.model.relation;

import ee.tu.eewn.model.core.WnwbSynset;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_synsetrelation")
public class WnwbSynsetrelation {
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

    @Column(name = "label")
    private String label;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "a_synset_id")
    private WnwbSynset aSynset;

    @ManyToOne
    @JoinColumn(name = "b_synset_id")
    private WnwbSynset bSynset;

    @ManyToOne
    @JoinColumn(name = "rel_type_id", nullable = false)
    private WnwbSynsetrelationtype relType;

    @Column(name = "dc_source")
    private String dcSource;

    @Column(name = "dc_type")
    private String dcType;

    @Column(name = "note")
    private String note;

    @Column(name = "rate", nullable = false)
    private Double rate;
}
