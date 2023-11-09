package frontend;

import java.util.ArrayList;
import java.util.HashMap;

import frontend.tokens.TokenCode;
import frontend.tokens.Token;

public class Lexer {
    private final String fileContent;

    private ArrayList<Token> tokens = new ArrayList<>();

    private HashMap<String,TokenCode> keywords = new HashMap<>();

    public Lexer(String fileContent){
        /*文件内容*/
        this.fileContent = fileContent;
        /*初始化关键词Map*/
        createKeywords();
    }

    private void createKeywords() {
        this.keywords.put("main",TokenCode.MAINTK);
        this.keywords.put("const",TokenCode.CONSTTK);
        this.keywords.put("int",TokenCode.INTTK);
        this.keywords.put("break",TokenCode.BREAKTK);
        this.keywords.put("continue",TokenCode.CONTINUETK);
        this.keywords.put("if",TokenCode.IFTK);
        this.keywords.put("else",TokenCode.ELSETK);
        this.keywords.put("for",TokenCode.FORTK);
        this.keywords.put("getint",TokenCode.GETINTTK);
        this.keywords.put("printf",TokenCode.PRINTFTK);
        this.keywords.put("return",TokenCode.RETURNTK);
        this.keywords.put("void",TokenCode.VOIDTK);
    }

    public ArrayList<Token> getTokens() {
        return this.tokens;
    }


    public void tokenize() {
        int lineNumber = 1;
        int fileLength = fileContent.length();
        int i;

        for (i = 0; i < fileLength;  i++) {
            char c = fileContent.charAt(i); //当前解析到的字符
            if (c == '\n') { //换行符，行数增加
                lineNumber++;
            } else if (c == '\t' || c == ' ') {
                continue;
            } else if (Character.isLetter(c) || c == '_') { // 标识符 & 关键字
                StringBuilder identifier = new StringBuilder();
                char temp; //用来构造identifier
                int j;
                for ( j = i; j < fileLength; j ++) {
                    temp = fileContent.charAt(j);
                    if (Character.isLetter(temp) || Character.isDigit(temp) || temp == '_') {
                        identifier.append(temp);
                    } else {
                        break;
                    }
                }
                String value = identifier.toString(); //token 的 value
                TokenCode tokenCode = keywords.getOrDefault(value,TokenCode.IDENFR);
                /*StringBuilder 中保存着value*/
                Token token = new Token(tokenCode,lineNumber,value); //完成构造
                this.tokens.add(token);
                i = j - 1; //更新i的值
            } else if (Character.isDigit(c)) { // 数字
                char temp;
                int j;
                StringBuilder number = new StringBuilder();
                for (j = i; j < fileLength; j++) {
                    temp = fileContent.charAt(j);
                    if (Character.isDigit(temp)) {
                        number.append(temp);
                    } else {
                        break;
                    }
                }
                String value = number.toString();
                Token token = new Token(TokenCode.INTCON,lineNumber,value);
                this.tokens.add(token);
                i = j -1;
            } else if (c == '/') { // 1. /  2. //  3. /*
                char temp = i+1 < fileLength ? fileContent.charAt(i + 1) : '\0';
                int j;
                if (temp == '/') {
                    j = fileContent.indexOf('\n',i+2);
                    if (j == -1) { //没找到，文件结束
                        i = fileLength - 1;
                    } else {
                        i = j -1;
                    }
                } else if (temp == '*') {
                    j = fileContent.indexOf("*/",i+2);
                    if (j == -1) { //没找到，文件结束
                        i = fileLength - 1;
                    } else {
                        i = j + 1;
                    }
                } else {
                    Token token =  new Token(TokenCode.DIV,lineNumber,"/");
                    tokens.add(token);
                }
            } else if (c == '\"') { // Format String
                int j;
                StringBuilder sb = new StringBuilder();
                sb.append('\"');
                for(j = i+1; j < fileLength; j++) {
                    if(fileContent.charAt(j) != '\"') {
                        sb.append(fileContent.charAt(j));
                    } else {
                        sb.append('\"');
                        break;
                    }
                }
                Token token = new Token(TokenCode.STRCON,lineNumber,sb.toString());
                this.tokens.add(token);
                i = j;
            } else if (c == '=') { // 1. =  2. ==
                char temp = i+1 < fileLength ? fileContent.charAt(i + 1) : '\0';
                if (temp == '=') { // ==
                    Token token = new Token(TokenCode.EQL,lineNumber,"==");
                    this.tokens.add(token);
                    i = i + 1;
                } else {
                    Token token = new Token(TokenCode.ASSIGN,lineNumber,"=");
                    this.tokens.add(token);
                }
            } else if (c == '!') { // 1. !  2. !=
                char temp = i+1 < fileLength ? fileContent.charAt(i + 1) : '\0';
                if (temp == '=') {
                    Token token = new Token(TokenCode.NEQ,lineNumber,"!=");
                    this.tokens.add(token);
                    i = i + 1;
                } else {
                    Token token = new Token(TokenCode.NOT,lineNumber,"!");
                    this.tokens.add(token);
                }
            } else if (c == '&') {
                char temp = i+1 < fileLength ? fileContent.charAt(i + 1) : '\0';
                if (temp == '&') {
                    Token token = new Token(TokenCode.AND,lineNumber,"&&");
                    i = i + 1;
                    this.tokens.add(token);
                }
            } else if (c == '|') {
                char temp = i+1 < fileLength ? fileContent.charAt(i + 1) : '\0';
                if (temp == '|') {
                    Token token = new Token(TokenCode.OR,lineNumber,"||");
                    i = i + 1;
                    this.tokens.add(token);
                }
            } else if (c == '+') {
                Token token = new Token(TokenCode.PLUS,lineNumber,"+");
                this.tokens.add(token);
            } else if (c == '-') {
                Token token = new Token(TokenCode.MINU,lineNumber,"-");
                this.tokens.add(token);
            }  else if (c == '*') {
                Token token = new Token(TokenCode.MULT,lineNumber,"*");
                this.tokens.add(token);
            }  else if (c == '%') {
                Token token = new Token(TokenCode.MOD,lineNumber,"%");
                this.tokens.add(token);
            }  else if (c == '<') {
                char temp = i+1 < fileLength ? fileContent.charAt(i + 1) : '\0';
                Token token;
                if (temp == '=') {
                    token = new Token(TokenCode.LEQ, lineNumber, "<=");
                    i++;
                } else {
                    token = new Token(TokenCode.LSS, lineNumber, "<");
                }
                this.tokens.add(token);
            }  else if (c == '>') {
                char temp = i+1 < fileLength ? fileContent.charAt(i + 1) : '\0';
                Token token;
                if (temp == '=') {
                    token = new Token(TokenCode.GEQ, lineNumber, ">=");
                    i++;
                } else {
                    token = new Token(TokenCode.GRE, lineNumber, ">");
                }
                this.tokens.add(token);
            } else if (c == ';') {
                Token token = new Token(TokenCode.SEMICN,lineNumber,";");
                this.tokens.add(token);
            } else if (c == ',') {
                Token token = new Token(TokenCode.COMMA,lineNumber,",");
                this.tokens.add(token);
            } else if (c == '(') {
                Token token = new Token(TokenCode.LPARENT,lineNumber,"(");
                this.tokens.add(token);
            } else if (c == ')') {
                Token token = new Token(TokenCode.RPARENT,lineNumber,")");
                this.tokens.add(token);
            } else if (c == '[') {
                Token token = new Token(TokenCode.LBRACK,lineNumber,"[");
                this.tokens.add(token);
            } else if (c == ']') {
                Token token = new Token(TokenCode.RBRACK,lineNumber,"]");
                this.tokens.add(token);
            } else if (c == '{') {
                Token token = new Token(TokenCode.LBRACE,lineNumber,"{");
                this.tokens.add(token);
            } else if (c == '}') {
                Token token = new Token(TokenCode.RBRACE,lineNumber,"}");
                this.tokens.add(token);
            }
        }
    }
}
