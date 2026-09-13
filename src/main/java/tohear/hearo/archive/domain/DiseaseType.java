package tohear.hearo.archive.domain;

import lombok.Getter;

/** AI가 반환한 영문 enum 이름 또는 한글 label의 통계용 분류. 의료진의 확정 진단을 의미하지 않는다. */
@Getter
public enum DiseaseType {
    COLD("감기"),
    UNDEFINED_1("미정1"),
    UNDEFINED_2("미정2"),
    UNDEFINED_3("미정3"),
    UNDEFINED_4("미정4"),
    UNDEFINED_5("미정5"),
    UNDEFINED_6("미정6"),
    UNDEFINED_7("미정7"),
    UNDEFINED_8("미정8"),
    OTHER("그 외");

    private final String label;

    DiseaseType(String label) {
        this.label = label;
    }

    public static DiseaseType fromAiWord(String word) {
        if (word == null || word.isBlank()) {
            return OTHER;
        }

        try {
            return DiseaseType.valueOf(word.strip());
        } catch (IllegalArgumentException e) {
            String koreanWord = word.strip().replace(" ", "");
            for (DiseaseType type : values()) {
                if (type.label.replace(" ", "").equals(koreanWord)) {
                    return type;
                }
            }
            return OTHER;
        }
    }
}
