package tohear.hearo.admin.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class SingleArchiveResponse {

    private Long archiveId;
    private String title;
    private LocalDateTime archiveDate;
    private String text;
    private String allChatText;
    private String mainSymptoms;
    private String doctorOpinion;
    private String remember;
    private String questionAnswer;
    private String difficultWords;

}
