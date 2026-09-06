package tohear.hearo.admin.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.care.domain.CareState;

@Data
@NoArgsConstructor
public class FindWardCareRequest {

    private String wardUserId;
    private String wardUserName;
    private CareState careState;

}
