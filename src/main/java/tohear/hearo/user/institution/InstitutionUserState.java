package tohear.hearo.user.institution;

public enum InstitutionUserState {

    APPROVED("승인됨"), // 승인됨
    PENDING("승인 대기 중"), // 승인 대기 중
    REJECTED("거절됨"), // 거절됨
    DELETE("삭제됨"); // 승인된 사용자를 삭제함

    private final String description;


    InstitutionUserState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
