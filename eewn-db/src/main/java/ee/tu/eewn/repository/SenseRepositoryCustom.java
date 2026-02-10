package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSense;
import java.util.List;
import java.util.Set;

public interface SenseRepositoryCustom {
    List<WnwbSense> findBySynsetId(Integer synsetId);

    List<WnwbSense> findBySynsetIdIn(Set<Integer> synsetIds);
}
