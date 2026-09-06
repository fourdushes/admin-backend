package tohear.hearo.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.care.domain.CareState;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class AdminGuardCareDto {

    private Long careId;
    private String wardUserId;
    private String wardUserName;
    private LocalDateTime createdAt; // 연결 요청 생성 시간
    private LocalDateTime updatedAt; // 연결 요청 상태 변경 시간
    private CareState careState;
    private Boolean mainGuardUser; // 메인 보호자 인지


}
