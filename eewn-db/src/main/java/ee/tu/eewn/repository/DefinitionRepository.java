package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface DefinitionRepository extends JpaRepository<WnwbDefinition, Integer> {
    @Query("SELECT d FROM WnwbDefinition d " +
           "WHERE d.sense.id = :senseId AND d.isDeleted = false AND d.language = :lang " +
           "ORDER BY d.isPrimary DESC, d.id")
    List<WnwbDefinition> findBySenseIdAndLang(@Param("senseId") Integer senseId, @Param("lang") String lang);

    @Query("SELECT d FROM WnwbDefinition d " +
           "WHERE d.synset.id = :synsetId AND d.isDeleted = false AND d.language = :lang " +
           "ORDER BY d.isPrimary DESC, d.id")
    List<WnwbDefinition> findBySynsetIdAndLang(@Param("synsetId") Integer synsetId, @Param("lang") String lang);

    @Query("SELECT d FROM WnwbDefinition d " +
           "WHERE d.synset.id IN :synsetIds AND d.isDeleted = false AND d.language = :lang " +
           "ORDER BY d.synset.id, d.isPrimary DESC, d.id")
    List<WnwbDefinition> findBySynsetIdInAndLang(@Param("synsetIds") Set<Integer> synsetIds, @Param("lang") String lang);
}
