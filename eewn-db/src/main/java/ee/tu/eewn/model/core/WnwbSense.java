package ee.tu.eewn.model.core;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_sense")
public class WnwbSense {
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

    @Column(name = "nr", nullable = false)
    private Integer nr;

    @Column(name = "label")
    private String label;

    @Column(name = "style")
    private String style;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "comment")
    private String comment;

    @Column(name = "locked_by")
    private String lockedBy;

    @Column(name = "date_locked")
    private OffsetDateTime dateLocked;

    @ManyToOne
    @JoinColumn(name = "lexical_entry_id", nullable = false)
    private WnwbLexicalentry lexicalEntry;

    @ManyToOne
    @JoinColumn(name = "synset_id")
    private WnwbSynset synset;

    @Column(name = "dc_source")
    private String dcSource;
}
