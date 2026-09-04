package tohear.hearo.admin.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@AllArgsConstructor
public class AdminWardUserResponse {

    private List<AdminWardUserDto> wardUserList;;
    private int totalCount;

}
