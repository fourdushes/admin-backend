package tohear.hearo.care.domain;

public enum CareState {

    APPROVED("승인됨"), // 승인됨
    PENDING("승인 대기 중"), // 승인 대기 중
    REJECTED("거절됨"); // 거절됨

    private final String description;

    CareState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
