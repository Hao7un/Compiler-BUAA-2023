package backend;

import backend.Maneger.RegisterManager;
import backend.optimize.MulDivOptmizer;
import midend.IRModule;
import midend.types.PointerType;
import midend.types.ValueType;
import midend.values.*;
import midend.values.Const.ConstInteger;
import midend.values.Const.ConstString;
import midend.values.Const.Function;
import midend.values.Const.GlobalVar;
import midend.values.Instructions.IRInstruction;
import midend.values.Instructions.binary.*;
import midend.values.Instructions.mem.*;
import midend.values.Instructions.terminator.*;
import midend.values.Instructions.others.*;
import backend.Components.MipsBasicBlock;
import backend.Components.MipsFunction;
import backend.Components.MipsGlobalVariable;
import backend.Components.MipsModule;
import backend.Components.instrutions.*;
import backend.Operands.Imm;
import backend.Operands.Label;
import backend.Operands.Register;

import java.util.ArrayList;

public class MipsGenerator {
    private final IRModule irModule = IRModule.getInstance();
    private final MipsModule mipsModule = MipsModule.getInstance();

    private static BasicBlock llvmBasicBlock;
    private MipsBasicBlock curMipsBasicBlock = null;
    private MipsFunction curMipsFunction = null;
    private RegisterManager registerManager;
    private int curStackOffset = 0;        // Tracing current offset to $sp

    private boolean optimize;

    public MipsGenerator(boolean optimize) {
        this.optimize = optimize;
        this.registerManager = new RegisterManager();
    }

    public void genModule() {
        for (GlobalVar globalVar : irModule.getGlobalVars()) {
            genGlobalVar(globalVar,mipsModule);
        }
        for (Function function : irModule.getFunctions()) {
            genFunction(function,mipsModule);
        }
    }

    public static BasicBlock getLlvmBasicBlock(){
        return llvmBasicBlock;
    }

    public void genGlobalVar(GlobalVar globalVar,MipsModule mipsModule) {
        mipsModule.addGlobalVariable(new MipsGlobalVariable(globalVar));
    }

    public void genFunction(Function function,MipsModule mipsModule) {
        this.curStackOffset = 0;
        registerManager.setFunctionReg(function);
        MipsFunction mipsFunction = new MipsFunction(function.getValueName(),function);
        /*save stack for param*/
        genFuncParam(mipsFunction,function);
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            llvmBasicBlock = basicBlock;
            genBasicBlock(basicBlock,mipsFunction);
        }
        mipsModule.addFunction(mipsFunction);
    }

    public void genFuncParam(MipsFunction mipsFunction,Function function){
        ArrayList<Argument> parameters = function.getArguments();
        for (int i = 3; i < parameters.size(); i++) {
            Argument parameter = parameters.get(i);
            curStackOffset -= 4;
            mipsFunction.insertToSymbolTable(parameter.getValueName(),curStackOffset);
        }
    }

    public void genBasicBlock(BasicBlock basicBlock,MipsFunction mipsFunction) {
        MipsBasicBlock mipsBasicBlock = new MipsBasicBlock(basicBlock.getValueName().toString());
        for (IRInstruction irInstruction : basicBlock.getInstructions()) {
            MipsComment comment = new MipsComment(irInstruction.toString());
            mipsBasicBlock.addInstruction(comment);

            if (irInstruction instanceof Add) {
                genMipsFromAdd(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Sub) {
                genMipsFromArithmetic(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Mul) {
                if (optimize) {
                    genMipsFromMul(irInstruction,mipsBasicBlock,mipsFunction);
                } else {
                    genMipsFromArithmetic(irInstruction,mipsBasicBlock,mipsFunction);
                }
            } else if (irInstruction instanceof Sdiv) {
                if (optimize) {
                    genMipsFromDiv(irInstruction,mipsBasicBlock,mipsFunction);
                } else {
                    genMipsFromArithmetic(irInstruction,mipsBasicBlock,mipsFunction);
                }
            } else if (irInstruction instanceof Srem) {
                if (optimize) {
                    genMipsFromMod(irInstruction,mipsBasicBlock,mipsFunction);
                } else {
                    genMipsFromArithmetic(irInstruction,mipsBasicBlock,mipsFunction);
                }
            } else if (irInstruction instanceof Icmp) {
                genMipsFromIcmp(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Alloca) {
                genMipsFromAlloca(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof GetElementPtr) {
                genMipsFromGetElementPtr(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Load) {
                genMipsFromLoad(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Store) {
                genMipsFromStore(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Call) {
                genMipsFromCall(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof ZextTo) {
                genMipsFromZextTo(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Br) {
                genMipsFromBr(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Ret) {
                genMipsFromRet(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Move) {
                genMipsFromMove(irInstruction,mipsBasicBlock,mipsFunction);
            }
        }
        mipsFunction.addBasicBlock(mipsBasicBlock);
    }

    /*处理operand，并返回对应的寄存器*/
    /*使用前需要设置curMipsBasicBlock 和 curMipsFunction*/
    public Register handleOperand(Value operand,Register defaultReg) {
        if (operand instanceof ConstInteger || operand instanceof UndefinedValue) {
            //常量或者未定义变量
            int value = Integer.parseInt(operand.getValueName());
            Imm imm = new Imm(value);
            MipsAddi addi = new MipsAddi(new Register("$0"),defaultReg,imm);
            curMipsBasicBlock.addInstruction(addi);
            return defaultReg;
        } else if (registerManager.hasAllocReg(operand)) {
            // 寄存器中已经分配
            Register reg = registerManager.getRegisterOfValue(operand);
            return reg;
        } else {
            //未分配，要从栈中读出来
            String name = operand.getValueName();
            Imm offset = new Imm(curMipsFunction.getNameOffset(name));
            MipsLw lw1 = new MipsLw(new Register("$sp"),defaultReg,offset);
            curMipsBasicBlock.addInstruction(lw1);
            return defaultReg;
        }
    }

    private void genMipsFromAdd(IRInstruction irInstruction,MipsBasicBlock mipsBasicBlock,MipsFunction mipsFunction) {
        Value leftOperand = irInstruction.getOperand(0);
        Value rightOperand = irInstruction.getOperand(1);
        Register dstReg = registerManager.getRegisterOfValue(irInstruction);
        if (dstReg == null) {
            dstReg = new Register("$t0");
        }
        if ( (leftOperand instanceof ConstInteger && ! (rightOperand instanceof ConstInteger))
            || (rightOperand instanceof ConstInteger && ! (leftOperand instanceof ConstInteger))) {
            /*使用addi优化*/
            Register reg = new Register("$t0");
            Imm imm;
            if (leftOperand instanceof ConstInteger) {
                curMipsBasicBlock = mipsBasicBlock;
                curMipsFunction = mipsFunction;
                reg = handleOperand(rightOperand,reg);
                imm = new Imm(((ConstInteger) leftOperand).getValue());
            } else {
                curMipsBasicBlock = mipsBasicBlock;
                curMipsFunction = mipsFunction;
                reg = handleOperand(leftOperand,reg);
                imm = new Imm(((ConstInteger) rightOperand).getValue());
            }
            MipsAddi addi = new MipsAddi(reg,dstReg,imm);
            mipsBasicBlock.addInstruction(addi);
        } else {
            Register leftReg = new Register("$t0");
            Register rightReg = new Register("$t1");
            if (dstReg == null) {   //没有分配寄存器
                dstReg = leftReg;
            }
            curMipsBasicBlock = mipsBasicBlock;
            curMipsFunction = mipsFunction;
            leftReg = handleOperand(leftOperand,leftReg);
            rightReg = handleOperand(rightOperand,rightReg);

            MipsAddu addu = new MipsAddu(dstReg,leftReg,rightReg);
            mipsBasicBlock.addInstruction(addu);
        }

        /*Push Into Stack*/
        if (registerManager.getRegisterOfValue(irInstruction) == null) {
            curStackOffset -=4;
            int offset = curStackOffset;
            MipsSw sw = new MipsSw(new Register("$sp"),dstReg,new Imm(offset));
            mipsBasicBlock.addInstruction(sw);
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
        }
    }


    private void genMipsFromMul(IRInstruction irInstruction,MipsBasicBlock mipsBasicBlock,MipsFunction mipsFunction) {

        Value operand1 = irInstruction.getOperand(0);
        Value operand2 = irInstruction.getOperand(1);
        curMipsBasicBlock = mipsBasicBlock;

        Register dstReg = registerManager.getRegisterOfValue(irInstruction);
        if (dstReg == null) {
            dstReg = new Register("$t1");
        }

        if (!(operand1 instanceof ConstInteger) && !(operand2 instanceof ConstInteger)) {
            // 两个都不是常数
            Register srcReg1 = new Register("$t0");
            srcReg1 = handleOperand(operand1,srcReg1);

            Register srcReg2 = new Register("$t1");
            srcReg2 = handleOperand(operand2,srcReg2);

            MipsMulu mul = new MipsMulu(dstReg,srcReg1,srcReg2);
            mipsBasicBlock.addInstruction(mul);
        } else {
            // 有一个是常数，可能可以优化
            boolean optResult;
            if (operand1 instanceof ConstInteger) {
                Register srcReg1 = new Register("$t0");
                srcReg1 = handleOperand(operand2,srcReg1);
                optResult = MulDivOptmizer.optimizeMul(irInstruction,srcReg1,dstReg,mipsBasicBlock);
                if (!optResult) {
                    Register srcReg2 = new Register("$t1");
                    srcReg2 = handleOperand(operand1,srcReg2);
                    MipsMulu mul = new MipsMulu(dstReg,srcReg1,srcReg2);
                    mipsBasicBlock.addInstruction(mul);
                }
            } else {
                Register srcReg1 = new Register("$t0");
                srcReg1 = handleOperand(operand1,srcReg1);
                optResult = MulDivOptmizer.optimizeMul(irInstruction,srcReg1,dstReg,mipsBasicBlock);
                if (!optResult) {
                    Register srcReg2 = new Register("$t1");
                    srcReg2 = handleOperand(operand2,srcReg2);
                    MipsMulu mul = new MipsMulu(dstReg,srcReg1,srcReg2);
                    mipsBasicBlock.addInstruction(mul);
                }
            }
        }

        if (registerManager.getRegisterOfValue(irInstruction) == null) {
            curStackOffset -=4;
            int offset = curStackOffset;
            MipsSw sw = new MipsSw(new Register("$sp"),dstReg,new Imm(offset));
            mipsBasicBlock.addInstruction(sw);
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
        }
    }

    private void genMipsFromDiv(IRInstruction irInstruction,MipsBasicBlock mipsBasicBlock,MipsFunction mipsFunction) {
        Value operand1 = irInstruction.getOperand(0);
        Value operand2 = irInstruction.getOperand(1);
        curMipsBasicBlock = mipsBasicBlock;

        Register dstReg = registerManager.getRegisterOfValue(irInstruction);
        if (dstReg == null) {
            dstReg = new Register("$t1");
        }

        if (!(operand2 instanceof ConstInteger)) {
            // 右操作数不是常数，肯定无法优化
            Register srcReg1 = new Register("$t0");
            srcReg1 = handleOperand(operand1,srcReg1);

            Register srcReg2 = new Register("$t1");
            srcReg2 = handleOperand(operand2,srcReg2);

            MipsDiv div = new MipsDiv(srcReg1,srcReg2);
            mipsBasicBlock.addInstruction(div);

            MipsMflo mflo = new MipsMflo(dstReg);
            mipsBasicBlock.addInstruction(mflo);
        } else {
            Register srcReg = new Register("$t0");
            srcReg = handleOperand(operand1,srcReg);

            boolean optResult = MulDivOptmizer.optimizeDiv(irInstruction,srcReg,dstReg,mipsBasicBlock);
            assert optResult;
        }

        if (registerManager.getRegisterOfValue(irInstruction) == null) {
            curStackOffset -= 4;
            int offset = curStackOffset;
            MipsSw sw = new MipsSw(new Register("$sp"), dstReg, new Imm(offset));
            mipsBasicBlock.addInstruction(sw);
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(), curStackOffset);
        }
    }

    private void genMipsFromMod(IRInstruction irInstruction,MipsBasicBlock mipsBasicBlock,MipsFunction mipsFunction) {
        Value operand1 = irInstruction.getOperand(0);
        Value operand2 = irInstruction.getOperand(1);
        curMipsBasicBlock = mipsBasicBlock;

        Register dstReg = registerManager.getRegisterOfValue(irInstruction);
        if (dstReg == null) {
            dstReg = new Register("$t1");
        }

        if (!(operand2 instanceof ConstInteger)) {
            // 右操作数不是常数，肯定无法优化
            Register srcReg1 = new Register("$t0");
            srcReg1 = handleOperand(operand1,srcReg1);

            Register srcReg2 = new Register("$t1");
            srcReg2 = handleOperand(operand2,srcReg2);

            MipsDiv div = new MipsDiv(srcReg1,srcReg2);
            mipsBasicBlock.addInstruction(div);

            MipsMfhi mfhi = new MipsMfhi(dstReg);
            mipsBasicBlock.addInstruction(mfhi);
        } else {
            Register srcReg = new Register("$t0");
            srcReg = handleOperand(operand1,srcReg);

            boolean optResult = MulDivOptmizer.optimizeMod(irInstruction,srcReg,dstReg,mipsBasicBlock);
            assert optResult;
        }

        if (registerManager.getRegisterOfValue(irInstruction) == null) {
            curStackOffset -= 4;
            int offset = curStackOffset;
            MipsSw sw = new MipsSw(new Register("$sp"), dstReg, new Imm(offset));
            mipsBasicBlock.addInstruction(sw);
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(), curStackOffset);
        }
    }

    private void genMipsFromArithmetic(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        /*Add $rd, $rs, $rt*/
        Value leftOperand = irInstruction.getOperand(0);
        Value rightOperand = irInstruction.getOperand(1);

        Register leftReg = new Register("$t0");
        Register rightReg = new Register("$t1");
        Register dstReg = registerManager.getRegisterOfValue(irInstruction);
        if (dstReg == null) {   //没有分配寄存器
            dstReg = leftReg;
        }

        curMipsBasicBlock = mipsBasicBlock;
        curMipsFunction = mipsFunction;
        leftReg = handleOperand(leftOperand,leftReg);
        rightReg = handleOperand(rightOperand,rightReg);

        /*Generate Instr*/
        if (irInstruction instanceof Add) {
            MipsAddu addu = new MipsAddu(dstReg,leftReg,rightReg);
            mipsBasicBlock.addInstruction(addu);
        } else if(irInstruction instanceof Sub) {
            MipsSubu sub = new MipsSubu(dstReg,leftReg,rightReg);
            mipsBasicBlock.addInstruction(sub);
        } else if (irInstruction instanceof Mul) {
            MipsMulu mul = new MipsMulu(dstReg,leftReg,rightReg);
            mipsBasicBlock.addInstruction(mul);
        } else if (irInstruction instanceof Sdiv) {
            MipsDiv div = new MipsDiv(leftReg,rightReg);
            MipsMflo mflo = new MipsMflo(dstReg);
            mipsBasicBlock.addInstruction(div);
            mipsBasicBlock.addInstruction(mflo);
        } else if (irInstruction instanceof Srem) {
            MipsDiv div = new MipsDiv(leftReg,rightReg);
            MipsMfhi mfhi = new MipsMfhi(dstReg);
            mipsBasicBlock.addInstruction(div);
            mipsBasicBlock.addInstruction(mfhi);
        }

        /*Push Into Stack*/
        if (registerManager.getRegisterOfValue(irInstruction) == null) {
            curStackOffset -=4;
            int offset = curStackOffset;
            MipsSw sw = new MipsSw(new Register("$sp"),dstReg,new Imm(offset));
            mipsBasicBlock.addInstruction(sw);
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
        }
    }


    public void genMipsFromGetElementPtr(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        /*进行一点优化，如果是输出字符串的直接不用管*/
        if (irInstruction.getOperand(0) instanceof GlobalVar globalVar && globalVar.getVarValue() instanceof ConstString) {
            return;
        }
        curMipsFunction = mipsFunction;
        curMipsBasicBlock = mipsBasicBlock;

        if (irInstruction.getOperands().size() == 2) {
            //  %_4 = getelementptr [2 x i32], [2 x i32]* %_3, i32 1
            Value base = irInstruction.getOperand(0);
            String baseName = base.getValueName();

            Register reg1 = new Register("$t0");

            /* load Base To $t0*/
            if (base instanceof GlobalVar) {
                MipsLa la = new MipsLa(reg1,baseName.substring(1));
                mipsBasicBlock.addInstruction(la);
            } else if (registerManager.hasAllocReg(base)) {
                reg1 = registerManager.getRegisterOfValue(base);
            } else {
                int baseOffsetFromSp = mipsFunction.getNameOffset(baseName);
                MipsLw lw = new MipsLw(new Register("$sp"),reg1,new Imm(baseOffsetFromSp));
                mipsBasicBlock.addInstruction(lw);
            }

            Register reg2 = new Register("$t1");

            /*Load getElementPtr offset*/
            reg2 = handleOperand(irInstruction.getOperand(1),reg2);

            int size = ((PointerType) irInstruction.getOperand(0).getValueType()).getPointedType().getSize();


            if (MulDivOptmizer.is2Power(size)) {
                int power = MulDivOptmizer.getCLZ(size);
                MipsSll sll = new MipsSll(reg2,new Register("$t1"),new Imm(power));
                mipsBasicBlock.addInstruction(sll);
            } else {
                MipsMulu mul = new MipsMulu(new Register("$t1"),reg2,new Imm(size));
                mipsBasicBlock.addInstruction(mul);
            }


            Register dstReg = registerManager.getRegisterOfValue(irInstruction);
            if (dstReg == null) {
                dstReg  = new Register("$t1");
            }

            MipsAddu addu = new MipsAddu(dstReg,reg1,new Register("$t1"));
            mipsBasicBlock.addInstruction(addu);

            if (registerManager.getRegisterOfValue(irInstruction) == null) {
                curStackOffset -= 4;
                mipsFunction.insertToSymbolTable(irInstruction.getValueName(), curStackOffset);
                MipsSw sw = new MipsSw(new Register("$sp"), dstReg, new Imm(curStackOffset));
                mipsBasicBlock.addInstruction(sw);
            }

        } else if (irInstruction.getOperands().size() == 3){
            //  %_5 = getelementptr [2 x i32], [2 x i32]* %_4, i32 0, i32 1
            int offset;
            Value base = irInstruction.getOperand(0);
            String baseName = base.getValueName();

            Register reg1 = new Register("$t0");

            if (base instanceof GlobalVar) {
                MipsLa la = new MipsLa(reg1,baseName.substring(1));
                mipsBasicBlock.addInstruction(la);
            } else if (registerManager.hasAllocReg(base)) {
                reg1 = registerManager.getRegisterOfValue(base);
            } else {
                int baseOffsetFromSp = mipsFunction.getNameOffset(baseName);
                MipsLw lw = new MipsLw(new Register("$sp"),reg1,new Imm(baseOffsetFromSp));
                mipsBasicBlock.addInstruction(lw);
            }

            /*这里可以优化计算，节省第一次计算的时间开销*/
            Register reg2;
            int size;
            MipsMulu mul;
            MipsAddu addu;
            if (!(irInstruction.getOperand(1) instanceof ConstInteger constInteger &&
                constInteger.getValue()==0)) {
                /*Load getElementPtr offset-----1*/
                reg2 = new Register("$t1");

                /*Load getElementPtr offset*/
                reg2 = handleOperand(irInstruction.getOperand(1),reg2);

                size = ((PointerType) irInstruction.getOperand(0).getValueType()).getPointedType().getSize();

                if (MulDivOptmizer.is2Power(size)) {
                    int power = MulDivOptmizer.getCLZ(size);
                    MipsSll sll = new MipsSll(reg2,new Register("$t1"),new Imm(power));
                    mipsBasicBlock.addInstruction(sll);
                } else {
                    mul = new MipsMulu(new Register("$t1"),reg2,new Imm(size));
                    mipsBasicBlock.addInstruction(mul);
                }

                addu = new MipsAddu(new Register("$t0"),reg1,new Register("$t1"));
                mipsBasicBlock.addInstruction(addu);
            }

            /*Load getElementPtr offset-----2*/
            reg2 = new Register("$t1");

            /*Load getElementPtr offset*/
            reg2 = handleOperand(irInstruction.getOperand(2),reg2);


            size = ((PointerType)irInstruction.getValueType()).getPointedType().getSize();

            if (MulDivOptmizer.is2Power(size)) {
                int power = MulDivOptmizer.getCLZ(size);
                MipsSll sll = new MipsSll(reg2,new Register("$t1"),new Imm(power));
                mipsBasicBlock.addInstruction(sll);
            } else {
                mul = new MipsMulu(new Register("$t1"),reg2,new Imm(size));
                mipsBasicBlock.addInstruction(mul);
            }

            Register dstReg = registerManager.getRegisterOfValue(irInstruction);

            if (dstReg == null) {
                dstReg  = new Register("$t1");
            }

            addu = new MipsAddu(dstReg,new Register("$t1"),reg1);
            mipsBasicBlock.addInstruction(addu);

            if (!registerManager.hasAllocReg(irInstruction)) {
                curStackOffset -= 4;
                mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
                MipsSw sw = new MipsSw(new Register("$sp"),dstReg,new Imm(curStackOffset));
                mipsBasicBlock.addInstruction(sw);
            }

        }
    }

    public void genMipsFromAlloca(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        ValueType valueType = ((PointerType)irInstruction.getValueType()).getPointedType();
        curStackOffset -= valueType.getSize();

        if (registerManager.hasAllocReg(irInstruction)) {
            int stackOffset = curStackOffset;
            Register dstReg = registerManager.getRegisterOfValue(irInstruction);
            MipsAddi addi  = new MipsAddi(new Register("$sp"),dstReg,new Imm(stackOffset));
            mipsBasicBlock.addInstruction(addi);
        } else {
            int stackOffset = curStackOffset;
            MipsAddi addi  = new MipsAddi(new Register("$sp"),new Register("$t0"),new Imm(stackOffset));
            mipsBasicBlock.addInstruction(addi);

            curStackOffset -=4;
            stackOffset = curStackOffset;
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(),stackOffset);
            //allocate space
            MipsSw sw = new MipsSw(new Register("$sp"),new Register("$t0"),new Imm(stackOffset));
            mipsBasicBlock.addInstruction(sw);
        }
    }

    public void genMipsFromStore(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        Value source = irInstruction.getOperand(0);
        Value dest = irInstruction.getOperand(1);

        Register srcReg = new Register("$t0");
        Register dstReg = new Register("$t1");

        /*Getting the Value to Store*/
        curMipsFunction = mipsFunction;
        curMipsBasicBlock = mipsBasicBlock;

        srcReg = handleOperand(source,srcReg);

        /*Getting the destination to store*/
        if (dest instanceof GlobalVar) {
            MipsLa la = new MipsLa(dstReg,dest.getValueName().substring(1));
            mipsBasicBlock.addInstruction(la);
        } else if (registerManager.hasAllocReg(dest)) {
            dstReg =registerManager.getRegisterOfValue(dest);
        } else {
            int offset = mipsFunction.getNameOffset(dest.getValueName());
            MipsLw lw = new MipsLw(new Register("$sp"),dstReg,new Imm(offset));
            mipsBasicBlock.addInstruction(lw);
        }

        MipsSw sw = new MipsSw(dstReg, srcReg, new Imm(0));
        mipsBasicBlock.addInstruction(sw);
    }


    public void genMipsFromLoad(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        Value srcOperand = irInstruction.getOperand(0);
        String srcName = srcOperand.getValueName();

        Register srcReg = new Register("$t0");
        Register dstReg = registerManager.getRegisterOfValue(irInstruction);
        if (dstReg == null) {
            dstReg = srcReg;
        }
        /*处理SrcReg*/
        if (srcOperand instanceof GlobalVar) {
            //MipsLa
            MipsLa la = new MipsLa(srcReg,srcName.substring(1));
            mipsBasicBlock.addInstruction(la);
        } else if (registerManager.hasAllocReg(srcOperand)) {
            srcReg = registerManager.getRegisterOfValue(srcOperand);
        } else {
            int srcOffset = mipsFunction.getNameOffset(srcName);
            MipsLw lw = new MipsLw(new Register("$sp"),srcReg,new Imm(srcOffset));
            mipsBasicBlock.addInstruction(lw);
        }

        MipsLw lw = new MipsLw(srcReg,dstReg,new Imm(0));
        mipsBasicBlock.addInstruction(lw);

        if (registerManager.getRegisterOfValue(irInstruction) == null) {
            curStackOffset -=4;
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(), curStackOffset);
            MipsSw sw = new MipsSw(new Register("$sp"),dstReg,new Imm(curStackOffset));
            mipsBasicBlock.addInstruction(sw);
        }
    }


    public void genMipsFromIcmp(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        // %3 = Icmp eq, %1, %2

        Value leftOperand = irInstruction.getOperand(0);
        Value rightOperand = irInstruction.getOperand(1);

        Register leftReg = new Register("$t0");
        Register rightReg = new Register("$t1");
        Register dstReg = registerManager.getRegisterOfValue(irInstruction);
        if (dstReg == null) {
            dstReg = leftReg;
        }
        
        curMipsBasicBlock = mipsBasicBlock;
        curMipsFunction = mipsFunction;
        leftReg = handleOperand(leftOperand,leftReg);
        rightReg = handleOperand(rightOperand,rightReg);


        String icmpOp = ((Icmp)irInstruction).getIcmpOp();
        switch (icmpOp) {
            case "eq" -> {
                MipsSeq seq = new MipsSeq(dstReg, leftReg, rightReg);
                mipsBasicBlock.addInstruction(seq);
            }
            case "ne" -> {
                MipsSne sne = new MipsSne(dstReg, leftReg, rightReg);
                mipsBasicBlock.addInstruction(sne);
            }
            case "sge" -> {
                MipsSge sge = new MipsSge(dstReg, leftReg, rightReg);
                mipsBasicBlock.addInstruction(sge);
            }
            case "sgt" -> {
                MipsSgt sgt = new MipsSgt(dstReg, leftReg, rightReg);
                mipsBasicBlock.addInstruction(sgt);
            }
            case "sle" -> {
                MipsSle sle = new MipsSle(dstReg, leftReg, rightReg);
                mipsBasicBlock.addInstruction(sle);
            }
            case "slt" -> {
                MipsSlt slt = new MipsSlt(dstReg, leftReg, rightReg);
                mipsBasicBlock.addInstruction(slt);
            }
        }

        /*Push Into Stack*/
        if (registerManager.getRegisterOfValue(irInstruction) == null) {
            curStackOffset -= 4;
            int offset = curStackOffset;
            MipsSw sw = new MipsSw(new Register("$sp"),dstReg,new Imm(offset));
            mipsBasicBlock.addInstruction(sw);
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
        }
    }


    public void genMipsFromBr(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        if (((Br)irInstruction).isDirectBranch()){
            //br label
            MipsJ j = new MipsJ(new Label(((Br) irInstruction).getTrueBlock().getValueName()));
            mipsBasicBlock.addInstruction(j);
        } else {
            //br i1 $1, label1, label2
            Label label1 = new Label(((Br) irInstruction).getTrueBlock().getValueName());
            Label label2 = new Label(((Br) irInstruction).getFalseBlock().getValueName());

            /*Handle i1*/
            MipsAddi addi = new MipsAddi(new Register("$0"),new Register("$t0"),new Imm(1));
            mipsBasicBlock.addInstruction(addi);

            /*Handle condition*/
            curMipsFunction = mipsFunction;
            curMipsBasicBlock = mipsBasicBlock;
            Register condReg = new Register("$t1");
            condReg = handleOperand(irInstruction.getOperand(0),condReg);

            /*Beq to label1*/
            MipsBeq beq = new MipsBeq(new Register("$t0"),condReg,label1);
            mipsBasicBlock.addInstruction(beq);

            /*Jump To label2*/
            MipsJ j = new MipsJ(label2);
            mipsBasicBlock.addInstruction(j);
        }
    }

    public void genMipsFromRet(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        boolean isVoid = ((Ret)irInstruction).isVoid();
        if (isVoid) {
            /*jr $ra*/
            MipsJr jr = new MipsJr(new Register("$ra"));
            mipsBasicBlock.addInstruction(jr);
        } else {
            /*Store Return Value into $v0*/
            Value retValue = irInstruction.getOperand(0);
            if (retValue instanceof ConstInteger || retValue instanceof UndefinedValue) {
                //常量或者未定义变量
                int value = Integer.parseInt(retValue.getValueName());
                Imm imm = new Imm(value);
                MipsAddi addi = new MipsAddi(new Register("$0"),new Register("$v0"),imm);
                mipsBasicBlock.addInstruction(addi);
            } else if (registerManager.hasAllocReg(retValue)) {
                Register srcReg = registerManager.getRegisterOfValue(retValue);
                MipsMove move = new MipsMove(new Register("$v0"),srcReg);
                mipsBasicBlock.addInstruction(move);
            } else {
                String name = retValue.getValueName();
                Imm offset = new Imm(mipsFunction.getNameOffset(name));
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$v0"),offset);
                mipsBasicBlock.addInstruction(lw);
            }

            if (mipsFunction.getName().equals("main")) {
                /*Main Function Return*/
                MipsAddi addi = new MipsAddi(new Register("$0"),new Register("$v0"),new Imm(10));
                MipsSyscall syscall = new MipsSyscall();
                mipsBasicBlock.addInstruction(addi);
                mipsBasicBlock.addInstruction(syscall);
            } else {
                MipsJr jr = new MipsJr(new Register("$ra"));
                mipsBasicBlock.addInstruction(jr);
            }
        }
    }

    public void genMipsFromZextTo(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        /*%5 = zext i1 %2 to i32*/

        Register dstReg = registerManager.getRegisterOfValue(irInstruction);
        if (dstReg == null) {
            dstReg = new Register("$t0");
        }

        /*Load To dstreg*/
        curMipsFunction = mipsFunction;
        curMipsBasicBlock = mipsBasicBlock;
        Value operand = irInstruction.getOperand(0);
        dstReg = handleOperand(operand,dstReg);

        if (registerManager.getRegisterOfValue(irInstruction) == null) {
            /*Store Back to Stack*/
            curStackOffset -= 4;
            MipsSw sw = new MipsSw(new Register("$sp"), dstReg, new Imm(curStackOffset));
            mipsBasicBlock.addInstruction(sw);
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(), curStackOffset);
        }
    }

    public void genMipsFromCall(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        Function function =  (Function) (irInstruction).getOperand(0);  //called function
        if(function.isLibFunction()) {
            genMipsFromLibCall(irInstruction,mipsBasicBlock,mipsFunction);
        } else {
            genMipsFromCustomCall(irInstruction,mipsBasicBlock,mipsFunction);
        }
    }

    public void genMipsFromLibCall(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        Function function =  (Function) (irInstruction).getOperand(0);  //called function
        if(function.getValueName().equals("getint")) {
            MipsAddi addi = new MipsAddi(new Register("$0"),new Register("$v0"),new Imm(5));
            mipsBasicBlock.addInstruction(addi);
            MipsSyscall syscall = new MipsSyscall();
            mipsBasicBlock.addInstruction(syscall);

            if (registerManager.hasAllocReg(irInstruction)) {
                /*分配了*/
                Register dstReg = registerManager.getRegisterOfValue(irInstruction);
                MipsMove move = new MipsMove(dstReg,new Register("$v0"));
                mipsBasicBlock.addInstruction(move);
            } else {
                /*没有分配，存入栈中*/
                curStackOffset -= 4;
                MipsSw sw = new MipsSw(new Register("$sp"),new Register("$v0"),new Imm(curStackOffset));
                mipsBasicBlock.addInstruction(sw);
                mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
            }
        } else if (function.getValueName().equals("putch")) {
            MipsAddi addi = new MipsAddi(new Register("$0"),new Register("$v0"),new Imm(11));

            mipsBasicBlock.addInstruction(addi);
            addi = new MipsAddi(new Register("$0"),new Register("$a0"),
                    new Imm(((ConstInteger)irInstruction.getOperand(1)).getValue()));

            mipsBasicBlock.addInstruction(addi);
            MipsSyscall syscall = new MipsSyscall();
            mipsBasicBlock.addInstruction(syscall);
        } else if (function.getValueName().equals("putint")) {
            MipsAddi addi = new MipsAddi(new Register("$0"),new Register("$v0"),new Imm(1));

            mipsBasicBlock.addInstruction(addi);
            if (irInstruction.getOperand(1) instanceof ConstInteger
                    || irInstruction.getOperand(1) instanceof UndefinedValue) {
                int value = Integer.parseInt(irInstruction.getOperand(1).getValueName());
                Imm imm = new Imm(value);
                addi = new MipsAddi(new Register("$0"),new Register("$a0"),imm);

                mipsBasicBlock.addInstruction(addi);
            }  else if (registerManager.hasAllocReg(irInstruction.getOperand(1))) {
                Register srcReg = registerManager.getRegisterOfValue(irInstruction.getOperand(1));
                MipsMove move = new MipsMove(new Register("$a0"),srcReg);
                mipsBasicBlock.addInstruction(move);
            } else {
                /*putint(i32 %4)*/
                String name = irInstruction.getOperand(1).getValueName();
                Imm offset = new Imm(mipsFunction.getNameOffset(name));
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$a0"),offset);
                mipsBasicBlock.addInstruction(lw);
            }
            MipsSyscall syscall = new MipsSyscall();
            mipsBasicBlock.addInstruction(syscall);
        } else if (function.getValueName().equals("putstr")) {
            /*putstr(i8* @str_0)*/
            Value globalString = ((User)(irInstruction.getOperand(1))).getOperand(0);
            MipsLa la = new MipsLa(new Register("$a0"),globalString.getValueName().substring(1));
            mipsBasicBlock.addInstruction(la);

            MipsAddi addi = new MipsAddi(new Register("$0"),new Register("$v0"),new Imm(4));
            mipsBasicBlock.addInstruction(addi);

            MipsSyscall syscall = new MipsSyscall();
            mipsBasicBlock.addInstruction(syscall);
        }
    }

    public void genMipsFromCustomCall(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        Function calledFunction =  (Function) (irInstruction).getOperand(0);  //called function
        ArrayList<Argument> argc = calledFunction.getArguments();                   //function arguments

        int allocRegNumber = 0;
        ArrayList<Register> allocRegs;
        allocRegs = registerManager.getAllocRegs();

        // push allocated registers into stack
        for (Register reg : allocRegs) {
            allocRegNumber += 1;
            MipsSw sw = new MipsSw(new Register("$sp"),reg,new Imm(curStackOffset - 4* allocRegNumber));
            mipsBasicBlock.addInstruction(sw);
        }

        // push $sp into stack
        //MipsSw sw = new MipsSw(new Register("$sp"),new Register("$sp"),new Imm(curStackOffset - 4* allocRegNumber-4));
        //mipsBasicBlock.addInstruction(sw);

        //push $ra into stack
        MipsSw sw = new MipsSw(new Register("$sp"),new Register("$ra"),new Imm(curStackOffset - 4* allocRegNumber - 8));
        mipsBasicBlock.addInstruction(sw);

        int argNumber = argc.size();
        int allocNumber = Math.min(argNumber,3);
        //加载到 $a1 - $a3
        for (int i = 1; i <= allocNumber; i ++) {
            Value arg = irInstruction.getOperand(i);

            if (arg instanceof ConstInteger || arg instanceof UndefinedValue) {
                //常量或者未定义变量
                int value = Integer.parseInt(arg.getValueName());
                Imm imm = new Imm(value);
                MipsAddi addi = new MipsAddi(new Register("$0"),new Register("$a" + i),imm);
                mipsBasicBlock.addInstruction(addi);
            } else if (registerManager.hasAllocReg(arg)) {
                // 这里需要特判一个情况，比如实际上分配了$a2 寄存器，但是在之前的时候已经将$a2进行修改了，因此会导致结果出错
                Register reg = registerManager.getRegisterOfValue(arg);
                if (reg.getRegName().charAt(1) == 'a') {
                    int regNo = Integer.parseInt(String.valueOf(reg.getRegName().charAt(2)));
                    if (regNo < i) {    //索引比i小，说明一定值已经修改了，需要从栈中读取
                        Imm offset = new Imm(curStackOffset - 4 * (allocRegs.indexOf(new Register("$a"+regNo)) + 1));
                        MipsLw lw = new MipsLw(new Register("$sp"),new Register("$a"+i),offset);
                        mipsBasicBlock.addInstruction(lw);
                        continue;
                    }
                }
                MipsMove move = new MipsMove(new Register("$a"+i),reg);
                mipsBasicBlock.addInstruction(move);
            } else {
                String name = arg.getValueName();
                Imm offset = new Imm(mipsFunction.getNameOffset(name));
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$a" + i),offset);
                mipsBasicBlock.addInstruction(lw);
            }
        }

        for (int  i = 4; i <= argNumber; i++) {
            Register dstReg = new Register("$t0"); //值暂时加载到$t0中
            Value arg = irInstruction.getOperand(i);
            if (arg instanceof ConstInteger || arg instanceof UndefinedValue) {
                //常量或者未定义变量
                int value = Integer.parseInt(arg.getValueName());
                Imm imm = new Imm(value);
                MipsAddi addi = new MipsAddi(new Register("$0"),dstReg,imm);
                mipsBasicBlock.addInstruction(addi);
            } else if (registerManager.hasAllocReg(arg)) {
                Register fromReg = registerManager.getRegisterOfValue(arg);
                if (fromReg.getRegName().charAt(1) == 'a') {    //一定已经改动过了
                    int regNo = Integer.parseInt(String.valueOf(fromReg.getRegName().charAt(2)));
                    Imm offset = new Imm(curStackOffset - 4 * (allocRegs.indexOf(new Register("$a"+regNo)) + 1));
                    MipsLw lw = new MipsLw(new Register("$sp"),dstReg,offset);
                    mipsBasicBlock.addInstruction(lw);
                } else {
                    dstReg = fromReg;
                }
            }else {
                String name = arg.getValueName();
                Imm offset = new Imm(mipsFunction.getNameOffset(name));
                MipsLw lw = new MipsLw(new Register("$sp"), dstReg, offset);
                mipsBasicBlock.addInstruction(lw);
            }
            // 存入栈中
            sw = new MipsSw(new Register("$sp"),dstReg,new Imm(curStackOffset - 8 - 4* allocRegNumber - 4*(i-3)));
            mipsBasicBlock.addInstruction(sw);
        }

        //move $sp
        MipsAddi addi = new MipsAddi(new Register("$sp"),new Register("$sp"),new Imm(curStackOffset - 4* allocRegNumber- 8));
        mipsBasicBlock.addInstruction(addi);

        //call function
        MIPSJal jal = new MIPSJal(new Label(calledFunction.getValueName()));
        mipsBasicBlock.addInstruction(jal);

        //restore $sp
        addi = new MipsAddi(new Register("$sp"),new Register("$sp"),new Imm(-(curStackOffset - 4* allocRegNumber- 8)));
        mipsBasicBlock.addInstruction(addi);

        //restore
        allocRegNumber = 0;
        for (Register reg : allocRegs) {
            allocRegNumber += 1;
            MipsLw lw = new MipsLw(new Register("$sp"),reg,new Imm(curStackOffset - 4* allocRegNumber));
            mipsBasicBlock.addInstruction(lw);
        }

        // MipsLw lw = new MipsLw(new Register("$sp"),new Register("$sp"),new Imm(curStackOffset - 4* allocRegNumber - 4));
        // mipsBasicBlock.addInstruction(lw);

        MipsLw lw = new MipsLw(new Register("$sp"),new Register("$ra"),new Imm(curStackOffset - 4* allocRegNumber - 8));
        mipsBasicBlock.addInstruction(lw);

        //return value
        if (calledFunction.getValueType().toString().equals("i32")) {
            /*has return value*/
            if (registerManager.hasAllocReg(irInstruction)) {
                Register dstReg = registerManager.getRegisterOfValue(irInstruction);
                MipsMove move = new MipsMove(dstReg,new Register("$v0"));
                mipsBasicBlock.addInstruction(move);
            } else {
                curStackOffset -= 4;
                sw = new MipsSw(new Register("$sp"),new Register("$v0"),new Imm(curStackOffset));
                mipsBasicBlock.addInstruction(sw);
                mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
            }
        }
    }

    private void genMipsFromMove(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        // 形式类似 move %1 , i32 %2
        Value dstOperand = irInstruction.getOperand(0);
        Value srcOperand = irInstruction.getOperand(1);

        Register dstReg = registerManager.getRegisterOfValue(dstOperand);
        if (dstReg == null) {
            dstReg = new Register("$t0");
        }

        /*先处理rightOperand*/
        if (srcOperand instanceof ConstInteger || srcOperand instanceof UndefinedValue) {
            /*li $t0,1*/
            int value = Integer.parseInt(srcOperand.getValueName());
            Imm imm = new Imm(value);
            MipsAddi addi = new MipsAddi(new Register("$0"),dstReg,imm);
            mipsBasicBlock.addInstruction(addi);
        } else if (registerManager.hasAllocReg(srcOperand)) {
            MipsMove move = new MipsMove(dstReg,registerManager.getRegisterOfValue(srcOperand));
            mipsBasicBlock.addInstruction(move);
        } else {
            /*lw $t1, offset($sp)*/
            String name = srcOperand.getValueName();
            if (mipsFunction.getMipsSymbolTable().containSymbol(name)) {
                /*已经出现了*/
                Imm offset = new Imm(mipsFunction.getNameOffset(name));
                MipsLw lw2 = new MipsLw( new Register("$sp"),dstReg,offset);
                mipsBasicBlock.addInstruction(lw2);
            } else {
                curStackOffset -=4;
                int offset = curStackOffset;    //相当于提前注册地址
                MipsLw lw = new MipsLw(new Register("$sp"),dstReg,new Imm(offset));
                mipsBasicBlock.addInstruction(lw);
                mipsFunction.insertToSymbolTable(name,curStackOffset);
            }
        }

        /*leftOperand 可能已经被声明过了，也可能没有*/
        if (registerManager.getRegisterOfValue(dstOperand) == null) {
            String dstName = dstOperand.getValueName();
            if (mipsFunction.getMipsSymbolTable().containSymbol(dstName)) {
                // 已经声明过了，直接存进去对应的内存空间
                Imm offset = new Imm(mipsFunction.getNameOffset(dstName));
                MipsSw sw = new MipsSw(new Register("$sp"),new Register("$t0"),new Imm(mipsFunction.getNameOffset(dstName)));
                mipsBasicBlock.addInstruction(sw);
            } else {
                // 还没有声明过
                /*Push Into Stack*/
                curStackOffset -=4;
                int offset = curStackOffset;
                MipsSw sw = new MipsSw(new Register("$sp"),new Register("$t0"),new Imm(offset));
                mipsBasicBlock.addInstruction(sw);
                mipsFunction.insertToSymbolTable(dstOperand.getValueName(),curStackOffset);
            }
        }
    }

}
