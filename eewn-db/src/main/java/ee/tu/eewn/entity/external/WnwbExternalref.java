package ee.tu.eewn.entity.external;

import ee.tu.eewn.entity.relation.WnwbExternalrelationtype;
import ee.tu.eewn.entity.core.WnwbSense;
import ee.tu.eewn.entity.core.WnwbSynset;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_externalref")
public class WnwbExternalref {
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

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sense_id")
    private WnwbSense sense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "synset_id")
    private WnwbSynset synset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sys_id_id", nullable = false)
    private WnwbExternalsystem sysId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rel_type_id")
    private WnwbExternalrelationtype relType;

    @Column(name = "rate", nullable = false)
    private Double rate;

    @Column(name = "dc_source")
    private String dcSource;

    @Column(name = "dc_type")
    private String dcType;

    @Column(name = "note")
    private String note;
}
