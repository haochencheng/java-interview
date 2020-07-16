package pers.interview.springboot.exception;

public class BusinessException extends Exception{

    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }
}
