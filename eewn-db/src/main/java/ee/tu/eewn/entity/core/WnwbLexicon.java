package ee.tu.eewn.entity.core;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_lexicon")
@Data
public class WnwbLexicon {
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

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private WnwbLexicalResource resource;

    @Column(name = "citation")
    private String citation;

    @Column(name = "dc_coverage")
    private String dcCoverage;

    @Column(name = "dc_publisher")
    private String dcPublisher;

    @Column(name = "dc_rights")
    private String dcRights;

    @Column(name = "dc_source")
    private String dcSource;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "license", nullable = false)
    private String license;

    @Column(name = "note")
    private String note;

    @Column(name = "url")
    private String url;

    @Column(name = "synsetcount")
    private Integer synsetCount;
}
