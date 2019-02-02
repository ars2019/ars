package reorder;

/*
 * Created Apr 27, 2005
 * @author Xuan
 */
public class SetThread2 extends Thread {
    SetCheck2 sc;
    int i;
    public SetThread2(SetCheck2 sc, int i) {
	this.sc=sc;
	this.i = i;
    }
    public void run() {
	sc.set(i);
    }
}
