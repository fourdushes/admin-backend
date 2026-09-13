package tohear.hearo.global.response;

import lombok.Getter;

@Getter
public class Result<T> {

    private final String status;
    private final String message;
    private final T data;

    public Result(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
