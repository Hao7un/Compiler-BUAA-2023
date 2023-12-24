package midend;

import midend.types.*;
import midend.values.BasicBlock;
import midend.values.Const.ConstInteger;
import midend.values.Const.Function;
import midend.values.Const.GlobalVar;
import midend.values.Instructions.binary.*;
import midend.values.Instructions.mem.*;
import midend.values.Instructions.others.*;
import midend.values.Instructions.terminator.Br;
import midend.values.Instructions.terminator.Ret;
import midend.values.Value;

import java.util.ArrayList;
import java.util.HashMap;

/*为Visitor提供构造LLVM IR语句的方法*/
public class IRBuilder {

    private static HashMap<Function , Integer > variableCounter = new HashMap<>();
    private static HashMap<Function , Integer > basicBlockCounter = new HashMap<>();

    public static int buildVariableName(Function function) {
        int number = variableCounter.get(function);
        variableCounter.put(function,number+1);
        return number;
    }
    public static int buildBasicBlockName(Function function) {
        int number = basicBlockCounter.get(function);
        basicBlockCounter.put(function,number+1);
        return number;
    }

    public static void buildLibFunction(IRModule IRModule, ArrayList<HashMap<String, Value>> symbolTables) {
        ValueType valueType = new IntegerType(32);
        ArrayList<ValueType> arguments = new ArrayList<>();
        arguments.add(valueType);
        Function getint = new Function("getint",new FunctionType(new IntegerType(32),new ArrayList<>()),true);
        Function putint = new Function("putint",new FunctionType(new VoidType(),arguments),true);
        Function putch = new Function("putch",new FunctionType(new VoidType(),arguments),true);
        ArrayList<ValueType> putstrArgument = new ArrayList<>();
        putstrArgument.add(new PointerType(new IntegerType(8)));
        Function putstr = new Function("putstr",new FunctionType(new VoidType(),putstrArgument),true);

        int length = symbolTables.size();
        symbolTables.get(length-1).put("getint",getint);
        symbolTables.get(length-1).put("putint",putint);
        symbolTables.get(length-1).put("putch",putch);
        symbolTables.get(length-1).put("putstr",putstr);

        IRModule.addLibFunction(getint);
        IRModule.addLibFunction(putint);
        IRModule.addLibFunction(putch);
        IRModule.addLibFunction(putstr);
    }
    public static Function buildFunction(String name, FunctionType type, IRModule IRModule) {
        int paramNumber = type.getParametersType().size();
        Function function = new Function(name, type, false);
        variableCounter.put(function,paramNumber);
        basicBlockCounter.put(function,0);

        IRModule.addFunction(function);
        return function;
    }

    public static Value transformBitsTo32(Value operand, BasicBlock basicBlock) {
        if (operand.getValueType() instanceof IntegerType &&
                ((IntegerType)operand.getValueType()).getBits() == 1) {
            return buildZextTo(basicBlock,new IntegerType(32),operand);
        } else {
            return operand;
        }
    }

    public static Value transformBitsTo1(Value operand, BasicBlock basicBlock) {
        if (operand.getValueType() instanceof IntegerType &&
                ((IntegerType)operand.getValueType()).getBits() == 32) {
            return buildTruncTo(basicBlock,new IntegerType(1),operand);
        } else {
            return operand;
        }
    }

    public static BasicBlock buildBasicBlock(Function function) {
        int number = basicBlockCounter.get(function);
        basicBlockCounter.put(function,number+1);
        BasicBlock basicBlock = new BasicBlock(function.getValueName()+"_"+"BasicBlock_"+number,function);
        function.addBasicBlock(basicBlock);
        return basicBlock;
    }


    public static Ret buildRet(BasicBlock basicBlock) {
        /*no return value */
        Ret retInstr = new Ret(basicBlock);
        basicBlock.addInstruction(retInstr);
        return retInstr;
    }

    public static Ret buildRet(BasicBlock basicBlock, Value value) {
        /*has return value*/
        value = transformBitsTo32(value,basicBlock);
        Ret retInstr = new Ret(basicBlock,value);
        basicBlock.addInstruction(retInstr);
        return retInstr;
    }

    public static Value buildAdd(BasicBlock basicBlock,Value leftOperand,Value rightOperand) {
        leftOperand = transformBitsTo32(leftOperand,basicBlock);
        rightOperand = transformBitsTo32(rightOperand,basicBlock);
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        Add addInstr = new Add("%_"+number,basicBlock,leftOperand,rightOperand);
        basicBlock.addInstruction(addInstr);
        return addInstr;
    }

    public static Value buildSub(BasicBlock basicBlock,Value leftOperand,Value rightOperand) {
        leftOperand = transformBitsTo32(leftOperand,basicBlock);
        rightOperand = transformBitsTo32(rightOperand,basicBlock);
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        Sub subInstr = new Sub("%_"+number,basicBlock,leftOperand,rightOperand);
        basicBlock.addInstruction(subInstr);
        return subInstr;
    }

    public static Value buildMul(BasicBlock basicBlock, Value leftOperand, Value rightOperand) {
        leftOperand = transformBitsTo32(leftOperand,basicBlock);
        rightOperand = transformBitsTo32(rightOperand,basicBlock);
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        Mul mulInstr = new Mul("%_"+number,basicBlock,leftOperand,rightOperand);
        basicBlock.addInstruction(mulInstr);
        return mulInstr;
    }

    public static Value buildSdiv(BasicBlock basicBlock, Value leftOperand, Value rightOperand) {
        leftOperand = transformBitsTo32(leftOperand,basicBlock);
        rightOperand = transformBitsTo32(rightOperand,basicBlock);
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        Sdiv sdivInstr = new Sdiv("%_"+number,basicBlock,leftOperand,rightOperand);
        basicBlock.addInstruction(sdivInstr);
        return sdivInstr;
    }

    public static Value buildSrem(BasicBlock basicBlock, Value leftOperand, Value rightOperand) {
        leftOperand = transformBitsTo32(leftOperand,basicBlock);
        rightOperand = transformBitsTo32(rightOperand,basicBlock);
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        Srem sremInstr = new Srem("%_"+number,basicBlock,leftOperand,rightOperand);
        basicBlock.addInstruction(sremInstr);
        return sremInstr;
    }

    public static GlobalVar buildGlovalVar(String name, ValueType valueType,Value varValue,Boolean isConst) {
        return new GlobalVar(name,valueType,varValue,isConst);
    }

    public static GlobalVar buildGlobalString(String name) {
        return new GlobalVar(name);
    }

    public static Alloca buildAlloca(ValueType valueType,BasicBlock basicBlock) {
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        Alloca allocaInstr = new Alloca("%_"+number,valueType,basicBlock);
        basicBlock.addInstruction(allocaInstr);
        return allocaInstr;
    }

    public static Store buildStore(Value storeValue, Value operand, BasicBlock basicBlock) {
        Store storeInstr = new Store("",basicBlock,storeValue,operand);
        basicBlock.addInstruction(storeInstr);
        return storeInstr;
    }

    public static Load buildLoad(ValueType valueType,Value operand, BasicBlock basicBlock) {
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        Load loadInstr = new Load("%_"+number,valueType,basicBlock,operand);
        basicBlock.addInstruction(loadInstr);
        return loadInstr;
    }

    public static Call buildCall(Function function,FunctionType functionType,BasicBlock basicBlock,ArrayList<Value> arguments)  {
        String name="";
        if (functionType.getReturnType() instanceof VoidType) {
            name = "";
        } else if (functionType.getReturnType() instanceof IntegerType) {
            int number = variableCounter.get(basicBlock.getParentFunction());
            variableCounter.put(basicBlock.getParentFunction(),number+1);
            name = "%_" + number;
        }
        Call callInstr = new Call(name,functionType,basicBlock,function,arguments);
        basicBlock.addInstruction(callInstr);
        return callInstr;
    }

    public static Br buildBr(BasicBlock curBasicBlock, BasicBlock exitBlock) {
        Br brInstr = new Br(exitBlock,curBasicBlock);
        curBasicBlock.addInstruction(brInstr);
        return brInstr;
    }

    public static Br buildBr(BasicBlock curBasicBlock, BasicBlock trueBlock,BasicBlock falseBlock,Value value) {
        value = transformBitsTo1(value,curBasicBlock);
        Br brInstr = new Br(trueBlock,falseBlock,curBasicBlock,value);
        curBasicBlock.addInstruction(brInstr);
        return brInstr;
    }

    public static Icmp buildIcmp(BasicBlock basicBlock,String operation,Value leftOperand,Value rightOperand) {
        //leftOperand = transformBitsTo32(leftOperand,basicBlock);
        //rightOperand = transformBitsTo32(rightOperand,basicBlock);

        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        Icmp icmpInstr = new Icmp("%_"+number,basicBlock,operation,leftOperand,rightOperand);
        basicBlock.addInstruction(icmpInstr);
        return icmpInstr;
    }

    public static ZextTo buildZextTo(BasicBlock basicBlock, ValueType valueType, Value operand1) {
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        ZextTo zextToInstr = new ZextTo("%_"+number,valueType,basicBlock,operand1);
        basicBlock.addInstruction(zextToInstr);
        return zextToInstr;
    }

    public static TruncTo  buildTruncTo(BasicBlock basicBlock, ValueType valueType, Value operand1) {
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        TruncTo truncToInstr = new TruncTo("%_"+number,valueType,basicBlock,operand1);
        basicBlock.addInstruction(truncToInstr);
        return truncToInstr;
    }

    public static GetElementPtr buildGetElementPtr(Value pointer,ArrayList<Value> indices,BasicBlock basicBlock) {
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        GetElementPtr getElementPtrInstr = new GetElementPtr("%_"+number,pointer,indices,basicBlock);
        basicBlock.addInstruction(getElementPtrInstr);
        return getElementPtrInstr;
    }

    /*Array*/
    public static int constantArrayIndex = 0;
    public static void buildLocalArray(ArrayList<Value> constArray,Value pointer,ArrayList<Integer> dimensions,BasicBlock curBasicBlock,int depth) {
        for (int i = 0; i < dimensions.get(depth - 1);i++) {
            ArrayList<Value> indices = new ArrayList<>();
            indices.add(new ConstInteger(32,0));
            indices.add(new ConstInteger(32,i));
            GetElementPtr getElementPtrInstr = buildGetElementPtr(pointer,indices,curBasicBlock);
            if (depth == dimensions.size()) {
                Store storeInstr = buildStore(constArray.get(constantArrayIndex++),getElementPtrInstr,curBasicBlock);
            } else {
                buildLocalArray(constArray,getElementPtrInstr, dimensions, curBasicBlock, depth + 1);
            }
        }
    }

    /*中端优化相关，由于instr插入的位置特殊，所以在build中不执行basicBlock增加instr*/

    public static Phi buildPhi(ArrayList<BasicBlock> prevs,BasicBlock basicBlock) {
        int number = variableCounter.get(basicBlock.getParentFunction());
        variableCounter.put(basicBlock.getParentFunction(),number+1);
        Phi phi = new Phi("%_"+number, prevs,basicBlock);
        return phi;
    }

    public static PC buildPC(BasicBlock basicBlock) {
        PC pc = new PC("",basicBlock);
        return pc;
    }

    public static Move buildMove(Value src, Value dst, BasicBlock parentBlock) {
        Move move = new Move(dst.getValueName(),dst,src,parentBlock);
        return move;
    }
}
