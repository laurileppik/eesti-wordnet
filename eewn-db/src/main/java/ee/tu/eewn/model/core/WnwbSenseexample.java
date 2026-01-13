package ee.tu.eewn.model.core;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_senseexample")
public class WnwbSenseexample {
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

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @ManyToOne
    @JoinColumn(name = "sense_id", nullable = false)
    private WnwbSense sense;
}
