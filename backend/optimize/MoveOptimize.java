package backend.optimize;

import backend.Components.MipsBasicBlock;
import backend.Components.MipsFunction;
import backend.Components.MipsModule;
import backend.Components.instrutions.MipsInstruction;
import backend.Components.instrutions.MipsMove;
import midend.IRModule;

import java.util.Iterator;

public class MoveOptimize {

    private MipsModule module;
    public MoveOptimize(MipsModule module) {
        this.module = module;
    }

    public void run() {
        for (MipsFunction function : module.getFunctions()) {
            for (MipsBasicBlock basicBlock : function.getMipsBasicBlocks()) {
                Iterator<MipsInstruction> iterator = basicBlock.getInstructions().iterator();
                while(iterator.hasNext()) {
                    MipsInstruction instr = iterator.next();
                    if (instr instanceof MipsMove) {
                        if (((MipsMove) instr).getRs().equals(((MipsMove) instr).getRt())) {    // 无效move，可以直接删除
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }
}
