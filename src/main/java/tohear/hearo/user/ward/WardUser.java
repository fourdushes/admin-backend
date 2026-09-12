package tohear.hearo.user.ward;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import lombok.Getter;
import tohear.hearo.user.auth.domain.UserType;

@Entity
@Getter
public class WardUser {

    @Id
    @Column(name = "ward_user_id")
    private String id;
    private String name; // 환자 이름
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType; // 사용자 유형 (피보호자)
    private LocalDateTime joinDateTime; // 회원가입한 시간
    private LocalDateTime lastLoginDateTime; // 마지막 로그인한 시간

    public WardUser() {
    }

    public WardUser(String id, String name, String email, String password, UserType userType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.userType = userType;
        this.joinDateTime = LocalDateTime.now();
        this.lastLoginDateTime = LocalDateTime.now();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void updateLastLoginDateTime() {
        this.lastLoginDateTime = LocalDateTime.now();
    }
}
