package tohear.hearo.user.institution;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import tohear.hearo.institution.domain.Institution;
import tohear.hearo.user.auth.domain.UserType;

@Entity
@Getter
public class InstitutionsUser {

    @Id
    @Column(name = "institutions_user_id")
    private String id;
    private String name; // 기관 사용자 이름
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType; // 사용자 유형 (기관 사용자)

    @JoinColumn(name = "institution_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution; // 기관 아이디

    @Enumerated(EnumType.STRING)
    private InstitutionUserState institutionState; // 기관 승인 상태
    private LocalDateTime sendRequestDateTime; // 기관에 가입 승인을 보낸 시간
    private LocalDateTime joinDateTime; // 회원가입한 시간
    private LocalDateTime lastLoginDateTime; // 마지막 로그인한 시간

    public InstitutionsUser() {
    }

    public InstitutionsUser(String id, String name, String email, String password, UserType userType, Institution institution) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.userType = userType;
        this.institution = institution;
        this.institutionState = InstitutionUserState.PENDING;
        this.sendRequestDateTime = LocalDateTime.now();
        this.joinDateTime = LocalDateTime.now();
        this.lastLoginDateTime = LocalDateTime.now();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void approved() {
        this.institutionState = InstitutionUserState.APPROVED;
    }

    public void reject() {
        this.institutionState = InstitutionUserState.REJECTED;
    }

    public void delete() {
        this.institutionState = InstitutionUserState.DELETE;
    }

    public void updateLastLoginDateTime() {
        this.lastLoginDateTime = LocalDateTime.now();
    }
}
