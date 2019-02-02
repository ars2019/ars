package reorder;

public class ReorderTest2 {
    static int iSet=4;

    public void run() {
	SetThread2[] sts = new SetThread2[iSet];
	CheckThread2 ct;
	SetCheck2 sc=new SetCheck2();
	for (int i=0;i<iSet;i++) {
	    (sts[i] = new SetThread2(sc, i)).start();
	}
	try {
   	    for (int i=0;i<iSet;i++) {
	        sts[i].join();
	    }
	}catch(java.lang.InterruptedException ie) {
    }
    
	ct = new CheckThread2(sc);
	ct.start();
    }

    public static void main(String[] args) {
	if (args != null && args.length == 1) {
		iSet = Integer.parseInt(args[0]);
	}
	ReorderTest2 t = new ReorderTest2();
	t.run();
    }
}
