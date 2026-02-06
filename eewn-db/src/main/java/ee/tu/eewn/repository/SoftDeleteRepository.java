package ee.tu.eewn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//TODO Kui kasutusse ss extendi kõiki vajalikke reposid
@NoRepositoryBean
public interface SoftDeleteRepository<T, ID> extends JpaRepository<T, ID> {
    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false")
    List<T> findAllActive();

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = false AND e.id = :id")
    Optional<T> findActiveById(@Param("id") ID id);
}
