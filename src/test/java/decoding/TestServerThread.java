package decoding;

import server.MultipleServer;
/*import utils.project.server.ServerGui;*/

public class TestServerThread {

  MultipleServer server;

  TestServerThread() {}

  public void startServer() {
    /*System.out.println("started testHandRando");
    ServerGui sgui = new ServerGui(8090);
    System.out.println("created new servergui");
    sgui.setFrameInvisible();*/
    server = new MultipleServer(8090);
    System.out.println("created new server");
    InThread t = new InThread();
    t.start();
  }

  public MultipleServer getServer() {
    return server;
  }

  class InThread extends Thread {

    public synchronized void run() {
      System.out.println("started run: server");
      server.start();
      while (true) {}
    }
  }
}
