package allocationvector;

//package Bug;
import java.io.*;

/**
 * class Test: Used to test class AllocationVector.
 */
public class TestCase {
  /**
   * Indicates number of thread runs to perform.
   */
  private static int runsNum = 1;
  private static int vectorSize = 100;
  private static int blockSize = 5;
  private static int numberOfThreads = 2;

  /**
  * MAIN METHOD.
  *   Refactored version of the original IBM example.   Eliminates
  *   coarse scalability "values".
  */

  public static void main(String[] args) {
    if (args != null && args.length == 4) {
      blockSize = Integer.parseInt(args[0]);
      vectorSize = Integer.parseInt(args[1]);
      runsNum = Integer.parseInt(args[2]);
      numberOfThreads = Integer.parseInt(args[3]);
    }
    for (int i=0; i < runsNum; i++) {
      runTest(); 
    }
  }

  /**
   * Gets from 'args': 1. Name of output file.
   *                   2. Concurrency Parameter (little,average,lot).
   * @param args command-line arguments as written above.
   */
  public static void runTest() {
    AllocationVector vector = null;
    TestThread thread1 = null;
    TestThread thread2 = null;
    int[] threadResult = null;
    FileOutputStream out = null;
    TestThread [] testThreads = new TestThread[numberOfThreads];
    

    /**
     * If here, then command-line arguments are correct.
     * Therefore, proceeding according to the concurrency parameter value.
     */
    // Setting threads run configuration according to concurrency parameter.
    vector = new AllocationVector(vectorSize);
    //thread1Result = new int[blockSize];
    //thread2Result = new int[blockSize];

    // Creating threads, starting their run and waiting till they finish.
    for (int i=0; i<numberOfThreads; i++){
       testThreads[i] = new TestThread(vector, new int[blockSize]);
    }
    for (int i=0; i<numberOfThreads; i++){
       testThreads[i].start();
    }
    try {
      for (int i=0; i<numberOfThreads; i++){
         testThreads[i].join();
      }
    } catch (InterruptedException e) {
      System.exit(1);
    }

    // Checking correctness of threads run results and printing the according
    // tuple to output file.
/*
     if (thread1Result[0] == -2) {
         throw new RuntimeException("bug found");
       //out.write("<Test, Thread1 tried to allocate block which is allocated, weak-reality (Two stage access)>\n".getBytes());
     } else if (thread1Result[0] == -3){
         throw new RuntimeException("bug found");
      // out.write("<Test, Thread1 tried to free block which is free, weak-reality (Two stage access)>\n".getBytes());
     } else if (thread2Result[0] == -2) {
         throw new RuntimeException("bug found");
     //  out.write("<Test, Thread2 tried to allocate block which is allocated, weak-reality (Two stage access)>\n".getBytes());
     } else if (thread2Result[0] == -3){
         throw new RuntimeException("bug found");
       //out.write("<Test, Thread2 tried to free block which is free, weak-reality (Two stage access)>\n".getBytes());
     } else {
        // out.write("<Test, correct-run, none>\n".getBytes());
     }
*/
  }
}
