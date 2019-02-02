package p_heu.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;
import p_heu.entity.SearchState;
import p_heu.entity.sequence.Sequence;

import java.util.*;

public class ErrorRateSearch extends Search {

    protected Set<Sequence> correctSeqs;
    protected LinkedList<Sequence> queue;
    protected Sequence revSequence;
    protected int scheduleThreshod;
    protected Set<Sequence> errorSequences;
    protected int TotalRun;
    protected int ErrorSeqs;

    public ErrorRateSearch(Config config, VM vm) {
        super(config, vm);
        this.correctSeqs = new HashSet<>();
        this.queue = new LinkedList<>();
        this.errorSequences = new HashSet<>();
        this.revSequence = null;
        scheduleThreshod = 2;
        TotalRun = 0;
        ErrorSeqs = 0;
    }

    @Override
    public boolean requestBacktrack () {
        doBacktrack = true;
        return true;
    }

    @Override
    public boolean supportsBacktrack () {
        return true;
    }


    @Override
    public void search() {
        // TODO 编写search函数
        //每个从队列中拿出的距离最远的Sequence
        Sequence sequence = null;
        notifySearchStarted();

        //保存初始状态
        RestorableVMState init_state = vm.getRestorableState();
        while(!done){

            if(TotalRun >= 100){
                break;
            }
            if(isEndState()){
                //设置正确执行序列的状态为TRUE
                sequence.setResult(true);
                sequence.setFinished(true);
                addCorrectSeqs(sequence);
                vm.restoreState(init_state);
                vm.resetNextCG();
                //当前序列置为空
                sequence = null;
                queue.clear();
                TotalRun++;
                continue;
            }
            while(forward()){

                notifyStateAdvanced();
                //将当前的状态合并到上一状态之后，并添加到队列中
                queue.add(mergeSeq(sequence,revSequence));

                if(currentError != null){
                    notifyPropertyViolated();
                    if(hasPropertyTermination()){
                        queue.clear();
                        break;
                    }
                }
                if(!checkStateSpaceLimit()){
                    notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
                    //can't go on, we exhausted our memory
                    queue.clear();
                    break;
                }
                if(backtrack()){
                    //回溯
                    notifyStateBacktracked();
                }
            }
            //对当前队列进行排序
            sortQueue();
            //根据阈值删除队列中多余的sequence
            while(queue.size() > scheduleThreshod){
                queue.removeLast();
            }
            //判断当前队列中是否存在sequence，当队列size 小于0 表明找到一个正确的sequence
            if(queue.size() > 0){
                sequence = queue.poll();
                vm.restoreState(sequence.getLastState().getState());
            }else{
                //将所有正确的sequence添加到正确的序列集合中
                if(done == true){
                    if(!hasSame(sequence)){
                        errorSequences.add(sequence);
                        ErrorSeqs++;
                    }
                    done = false;
                }else{
                    sequence.setResult(true);
                    sequence.setFinished(true);
                    addCorrectSeqs(sequence);
                }
                vm.restoreState(init_state);
                vm.resetNextCG();
                //当前序列置为空
                sequence = null;
                TotalRun++;
            }
        }
        notifySearchFinished();
    }

    protected  Sequence mergeSeq(Sequence seqOld,Sequence seqNew){

        if(seqOld!=null){
            SearchState currentState = seqNew.getLastState();
            return seqOld.advance(currentState.getStateId(),currentState.getState(),seqNew.getNodes());
        }else{
            return seqNew;
        }

    }

    protected boolean hasSame(Sequence sequence){

        for(Sequence seq : errorSequences){
            if(isSameSeq(seq,sequence)){
                return true;
            }
        }
        return false;
    }

    protected boolean isSameSeq(Sequence seq1,Sequence seq2){

        int seq1size = seq1.getStates().size();
        int seq2size = seq2.getStates().size();
        if(seq1size != seq2size){
            return false;
        }
        for(int i = 0;i < Math.min(seq1size,seq2size);i++){
            if(seq1.getStates().get(i).getStateId() != seq2.getStates().get(i).getStateId()){
                return false;
            }
        }
        return true;
    }

    protected void addCorrectSeqs(Sequence seqs) {
        correctSeqs.add(seqs);
    }

    public void addCurrentSequence(Sequence seq){
        this.revSequence = seq;
    }

    public Set<Sequence> getErrorSequence() {
        return errorSequences;
    }

    public void addQueue(Sequence seq) {
        queue.add(seq);
    }

    public Set<Sequence> getCorrectSeqs() {
        return correctSeqs;
    }

    public double getErrorRate(){
        return (ErrorSeqs*1.0)/(TotalRun*1.0);
    }

    public void setCorrectSeqs(Set<Sequence> correctSeqs) {
        this.correctSeqs = correctSeqs;
    }

    protected void sortQueue() {
        Collections.sort(this.queue, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence seq1, Sequence seq2) {
                return -Integer.compare(seq1.getDistance(), seq2.getDistance());
            }
        });
    }
}


