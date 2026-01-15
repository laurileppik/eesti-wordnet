package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSynset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WnwbSynsetRepository extends JpaRepository<WnwbSynset, Integer> {
}
