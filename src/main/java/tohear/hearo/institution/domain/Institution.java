package tohear.hearo.institution.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Getter;

@Entity
@Getter
public class Institution {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Institution_id")
    private Long id;
    private String institutionName;
    private String email;
    private String institutionLoginId;
    private String passward;
    private InstitutionApprovalState institutionState;
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;

    @Enumerated(EnumType.STRING)
    private InstitutionRegion region;

    public Institution() {
    }

    public Institution(String institutionName, String email,String institutionLoginId, String passward) {
        this.institutionName = institutionName;
        this.email = email;
        this.institutionLoginId = institutionLoginId;
        this.passward = passward;
        this.institutionState = InstitutionApprovalState.PENDING;
        this.requestDate = LocalDateTime.now();
        this.approvalDate = LocalDateTime.now();
    }

    public void approve() {
        this.institutionState = InstitutionApprovalState.APPROVED;
    }

    public void reject() {
        this.institutionState = InstitutionApprovalState.REJECTED;
    }
    public void setApprovalDate() {
        this.approvalDate = LocalDateTime.now();
    }
}
