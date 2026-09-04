package tohear.hearo.admin.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@AllArgsConstructor
public class AdminInstitutionsUserResponse {

    private List<AdminInstitutionsUserDto> institutionsUserList;
    private int totalCount;

}
