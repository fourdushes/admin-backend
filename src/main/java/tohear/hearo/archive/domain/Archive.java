package tohear.hearo.archive.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import lombok.Getter;
import tohear.hearo.user.ward.WardUser;

@Entity
@Getter
public class Archive {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_id")
    private Long id;

    private String title;
    private LocalDateTime archiveDate;
    private String text;

    @Column(columnDefinition = "TEXT")
    private String allChatText;

    @Column(columnDefinition = "TEXT")
    private String mainSymptoms;

    @Column(columnDefinition = "TEXT")
    private String doctorOpinion;

    @Column(columnDefinition = "TEXT")
    private String remember;

    @Column(columnDefinition = "TEXT")
    private String questionAnswer;

    @Column(columnDefinition = "TEXT")
    private String difficultWords;

    @JoinColumn(name = "ward_user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private WardUser wardUser;

    @OneToOne(mappedBy = "archive", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ArchiveDisease archiveDisease;

    public Archive() {
    }

    public Archive(String text, WardUser wardUser) {
        LocalDateTime createTime = archiveDate == null ? LocalDateTime.now() : archiveDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년MM월dd일");

        this.title = createTime.format(formatter) + " " + wardUser.getName() + "님의 진료 기록";
        this.archiveDate = createTime;
        this.text = text;
        this.wardUser = wardUser;
        this.archiveDisease = new ArchiveDisease(this);
    }

    public void updateDisease(String diseaseWord) {
        // 변경 전에 만들어진 아카이브에는 질병 기록이 없을 수 있다.
        if (archiveDisease == null) {
            archiveDisease = new ArchiveDisease(this);
        }
        archiveDisease.updateFromAiWord(diseaseWord);
    }

    public void updateText(String text) {
        this.text = text;
    }

    public void updateAllChatText(String allChatText) {
        this.allChatText = allChatText;
    }

    public void updateSummary(
            String mainSymptoms,
            String doctorOpinion,
            String remember,
            String questionAnswer,
            String difficultWords) {
        this.mainSymptoms = mainSymptoms;
        this.doctorOpinion = doctorOpinion;
        this.remember = remember;
        this.questionAnswer = questionAnswer;
        this.difficultWords = difficultWords;
    }

}
