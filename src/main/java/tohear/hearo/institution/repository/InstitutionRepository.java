package tohear.hearo.institution.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tohear.hearo.institution.domain.Institution;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
}
