package tohear.hearo.care.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tohear.hearo.care.domain.Care;

public interface CareRepository extends JpaRepository<Care, Long> {
}
