package ee.tu.eewn.entity.core;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Data
@Table(name = "wnwb_synset")
public class WnwbSynset {
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

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "synset_type", nullable = false)
    private String synsetType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "comment")
    private String comment;

    @Column(name = "locked_by")
    private String lockedBy;

    @Column(name = "date_locked")
    private OffsetDateTime dateLocked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lexicon_id", nullable = false)
    private WnwbLexicon lexicon;
}
