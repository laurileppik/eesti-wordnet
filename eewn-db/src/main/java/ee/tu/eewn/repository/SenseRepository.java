package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SenseRepository extends JpaRepository<WnwbSense, Integer>, SenseRepositoryCustom {
    @EntityGraph(attributePaths = {"lexicalEntry", "synset"})
    @Query("SELECT s FROM WnwbSense s WHERE s.lexicalEntry.id = :lexicalEntryId AND s.isDeleted = false")
    List<WnwbSense> findByLexicalEntryId(@Param("lexicalEntryId") Integer lexicalEntryId);

    @EntityGraph(attributePaths = {"lexicalEntry", "lexicalEntry.lexicon", "synset"})
    @Query("SELECT s FROM WnwbSense s WHERE LOWER(s.lexicalEntry.lemma) = LOWER(:lemma) AND s.isDeleted = false")
    List<WnwbSense> findByLemma(@Param("lemma") String lemma);

    @EntityGraph(attributePaths = {"lexicalEntry", "lexicalEntry.lexicon", "synset"})
    @Query("SELECT s FROM WnwbSense s WHERE LOWER(s.lexicalEntry.lemma) = LOWER(:lemma) " +
           "AND s.isDeleted = false AND s.lexicalEntry.lexicon.language = :lang")
    List<WnwbSense> findByLemmaAndLanguage(@Param("lemma") String lemma, @Param("lang") String lang);

    @Query("SELECT s FROM WnwbSense s WHERE LOWER(s.lexicalEntry.lemma) = LOWER(:lemma) AND s.isDeleted = false")
    List<WnwbSense> findByLemmaWithoutEntityGraph(@Param("lemma") String lemma);
}
