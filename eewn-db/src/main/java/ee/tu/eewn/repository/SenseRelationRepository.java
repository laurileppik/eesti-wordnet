package ee.tu.eewn.repository;

import ee.tu.eewn.entity.relation.WnwbSenserelation;
import ee.tu.eewn.entity.core.WnwbSense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SenseRelationRepository extends JpaRepository<WnwbSenserelation, Integer> {
    @Query("SELECT sr FROM WnwbSenserelation sr WHERE sr.aSense = :sense OR sr.bSense = :sense")
    List<WnwbSenserelation> findAllBySense(@Param("sense") WnwbSense sense);
}
