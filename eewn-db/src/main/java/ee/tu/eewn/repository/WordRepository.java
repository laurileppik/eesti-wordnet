package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbLexicalentry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WordRepository extends JpaRepository<WnwbLexicalentry, Integer> {
    @Query("SELECT w FROM WnwbLexicalentry w WHERE LOWER(w.lemma) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<WnwbLexicalentry> searchByLemma(@Param("query") String query);
}
