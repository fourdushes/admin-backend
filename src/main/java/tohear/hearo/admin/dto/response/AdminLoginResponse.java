package tohear.hearo.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class AdminLoginResponse {

    private String accessToken;
    private String refreshToken;
}
