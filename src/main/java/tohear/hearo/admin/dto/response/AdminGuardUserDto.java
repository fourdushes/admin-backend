package tohear.hearo.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AdminGuardUserDto {

    private String userId;
    private String userName;
    private long totalWardCount; // 총 피보호자 수
    private LocalDateTime joinDateTime; // 회원가입 시점
    private LocalDateTime lastLoginDateTime; // 마지막 로그인 시점

}
