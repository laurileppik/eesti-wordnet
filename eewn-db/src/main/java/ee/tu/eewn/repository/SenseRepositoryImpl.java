package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSense;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SenseRepositoryImpl implements SenseRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<WnwbSense> findBySynsetId(Integer synsetId) {
        return entityManager.createQuery(
            "SELECT s FROM WnwbSense s WHERE s.synset.id = :synsetId AND s.isDeleted = false",
            WnwbSense.class
        ).setParameter("synsetId", synsetId).getResultList();
    }
}
