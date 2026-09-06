package tohear.hearo.admin.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminWardCareResponse {

    private String wardUserId;
    private String wardUserName;
    private Long totalCount;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private List<AdminWardCareDto> list;

}
