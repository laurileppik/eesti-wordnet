package ee.tu.eewn.repository;

import ee.tu.eewn.entity.relation.WnwbSynsetrelation;
import ee.tu.eewn.entity.core.WnwbSynset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SynsetRelationRepository extends JpaRepository<WnwbSynsetrelation, Integer> {
    @Query("SELECT sr FROM WnwbSynsetrelation sr WHERE sr.aSynset = :synset OR sr.bSynset = :synset")
    List<WnwbSynsetrelation> findAllBySynset(@Param("synset") WnwbSynset synset);
}

