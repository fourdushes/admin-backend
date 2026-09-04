package tohear.hearo.admin.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@AllArgsConstructor
public class AdminGuardUserResponse {

    private List<AdminGuardUserDto> guardUserList;
    private int totalCount;

}
