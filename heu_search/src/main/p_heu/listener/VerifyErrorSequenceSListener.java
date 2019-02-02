package p_heu.listener;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.ScheduleNode;
import p_heu.entity.SearchState;
import p_heu.entity.filter.Filter;
import p_heu.entity.sequence.Sequence;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VerifyErrorSequenceSListener extends ListenerAdapter {
    private Sequence sequence;
    private List<Node> currentStateNodes;
    private int nodeId;
    private SearchState currentState;
    private boolean execResult;
    private Filter positionFilter;
    //store error sequence
    private Sequence errorSequence;
    private List<Node> nodes;
    //store schudule nodes in the error sequence
    private LinkedList<ScheduleNode> scheduleNodes;

    public VerifyErrorSequenceSListener(Sequence errorSequence) {
        this.sequence = new Sequence();
        nodeId = 0;
        currentStateNodes = null;
        currentState = null;
        execResult = true;
        positionFilter = null;

        this.errorSequence = errorSequence;
        nodes = new ArrayList<>();
        scheduleNodes = new LinkedList<>();
    }

    public void setPositionFilter(Filter filter) {
        positionFilter = filter;
    }

    public Sequence getSequence() {
        return sequence;
    }

    private int getNodeId() {
        return nodeId++;
    }

    private void initCurrentState(VM vm,Search search) {
        currentState = new SearchState(vm.getStateId(), vm.getRestorableState());
        if (currentState != null) {
            saveLastState();
        }
        currentStateNodes = new ArrayList<>();
    }

    public void searchStarted(Search search) {
        VM vm = search.getVM();
        currentStateNodes = new ArrayList<>();

        nodes = errorSequence.getNodes();
        for(Node node : nodes){
            if(node instanceof ScheduleNode){
                scheduleNodes.add((ScheduleNode) node);
            }
        }
    }

    public void stateAdvanced(Search search) {

        VM vm = search.getVM();
        initCurrentState(vm,search);
    }

    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
        if (executedInstruction instanceof FieldInstruction) {
            FieldInstruction fins = (FieldInstruction)executedInstruction;
            if (positionFilter != null && !positionFilter.filter(fins.getFileLocation())) {
                return;
            }

            FieldInfo fi = fins.getFieldInfo();
            ElementInfo ei = fins.getElementInfo(currentThread);

            String type = fins.isRead() ? "READ" : "WRITE";
            String eiString = ei == null ? "null" : ei.toString();
            String fiName = fi.getName();
            ReadWriteNode node = new ReadWriteNode(getNodeId(), eiString, fiName, type, currentThread.getName(), fins.getFileLocation());
            currentStateNodes.add(node);
        }
    }

    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {

        int numOfChoice = currentCG.getTotalNumberOfChoices();
        Object[] currentChoices = (Object[]) currentCG.getAllChoices();
        ScheduleNode currentScheduleNode = null;
//        for(int i = 0; i < numOfChoice;i++){
//            System.out.println(currentCG.getChoice(i));
//        }
        if (currentCG instanceof ThreadChoiceGenerator) {
            if (!scheduleNodes.isEmpty()) {

                currentScheduleNode = scheduleNodes.getFirst();
                for (int i = 0; i < numOfChoice; i++) {
                    if (((ThreadInfo) currentChoices[i]).getName().equals(currentScheduleNode.getThread())) {
                        currentCG.select(i);
//                        System.out.println(currentScheduleNode);
//                        System.out.println("found");
                        scheduleNodes.removeFirst();
                        break;
                    }
                }
                //System.out.println("- - - - - - - - - - - - - - - - - - - ");
            }
        }

        if (currentCG instanceof ThreadChoiceFromSet) {

            ThreadInfo[] threads = ((ThreadChoiceFromSet)currentCG).getAllThreadChoices();
            if (threads.length == 1) {
                return;
            }
            ThreadInfo ti = (ThreadInfo)currentCG.getNextChoice();
            Instruction insn = ti.getPC();
            String type = insn.getClass().getName();
            ScheduleNode node = new ScheduleNode(getNodeId(), ti.getName(), insn.getFileLocation(), type);
            currentStateNodes.add(node);
        }
    }

    private void saveLastState() {
        sequence = sequence.advance(currentState.getStateId(), currentState.getState(), currentStateNodes);
    }

    public void propertyViolated(Search search) {
        execResult = false;
    }

    public void searchFinished(Search search) {
        sequence = sequence.advanceToEnd(currentState.getStateId(),currentState.getState(), currentStateNodes, execResult);
    }
}
