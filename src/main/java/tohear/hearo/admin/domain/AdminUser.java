package tohear.hearo.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import lombok.Getter;

@Entity
@Getter
public class AdminUser {

    @Id
    @Column(name = "admin_user_id")
    private String id;
    private String name; // 관리자 이름
    private String password;

    @Enumerated(EnumType.STRING)
    private AdminType adminType; // 관리자 유형 (최고 관리자, 운영 관리자, 일반 관리자)

    public AdminUser() {
    }

    public AdminUser(Long id, String name, String password, AdminType adminType) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.adminType = AdminType.SUPER_ADMIN; // 기본적으로 일반 관리자(AdminType.ADMIN)로 설정
    }

    

}
