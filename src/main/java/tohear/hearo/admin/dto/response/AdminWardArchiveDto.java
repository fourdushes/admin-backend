package tohear.hearo.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminWardArchiveDto {

    private long archiveId;
    private String archiveName;
    private LocalDateTime archiveDate;
    

}
