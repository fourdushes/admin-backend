package tohear.hearo.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tohear.hearo.admin.domain.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, String>, AdminUserCustomRepository {

}
