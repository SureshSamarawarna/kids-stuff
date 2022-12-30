package lk.ijse.dep9.kids.exception;

public class InvalidFieldException extends InvalidBookException {

    public InvalidFieldException(String field) {
        super(field);
    }

    public InvalidFieldException(String message, String field) {
        super(message, field);
    }

    public InvalidFieldException(String message, Throwable cause, String field) {
        super(message, cause, field);
    }

    public InvalidFieldException(Throwable cause, String field) {
        super(cause, field);
    }
}
