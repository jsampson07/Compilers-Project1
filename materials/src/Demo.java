import ir.*;
import ir.datatype.IRArrayType;
import ir.datatype.IRIntType;
import ir.datatype.IRType;
import ir.operand.IRConstantOperand;
import ir.operand.IROperand;
import ir.operand.IRVariableOperand;

import java.io.PrintStream;
import java.util.*;

public class Demo {
    public static void main(String[] args) throws Exception {
        // Parse the IR file
        IRReader irReader = new IRReader();

        /*
        IRProgram: (list of functions within the program)

            [IRFunction1, IRFunction2, IRFunction3 ... IRFunctionN]
            
        IRFunction: (list of a particular functions information)

            String name
            IRType returnType
            List<IRVariableOperand> parameters
            List<IRVariableOperand> variables
            List<IRInstruction> instructions

        IRInstruction: (each instructions information)
        
            OpCode opCode
            IROperand[] operands (list of operands in the instruction)
                - each IROperand has a "value" and "parent" for instruction it belongs to
            int irLineNumber (where does this get set????)
         */
        IRProgram program = irReader.parseIRFile(args[0]); //Work on this --> List of functions --> List of operands, instructions, etc
                                                            // use this as a template

        for (IRFunction function : program.functions) {
            IRcfg cfg = new IRcfg(function); // we create the CFG for this function
            // now we want to run the optimizer
            /* Reaching Definitions Analysis 
                    We want to go through each node and then set the GEN and KILL sets
                    AND we want to initialize OUT = GEN and IN = null
                    THEN we want to do a SECOND PASS:
                        - where we calculate the IN and OUT sets UNTIL we reach a fixed point (IN/OUT sets are equal after 2 iterations) */
            
            //1. Calculate GEN/KILL Sets and Initialize OUT set = GEN
            calculateSets(cfg);

            //2. Calculate IN/OUT Sets
            /* Continuously traverse CFG until IN/OUT do NOT change ==> reached the Fixed Point 
            NOTE: IN[B] = OUT[P] for P in B.predecessors 
                  OUT[B] = GEN[B] U (IN[B] - KILL[B]) */
            fixedPointAlg(cfg);

            /* USED FOR TESTING AND CHECKING SETS for the nodes after calculating them
            for (IRNode node : cfg.nodes) {
                System.out.println("GEN[node]: " + node.GEN);
                System.out.println("KILL[node]: " + node.KILL);
                System.out.println("IN[node]: " + node.IN);
                System.out.println("OUT[node]: " + node.OUT);
            }
            */

            //3. Mark Algorithm (Lecture 4 Slide 4)
                //a. mark critical instructions
            markAlg(cfg);

            /* CAUSES ConcurrentModificationException --> just add the "correct" nodes to another list
            int counter = 1;
            for (IRNode critical_nodes : cfg.nodes) {
                if (critical_nodes.is_marked) {
                    System.out.println(counter);
                    counter++;
                    System.out.println(critical_nodes.toString());
                }
            }
            */

            //4. Sweep Algorithm (Lecture 4 Slide 4) and Get the critical instructions and update the functions instructions list
            sweepAlg(cfg, function);
        }

        // Print the IR to another file
        IRPrinter filePrinter = new IRPrinter(new PrintStream(args[1]));
        filePrinter.printProgram(program);

        // Create an IR printer that prints to stdout
        IRPrinter irPrinter = new IRPrinter(new PrintStream(System.out));

        // Print all instructions that stores a constant to an array
        System.out.println("Instructions that stores a constant to an array:");
        // Implement the algorithm here
        for (IRFunction function : program.functions) { // for each function in our program
            for (IRInstruction instruction : function.instructions) { // we go through every instruction
                //This is specific to the Demo (I think)
                if (instruction.opCode == IRInstruction.OpCode.ARRAY_STORE) {
                    if (instruction.operands[0] instanceof IRConstantOperand) {
                        System.out.print(String.format("Line %d:", instruction.irLineNumber));
                        irPrinter.printInstruction(instruction);
                    }
                }
            }
        }
        System.out.println();

        // Print the name of all int scalars and int arrays with a size of 1
        System.out.println("Int scalars and 1-sized arrays:");
        for (IRFunction function : program.functions) {
            List<String> vars = new ArrayList<>();
            for (IRVariableOperand v : function.variables) {
                IRType type = v.type;
                // For each unique data type, only one IRType object will be created
                // so that IRType objects can be compared using '=='
                if (type == IRIntType.get() || type == IRArrayType.get(IRIntType.get(), 1))
                    vars.add(v.getName());
            }
            if (!vars.isEmpty())
                System.out.println(function.name + ": " + String.join(", ", vars));
        }
        System.out.println();

        // Print all variables that are declared but not used (including unused parameters)
        System.out.println("Unused variables/parameters:");
        for (IRFunction function : program.functions) {
            // IROperand objects are not shared between instructions/parameter list/variable list
            // They should be compared using their names
            Set<String> vars = new HashSet<>();
            // Parameters are not included in the variable list
            for (IRVariableOperand v : function.parameters)
                vars.add(v.getName());
            for (IRVariableOperand v : function.variables)
                vars.add(v.getName());
            for (IRInstruction instruction : function.instructions)
                for (IROperand operand : instruction.operands)
                    if (operand instanceof IRVariableOperand) {
                        IRVariableOperand variableOperand = (IRVariableOperand) operand;
                        vars.remove(variableOperand.getName());
                    }
            if (!vars.isEmpty())
                System.out.println(function.name + ": " + String.join(", ", vars));
        }
        System.out.println();
    }

    public static void calculateSets(IRcfg cfg) {
        //IRNode curr_node = cfg.entry_node;
        for (IRNode node : cfg.nodes) {
            if (node.defined_var == null) {
                continue;
            }
            node.addToGen(node);
            node.addToOut(node); // combine Step 2 from Lecture 3 Slide 30 into this step


            for (IRNode nested_node : cfg.nodes) {
                if (nested_node.defined_var == null) {
                    continue;
                }
                // if they both define the same variable AND they are not the same node
                if ((nested_node.defined_var.equals(node.defined_var)) && (!nested_node.equals(node))) {
                    node.addToKill(nested_node);
                }
            }
        }
        //after: GEN, KILL are intialized, IN = null/empty, OUT = GEN
    }

    public static void fixedPointAlg(IRcfg cfg) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (IRNode node : cfg.nodes) {
                //CALCULATE IN SET: loop through IMMEDIATE predecessor(s),find "their" OUT and "union" all of it together
                Set<IRNode> new_IN = new HashSet<>();
                for (IRNode pre_node : node.predecessors) {
                    for (IRNode out_elem : pre_node.OUT) {
                        new_IN.add(out_elem);
                    }
                }
                node.IN = new_IN;

                //Following the instructions in Lecture 3 Slide 32, lets just directly set OUT set
                Set<IRNode> new_OUT = new HashSet<>(node.GEN);
                Set<IRNode> in_minus_kill = new HashSet<>();
                for (IRNode in_node : node.IN) {
                    in_minus_kill.add(in_node);
                }
                for (IRNode kill_node : node.KILL) {
                    in_minus_kill.remove(kill_node);
                }
                for (IRNode imk_node : in_minus_kill) {
                    new_OUT.add(imk_node);
                }

                if (!node.OUT.equals(new_OUT)) {
                    changed = true;
                    node.OUT = new_OUT;
                }
            }
        }
    }

    public static void markAlg(IRcfg cfg) {
        Queue<IRNode> worklist = new LinkedList<>();
        for (IRNode node : cfg.nodes) {
            node.is_marked = false;
            switch(node.instruction.opCode) {
                case GOTO, BREQ, BRNEQ, BRLT, BRGT, BRGEQ, CALL, CALLR, RETURN, ARRAY_STORE -> {
                    node.is_marked = true;
                    worklist.add(node);
                }
                default -> {
                    break;
                }
            }
        }
        // With worklist created, let's implement part 2 of the Mark Algorithm
        IRNode worklist_node = worklist.poll();
        while (worklist_node != null) {
            IRInstruction wn_instruction = worklist_node.instruction;
            List<String> used_vars = worklist_node.used_vars;
            /* worklist_node - FIND OUT what kind of instruction it is
                we can ignore everything accounted for in initialization of worklist 
                consider:   3 operand instructions:
                                - ASSIGN (array), ADD, SUB, MULT, DIV, AND, OR, BREQ, BRNEQ, BRLT, BRGT, BRGEQ
                            2 operand instructions:
                                - ASSIGN (variable), ARRAY_STORE, ARRAY_LOAD 
                            1 operand instruction:
                                - RETURN  */
            /* What are the different formats of an instruction?
                    1. a <- b (ASSIGN variable)
                    2. t <- a (+,-,*,/,&,|) b 
                    3. t <- t (+,-,*,/,&,|) a
                    4. t <- t (+,-,*,/,&,|) t
                    5. 
                    */
            
            for (String used_var : used_vars) {
                for (IRNode maybe_important : worklist_node.IN) {
                    if (maybe_important.defined_var.equals(used_var)) {
                        if (!maybe_important.is_marked) {
                            maybe_important.is_marked = true;
                            worklist.add(maybe_important);
                        }
                    }
                }
            }

            /*
            if (wn_instruction.operands.length == 3) {
                IROperand op1 = wn_instruction.operands[1];
                IROperand op2 = wn_instruction.operands[2];
                // find all instructions that exist with op1 or op2
                for (IRNode important_node : worklist_node.IN) {
                    if (important_node.defined_var.equals(op1) || important_node.defined_var.equals(op2)) {
                        if (!important_node.is_marked) {
                            important_node.is_marked = true;
                            worklist.add(important_node);
                        }
                    }
                }
                //System.out.println(wn_instruction.opCode.toString() + " " + wn_instruction.operands[0] + " <- " + op1 + ", " + op2);
            } else if (wn_instruction.operands.length == 2) {                
                IROperand op1 = wn_instruction.operands[1];
                // find instructions with op1
                for (IRNode important_node : worklist_node.IN) {
                    if (important_node.defined_var.equals(op1)) {
                        if (!important_node.is_marked) {
                            important_node.is_marked = true;
                            worklist.add(important_node);
                        }
                    }
                }
                //System.out.println(wn_instruction.opCode.toString() + " " + wn_instruction.operands[0] + " <- " + op1);
            }
            */
            //for every instruc "j" that
            worklist_node = worklist.poll();
        }
    }

    public static void sweepAlg(IRcfg cfg, IRFunction func) {
        List<IRInstruction> final_instructions = new ArrayList<>();
        for (IRNode node : cfg.nodes) {
            if (node.is_marked || node.instruction.opCode == IRInstruction.OpCode.LABEL) {
                final_instructions.add(node.instruction);
            }
        }
        func.instructions = final_instructions;
    }
}
