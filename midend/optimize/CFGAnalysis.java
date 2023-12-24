package midend.optimize;

import midend.IRModule;
import midend.values.BasicBlock;
import midend.values.Const.Function;
import midend.values.Instructions.IRInstruction;
import midend.values.Instructions.terminator.Br;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class CFGAnalysis {
    private IRModule module;

    private HashMap<Function,LinkedHashMap<BasicBlock, ArrayList<BasicBlock>>> prevsMap;  //基本块 -> 该块的前驱
    private HashMap<Function,LinkedHashMap<BasicBlock, ArrayList<BasicBlock>>>  nextsMap; //基本块 -> 该块的后继
    private HashMap<Function,LinkedHashMap<BasicBlock,ArrayList<BasicBlock>>> domMap;     // 基本块 -> 支配该基本块的基本块
    private HashMap<Function,LinkedHashMap<BasicBlock,BasicBlock>> iDomMap;               //直接支配关系 key is dominated by value
    private HashMap<Function,LinkedHashMap<BasicBlock,ArrayList<BasicBlock>>> domTree;               //支配树

    private HashMap<Function,LinkedHashMap<BasicBlock,ArrayList<BasicBlock>>> dfMap;      //支配边界

    public CFGAnalysis(IRModule module) {
        this.module = module;
        this.prevsMap = new LinkedHashMap<>();
        this.nextsMap = new LinkedHashMap<>();
        this.domMap = new LinkedHashMap<>();
        this.iDomMap = new LinkedHashMap<>();
        this.domTree = new LinkedHashMap<>();
        this.dfMap = new LinkedHashMap<>();
    }

    public HashMap<Function, LinkedHashMap<BasicBlock, ArrayList<BasicBlock>>> getDfMap() {
        return dfMap;
    }

    public HashMap<Function, LinkedHashMap<BasicBlock, ArrayList<BasicBlock>>> getDomMap() {
        return domMap;
    }

    public HashMap<Function, LinkedHashMap<BasicBlock, ArrayList<BasicBlock>>> getNextsMap() {
        return nextsMap;
    }

    public HashMap<Function, LinkedHashMap<BasicBlock, ArrayList<BasicBlock>>> getPrevsMap() {
        return prevsMap;
    }

    public HashMap<Function, LinkedHashMap<BasicBlock, BasicBlock>> getiDomMap() {
        return iDomMap;
    }

    public HashMap<Function, LinkedHashMap<BasicBlock, ArrayList<BasicBlock>>> getDomTree() {
        return domTree;
    }

    public void run() {
        initialize(module.getFunctions());
        for (Function function :  module.getFunctions()) {
            buildPrevAndNext(function);
            buildDomination(function);
            buildImmediateDominators(function);
            buildDF(function);
        }
    }

    public void initialize(ArrayList<Function> functions) {
        for (Function function : functions) {
            prevsMap.put(function,new LinkedHashMap<>());
            nextsMap.put(function,new LinkedHashMap<>());
            domMap.put(function,new LinkedHashMap<>());
            iDomMap.put(function,new LinkedHashMap<>());
            domTree.put(function,new LinkedHashMap<>());
            dfMap.put(function,new LinkedHashMap<>());

            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                prevsMap.get(function).put(basicBlock,new ArrayList<>());
                nextsMap.get(function).put(basicBlock,new ArrayList<>());
                domMap.get(function).put(basicBlock,new ArrayList<>());
                iDomMap.get(function).put(basicBlock,null);
                domTree.get(function).put(basicBlock,new ArrayList<>());
                dfMap.get(function).put(basicBlock,new ArrayList<>());
            }
        }
    }

    public void buildPrevAndNext(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            IRInstruction instruction = basicBlock.getLastInstr();
            if (instruction instanceof Br) {
                BasicBlock trueBlock = ((Br) instruction).getTrueBlock();
                BasicBlock falseBlock = ((Br) instruction).getFalseBlock();

                nextsMap.get(function).get(basicBlock).add(trueBlock);
                prevsMap.get(function).get(trueBlock).add(basicBlock);

                if (falseBlock != null) {
                    nextsMap.get(function).get(basicBlock).add(falseBlock);
                    prevsMap.get(function).get(falseBlock).add(basicBlock);
                }
            }
        }
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            basicBlock.setPredecessors(prevsMap.get(function).get(basicBlock));
            basicBlock.setSuccessors(nextsMap.get(function).get(basicBlock));
        }
    }

    public void buildDomination(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();

        HashSet<BasicBlock> allBlocks = new HashSet<>(basicBlocks);
        for (BasicBlock basicBlock : basicBlocks) {
            domMap.get(function).put(basicBlock, new ArrayList<>(allBlocks));
        }

        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : basicBlocks) {
                HashSet<BasicBlock> newDom = new HashSet<>();
                ArrayList<BasicBlock> prevs = prevsMap.get(function).get(basicBlock);

                if (prevs.isEmpty()) {
                    newDom.add(basicBlock); // 如果没有前驱，则只包含自身
                } else {
                    // 初始化为第一个前驱的dom集合
                    newDom.addAll(domMap.get(function).get(prevs.get(0)));

                    // 计算所有前驱的dom集合的交集
                    for (BasicBlock prevBasicBlock : prevs) {
                        newDom.retainAll(domMap.get(function).get(prevBasicBlock));
                    }

                    // 加入自身
                    newDom.add(basicBlock);
                }
                // 检查并更新dom集合
                if (!newDom.equals(new HashSet<>(domMap.get(function).get(basicBlock)))) {
                    domMap.get(function).put(basicBlock, new ArrayList<>(newDom));
                    changed = true;
                }
            }
        } while (changed);
    }

    private void buildImmediateDominators(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();

        for (BasicBlock basicBlock : basicBlocks) {
            ArrayList<BasicBlock> dominators = domMap.get(function).get(basicBlock);

            for (BasicBlock dominator : dominators) {
                if (dominator.equals(basicBlock)) {
                    /*strict dominate*/
                    continue;
                }
                //严格支配n，且不严格支配任何严格支配n的节点的节点
                boolean isImmediate = true;
                for (BasicBlock other : dominators) {
                    if(!other.equals(basicBlock) && !other.equals(dominator) && domMap.get(function).get(other).contains(dominator)) {
                        isImmediate = false;
                        break;
                    }
                }
                if (isImmediate) {
                    //每个节点的直接支配者有且只有一个,except for entry
                    iDomMap.get(function).put(basicBlock,dominator);
                    domTree.get(function).get(dominator).add(basicBlock);
                    break;
                }
            }
        }

        for (BasicBlock basicBlock : basicBlocks) {
            HashSet<BasicBlock> idoms = new HashSet<>(domTree.get(function).get(basicBlock));
            basicBlock.setIdoms(idoms);
        }
    }

    private void buildDF(Function function) {
        HashMap<BasicBlock,ArrayList<BasicBlock>> curNextsMap = nextsMap.get(function);
        for (HashMap.Entry<BasicBlock, ArrayList<BasicBlock>> entry : curNextsMap.entrySet()) {
            for (BasicBlock b : entry.getValue()) {                //for all edges
                BasicBlock x = entry.getKey();   //x <- a
                while (!domMap.get(function).get(b).contains(x) || x.equals(b)) {
                    //while x doesn't strictly dominate b <-> x doest not domiate b or x equals b
                    dfMap.get(function).get(x).add(b);
                    x = iDomMap.get(function).get(x);
                }
            }
        }
    }
}
