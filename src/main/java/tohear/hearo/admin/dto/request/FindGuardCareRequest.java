package tohear.hearo.admin.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.care.domain.CareState;

@Data 
@NoArgsConstructor 
public class FindGuardCareRequest {

    private String guardUserId;
    private String guardUserName;
    private CareState careState;

}
