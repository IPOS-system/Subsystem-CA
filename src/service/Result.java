package service;

//a helper class to return success or fail messages to the GUI.
public class Result {
    private final boolean success;
    private final String message;

    private Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static Result success(String message) {
        return new Result(true, message);
    }

    public static Result fail(String message) {
        return new Result(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}