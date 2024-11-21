package se233.teamnoonkhem.Controller;

public class ExceptionHandler {

    public static class OperationErrorException extends RuntimeException {
        public OperationErrorException(String message) {
            super(message);
        }

        public OperationErrorException(String message, Throwable cause) {
            super(message, cause);
        }

        public void printOperationErrorStackTrace(){
            System.out.println("se233.teamnoonkhem.Controller.ExceptionHandler.OperationErrorException" + getMessage());
        }

    }
}
