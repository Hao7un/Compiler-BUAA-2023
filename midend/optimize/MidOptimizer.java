package midend.optimize;

import midend.IRModule;
import utils.InOututils;

import java.io.IOException;

public class MidOptimizer {

    public MidOptimizer() {

    }

    public void optimize(IRModule module) throws IOException {
        /*Simplify Basic Block*/
        DeadCodeRemoval deadCodeRemoval = new DeadCodeRemoval(module);
        deadCodeRemoval.runSimplifyBlock();

        /*Build CFG*/
        CFGAnalysis cfgAnalysis = new CFGAnalysis(module);
        cfgAnalysis.run();

        /*Mem2reg*/
        Mem2reg mem2reg = new Mem2reg(cfgAnalysis,module);
        mem2reg.run();

        /*LVN*/
        LVN lvn = new LVN(module);
        lvn.run();

        /*Remove Dead Code*/
        deadCodeRemoval.runRemoveDeadCode();

        /*Register Allocation*/
        RegisterAllocator registerAllocator = new RegisterAllocator(module);
        registerAllocator.run();

        /*Remove Phi*/
        PhiRemoval phiRemoval = new PhiRemoval(module);
        phiRemoval.run();

        /*BranchOpt*/
        BranchOptimizer branchOptimizer = new BranchOptimizer(module);
        branchOptimizer.run();

        deadCodeRemoval.runSimplifyBlock();
    }
}
