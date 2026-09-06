package tohear.hearo.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.user.institution.InstitutionUserState;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class AdminInstitutionUserInIsDto {

    private String id;
    private String name;
    private InstitutionUserState institutionState; // 기관 승인 상태
    private LocalDateTime sendRequestDateTime; // 기관에 가입 승인을 보낸 시간
    private LocalDateTime joinDateTime; // 기관이 승인해준 시간

}
