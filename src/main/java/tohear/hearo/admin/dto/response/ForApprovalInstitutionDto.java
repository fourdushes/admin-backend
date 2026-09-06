package tohear.hearo.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.institution.domain.InstitutionApprovalState;
import tohear.hearo.institution.domain.InstitutionRegion;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForApprovalInstitutionDto {

    private Long id;
    private String institutionLoginId;
    private String institutionName;
    private InstitutionApprovalState institutionState;
    private InstitutionRegion region;
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
}