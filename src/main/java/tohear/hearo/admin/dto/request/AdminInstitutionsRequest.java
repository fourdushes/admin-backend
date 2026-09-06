package tohear.hearo.admin.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.institution.domain.InstitutionRegion;

@Data
@NoArgsConstructor
public class AdminInstitutionsRequest {

    private String keyword; // 기관 아이디 또는 이름

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;
        
    private InstitutionRegion region;

}
