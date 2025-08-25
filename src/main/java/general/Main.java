package general;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import client.ClientGui;
import server.MultipleServer;
/*import utils.project.server.ServerGui;*/

import java.lang.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class is the start of our game. In this class, you have to give as client the ip.address,
 * the port and (if you want) a username. If you don't give a username, the program tries to get the
 * name of your system. As server, you have to give the port.
 */
public class Main {

  /**
   * This method tries to find out whether the user wants to start a server or a client. It finds
   * this out by taking the first word out of the input. The first word is either 'server' or
   * 'client'.
   *
   * @param args contains the inputs from the command line: client: client
   *     <IP-address><portNumber>[<username>] server: server <port number>
   */
  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(Main.class);

  public static void main(String[] args) {

    try {

      // changes every letter to lower case
      args[0] = args[0].toLowerCase();

      // replaces everything which is not a small letter
      args[0] = args[0].replaceAll("[^a-z]", "");

      // looks whether the first word was 'client' or 'server'

      switch (args[0]) {

          // first word was client
        case "client":
          LOGGER.debug("Client is started...");

          int count = 0;
          String hostAddress = "";
          int port = 0;
          if (args[1].indexOf(58) != -1) {
            // splits at : to get ip and port
            String[] data = args[1].split(":");
            // changes every letter to lower case
            data[0] = data[0].toLowerCase();
            // ip-address, replaces everything which is not a number or a point
            data[0] = data[0].replaceAll("[^a-z.0-9]", "");

            // port, replaces everything which is not a number
            data[1] = data[1].replaceAll("[^0-9]", "");

            if (data[0].equals("localhost")) {
              try {
                // trying to find ip of host
                InetAddress ia = InetAddress.getLocalHost();
                hostAddress = ia.getHostAddress();
              } catch (UnknownHostException e) {
                e.printStackTrace();
              }
            } else {
              // first word is ip-address
              hostAddress = data[0];
            }
            // second word is port number
            port = Integer.parseInt(data[1]);
            count = 2;
          } else {
            // ip-address, replaces everything which is not a number or a point
            args[1] = args[1].replaceAll("[^a-z.0-9]", "");

            // port, replaces everything which is not a number
            args[2] = args[2].replaceAll("[^0-9]", "");

            if (args[1].equals("localhost")) {
              try {
                // trying to find ip of host
                InetAddress ia = InetAddress.getLocalHost();
                hostAddress = ia.getHostAddress();
              } catch (UnknownHostException e) {
                e.printStackTrace();
              }
            } else {
              // second word is ip-address
              hostAddress = args[1];
            }
            // third word is port number
            port = Integer.parseInt(args[2]);
            count = 3;
          }
          String username = "";
          // if a third word exist, it is the user name.
          if (args.length == count + 1) {
            username = args[count];
          } else {
            username = System.getProperty("user.name");
          }
          // tries to connect to ClientGui
          new ClientGui(hostAddress, port, username);
          break;

          // first word was 'server'
        case "server":
          LOGGER.debug("Starting Server...");
          // port, replaces everything which is not a number
          args[1] = args[1].replaceAll("[^0-9]", "");

          // second word must be the port, trying to start ServerGui
          /*new ServerGui(Integer.parseInt(args[1]));*/
          MultipleServer server = new MultipleServer(Integer.parseInt(args[1]));
          server.start();
          break;

          // user selected help
        case "help":
          System.out.println("To start client: client <IP-address>:<portNumber>[<username>]");
          System.out.println("To start server: server <portNumber>");
          break;

          // if program can't recognize the first word.
        default:
          System.out.println("The program couldn't recognize your input arguments.");
          System.out.println("Please try again.");
          System.out.println("For help, please use option help.");
          break;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("You forgot to give an important input.");
      System.out.println(
          "If you don't know how to start a client or a server," + "\nplease use option help.");
    } catch (NumberFormatException e) {
      System.out.println("You forgot to give an important input.");
      System.out.println(
          "If you don't know how to start a client or a server," + "\nplease use option help.");
    }
  }
}
