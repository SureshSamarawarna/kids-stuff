package lk.ijse.dep9.kids.exception;

public class InvalidBookException extends Exception{

    private final String field;

    public InvalidBookException(String field) {
        this.field = field;
    }

    public InvalidBookException(String message, String field) {
        super(message);
        this.field = field;
    }

    public InvalidBookException(String message, Throwable cause, String field) {
        super(message, cause);
        this.field = field;
    }

    public InvalidBookException(Throwable cause, String field) {
        super(cause);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}


