package ee.tu.eewn.entity.core;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wnwb_lexicalentry")
public class WnwbLexicalentry {
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

    @Column(name = "part_of_speech", nullable = false)
    private String partOfSpeech;

    @Column(name = "scheme")
    private String scheme;

    @Column(name = "lemma", nullable = false)
    private String lemma;

    @Column(name = "distinctive_case")
    private String distinctiveCase;

    @Column(name = "distinctive_form")
    private String distinctiveForm;

    @Column(name = "grammatical_number")
    private String grammaticalNumber;

    @Column(name = "grammatical_gender")
    private String grammaticalGender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lexicon_id", nullable = false)
    private WnwbLexicon lexicon;

    @Column(name = "dc_source")
    private String dcSource;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "lexicalEntry", fetch = FetchType.LAZY)
    private List<WnwbSense> senses;
}
