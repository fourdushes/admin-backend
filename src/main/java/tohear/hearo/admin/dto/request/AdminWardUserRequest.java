package tohear.hearo.admin.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.user.auth.domain.UserType;

@Data 
@NoArgsConstructor 
public class AdminWardUserRequest {

    private String userId;
    private String startDate;
    private String endDate;
    private UserType userType; // USER, GUARDIAN, WARD
}
