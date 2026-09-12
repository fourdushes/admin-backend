package tohear.hearo.archive.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "archive_disease")
@Getter
public class ArchiveDisease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "archive_id", nullable = false, unique = true)
    private Archive archive;

    // 진료 시작 시에는 AI 응답이 없으므로 null(미분류)로 생성한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "disease_type", length = 32)
    private DiseaseType diseaseType;

    // 변환 전 AI 응답값을 보관한다. 정해진 enum 이름이면 diseaseType과 같은 값이다.
    @Column(name = "disease_word", columnDefinition = "TEXT")
    private String diseaseWord;

    protected ArchiveDisease() {
    }

    ArchiveDisease(Archive archive) {
        this.archive = Objects.requireNonNull(archive, "아카이브가 필요합니다.");
    }

    void updateFromAiWord(String word) {
        this.diseaseWord = word == null || word.isBlank() ? null : word.strip();
        this.diseaseType = DiseaseType.fromAiWord(word);
    }
}
