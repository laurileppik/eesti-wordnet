package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSynset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WnwbSynsetRepository extends JpaRepository<WnwbSynset, Integer> {

    @Query("SELECT s FROM WnwbSynset s WHERE s.lexicon.language = :language AND s.isDeleted = false")
    List<WnwbSynset> findByLexiconLanguage(@Param("language") String language);
}
