package frontend.tokens;

public class Token {
    private TokenCode tokenCode;
    private String value;
    private int lineNumber;

    public Token(TokenCode tokenCode, int lineNumber, String value) {
        this.tokenCode = tokenCode;
        this.lineNumber = lineNumber;
        this.value = value;
    }

    public TokenCode getTokenCode() {
        return this.tokenCode;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return tokenCode.toString() + " " + value + "\n";
    }

}
