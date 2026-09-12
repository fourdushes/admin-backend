package tohear.hearo.care.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import tohear.hearo.user.guardian.GuardUser;
import tohear.hearo.user.ward.WardUser;

@Entity
@Getter
public class Care {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "ward_user_id")

    @ManyToOne(fetch = FetchType.LAZY)
    private WardUser wardUser; // 피보호자

    @JoinColumn(name = "guard_user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GuardUser guardUser; // 보호자

    @Enumerated(EnumType.STRING)
    private CareState careState; // 피보호자 승인 상태
    private Boolean mainGuardUser; // 메인 보호자 인지
    private LocalDateTime createdAt; // 연결 요청 생성 시간
    private LocalDateTime updatedAt; // 연결 요청 상태 변경 시간

    public Care() {
    }

    public Care(WardUser wardUser, GuardUser guardUser) {
        this.wardUser = wardUser;
        this.guardUser = guardUser;
        this.careState = CareState.PENDING; // 기본값은 PENDING으로 설정
        this.mainGuardUser = false;
        this.createdAt = LocalDateTime.now(); // 생성 시간은 현재 시간으로 설정
        this.updatedAt = LocalDateTime.now(); // 상태 변경 시간은 일단은 현재 시간으로 설정
    }

    // 보호자, 피보호자 연결상태 승인으로 변경
    public void approve() {
        this.careState = CareState.APPROVED;
        this.updatedAt = LocalDateTime.now(); // 상태 변경 시간 설정
    }

    // 보호자, 피보호자 연결상태 거절로 변경
    public void reject() {
        this.careState = CareState.REJECTED;
        this.updatedAt = LocalDateTime.now(); // 상태 변경 시간 설정
    }

    public void changeMainGuard() {
        this.mainGuardUser = true;
    }

    public void deleteMainGuard() {
        this.mainGuardUser = false;
    }
}
