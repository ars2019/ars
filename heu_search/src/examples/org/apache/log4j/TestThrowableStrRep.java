package org.apache.log4j;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.spi.LoggingEvent;

public class TestThrowableStrRep {
  
  public static void main(String[] args) {
    TestThrowableStrRep t = new TestThrowableStrRep();
    t.testNPE();
  }
  
  public void testNPE(){
    WriterAppender w = new WriterAppender();
    WriterAppender w2 = new WriterAppender();
    
    WriterAppender w3 = new WriterAppender();
    WriterAppender w4 = new WriterAppender();
    try {
      w.setWriter(new FileWriter("test"));
      w.setLayout(new SimpleLayout());
      w2.setWriter(new FileWriter("test"));
      w2.setLayout(new SimpleLayout());
      
      w3.setWriter(new FileWriter("test"));
      w3.setLayout(new SimpleLayout());
      w4.setWriter(new FileWriter("test"));
      w4.setLayout(new SimpleLayout());

    } catch (Exception e1) {
      e1.printStackTrace();
    }
    
    LoggingEvent event = new LoggingEvent("log1", Logger.getRootLogger(), Level.DEBUG, new String("message"), new Exception("test"));
    AppendThread at = new AppendThread(w, event);
    AppendThread at2 = new AppendThread(w2, event);
    
    AppendThread at3 = new AppendThread(w3, event);
    AppendThread at4 = new AppendThread(w4, event);
    at.start();
    at2.start();
    
    at3.start();
    at4.start();
    
    try {
      at.join();
      at2.join();
      
      at3.join();
      at4.join();
      
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  class AppendThread extends Thread {
    WriterAppender appender;
    LoggingEvent event;
    public AppendThread(WriterAppender app, LoggingEvent e){
      appender = app;
      event = e;
    }
    @Override
    public void run() {
      appender.append(event);
    }
  }
}
