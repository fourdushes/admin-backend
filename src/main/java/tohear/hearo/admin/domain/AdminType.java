package tohear.hearo.admin.domain;

public enum AdminType {
    SUPER_ADMIN("최고 관리자"),
    OPERATIONS_ADMIN("운영 관리자"),
    ADMIN("일반 관리자");

    private final String description;

    AdminType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
