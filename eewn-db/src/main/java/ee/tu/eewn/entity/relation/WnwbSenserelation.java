package ee.tu.eewn.entity.relation;

import ee.tu.eewn.entity.core.WnwbSense;
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
@Table(name = "wnwb_senserelation")
public class WnwbSenserelation {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_sense_id")
    private WnwbSense aSense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b_sense_id")
    private WnwbSense bSense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rel_type_id", nullable = false)
    private WnwbSenserelationtype relType;

    @Column(name = "dc_source")
    private String dcSource;

    @Column(name = "dc_type")
    private String dcType;

    @Column(name = "note")
    private String note;

    @Column(name = "rate", nullable = false)
    private Double rate;
}
