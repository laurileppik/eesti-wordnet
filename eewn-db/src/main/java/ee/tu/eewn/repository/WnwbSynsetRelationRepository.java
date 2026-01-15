package ee.tu.eewn.repository;

import ee.tu.eewn.entity.relation.WnwbSynsetRelation;
import ee.tu.eewn.entity.core.WnwbSynset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WnwbSynsetRelationRepository extends JpaRepository<WnwbSynsetRelation, Integer> {
    List<WnwbSynsetRelation> findByASynset(WnwbSynset aSynset);
}
