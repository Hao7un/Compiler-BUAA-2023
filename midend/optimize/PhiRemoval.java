package midend.optimize;

import backend.Operands.Register;
import midend.IRBuilder;
import midend.IRModule;
import midend.types.IntegerType;
import midend.values.BasicBlock;
import midend.values.Const.Function;
import midend.values.Instructions.IRInstruction;
import midend.values.Instructions.others.Move;
import midend.values.Instructions.others.PC;
import midend.values.Instructions.others.Phi;
import midend.values.Instructions.terminator.Br;
import midend.values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class PhiRemoval {
    private final IRModule module;

    public PhiRemoval(IRModule module) {
        this.module = module;
    }

    public void run(){
        for (Function function : module.getFunctions()) {
            criticalEdgeSplitting(function);
            replaceParallelCopies(function);
        }
    }

    private void criticalEdgeSplitting(Function function) {
        ArrayList<BasicBlock> basicBlocks = new ArrayList<>(function.getBasicBlocks());
        for (int i =0; i < basicBlocks.size();i++) {   // basic block of CFG
            BasicBlock basicBlock = basicBlocks.get(i);
            ArrayList<Phi> phis = new ArrayList<>();
            for (IRInstruction instr : basicBlock.getInstructions()) {
                if (instr instanceof Phi phi) {
                    phis.add(phi);
                }
            }
            if (phis.isEmpty()) {
                /*没有phi指令，不需要考虑*/
                continue;
            }
            //当前basic block的所有前驱
            ArrayList<BasicBlock> predecessors = new ArrayList<>(basicBlock.getPredecessors());
            ArrayList<PC> pcs = new ArrayList<>();
            for (int j =0; j < predecessors.size(); j++) { // for every B_i
                BasicBlock predecessor = predecessors.get(j);
                if (predecessor.getSuccessors().size() == 1) {      //append PC_i at the end of B_i
                    ArrayList<IRInstruction> instrs = predecessor.getInstructions();
                    PC pc = IRBuilder.buildPC(predecessor);
                    pcs.add(pc);
                    instrs.add(instrs.size() - 1,pc);
                } else {
                    int number = IRBuilder.buildBasicBlockName(function);
                    BasicBlock splitBlock = new BasicBlock(function.getValueName()+"_"+"BasicBlock_"+number,function);

                    // from predecessor -> basicBlock
                    // to   predecessor -> splitBlock -> basicBlock
                    function.getBasicBlocks().add(function.getBasicBlocks().indexOf(basicBlock),splitBlock);    // add split block to funtion

                    /*insert PC_i in B_i'*/
                    PC pc = IRBuilder.buildPC(splitBlock);
                    pcs.add(pc);
                    splitBlock.addInstruction(pc);

                    /*replace edge*/
                    assert predecessor.getLastInstr() instanceof Br;
                    Br originalBranch = (Br) predecessor.getLastInstr();
                    //这里一定有多个后继，其实也就只有br [cond]的情况
                    BasicBlock trueBlock = originalBranch.getTrueBlock();
                    BasicBlock falseBlock = originalBranch.getFalseBlock();

                    if (basicBlock.equals(trueBlock)) {
                        originalBranch.setTrueBlock(splitBlock);
                        IRBuilder.buildBr(splitBlock,basicBlock);
                    } else if (basicBlock.equals(falseBlock)) {
                        originalBranch.setFalseBlock(splitBlock);
                        IRBuilder.buildBr(splitBlock,basicBlock);
                    }

                    /*维护 predecessor splitBlock basicBlock的数据结构*/
                    predecessor.removeSuccessor(basicBlock);
                    predecessor.addSuccessor(splitBlock);

                    basicBlock.removePredecessor(predecessor);
                    basicBlock.addPredecessor(splitBlock);

                    splitBlock.addPredecessor(predecessor);
                    splitBlock.addSuccessor(basicBlock);
                }
            }

            Iterator<IRInstruction> iterator = basicBlock.getInstructions().iterator();
            while(iterator.hasNext()) {
                IRInstruction instr = iterator.next();
                if (instr instanceof Phi) {
                    ArrayList<Value> values = instr.getOperands();  // a_i
                    for (int k = 0; k < values.size(); k++) {
                        pcs.get(k).addDst(instr);
                        pcs.get(k).addSrc(values.get(k));
                    }
                    iterator.remove();
                }
            }
        }
    }

    private void replaceParallelCopies(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            ArrayList<IRInstruction> instrs = basicBlock.getInstructions();
            //PC一定在instrs的倒数第二句之中
            if(instrs.size() < 2 || ! (instrs.get(instrs.size()-2) instanceof PC)) {
                //不符合条件
                continue;
            }
            PC pc = (PC) instrs.get(instrs.size() - 2);
            instrs.remove(instrs.size() - 2);
            ArrayList<Move> moveList;
            moveList = transformParallelCopy(pc,basicBlock,function);
            /*Add Move List to Basic Block*/
            for (Move move : moveList) {
                instrs.add(instrs.size()-1,move);
            }
        }
    }

    private ArrayList<Move> transformParallelCopy(PC pc, BasicBlock basicBlock,Function function) {
        ArrayList<Move> moveList = new ArrayList<>();
        ArrayList<Value> dsts = pc.getDsts();   // PC中左边的值
        ArrayList<Value> srcs = pc.getSrcs();   // PC中右边的值


        boolean found;
        do {
            found = false;
            for (int i = 0; i < srcs.size(); i++) {
                Value src = srcs.get(i);    // a
                Value dst = dsts.get(i);    // b
                if (!src.equals(dst) && !srcs.contains(dst)) {
                    Move move = IRBuilder.buildMove(src, dst, basicBlock);
                    moveList.add(move);

                    srcs.remove(i);
                    dsts.remove(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                // 存在循环赋值，需要拆
                for (int i = 0; i < srcs.size(); i++) {
                    Value src = srcs.get(i);
                    Value dst = dsts.get(i);

                    if (dsts.contains(src) && !src.equals(dst)) {
                        Value tempValue = new Value(src.getValueName() +"_temp",new IntegerType(32)); // Create a fresh variable
                        Move move = IRBuilder.buildMove(src, tempValue, basicBlock);
                        moveList.add(move);

                        srcs.set(i, tempValue);
                        found = true;
                        break;
                    }
                }
            }
        } while (found);

        // 处理寄存器冲突
        ArrayList<Move> newMoves = handleRegConflict(moveList,basicBlock,function);
        for (Move move:  newMoves) {
            moveList.add(0,move);
        }
        return moveList;
    }

    private ArrayList<Move> handleRegConflict(ArrayList<Move> moveList,BasicBlock basicBlock,Function function) {
        HashMap<Value,Register> value2Reg = function.getValue2Reg();

        boolean found;
        ArrayList<Move> newMoves = new ArrayList<>();
        do {
            found = false;
            for (int i = moveList.size() - 1; i >= 0; i--) {
                Value src = moveList.get(i).getSrc();
                Register srcReg = value2Reg.get(src);
                if (srcReg == null) {
                    continue;
                }
                for (int j = 0; j < i ; j++) {
                    Value dst = moveList.get(j).getDst();
                    Register dstReg = value2Reg.get(dst);
                    if (dstReg == null) {
                        continue;
                    }
                    if (srcReg.equals(dstReg)) {    //出现了寄存器分配的问题
                        found = true;
                        Value tempValue = new Value(src.getValueName()+"_temp",new IntegerType(32)); // Create a fresh variable
                        for (Move move : moveList) {
                            if (move.getSrc().equals(src)) {
                                move.setSrc(tempValue);
                            }
                        }
                        Move move = IRBuilder.buildMove(src, tempValue, basicBlock);
                        newMoves.add(0,move);
                    }
                }
            }
        }while (found);

        return newMoves;
    }
}
