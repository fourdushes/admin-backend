package tohear.hearo.medicaltreatment.medicalrequest.domain;

public enum MedicalRequestStatus {
    REQUESTED("요청됨"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
    IN_PROGRESS("진료 중"),
    COMPLETED("완료됨"),
    CANCELED("취소됨");

    private final String description;

    MedicalRequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
