import java.io.IOException;
import java.util.ArrayList;

import backend.Components.MipsModule;
import backend.MipsGenerator;
import backend.optimize.BackOptimizer;
import midend.IRModule;
import frontend.ASTNode.CompUnitNode;
import frontend.Lexer;
import frontend.Parser;
import frontend.tokens.Token;
import midend.Visitor;
import Error.ErrorHandler;
import midend.optimize.MidOptimizer;
import utils.InOututils;

public class Compiler {

    private static boolean optimize = true; // true ： 开启优化 ； false ： 关闭优化

    public static void main(String[] args) {
        try {
            String fileContent = InOututils.read();

            /*LEXER*/
            Lexer lexer = new Lexer(fileContent);
            lexer.tokenize();
            ArrayList<Token> tokens = lexer.getTokens();

            /*PARSER*/
            Parser parser = new Parser(tokens);
            CompUnitNode compUnitNode = parser.parseCompUnit();

            /*ERROR HANDLING*/
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            InOututils.write(errorHandler.toString(),"error.txt");

            if (errorHandler.toString().isEmpty()) {
                InOututils.write(compUnitNode.toString(),"output.txt");

                /*LLVM GENERATION*/
                Visitor visitor = new Visitor();
                visitor.visitCompUnit(compUnitNode);
                IRModule irModule = IRModule.getInstance();
                InOututils.write(irModule.toString(),"testfilei_21371372_严皓钧_优化前中间代码.txt.txt");

                /*Mid-OPTIMIZATION*/
                if (optimize) {
                    MidOptimizer midOptimizer = new MidOptimizer();
                    midOptimizer.optimize(IRModule.getInstance());
                }

                InOututils.write(irModule.toString(),"testfilei_21371372_严皓钧_优化后中间代码.txt");

                /*MIPS GENERATION*/
                MipsGenerator mipsGenerator = new MipsGenerator(optimize);
                mipsGenerator.genModule();

                /*Back-Optimization*/
                if (optimize) {
                    BackOptimizer backOptimizer = new BackOptimizer();
                    backOptimizer.optimize(MipsModule.getInstance());
                }

                InOututils.write(MipsModule.getInstance().toString(),"mips.txt");
                if (optimize) {
                    InOututils.write(MipsModule.getInstance().toString(),"testfilei_21371372_严皓钧_优化后目标代码.txt");
                } else {
                    InOututils.write(MipsModule.getInstance().toString(),"testfilei_21371372_严皓钧_优化前目标代码.txt");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
