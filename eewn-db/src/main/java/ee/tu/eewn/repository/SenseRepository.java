package ee.tu.eewn.repository;

import ee.tu.eewn.entity.core.WnwbSense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SenseRepository extends JpaRepository<WnwbSense, Integer> {
    @Query("SELECT s FROM WnwbSense s WHERE s.lexicalEntry.id = :lexicalEntryId AND s.isDeleted = false")
    List<WnwbSense> findByLexicalEntryId(@Param("lexicalEntryId") Integer lexicalEntryId);
}
