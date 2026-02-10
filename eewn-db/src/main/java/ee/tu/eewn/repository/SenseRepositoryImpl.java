package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSense;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class SenseRepositoryImpl implements SenseRepositoryCustom {
    private static final String LEXICAL_ENTRY = "lexicalEntry";
    private static final String LEXICON = "lexicon";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<WnwbSense> findBySynsetId(Integer synsetId) {
        if (synsetId == null) {
            return List.of();
        }
        EntityGraph<WnwbSense> graph = entityManager.createEntityGraph(WnwbSense.class);
        graph.addAttributeNodes(LEXICAL_ENTRY);
        graph.addSubgraph(LEXICAL_ENTRY).addAttributeNodes(LEXICON);
        return entityManager.createQuery(
                                "SELECT s FROM WnwbSense s WHERE s.synset.id = :synsetId AND s.isDeleted = false",
                                WnwbSense.class
                            )
                            .setParameter("synsetId", synsetId)
                            .setHint("jakarta.persistence.fetchgraph", graph)
                            .getResultList();
    }

    @Override
    public List<WnwbSense> findBySynsetIdIn(Set<Integer> synsetIds) {
        if (synsetIds == null || synsetIds.isEmpty()) {
            return Collections.emptyList();
        }
        EntityGraph<WnwbSense> graph = entityManager.createEntityGraph(WnwbSense.class);
        graph.addAttributeNodes(LEXICAL_ENTRY, "synset");
        graph.addSubgraph(LEXICAL_ENTRY).addAttributeNodes(LEXICON);

        TypedQuery<WnwbSense> query = entityManager.createQuery(
            "SELECT s FROM WnwbSense s WHERE s.synset.id IN :synsetIds AND s.isDeleted = false",
            WnwbSense.class);
        query.setParameter("synsetIds", synsetIds);
        query.setHint("jakarta.persistence.fetchgraph", graph);
        return query.getResultList();
    }
}
