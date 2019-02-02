package reorder;

/*
 * Created Apr 27, 2005
 * @author Xuan
 * Removed the try-catch block so assertion works properly
 */
public class CheckThread2 extends Thread {
    SetCheck2 sc;
    public CheckThread2(SetCheck2 sc) {
	this.sc=sc;
    }
    public void run() {
	boolean rst=sc.check();
	if (rst!= true)
		throw new RuntimeException("bug found");
	//assert (rst==true);
    }
}
