package tohear.hearo.admin.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.institution.domain.InstitutionApprovalState;

@Data 
@NoArgsConstructor 
// 관리자가 기관 승인하기 전에 목록 뽑기
public class ForApprovalInstitutionRequest {

    private String keyword; // 기관 이름 또는 로그인 아이디
    private InstitutionApprovalState state; 

}
