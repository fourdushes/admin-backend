package tohear.hearo.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AdminWardUserDto {

    private String userId;
    private String userName;
    private long coldCount; // 감기 횟수
    private long undefinedCount1; // 정의되지 않은 질환 횟수
    private long undefinedCount2; // 정의되지 않은 질환 횟수
    private long undefinedCount3; // 정의되지 않은 질환 횟수
    private long undefinedCount4; // 정의되지 않은 질환 횟수
    private long undefinedCount5; // 정의되지 않은 질환 횟수
    private long undefinedCount6; // 정의되지 않은 질환 횟수
    private long undefinedCount7; // 정의되지 않은 질환 횟수
    private long undefinedCount8; // 정의되지 않은 질환 횟수
    private long otherCount; // 기타 질환 횟수
    private long totalArchiveCount; // 총 진료 기록 개수
    private long totalGuardCount; // 총 보호자 수
    private LocalDateTime joinDateTime; // 회원가입 시점
    private LocalDateTime lastLoginDateTime; // 마지막 로그인 시점

}
