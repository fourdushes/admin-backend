package tohear.hearo.medicaltreatment.medicalrequest.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import tohear.hearo.user.institution.InstitutionsUser;
import tohear.hearo.user.ward.WardUser;

@Entity
@Getter
@Table(indexes = {
    @Index(name = "idx_medical_request_ward_status", columnList = "ward_user_id,status"),
    @Index(name = "idx_medical_request_institution_status", columnList = "institutions_user_id,status")
})
public class MedicalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medical_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_user_id", nullable = false)
    private WardUser wardUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institutions_user_id", nullable = false)
    private InstitutionsUser institutionUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicalRequestStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    protected MedicalRequest() {
    }

    public MedicalRequest(WardUser wardUser, InstitutionsUser institutionUser) {
        this.wardUser = wardUser;
        this.institutionUser = institutionUser;
        this.status = MedicalRequestStatus.REQUESTED;
        this.createdAt = LocalDateTime.now();
    }

    public void accept() {
        requireStatus(MedicalRequestStatus.REQUESTED);
        status = MedicalRequestStatus.ACCEPTED;
        respondedAt = LocalDateTime.now();
    }

    public void reject() {
        requireStatus(MedicalRequestStatus.REQUESTED);
        status = MedicalRequestStatus.REJECTED;
        respondedAt = LocalDateTime.now();
    }

    public void cancel() {
        requireStatus(MedicalRequestStatus.REQUESTED);
        status = MedicalRequestStatus.CANCELED;
    }

    public void start() {
        requireStatus(MedicalRequestStatus.ACCEPTED);
        status = MedicalRequestStatus.IN_PROGRESS;
        startedAt = LocalDateTime.now();
    }

    public void complete() {
        requireStatus(MedicalRequestStatus.IN_PROGRESS);
        status = MedicalRequestStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }

    private void requireStatus(MedicalRequestStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("현재 상태에서는 요청을 처리할 수 없습니다. 현재 상태: " + status);
        }
    }
}
