package tohear.hearo.archive.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tohear.hearo.archive.domain.Archive;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {
}
