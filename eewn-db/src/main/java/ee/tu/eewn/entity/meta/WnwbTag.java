package ee.tu.eewn.entity.meta;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wnwb_tag")
public class WnwbTag {
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

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "value", nullable = false)
    private String value;
}
