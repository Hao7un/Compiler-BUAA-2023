package backend;

import IR.IRModule;
import IR.types.PointerType;
import IR.types.ValueType;
import IR.values.Argument;
import IR.values.BasicBlock;
import IR.values.Const.ConstInteger;
import IR.values.Const.Function;
import IR.values.Const.GlobalVar;
import IR.values.Instructions.IRInstruction;
import IR.values.Instructions.binary.*;
import IR.values.Instructions.mem.*;
import IR.values.Instructions.terminator.*;
import IR.values.Instructions.others.*;
import IR.values.Value;
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
    private int curStackOffset = 0;        // Tracing current offset to $sp
    public MipsGenerator() {

    }

    public void genModule() {
        for (GlobalVar globalVar : irModule.getGlobalVars()) {
            genGlobalVar(globalVar,mipsModule);
        }
        for (Function function : irModule.getFunctions()) {
            genFunction(function,mipsModule);
        }
    }

    public void genGlobalVar(GlobalVar globalVar,MipsModule mipsModule) {
        mipsModule.addGlobalVariable(new MipsGlobalVariable(globalVar));
    }

    public void genFunction(Function function,MipsModule mipsModule) {
        this.curStackOffset = 0;
        MipsFunction mipsFunction = new MipsFunction(function.getValueName(),function);
        /*save stack for param*/
        genFuncParam(mipsFunction,function);
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            genBasicBlock(basicBlock,mipsFunction);
        }
        mipsModule.addFunction(mipsFunction);
    }

    public void genFuncParam(MipsFunction mipsFunction,Function function){
        ArrayList<Argument> parameters = function.getArguments();
        for (Argument parameter : parameters) {
            curStackOffset-=4;

            mipsFunction.insertToSymbolTable(parameter.getValueName(),curStackOffset);
        }
    }

    public void genBasicBlock(BasicBlock basicBlock,MipsFunction mipsFunction) {
        MipsBasicBlock mipsBasicBlock = new MipsBasicBlock(basicBlock.getValueName().toString());
        for (IRInstruction irInstruction : basicBlock.getInstructions()) {
            MipsComment comment = new MipsComment(irInstruction.toString());
            mipsBasicBlock.addInstruction(comment);
            if (irInstruction instanceof Add) {
                genMipsFromArithmetic(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Sub) {
                genMipsFromArithmetic(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Mul) {
                genMipsFromArithmetic(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Sdiv) {
                genMipsFromArithmetic(irInstruction,mipsBasicBlock,mipsFunction);
            } else if (irInstruction instanceof Srem) {
                genMipsFromArithmetic(irInstruction,mipsBasicBlock,mipsFunction);
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
            }
        }
        mipsFunction.addBasicBlock(mipsBasicBlock);
    }

    /*Instruction Builder*/

    public void genMipsFromGetElementPtr(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        ValueType pointedType = ((PointerType) (irInstruction).getValueType()).getPointedType();
        if (irInstruction.getOperands().size() == 2) {
            //  %_4 = getelementptr [2 x i32], [2 x i32]* %_3, i32 1
            int offset;
            Value base = irInstruction.getOperand(0);
            String baseName = base.getValueName();
            int baseOffsetFromSp = mipsFunction.getNameOffset(baseName);

            /* load Base To $t0*/
            if (base instanceof GlobalVar) {
                MipsLa la = new MipsLa(new Register("$t0"),baseName.substring(1));
                mipsBasicBlock.addInstruction(la);
            } else {
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t0"),new Imm(baseOffsetFromSp));
                mipsBasicBlock.addInstruction(lw);
            }

            /*Load getElementPtr offset*/
            Value operand = irInstruction.getOperand(1);
            if (operand instanceof ConstInteger) {
                MipsLi li = new MipsLi(new Register("$t1"),new Imm(((ConstInteger) operand).getValue()));
                mipsBasicBlock.addInstruction(li);
            } else {
                offset = mipsFunction.getNameOffset(operand.getValueName());
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t1"),new Imm(offset));
                mipsBasicBlock.addInstruction(lw);
            }
            int size = ((PointerType) irInstruction.getOperand(0).getValueType()).getPointedType().getSize();

            MipsMul mul = new MipsMul(new Register("$t2"),new Register("$t1"),new Imm(size));
            mipsBasicBlock.addInstruction(mul);

            MipsAdd add = new MipsAdd(new Register("$t3"),new Register("$t0"),new Register("$t2"));
            mipsBasicBlock.addInstruction(add);

            curStackOffset -= 4;
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
            MipsSw sw = new MipsSw(new Register("$sp"),new Register("$t3"),new Imm(curStackOffset));
            mipsBasicBlock.addInstruction(sw);

        } else if (irInstruction.getOperands().size() == 3){
            //  %_5 = getelementptr [2 x i32], [2 x i32]* %_4, i32 0, i32 1
            int offset;
            Value base = irInstruction.getOperand(0);
            String baseName = base.getValueName();

            /* load Base To $t0*/
            if (base instanceof GlobalVar) {
                MipsLa la = new MipsLa(new Register("$t0"),baseName.substring(1));
                mipsBasicBlock.addInstruction(la);
            } else {
                int baseOffsetFromSp = mipsFunction.getNameOffset(baseName);
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t0"),new Imm(baseOffsetFromSp));
                mipsBasicBlock.addInstruction(lw);
            }


            /*Load getElementPtr offset-----1*/
            Value operand = irInstruction.getOperand(1);
            if (operand instanceof ConstInteger) {
                MipsLi li = new MipsLi(new Register("$t1"),new Imm(((ConstInteger) operand).getValue()));
                mipsBasicBlock.addInstruction(li);
            } else {
                offset = mipsFunction.getNameOffset(operand.getValueName());
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t1"),new Imm(offset));
                mipsBasicBlock.addInstruction(lw);
            }

            int size = ((PointerType) irInstruction.getOperand(0).getValueType()).getPointedType().getSize();

            MipsMul mul = new MipsMul(new Register("$t2"),new Register("$t1"),new Imm(size));
            mipsBasicBlock.addInstruction(mul);

            MipsAdd add = new MipsAdd(new Register("$t3"),new Register("$t0"),new Register("$t2"));
            mipsBasicBlock.addInstruction(add);

            /*Load getElementPtr offset-----2*/
            operand = irInstruction.getOperand(2);
            if (operand instanceof ConstInteger) {
                MipsLi li = new MipsLi(new Register("$t4"),new Imm(((ConstInteger) operand).getValue()));
                mipsBasicBlock.addInstruction(li);
            } else {
                offset = mipsFunction.getNameOffset(operand.getValueName());
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t4"),new Imm(offset));
                mipsBasicBlock.addInstruction(lw);
            }

            size = ((PointerType)irInstruction.getValueType()).getPointedType().getSize();
            mul = new MipsMul(new Register("$t5"),new Register("$t4"),new Imm(size));
            mipsBasicBlock.addInstruction(mul);

            add = new MipsAdd(new Register("$t6"),new Register("$t3"),new Register("$t5"));
            mipsBasicBlock.addInstruction(add);

            curStackOffset -= 4;
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
            MipsSw sw = new MipsSw(new Register("$sp"),new Register("$t6"),new Imm(curStackOffset));
            mipsBasicBlock.addInstruction(sw);

        }
    }

    public void genMipsFromAlloca(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        ValueType valueType = ((PointerType)irInstruction.getValueType()).getPointedType();
        curStackOffset -= valueType.getSize();

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

    public void genMipsFromStore(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        Value source = irInstruction.getOperand(0);
        Value dest = irInstruction.getOperand(1);

        /*Getting the Value to Store*/
        if (source instanceof ConstInteger) {
            int value = ((ConstInteger) source).getValue();
            MipsLi li = new MipsLi(new Register("$t0"), new Imm(value));
            mipsBasicBlock.addInstruction(li);
        } else {
            String valueName = source.getValueName();
            Integer valueOffset = mipsFunction.getNameOffset(valueName);
            if (valueOffset != null) {
                MipsLw lw = new MipsLw(new Register("$sp"), new Register("$t0"), new Imm(valueOffset));
                mipsBasicBlock.addInstruction(lw);
            } else {
                /*From Function Parameter*/
                // "replenish an alloca instruction"
                curStackOffset -= 4;
                mipsFunction.insertToSymbolTable(valueName,curStackOffset);
            }
        }

        /*Getting the destination to store*/
        if (dest instanceof GlobalVar) {
            MipsLa la = new MipsLa(new Register("$t1"),dest.getValueName().substring(1));
            mipsBasicBlock.addInstruction(la);
        } else {
            int offset = mipsFunction.getNameOffset(dest.getValueName());
            MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t1"),new Imm(offset));
            mipsBasicBlock.addInstruction(lw);
        }
        String destName = dest.getValueName();
        MipsSw sw = new MipsSw(new Register("$t1"), new Register("$t0"), new Imm(0));
        mipsBasicBlock.addInstruction(sw);
    }


    public void genMipsFromLoad(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        Value srcOperand = irInstruction.getOperand(0);
        String srcName = srcOperand.getValueName();
        if (srcOperand instanceof GlobalVar) {
            //MipsLa
            MipsLa la = new MipsLa(new Register("$t0"),srcName.substring(1));
            mipsBasicBlock.addInstruction(la);
        } else {
            int srcOffset = mipsFunction.getNameOffset(srcName);
            MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t0"),new Imm(srcOffset));
            mipsBasicBlock.addInstruction(lw);
        }

        MipsLw lw = new MipsLw(new Register("$t0"),new Register("$t0"),new Imm(0));
        mipsBasicBlock.addInstruction(lw);

        curStackOffset -=4;
        mipsFunction.insertToSymbolTable(irInstruction.getValueName(), curStackOffset);
        MipsSw sw = new MipsSw(new Register("$sp"),new Register("$t0"),new Imm(curStackOffset));
        mipsBasicBlock.addInstruction(sw);

    }


    private void genMipsFromArithmetic(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        /*Add $rd, $rs, $rt*/
        Value leftOperand = irInstruction.getOperand(0);
        Value rightOperand = irInstruction.getOperand(1);

        /*Handle Left Operand*/
        if (leftOperand instanceof ConstInteger) {
            /*li $t0,1*/
            int value = ((ConstInteger) leftOperand).getValue();
            Imm imm = new Imm(value);
            Register reg = new Register("$t0");
            MipsLi li1 = new MipsLi(reg,imm);
            mipsBasicBlock.addInstruction(li1);
        } else {
            /*lw $t0, offset($sp)*/
            String name = leftOperand.getValueName();
            Imm offset = new Imm(mipsFunction.getNameOffset(name));
            Register rs = new Register("$sp");
            Register rt = new Register("$t0");
            MipsLw lw1 = new MipsLw(rs,rt,offset);
            mipsBasicBlock.addInstruction(lw1);
        }

        /*Handle Right Operand*/
        if (rightOperand instanceof ConstInteger) {
            /*li $t0,1*/
            int value = ((ConstInteger) rightOperand).getValue();
            Imm imm = new Imm(value);
            Register reg = new Register("$t1");
            MipsLi li2 = new MipsLi(reg,imm);
            mipsBasicBlock.addInstruction(li2);
        } else {
            /*lw $t1, offset($sp)*/
            String name = rightOperand.getValueName();
            Imm offset = new Imm(mipsFunction.getNameOffset(name));
            Register rs = new Register("$sp");
            Register rt = new Register("$t1");
            MipsLw lw2 = new MipsLw(rs,rt,offset);
            mipsBasicBlock.addInstruction(lw2);
        }
        /*Generate Instr*/
        if (irInstruction instanceof Add) {
            MipsAdd add = new MipsAdd(new Register("$t2"),new Register("$t0"),new Register("$t1"));
            mipsBasicBlock.addInstruction(add);
        } else if(irInstruction instanceof Sub) {
            MipsSub sub = new MipsSub(new Register("$t2"),new Register("$t0"),new Register("$t1"));
            mipsBasicBlock.addInstruction(sub);
        } else if (irInstruction instanceof Mul) {
            MipsMul mul = new MipsMul(new Register("$t2"),new Register("$t0"),new Register("$t1"));
            mipsBasicBlock.addInstruction(mul);
        } else if (irInstruction instanceof Sdiv) {
            MipsDiv div = new MipsDiv(new Register("$t0"),new Register("$t1"));
            MipsMflo mflo = new MipsMflo(new Register("$t2"));
            mipsBasicBlock.addInstruction(div);
            mipsBasicBlock.addInstruction(mflo);
        } else if (irInstruction instanceof Srem) {
            MipsDiv div = new MipsDiv(new Register("$t0"),new Register("$t1"));
            MipsMfhi mfhi = new MipsMfhi(new Register("$t2"));
            mipsBasicBlock.addInstruction(div);
            mipsBasicBlock.addInstruction(mfhi);
        }

        /*Push Into Stack*/
        curStackOffset -=4;
        int offset = curStackOffset;
        MipsSw sw = new MipsSw(new Register("$sp"),new Register("$t2"),new Imm(offset));
        mipsBasicBlock.addInstruction(sw);
        mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
    }

    public void genMipsFromIcmp(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        // %3 = Icmp eq, %1, %2

        Value leftOperand = irInstruction.getOperand(0);
        Value rightOperand = irInstruction.getOperand(1);

        /*Handle Left Operand*/
        if (leftOperand instanceof ConstInteger) {
            /*li $t0,1*/
            int value = ((ConstInteger) leftOperand).getValue();
            Imm imm = new Imm(value);
            Register reg = new Register("$t0");
            MipsLi li1 = new MipsLi(reg,imm);
            mipsBasicBlock.addInstruction(li1);
        } else {
            /*lw $t0, offset($sp)*/
            String name = leftOperand.getValueName();
            Imm offset = new Imm(mipsFunction.getNameOffset(name));
            Register rs = new Register("$sp");
            Register rt = new Register("$t0");
            MipsLw lw1 = new MipsLw(rs,rt,offset);
            mipsBasicBlock.addInstruction(lw1);
        }

        /*Handle Right Operand*/
        if (rightOperand instanceof ConstInteger) {
            /*li $t0,1*/
            int value = ((ConstInteger) rightOperand).getValue();
            Imm imm = new Imm(value);
            Register reg = new Register("$t1");
            MipsLi li2 = new MipsLi(reg,imm);
            mipsBasicBlock.addInstruction(li2);
        } else {
            /*lw $t1, offset($sp)*/
            String name = rightOperand.getValueName();
            Imm offset = new Imm(mipsFunction.getNameOffset(name));
            Register rs = new Register("$sp");
            Register rt = new Register("$t1");
            MipsLw lw2 = new MipsLw(rs,rt,offset);
            mipsBasicBlock.addInstruction(lw2);
        }

        String icmpOp = ((Icmp)irInstruction).getIcmpOp();
        switch (icmpOp) {
            case "eq" -> {
                MipsSeq seq = new MipsSeq(new Register("$t2"), new Register("$t0"), new Register("$t1"));
                mipsBasicBlock.addInstruction(seq);
            }
            case "ne" -> {
                MipsSne sne = new MipsSne(new Register("$t2"), new Register("$t0"), new Register("$t1"));
                mipsBasicBlock.addInstruction(sne);
            }
            case "sge" -> {
                MipsSge sge = new MipsSge(new Register("$t2"), new Register("$t0"), new Register("$t1"));
                mipsBasicBlock.addInstruction(sge);
            }
            case "sgt" -> {
                MipsSgt sgt = new MipsSgt(new Register("$t2"), new Register("$t0"), new Register("$t1"));
                mipsBasicBlock.addInstruction(sgt);
            }
            case "sle" -> {
                MipsSle sle = new MipsSle(new Register("$t2"), new Register("$t0"), new Register("$t1"));
                mipsBasicBlock.addInstruction(sle);
            }
            case "slt" -> {
                MipsSlt slt = new MipsSlt(new Register("$t2"), new Register("$t0"), new Register("$t1"));
                mipsBasicBlock.addInstruction(slt);
            }
        }

        /*Push Into Stack*/
        curStackOffset -= 4;
        int offset = curStackOffset;
        MipsSw sw = new MipsSw(new Register("$sp"),new Register("$t2"),new Imm(offset));
        mipsBasicBlock.addInstruction(sw);
        mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
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
            MipsLi li = new MipsLi(new Register("$t0"),new Imm(1));
            mipsBasicBlock.addInstruction(li);

            /*Handle Right Operand*/
            Value value = irInstruction.getOperand(0);
            if (value instanceof ConstInteger) {
                MipsLi li1 = new MipsLi(new Register("$t1"), new Imm(((ConstInteger) value).getValue()));
                mipsBasicBlock.addInstruction(li1);
            } else {
                String name = value.getValueName();
                Imm offset = new Imm(mipsFunction.getNameOffset(name));
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t1"),offset);
                mipsBasicBlock.addInstruction(lw);
            }

            /*Beq to label1*/
            MipsBeq beq = new MipsBeq(new Register("$t0"),new Register("$t1"),label1);
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
            if (retValue instanceof ConstInteger) {
                int value = ((ConstInteger) retValue).getValue();
                MipsLi li = new MipsLi(new Register("$v0"), new Imm(value));
                mipsBasicBlock.addInstruction(li);
            } else {
                String name = retValue.getValueName();
                Imm offset = new Imm(mipsFunction.getNameOffset(name));
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$v0"),offset);
                mipsBasicBlock.addInstruction(lw);
            }

            if (mipsFunction.getName().equals("main")) {
                /*Main Function Return*/
                MipsLi li = new MipsLi(new Register("$v0"),new Imm(10));
                MipsSyscall syscall = new MipsSyscall();
                mipsBasicBlock.addInstruction(li);
                mipsBasicBlock.addInstruction(syscall);
            } else {
                MipsJr jr = new MipsJr(new Register("$ra"));
                mipsBasicBlock.addInstruction(jr);
            }
        }
    }

    public void genMipsFromZextTo(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        /*%5 = zext i1 %2 to i32*/

        /*Load To $t0*/
        Value operand = irInstruction.getOperand(0);
        String name = operand.getValueName();
        Imm offset = new Imm(mipsFunction.getNameOffset(name));
        MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t0"),offset);
        mipsBasicBlock.addInstruction(lw);

        /*Store Back to Stack*/
        curStackOffset -= 4;
        MipsSw sw = new MipsSw(new Register("$sp"),new Register("$t0"),new Imm(curStackOffset));
        mipsBasicBlock.addInstruction(sw);
        mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
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
            MipsLi li = new MipsLi(new Register("$v0"), new Imm(5));
            mipsBasicBlock.addInstruction(li);
            MipsSyscall syscall = new MipsSyscall();
            mipsBasicBlock.addInstruction(syscall);

            /*Push To Stack*/
            curStackOffset -= 4;
            MipsSw sw = new MipsSw(new Register("$sp"),new Register("$v0"),new Imm(curStackOffset));
            mipsBasicBlock.addInstruction(sw);
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);

        } else if (function.getValueName().equals("putch")) {
            MipsLi li = new MipsLi(new Register("$v0"), new Imm(11));
            mipsBasicBlock.addInstruction(li);
            li = new MipsLi(new Register("$a0"), new Imm(((ConstInteger)irInstruction.getOperand(1)).getValue()));
            mipsBasicBlock.addInstruction(li);
            MipsSyscall syscall = new MipsSyscall();
            mipsBasicBlock.addInstruction(syscall);
        } else if (function.getValueName().equals("putint")) {
            MipsLi li = new MipsLi(new Register("$v0"), new Imm(1));
            mipsBasicBlock.addInstruction(li);
            if (irInstruction.getOperand(1) instanceof ConstInteger) {
                /*putint(i32 100)*/
                li = new MipsLi(new Register("$a0"), new Imm(((ConstInteger)irInstruction.getOperand(1)).getValue()));
                mipsBasicBlock.addInstruction(li);
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
            /*TO BE DONE*/

        }
    }

    public void genMipsFromCustomCall(IRInstruction irInstruction, MipsBasicBlock mipsBasicBlock, MipsFunction mipsFunction) {
        Function calledFunction =  (Function) (irInstruction).getOperand(0);  //called function
        ArrayList<Argument> argc = calledFunction.getArguments();                   //function arguments

        // push $sp
        MipsSw sw = new MipsSw(new Register("$sp"),new Register("$sp"),new Imm(curStackOffset - 4));
        mipsBasicBlock.addInstruction(sw);

        //push $ra
        sw = new MipsSw(new Register("$sp"),new Register("$ra"),new Imm(curStackOffset -  8));
        mipsBasicBlock.addInstruction(sw);

        //push args
        int argNumber = argc.size();
        for (int i=1; i <= argNumber; i++) {
            Value arg = irInstruction.getOperand(i);
            if (arg instanceof ConstInteger) {
                MipsLi li = new MipsLi(new Register("$t0"), new Imm(((ConstInteger)arg).getValue()));
                mipsBasicBlock.addInstruction(li);
            } else {
                String name = arg.getValueName();
                Imm offset = new Imm(mipsFunction.getNameOffset(name));
                MipsLw lw = new MipsLw(new Register("$sp"),new Register("$t0"),offset);
                mipsBasicBlock.addInstruction(lw);
            }
            sw = new MipsSw(new Register("$sp"),new Register("$t0"),new Imm(curStackOffset - 8 - 4*i));
            mipsBasicBlock.addInstruction(sw);
        }
        //move $sp
        MipsAddi addi = new MipsAddi(new Register("$sp"),new Register("$sp"),new Imm(curStackOffset - 8));
        mipsBasicBlock.addInstruction(addi);

        //call function
        MIPSJal jal = new MIPSJal(new Label(calledFunction.getValueName()));
        mipsBasicBlock.addInstruction(jal);

        //restore $sp
        addi = new MipsAddi(new Register("$sp"),new Register("$sp"),new Imm(-(curStackOffset - 8)));
        mipsBasicBlock.addInstruction(addi);

        //restore
        MipsLw lw = new MipsLw(new Register("$sp"),new Register("$ra"),new Imm(curStackOffset - 8));
        mipsBasicBlock.addInstruction(lw);
        lw = new MipsLw(new Register("$sp"),new Register("$sp"),new Imm(curStackOffset - 4));
        mipsBasicBlock.addInstruction(lw);

        //return value
        if (calledFunction.getValueType().toString().equals("i32")) {
            /*has return value*/
            /*allocate 4 byte to store return value*/
            curStackOffset -= 4;
            sw = new MipsSw(new Register("$sp"),new Register("$v0"),new Imm(curStackOffset));
            mipsBasicBlock.addInstruction(sw);
            mipsFunction.insertToSymbolTable(irInstruction.getValueName(),curStackOffset);
        }
    }
}
