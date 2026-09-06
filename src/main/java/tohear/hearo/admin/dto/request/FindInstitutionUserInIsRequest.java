package tohear.hearo.admin.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import tohear.hearo.user.institution.InstitutionUserState;

@Data 
@NoArgsConstructor 
public class FindInstitutionUserInIsRequest {

    private String keyword;
    private long institutionId;
    private InstitutionUserState state;

}
