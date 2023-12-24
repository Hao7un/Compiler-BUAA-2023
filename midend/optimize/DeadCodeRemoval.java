package midend.optimize;

import midend.IRModule;
import midend.values.BasicBlock;
import midend.values.Const.Function;
import midend.values.Instructions.IRInstruction;
import midend.values.Instructions.mem.Store;
import midend.values.Instructions.others.Call;
import midend.values.Instructions.terminator.Br;
import midend.values.Instructions.terminator.Ret;
import midend.values.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class DeadCodeRemoval {
    /*Remove the dead code in LLVM*/
    private final IRModule module;
    public DeadCodeRemoval(IRModule module) {
        this.module = module;
    }

    public void runSimplifyBlock(){
        removeUnreachableCode();
        removeDeadBasicBlocks();
    }

    public void runRemoveDeadCode() {
        removeDeadFunction();
        removeDeadInstr();
    }

    private void removeDeadFunction() {
        // 删除无用函数
        Iterator<Function> iterator = module.getFunctions().iterator();
        while (iterator.hasNext()) {
            Function function = iterator.next();
            if (!function.isLibFunction() && function.getCalledByFunction().isEmpty() && !function.getValueName().equals("main")) {
                iterator.remove();
            }
        }
    }

    private void removeUnreachableCode() {
        for (Function function : module.getFunctions()) {
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                ArrayList<IRInstruction> instructions = basicBlock.getInstructions();
                boolean foundTerminator = false;
                Iterator<IRInstruction> iterator = instructions.iterator();
                while (iterator.hasNext()) {
                    IRInstruction instruction = iterator.next();
                    if (foundTerminator) {
                        iterator.remove();
                        continue;
                    }
                    if (instruction instanceof Br || instruction instanceof Ret) {
                        foundTerminator = true;
                    }
                }
            }
        }
    }

    private void removeDeadBasicBlocks() {
        for (Function function : module.getFunctions()) {
            HashSet<BasicBlock> reachableBlocks = new HashSet<>();
            BasicBlock entry = function.getBasicBlocks().get(0);

            // DFS标记可达块
            dfs(entry, reachableBlocks);

            // 删除所有不可达的基本块
            Iterator<BasicBlock> iterator = function.getBasicBlocks().iterator();
            while(iterator.hasNext()) {
                BasicBlock basicBlock = iterator.next();
                if (!reachableBlocks.contains(basicBlock)) {
                    iterator.remove();
                }
            }
        }
    }

    private void removeDeadInstr() {
        /*mark initial useful instr*/
        for (Function function : module.getFunctions()) {
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                for (IRInstruction instruction : basicBlock.getInstructions()) {
                    if (instruction instanceof Call || instruction instanceof Br || instruction instanceof  Ret || instruction instanceof Store) {
                        instruction.setUseful();
                    }
                }
            }
        }

        for (Function function : module.getFunctions()) {
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                for (IRInstruction instruction : basicBlock.getInstructions()) {
                    if (instruction.isUseful()) {
                        markUseful(instruction);
                    }
                }
            }
        }

        for (Function function : module.getFunctions()) {
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                Iterator<IRInstruction> iterator = basicBlock.getInstructions().iterator();
                while (iterator.hasNext()) {
                    IRInstruction instruction = iterator.next();
                    if (!instruction.isUseful()) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void markUseful(IRInstruction instr) {
        if (instr.getOperands().isEmpty()) {
            return;
        }
        for (Value value : instr.getOperands()) {
            if (value instanceof IRInstruction instr1) {
                if (instr1.isUseful()) {    //已经被标记过的不需要再次遍历，不然会进入循环
                    continue;
                }
                instr1.setUseful();
                markUseful(instr1);
            }
        }
    }

    private void dfs(BasicBlock block, HashSet<BasicBlock> reachableBlocks) {
        if (reachableBlocks.contains(block)) {
            return;
        }
        reachableBlocks.add(block);
        IRInstruction lastInstr = block.getLastInstr();
        if (lastInstr instanceof Br) {
            BasicBlock trueBlock = ((Br) lastInstr).getTrueBlock();
            BasicBlock falseBlock = ((Br) lastInstr).getFalseBlock();

            dfs(trueBlock, reachableBlocks);
            if (falseBlock != null) {
                dfs(falseBlock, reachableBlocks);
            }
        }
    }
}
