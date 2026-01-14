package ee.tu.eewn.entity.meta;

import ee.tu.eewn.entity.core.WnwbLexicon;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_taskorder")
public class WnwbTaskorder {
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

    @Column(name = "resources")
    private String resources;

    @Column(name = "params")
    private String params;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "progress")
    private String progress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_lexicon_id")
    private WnwbLexicon mainLexicon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "other_lexicon_id")
    private WnwbLexicon otherLexicon;
}
