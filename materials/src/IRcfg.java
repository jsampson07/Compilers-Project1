import ir.IRFunction;
import ir.IRInstruction;
import ir.operand.IRLabelOperand;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/*
I want my CFG to contain...
1) List of IRNodes
*/

public class IRcfg {
    public IRNode entry_node; // the entry node for the CFG (i.e. head)
    public List<IRNode> nodes = new ArrayList<>(); // list of nodes in the graph (each are individual instrucs; in order when "executing" code)

    public IRcfg() {}

    public IRcfg(IRFunction function) {
        Map<Integer, IRNode> irInstrucToNode = new HashMap<>();
        Map<String, IRNode> irLabelToNode = new HashMap<>();
        //public Map<String, IRNode> irFunctionToNode = new HashMap();

        // first pass: create a node for each instruction, then we can work off the nodes rather than instructions
        boolean is_entry = false;
        for (IRInstruction instruction : function.instructions) {
            // I do not want to keep labels as instructions, just add to irLabelToNode
            /*actually, i cannot skip labels because what if i have label0, label1, label2 sequentially (back-to-back-to-back) ==> edge case
            if (instruction.opCode == IRInstruction.OpCode.LABEL) {
                continue;
            }
            */
            IRNode new_node = new IRNode(instruction);
            irInstrucToNode.put(instruction.irLineNumber, new_node);
            if (!is_entry) {
                this.entry_node = new_node;
            }

            if (instruction.opCode == IRInstruction.OpCode.LABEL) { // this instruction is a label, lets add it for easy data flow
                String label_name = ((IRLabelOperand) instruction.operands[0]).getName();
                irLabelToNode.put(label_name, irInstrucToNode.get(instruction.irLineNumber));
            }
        }

        // now that I have the nodes of the graph, i need to connect them to represent the data flow correctly
        IRInstruction curr_instruction = null;
        IRNode curr_node = null;
        List<IRInstruction> instructions = function.instructions;
        for (int i = 0; i < instructions.size(); i++) {
            curr_instruction = instructions.get(i);
            curr_node = irInstrucToNode.get(curr_instruction);
            //now that we have the node, we can actually connect nodes when we find the data flow (branch, goto, regular instruction)
            switch (curr_instruction.opCode) {
                case GOTO -> {
                    String goto_label = ((IRLabelOperand) curr_instruction.operands[0]).getName();
                    IRNode target_node = irLabelToNode.get(goto_label);
                    connectNodes(curr_node, target_node);
                }
                //BRANCH CASES: we need to make a target_node for jumping to label AND for just going to next-line (condition is not met)
                case BREQ, BRNEQ, BRLT, BRGT, BRGEQ -> {
                    /* we do not want to actually jump based on condition, we are ONLY constructing the CFG !!!!!
                    String val_op1 = curr_instruction.operands[1];
                    String val_op2 = curr_instruction.operands[2];
                    if (val_op1.equals(val_op2)) { // we want to branch
                        String branch_label = ((IRLabelOperand) curr_instruction.operands[0]).getName();
                        IRNode target_node = irLabelToNode.get(target_label);

                    } else {
                        String target_node = irInstrucToNode.get(i+1); // go to the next instruction (node)
                    }
                    */
                    //if we were to take the branch
                    String branch_label = ((IRLabelOperand) curr_instruction.operands[0]).getName();
                    IRNode target_node1 = irLabelToNode.get(branch_label);
                    //if we didn't take the branch, we just go to the next instruction
                    IRNode target_node2 = irInstrucToNode.get(curr_instruction.irLineNumber + 1); //curr instruc, we just want next instruc (node)
                    connectNodes(curr_node, target_node1);
                    connectNodes(curr_node, target_node2);
                }
                case RETURN -> {
                    break;
                }
                default -> { //everything else including CALL, CALLR
                    IRNode target_node = irInstrucToNode.get(curr_instruction.irLineNumber + 1);
                    connectNodes(curr_node, target_node);
                }
            }
            this.nodes.add(curr_node); // add this to the list of nodes after all info needed for it
        }
    }

    public void addToCFG(IRNode node) {
        this.nodes.add(node);
    }

    public void connectNodes(IRNode from, IRNode to) {
        from.successors.add(to);
        to.predecessors.add(from);
    }
}

/*

I want to construct CFG,
then I want to iterate over and over again until the sets do NOT change (fixed point iteration method)

when constructing the CFG, how do I know how to construct the basic blocks
Q: we know the head of each of the blocks given the conditions:
1) first instruction in the procedure
2) the target of a goto or branch instruction
3) the successor of a branch instruction
Q: how do i know when the basic block ends?
everytime there is a branch instruction or goto, then there are immediately 2+ successor basic blocks that will be created
Q: how do i know the data flow of the program?

maybe: maximal block versus minimal (one instruction per block)


 */