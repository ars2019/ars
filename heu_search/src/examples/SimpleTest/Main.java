package SimpleTest;

public class Main {

    public static void main(String[] args){

        HasSelfPrivateNum hspn = new HasSelfPrivateNum();

        Thread thread1 = new Thread() {

            int a = 0 , b = 0;
            public void run() {

                a = hspn.x;
                hspn.x = a - 1;
                b = hspn.y;
                hspn.y = b + 1;
            }
        };

        Thread thread2 = new Thread() {
            int c = 0;
            public void run() {
               c = hspn.x;
               hspn.y = c - 1;
               if(hspn.y <= 0 ){
                   System.out.println("hspn.y:"+hspn.y);
                   assert hspn.y <= 0 : (" wrong detected " + hspn.y);
               }
            }
        };
        thread1.setName("thread1");
        thread2.setName("thread2");

        thread1.start();
        thread2.start();

    }
}
