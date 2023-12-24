package midend.optimize;

import midend.IRModule;
import midend.values.BasicBlock;
import midend.values.Const.Function;
import midend.values.Instructions.IRInstruction;
import midend.values.Instructions.terminator.Br;

import java.util.ArrayList;
import java.util.Iterator;

public class BranchOptimizer {
    private final IRModule module;

    private boolean changed = false;
    public BranchOptimizer(IRModule module) {
        this.module = module;
    }

    public void run() {
        for (int i = 0; i < module.getFunctions().size(); i++) {
            changed = false;
            Function function = module.getFunctions().get(i);
            mergeSingleBranchInstrBlock(function);
            mergeSingePredecessorBlock(function);
            if (changed) { //until不再变化
                i--;
            }
        }

    }

    private void mergeSingleBranchInstrBlock(Function function) {
        Iterator<BasicBlock> iterator = function.getBasicBlocks().iterator();
        while(iterator.hasNext()) {
            BasicBlock basicBlock1 = iterator.next();
            if (basicBlock1.getInstructions().size() == 1 && basicBlock1.getInstructions().get(0) instanceof Br) {
                // bb1:
                //      br bb2
                BasicBlock basicBlock2 = ((Br) basicBlock1.getInstructions().get(0)).getTrueBlock();
                //把所有跳转到bb1的都修改为跳转到bb2
                for (int i = 0; i < function.getBasicBlocks().size(); i++) {
                    BasicBlock basicBlock3 = function.getBasicBlocks().get(i);
                    if (basicBlock3.equals(basicBlock1) || basicBlock3.equals(basicBlock2)) {
                        continue;
                    }
                    IRInstruction lastInstr = basicBlock3.getLastInstr();
                    if (lastInstr instanceof Br brInstr) {
                        BasicBlock trueBlock = brInstr.getTrueBlock();
                        BasicBlock falseBlock = brInstr.getFalseBlock();
                        if (trueBlock.equals(basicBlock1)) {
                            changed = true;
                            brInstr.setTrueBlock(basicBlock2);
                            // bb1减少一个前驱bb1
                            basicBlock1.removePredecessor(basicBlock3);
                            // bb3 减少后继bb1，增加后继bb2
                            basicBlock3.removeSuccessor(basicBlock1);
                            basicBlock3.addSuccessor(basicBlock2);
                            //bb2增加一个前驱bb3
                            basicBlock2.addPredecessor(basicBlock3);
                        } else if (falseBlock != null && falseBlock.equals(basicBlock1)) {
                            changed = true;
                            brInstr.setFalseBlock(basicBlock2);
                            // bb1减少一个前驱bb1
                            basicBlock1.removePredecessor(basicBlock3);
                            // bb3 减少后继bb1，增加后继bb2
                            basicBlock3.removeSuccessor(basicBlock1);
                            basicBlock3.addSuccessor(basicBlock2);
                            //bb2增加一个前驱bb3
                            basicBlock2.addPredecessor(basicBlock3);
                        }
                    }
                }
            }
        }
    }

    private void mergeSingePredecessorBlock(Function function) {
        Iterator<BasicBlock> iterator = function.getBasicBlocks().iterator();
        while(iterator.hasNext()) {
            BasicBlock basicBlock1 = iterator.next();
            IRInstruction lastInstr = basicBlock1.getLastInstr();
            if (lastInstr instanceof Br brInstr && brInstr.getFalseBlock() ==null) {    //无条件跳转
                BasicBlock basicBlock2 = brInstr.getTrueBlock();
                if (basicBlock2.getPredecessors().size() == 1) {    //只有一个前驱，可以合并
                    changed = true;
                    basicBlock1.getInstructions().remove(lastInstr);    //删除br跳转
                    ArrayList<IRInstruction> basicBlock2Instructions = basicBlock2.getInstructions(); //合并两个block
                    basicBlock1.getInstructions().addAll(basicBlock2Instructions);

                    //维护前驱和后继
                    basicBlock1.removeSuccessor(basicBlock2);
                    basicBlock2.removePredecessor(basicBlock1);
                }
            }
        }
    }
}
