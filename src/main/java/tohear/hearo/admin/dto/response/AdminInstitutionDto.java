package tohear.hearo.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.institution.domain.InstitutionApprovalState;
import tohear.hearo.institution.domain.InstitutionRegion;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminInstitutionDto {

    private Long id;
    private String institutionName;
    private InstitutionApprovalState institutionState;
    private Long institutionUserCount;
    private InstitutionRegion region;
}