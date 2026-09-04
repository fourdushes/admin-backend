package tohear.hearo.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AdminInstitutionsUserDto {

    private String userId;
    private String userName;
    private long totalTreatmentCount; // 총 진료 수
    private String institutionName; // 기관 이름
    private LocalDateTime joinDateTime; // 회원가입 시점
    private LocalDateTime lastLoginDateTime; // 마지막 로그인 시점

}
