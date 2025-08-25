package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import client.HumanModem;
import general.Protocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// All the methods and functions of the class ServerGui are temporarily excluded from project

/**
 * This class is the starting Gui of the Server. It tries to start MultipleServer with the given
 * port number. It sets up a Gui, where you can adjust your port and start your Server. It appends
 * all Messages and Events in separate JTextAreas.
 */
/*public class ServerGui extends JFrame implements ActionListener, WindowListener {*/

  /** log4j Logger helping debug everything */
  // private static final Logger LOGGER = LogManager.getLogger(ServerGui.class);
  /** for broadcast */
  // private JLabel label;
  /** the stop and start buttons */
  // private JButton stopStart;
  /** JTextArea for the chat room and the events */
  // private JTextArea chat, event;
  /** The port numberJTextArea, text field to broadcast */
  // private JTextField tPortNumber, tChat;
  /** object of MultipleServer */
  // private MultipleServer server;
  /** JFrame where the whole setting will be printed */
  // private JFrame frame;

  /**
   * constructor of this class which sets up the Gui
   *
   * @param port port number of the starting server
   */
  /*public ServerGui(int port) {

    super("Server");
    LOGGER.debug("Successfully called the ServerGui constructor");
    server = null;
    frame = new JFrame();
    JPanel northPanel = new JPanel(new GridLayout(3, 1));
    // in the north panel are the port number, the start and stop buttons
    JPanel north = new JPanel(new GridLayout(1, 10, 1, 1));
    north.add(new JLabel("Port number: "));
    tPortNumber = new JTextField("  " + port);
    // setting up position
    north.add(tPortNumber);

    northPanel.add(north);

    JPanel southPanel = new JPanel();
    // to stop or start the server
    stopStart = new JButton("Start");
    stopStart.setPreferredSize(new Dimension(100, 30));
    stopStart.addActionListener(this);
    southPanel.add(stopStart);
    // add to south position
    frame.add(southPanel, BorderLayout.SOUTH);

    JPanel textMessage = new JPanel(new GridLayout(3, 1));
    label = new JLabel("Broadcast", SwingConstants.CENTER);
    northPanel.add(label);
    tChat = new JTextField("");
    // background color is white
    tChat.setBackground(Color.WHITE);
    // because server didn't start yet
    tChat.setEditable(false);
    northPanel.add(tChat);
    // setting up position
    frame.add(northPanel, BorderLayout.NORTH);

    // the event and the chat room
    JPanel center = new JPanel(new GridLayout(2, 1));
    // size of area
    chat = new JTextArea(80, 80);
    // can't have chat messages because the server is not activated
    chat.setEditable(false);
    // setting up title
    appendRoom("Chat room.\n");
    // setting up position
    center.add(new JScrollPane(chat));
    // size of area
    event = new JTextArea(80, 80);
    // can't have events because the server is not activated
    event.setEditable(false);
    // setting up title
    appendEvent("Events log.\n");
    // setting up position
    center.add(new JScrollPane(event));
    frame.add(center);
    // need to be informed when the user click the close button on the frame
    frame.addWindowListener(this);
    // size of Gui
    frame.setSize(400, 600);
    // makes it visible to the user
    frame.setVisible(true);
  }*/

  /**
   * This method appends messages to the chat-room
   *
   * @param str the message
   */
  /*void appendRoom(String str) {
    // appends chat message to the chat room
    chat.append(str);
    chat.setCaretPosition(chat.getText().length() - 1);
  }*/

  /**
   * This method appends events to the event-room
   *
   * @param str the event
   */
  /*void appendEvent(String str) {
    // appends event to the event room
    event.append(str);
    event.setCaretPosition(chat.getText().length() - 1);
  }*/

  /*public void setFrameInvisible() {
    frame.setVisible(false);
  }*/

  /**
   * This method tries to create an object of MultipleServer by clicking on the button 'Start' and
   * can stop the server by clicking the button 'Stop'
   *
   * @param e is the actionevent start or stop
   */
  /*public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    // if the server is already running
    if (server != null) {
      if (o == stopStart) {
        server.stop();
        server = null;
        stopStart.setText("Start");
        // area of port number invisible
        tPortNumber.setEditable(true);
        tChat.setEditable(false);
        return;
      } else {
        String message = tChat.getText();
        if (message.length() > 0) {
          String line = HumanModem.getCommand(message);
          String[] commands = line.split(" ");
          switch (Protocol.valueOf(commands[0])) {
            case CHAT:
              // command[0] = "CHAT", the rest: the message

              // tries to get the message
              StringBuilder serverMessage = new StringBuilder();
              for (int j = 1; j < commands.length; j++) {
                serverMessage.append(commands[j] + " ");
              }
              // broadcasts the message to clients who are not in a game
              server.chat("Server: " + serverMessage.toString());
              break;

            case BROADCAST:
              // command[0] = "BROADCAST", the rest: the message

              // tries to get the message
              StringBuilder serverBroadcast = new StringBuilder();
              for (int j = 1; j < commands.length; j++) {
                serverBroadcast.append(commands[j] + " ");
              }
              // broadcasts the message to all clients
              server.broadcast("Server: " + serverBroadcast.toString());
              break;

            case WHISPER:
              // command[0] = "WHISPER", command[1] = name of client who should get the message,
              // the rest: the message

              // second input must be the name
              try {
                String nameOfGetter = commands[1];

                // StringBuilder to get message
                StringBuilder getMessage = new StringBuilder();
                // for-loop to get message
                for (int j = 2; j < commands.length; j++) {
                  getMessage.append(commands[j] + " ");
                }

                String msg = "Server: *private* " + getMessage.toString();
                server.whisperChat(msg, nameOfGetter);
              } catch (ArrayIndexOutOfBoundsException aE) {
                appendEvent("Array index out of bounds");
              }
              break;
            default:
              appendEvent("Protocol command can't be used by you.");
          }
          tChat.setText("");
          return;
        }
      }
    }
    // starts the server
    int port;
    try {
      // tries to get the port number as an integer
      port = Integer.parseInt(tPortNumber.getText().trim());
    } catch (Exception er) {
      appendEvent("Invalid port number");
      return;
    }

    LOGGER.debug("New MultipleServer object will be created.");

    // creates a new server with given port number
    server = new MultipleServer(port, this);
    // and starts the server as a thread
    ServerRunning sr = new ServerRunning();
    sr.start();

    LOGGER.debug("Multiple server thread has been started.");

    // make it to a stop button
    stopStart.setText("Stop");
    // area of port number invisible
    tPortNumber.setEditable(false);
    tChat.setEditable(true);
    tChat.addActionListener(this);
  }*/

  /**
   * If the user click the X button to close the application
   *
   * @param e which says that the close button was been clicked
   */
  /*public void windowClosing(WindowEvent e) {
    // if the server already exists
    if (server != null) {
      try {
        // stops server
        server.stop();
      } catch (Exception eClose) {
      }
      // delete server
      server = null;
    }
    // dispose the frame
    dispose();
    // close program
    System.exit(0);
  }

  // the other WindowListener methods, which aren't important at the moment
  public void windowClosed(WindowEvent e) {}

  public void windowOpened(WindowEvent e) {}

  public void windowIconified(WindowEvent e) {}

  public void windowDeiconified(WindowEvent e) {}

  public void windowActivated(WindowEvent e) {}

  public void windowDeactivated(WindowEvent e) {}*/

  /** This class represents a thread which starts the server */
  /*class ServerRunning extends Thread {*/

    /**
     * This method starts the server as a thread. If we come out of the start()-method, it means
     * that the server crashed
     */
    /*public void run() {
        // starts start method of MultipleServer
        server.start();
        // stops server
        stopStart.setText("Start");
        tPortNumber.setEditable(true);
        appendEvent("Server crashed\n");
        server = null;
      }
    }*/
// }
