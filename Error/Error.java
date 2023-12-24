package Error;

public class Error {
    private int lineNumber;
    private ErrorType errorType;

    public Error(int lineNumber, ErrorType errorType) {
        this.lineNumber = lineNumber;
        this.errorType = errorType;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public ErrorType getType() {
        return errorType;
    }

    @Override
    public String toString() {
        return lineNumber + " " + errorType.toString() + "\n";
    }

}
