package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSynset;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface WnwbSynsetRepository extends JpaRepository<WnwbSynset, Integer> {
    @EntityGraph(attributePaths = {"lexicon"})
    @Query("SELECT s FROM WnwbSynset s WHERE s.id = :id")
    Optional<WnwbSynset> findByIdWithLexicon(@Param("id") Integer id);

    @Query("SELECT DISTINCT s FROM WnwbSynset s LEFT JOIN FETCH s.definitions d LEFT JOIN FETCH s.lexicon l WHERE s.id = :id")
    Optional<WnwbSynset> findByIdWithLexiconAndDefinitions(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"lexicon"})
    @Query("SELECT s FROM WnwbSynset s WHERE s.lexicon.language = :language AND s.isDeleted = false")
    List<WnwbSynset> findByLexiconLanguage(@Param("language") String language);

    @EntityGraph(attributePaths = {"lexicon"})
    @Query("SELECT s FROM WnwbSynset s WHERE s.label = :label " +
           "AND s.lexicon.language = :language AND s.isDeleted = false")
    List<WnwbSynset> findByLabelAndLanguage(@Param("label") String label, @Param("language") String language);

    @EntityGraph(attributePaths = {"lexicon"})
    @Query("SELECT s FROM WnwbSynset s WHERE s.label IN :labels " +
           "AND s.lexicon.language = :language AND s.isDeleted = false")
    List<WnwbSynset> findByLabelInAndLanguage(@Param("labels") Set<String> labels, @Param("language") String language);

    @Query("SELECT s FROM WnwbSynset s WHERE s.lexicon.language = :language AND s.isDeleted = false")
    List<WnwbSynset> findByLexiconLanguageWithoutEntityGraph(@Param("language") String language);
}
