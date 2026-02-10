package ee.tu.eewn.repository;

import ee.tu.eewn.entity.relation.WnwbSynsetRelation;
import ee.tu.eewn.entity.core.WnwbSynset;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SynsetRelationRepository extends JpaRepository<WnwbSynsetRelation, Integer> {
    @EntityGraph(attributePaths = {"relType", "aSynset", "bSynset"})
    @Query("SELECT sr FROM WnwbSynsetRelation sr WHERE sr.aSynset = :synset OR sr.bSynset = :synset")
    List<WnwbSynsetRelation> findAllBySynset(@Param("synset") WnwbSynset synset);
}
