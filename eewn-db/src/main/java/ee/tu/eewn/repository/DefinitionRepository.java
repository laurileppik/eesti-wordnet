package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DefinitionRepository extends JpaRepository<WnwbDefinition, Integer> {
    @Query("SELECT d FROM WnwbDefinition d WHERE d.sense.id = :senseId AND d.isDeleted = false AND d.language = :lang ORDER BY d.isPrimary DESC, d.id ASC")
    List<WnwbDefinition> findBySenseIdAndLang(@Param("senseId") Integer senseId, @Param("lang") String lang);

    @Query("SELECT d FROM WnwbDefinition d WHERE d.synset.id = :synsetId AND d.isDeleted = false AND d.language = :lang ORDER BY d.isPrimary DESC, d.id ASC")
    List<WnwbDefinition> findBySynsetIdAndLang(@Param("synsetId") Integer synsetId, @Param("lang") String lang);
}

