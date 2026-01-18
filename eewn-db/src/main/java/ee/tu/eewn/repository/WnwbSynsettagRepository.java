package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSynset;
import ee.tu.eewn.entity.relation.WnwbSynsettag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WnwbSynsettagRepository extends JpaRepository<WnwbSynsettag, Integer> {
    List<WnwbSynsettag> findBySynset(WnwbSynset synset);
}
