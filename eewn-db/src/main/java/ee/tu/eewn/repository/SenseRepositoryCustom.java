package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSense;
import java.util.List;

public interface SenseRepositoryCustom {
    List<WnwbSense> findBySynsetId(Integer synsetId);
}
