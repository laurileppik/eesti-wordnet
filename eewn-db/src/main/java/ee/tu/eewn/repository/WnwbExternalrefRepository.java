package ee.tu.eewn.repository;

import ee.tu.eewn.entity.external.WnwbExternalref;
import ee.tu.eewn.entity.external.WnwbExternalsystem;
import ee.tu.eewn.entity.core.WnwbSynset;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface WnwbExternalrefRepository extends JpaRepository<WnwbExternalref, Integer> {
    @EntityGraph(attributePaths = {"synset", "sysId", "sense", "relType"})
    @Query("SELECT e FROM WnwbExternalref e WHERE e.synset = :synset AND e.isDeleted = false")
    List<WnwbExternalref> findBySynset(@Param("synset") WnwbSynset synset);

    @EntityGraph(attributePaths = {"sysId", "sysId.localLexicon", "synset"})
    @Query("SELECT e FROM WnwbExternalref e " +
           "WHERE e.reference = :reference AND e.sysId.localLexicon IS NOT NULL AND e.isDeleted = false")
    List<WnwbExternalref> findByReferenceWithLocalLexicon(@Param("reference") String reference);

    @EntityGraph(attributePaths = {"synset", "synset.lexicon", "sysId"})
    @Query("SELECT e FROM WnwbExternalref e " +
           "WHERE e.reference = :reference AND e.sysId = :system AND e.synset IS NOT NULL AND e.isDeleted = false")
    List<WnwbExternalref> findByReferenceAndSystem(@Param("reference") String reference,
                                                     @Param("system") WnwbExternalsystem system);

    @EntityGraph(attributePaths = {"synset", "synset.lexicon", "sysId"})
    @Query("SELECT e FROM WnwbExternalref e WHERE e.reference = :reference AND e.sysId = :system AND e.synset IS NOT NULL AND e.synset.lexicon.language = :language AND e.isDeleted = false")
    List<WnwbExternalref> findByReferenceAndSystemAndLanguage(@Param("reference") String reference, @Param("system") WnwbExternalsystem system, @Param("language") String language);

    @EntityGraph(attributePaths = {"synset", "sysId", "sense", "relType"})
    @Query("SELECT e FROM WnwbExternalref e WHERE e.synset IN :synsets AND e.isDeleted = false")
    List<WnwbExternalref> findBySynsetIn(@Param("synsets") Collection<WnwbSynset> synsets);
}
