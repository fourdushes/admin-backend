package tohear.hearo.user.auth.domain;

public enum UserType {
    WARD("피보호자"),
    GUARDIAN("보호자"),
    INSTITUTIONS("기관 사용자");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
