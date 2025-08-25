package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import general.Protocol;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is a class which creates a server that has the ability to deal with multiple
 * client-connections. The clients must hand over their respective username to the server. After
 * that, the client will be saved as a thread in an ArrayList.
 */
public class MultipleServer {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(MultipleServer.class);
  /** ArrayList with all players */
  public static ArrayList<Player> playerList;
  /** ArrayList with all connected client */
  private ArrayList<ClientConnectionHandler> connectionList;
  /** object of ServerGui to print there messages and events */
  // private ServerGui sgui;
  /** the port number */
  private int port;
  /** the boolean that will be turned off to stop the server */
  private boolean continueServer;
  /** creating ArrayList for Group */
  public static ArrayList<Group> groupList;

  /**
   * This method is the constructor of this class
   *
   * @param port port number of the server
   */
  public MultipleServer(int port) {
    // this.sgui = sgui;
    this.port = port;
    // initialization of ArrayList which will include all connected clients
    this.connectionList = new ArrayList<ClientConnectionHandler>();
    // initialization of ArrayList which will include all connected players
    playerList = new ArrayList<Player>();
    // initialization of ArrayList which will include all groups
    groupList = new ArrayList<Group>();
    LOGGER.debug("MS: constructor-created new server");
  }

  /**
   * This method starts the server by creating the server with the given port number. Furthermore it
   * accepts connection request and adds these clients in an arrayList.
   */
  public void start() {
    LOGGER.debug("MS: start-started start() method");
    continueServer = true;
    try {
      // the socket used by the server
      ServerSocket serverSocket = new ServerSocket(port);
      LOGGER.debug("MS: start-created object of ServerSocket");
      // a message saying that the server is waiting for new client connections
      /*display("Server waiting for Clients on port " + port + ".");*/
      // while-loop to wait for clients
      while (continueServer) {
        // accepts connection request
        Socket socket = serverSocket.accept();
        LOGGER.debug("MS: start-accepted connection to client");
        // checks whether the server is continuing with his function
        if (!continueServer) {
          break;
        }
        // new object of ClientConnectionHandler will be created with the new client
        ClientConnectionHandler ccHandler = new ClientConnectionHandler(socket);
        // saves client as a thread in an arrayList
        connectionList.add(ccHandler);
        LOGGER.debug("MS: start-added client as thread to connectionList");
        // starts thread
        ccHandler.start();
        LOGGER.debug("MS: start-started client connection as thread");
      }
      // stop server
      try {
        // closes server
        serverSocket.close();
        LOGGER.debug("MS: start-closed serverSocket");
        // closes ever Input/Output streams and connections to clients
        for (ClientConnectionHandler ccHandler : connectionList) {
          try {
            // closes input stream
            ccHandler.in.close();
            // closes output stream
            ccHandler.out.close();
            // closes client connection
            ccHandler.socket.close();
          } catch (IOException ioE) {
          }
        }
      } catch (Exception e) {
        /*display("Exception during closing the server and clients: " + e);*/
      }
    } catch (IOException e) {
      System.out.println("IO exception in MultipleServer.");
    }
  }

  /**
   * This method tries to compare the given name with the names of already connected clients, to
   * avoid that two client share the same name
   *
   * @param newName the given name of the client
   * @return newName the effective name, might be changed from input if there is a collision
   */
  public String checkNames(String newName) {
    for (ClientConnectionHandler cchandler : connectionList) {
      // tries to get the ClientConnectionHandler object from connectionList
      // if the given name equals a name of another client, the program adds an I to the given name
      if (newName.equals(cchandler.username) || newName.equals("Server")) {
        newName = newName + "I";
        LOGGER.debug("MS: checkNames-added I to name of new client");
      }
    }
    return newName;
  }

  /** This is method controlled by the servergui to stop the program and to create a new server */
  public void stop() {
    // server doesn't continue
    continueServer = false;
    try {
      // new socket
      for (ClientConnectionHandler cchandler : connectionList) {
        // tries to get the ClientConnectionHandler object from connectionList
        cchandler.writeMsg("CHAT Server shutting down");
        // closes output stream to client
        cchandler.out.close();
        // closes input stream from client
        cchandler.in.close();
        // deletes client from connectionList
        deletePlayer(cchandler.socket);
        cchandler.stopClient();
      }
    } catch (Exception e) {
    }
  }

  /**
   * Display an event like an error to the GUI
   *
   * @param msg the event as a String
   */
  private void display(String msg) {
    // appends the message to the event room of sgui
    // System.out.println("display");
    // sgui.appendEvent(msg + "\n");
  }

  /**
   * This method is a broadcast to all clients who are connected to the server. Is used especially
   * for the chat.
   *
   * @param message includes the message which should be broadcasted to all clients
   */
  synchronized void broadcast(String message) {
    // display message on Server GUI
    /*sgui.appendRoom(message + "\n");*/
    // we loop in reverse order to check whether there is a client who is not active anymore due to
    // a disconnection for example
    // for(int i = connectionList.size(); --i >= 0;)
    for (int i = 0; i < connectionList.size(); i++) {
      // tries to get the ClientConnectionHandler object from connectionList
      ClientConnectionHandler cchandler = connectionList.get(i);
      // try to write to the Client, if this fails, that would mean that the client is inactive. As
      // consequence, the client will be removed
      if (!cchandler.writeMsg("CHAT " + message)) {
        // removes from arraylist
        connectionList.remove(i);
        display("Disconnected Client " + cchandler.username + " removed from list.");
      }
    }
    LOGGER.debug("MS: broadcast-sended broadcast message: " + message);
  }

  /**
   * This method is a chat to all clients who are connected to the server, but not connected to a
   * lobby. Is used especially for the chat.
   *
   * @param message includes the message which should be broadcasted to all clients who are not
   *     connected to a lobby or to all clients who are connected in the same lobby
   */
  synchronized void chat(String message) {
    // display message on GUI
    /*sgui.appendRoom(message + "\n");*/
    // for-loop to get all clients
    for (int i = connectionList.size(); --i >= 0; ) {
      ClientConnectionHandler cchandler = connectionList.get(i);
      // try to only write to clients who are not in a lobby

      if (!cchandler.inLobby) {
        cchandler.writeMsg("CHAT " + message);
      }
    }
    LOGGER.debug("MS: chat-sended chat message: " + message);
  }

  /**
   * This method is a private chat to a specific client.
   *
   * @param message includes the message which should be sended to specific client
   */
  synchronized void whisperChat(String message, String name) {

    // display message on GUI
    /* sgui.appendRoom(message + "\n"); */
    // for-loop to get client
    for (ClientConnectionHandler cchandler : connectionList) {
      // try to write to the client with the given username
      if (cchandler.username.equals(name)) {
        cchandler.writeMsg("CHAT " + message);
        break;
      }
    }
    LOGGER.debug("MS: whisperChat-sended whisper chat message");
  }

  /**
   * This method deletes a client from the arraylist
   *
   * @param client this is the socket
   */
  synchronized void deletePlayer(Socket client) {

    // goes through the arraylist until it finds the right thread which should be deleted
    for (int i = 0; i < connectionList.size(); i++) {
      // tries to get the ClientConnectionHandler object from connectionList
      ClientConnectionHandler cchandler = connectionList.get(i);
      if (cchandler.socket == client) {
        // got right client
        // removes client
        connectionList.remove(i);
        playerList.remove(i);
        return;
      }
    }
    LOGGER.debug("MS: deletePlayer-deleted player from server");
  }

  /**
   * This class saves all connected clients as threads. Furthemore, it communicates with the clients
   * and validates their inputs. This class compares the inputs with the protocol commands and
   * executes them.
   */
  class ClientConnectionHandler extends Thread {

    /** the ingoing stream */
    InputStream in;

    BufferedReader bu;
    /** outgoing stream */
    OutputStream out;
    /** the socket */
    private Socket socket;
    /** the Username of the client */
    private String username;
    /** the group the client wants to play in */
    private String groupname;
    /** boolean to check whether client is in a lobby */
    private boolean inLobby;
    /** object of class player */
    private Player player;
    /** boolean which can stop thread */
    private volatile boolean keepGoing;

    /**
     * This method is the constructor.
     *
     * @param socket the socket of the client
     */
    ClientConnectionHandler(Socket socket) {

      this.socket = socket;
      this.inLobby = false;
      this.groupname = "NotInAGroup";
      try {
        // initialization of output and input streams
        out = this.socket.getOutputStream();
        in = this.socket.getInputStream();
        bu = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        // the very first message from the client is the name of the client
        String uncheckedName = bu.readLine();
        // checks whether the given name already exists in method checkNames
        this.username = checkNames(uncheckedName);
        // creates object of player
        this.player = new Player(this.socket, this.username, this.groupname, out);
        // adds player to playerList
        MultipleServer.playerList.add(this.player);
        // print info
        /*display(this.username + " just connected.");*/
        chat(this.username + " just connected.");
      } catch (IOException e) {
        /*display("Exception creating new Input/output Streams: " + e);*/
      }
    }

    /**
     * This is the run method which should run all time until the client closes the connection to
     * the server. Furthermore, it reads all ingoing messages/commands from the client and tries to
     * find out the right protocol command and executes it.
     */
    public void run() {

      String oldName = this.username;
      try {
        writeMsg("CHAT Server: Welcome to my Server!");
        writeMsg("CHAT Server: Your name will be " + this.username);
        writeMsg("CHAT Server: You are in the Group " + this.groupname);
        keepGoing = true;
        while (keepGoing) {
          oldName = this.username;
          // tries to save the ingoing message in a String
          String line = bu.readLine();
          // split the input to get the very first word which should be the protocol command
          String[] command = line.split(" ");
          // tries to find the command and executes the command
          LOGGER.debug(line);
          LOGGER.debug(oldName);
          switch (Protocol.valueOf(command[0])) {
            case LOBBY:
              // command[0] = "LOBBY", the rest will be ignored.

              showLobby();
              break;

            case CREATEGROUP:
              // command[0] = "CREATEGROUP", command[1] = name of group which should
              // be created, the rest will be ignored.

              // tries to get the name of the group
              String groupName = command[1];
              // tries to find out whether there is already a group with the same name
              for (Group g : groupList) {
                if (g.getGroupName().equals(groupName)) {
                  // if there is a group with the same name, add letter I
                  groupName = groupName + "I";
                }
              }
              // creates group
              Group createdGroup = new Group(groupName);
              // adds client as a player to that group
              createdGroup.groupMembers.add(this.player);
              // sets groupname of client
              this.groupname = groupName;
              // adds group to groupList
              MultipleServer.groupList.add(createdGroup);
              this.inLobby = true;
              chat(
                  this.username
                      + " created Group: "
                      + MultipleServer.groupList.get(0).getGroupName());
              break;

            case JOINGROUP:
              // command[0] = "JOINGROUP", command[1] = name of the already existing group
              // the rest will be ignored

              // tries to get the name of the group
              String group = command[1];
              int count = 0;
              // tries to find out, if there is a group with the same name
              while (count < MultipleServer.groupList.size()) {
                // gets group from the groupList
                Group g = MultipleServer.groupList.get(count);
                if (group.equals(g.getGroupName())) {
                  // found group, adds player to that group
                  g.groupMembers.add(this.player);
                  // sets inLonny true to show that client is in a Group
                  this.inLobby = true;
                  break;
                }
                count++;
              }
              if (count == MultipleServer.groupList.size()) {
                // the group doesn't exist
                writeMsg("CHAT Server: The given groupname does not exist.");
              } else {
                // group does exist
                writeMsg("CHAT You joined the group: " + group);
                this.groupname = group;
                for (int i = 0; i < groupList.size(); i++) {
                  Group p = MultipleServer.groupList.get(i);
                  if ((p.getGroupName()).equals(group)) {
                    // checks whether the group size is equal to 4
                    if (p.getGroupSize(p) == 4) {
                      p.setStatus("inGame");
                      // if so, start the game
                      p.startGame();
                    }
                    break;
                  }
                }
              }
              break;

            case CLOSE:

              // command[0] = CLOSE

              if (this.inLobby == true) {
                // client was already in a group
                for (Group q : MultipleServer.groupList) {
                  // trying to find the group
                  if (q.groupName.equals(this.groupname)) {
                    for (int i = 0; i < q.groupMembers.size(); i++) {
                      // trying to get plyer's id
                      if (q.groupMembers.get(i).getName().equals(this.username)) {
                        // activating method connectionLoss in class Group
                        q.connectionLoss(i + 1);
                      }
                    }
                  }
                }
              }
              this.inLobby = false;
              break;


            case STATE:
              // command[0] = "STATE", the rest will be int[] fields, boolean[] blockade,
              // int[] cage, int[][] goal and int numberOfPlayers as String
              System.out.println(line);
              for (Group gr : groupList) {
                if (gr.groupName.equals(this.groupname)) {
                  gr.setState(line);
                }
              }
              break;

            case PLAYEDCARDS:
              // command[0] = "PLAYEDCARDS", the rest will playedCards as String
              System.out.println(line);
              for (Group gr : groupList) {
                if (gr.groupName.equals(this.groupname)) {
                  gr.setPlayedCards(line);
                }
              }
              break;

            case GETLOBBY:
              // command[0] = "GETLOBBY", the rest will be a String of all the GroupNames connected
              // to the server
              StringBuilder groupstr = new StringBuilder();
              groupstr.append("GETLOBBY ");
              for (int i = 0; i < groupList.size(); i++) {
                Group gr = MultipleServer.groupList.get(i);
                if (gr.getStatus().equals("open")) {
                  groupstr.append(gr.groupName).append(" ");
                }
              }
              writeMsg(groupstr.toString());
              break;

            case HAND:
              // command[0] = "HAND", the rest can be command[1] = null or the hand of the Player
              // and his id as String
              for (Group gro : groupList) {
                if (gro.groupName.equals(this.groupname)) {
                  gro.setMyHand(line);
                }
              }
              break;

            case CHAT:
              // command[0] = "CHAT", the rest: the message

              // tries to get the message
              StringBuilder message = new StringBuilder();
              for (int j = 1; j < command.length; j++) {
                message.append(command[j] + " ");
              }

              if (!this.inLobby) {
                // broadcasts the message
                chat(this.username + ": " + message.toString());
              } else {
                for (Group q : MultipleServer.groupList) {
                  if (q.groupName.equals(this.groupname)) {
                    q.sendCommand("CHAT " + this.username + " : " + message.toString());
                  }
                }
              }
              break;

            case BROADCAST:
              // command[0] = "BROADCAST", the rest: the message

              // tries to get the message
              StringBuilder bcMessage = new StringBuilder();
              for (int j = 1; j < command.length; j++) {
                bcMessage.append(command[j] + " ");
              }
              // broadcasts the message to all clients
              broadcast(this.username + ": " + bcMessage.toString());
              break;

            case CHANGE:
              // command[0] = "CHANGE", command[1] = the new unchecked name,
              // the rest will be ignored

              String clientName = "";
              try {
                // tries to get the name
                clientName = command[1];
                // checks whether the name already exists
                this.username = checkNames(clientName);
                // broadcast the change
                broadcast(oldName + " changed his/her name to " + this.username);
                // saves the change also in player
                this.player.clientName = this.username;
              } catch (ArrayIndexOutOfBoundsException e) {
                writeMsg("CHAT Server: *private* socketArrayList-IndexOutOfBounds");
              }
              break;

            case WHISPER:
              // command[0] = "WHISPER", command[1] = name of client who should get the message,
              // the rest: the message

              // second input must be the name
              try {
                String nameOfGetter = command[1];

                // StringBuilder to get message
                StringBuilder getMessage = new StringBuilder();
                // for-loop to get message
                for (int j = 2; j < command.length; j++) {
                  getMessage.append(command[j] + " ");
                }

                String msg = this.username + ": *private* " + getMessage.toString();
                whisperChat(msg, nameOfGetter);
              } catch (ArrayIndexOutOfBoundsException e) {
                writeMsg("CHAT Server: *private* ArrayIndexOutOfBoundsException");
              }
              break;

            case LOGOUT:
              // command[0] = "LOGOUT", the rest will be ignored.

              // get username
              String name = this.username;
              this.username = "";
              // deletes client from array list
              deletePlayer(this.socket);
              // broadcast the message that the client terminated the connection to the server
              broadcast("Connection to " + name + " is terminated");

              try {
                // closes connection to client
                this.socket.close();
              } catch (IOException e) {
              }
              break;

            case GETOUT:
              this.inLobby = false;
              break;

            case DEFAULT:
              writeMsg("CHAT Server: This command can not be executed by the server.");
              break;
          }
          display(this.username + " tried to do the command:" + command[0]);
        }
      } catch (SocketException e) {
        if (!(this.username.equals(""))) {
          try {
            if (this.inLobby == true) {
              // client was already in a group
              for (Group q : MultipleServer.groupList) {
                // trying to find the group
                if (q.groupName.equals(this.groupname)) {
                  for (int i = 0; i < q.groupMembers.size(); i++) {
                    // trying to get plyer's id
                    if (q.groupMembers.get(i).getName().equals(this.username)) {
                      // activating method connectionLoss in class Group
                      q.connectionLoss(i + 1);
                    }
                  }
                }
              }
            }
            deletePlayer(this.socket);
            // unexpected connection loss
            broadcast(oldName + " lost the connection to this Server.");
            display(oldName + " lost the connection to this Server.");
            this.out.close();
            this.in.close();
            this.bu.close();
            // deletes player from the arraylist
            this.socket = null;
          } catch (IndexOutOfBoundsException iE) {
          } catch (IOException ioE) {
          }
        }
      } catch (NullPointerException e) {
      } catch (IOException e) {
      }
      // deletes client from the arraylist and closes the connection
      // deletePlayer(this.socket);
      close();
    }

    /** This method closes the input/output stream and the connection to the client */
    private void close() {
      // tryies to close the connection
      try {
        if (out != null) {
          this.out.close(); // closes outgoing stream
        }
      } catch (Exception e) {
      }
      try {
        if (in != null) {
          this.in.close(); // closes ingoing stream
        }
      } catch (Exception e) {
      }
      try {
        if (socket != null) {
          this.socket.close(); // closes connection to client
        }
      } catch (Exception e) {
      }
    }

    /** This is a method which lists all connected clients to the server */
    private synchronized void showLobby() {
      StringBuilder sb = new StringBuilder();
      sb.append("LOBBY Player List: ");
      for (Player p : playerList) {
        sb.append(p.getName()).append(" ");
      }
      // writes playerlist to client
      writeMsg(sb.toString());
      StringBuilder s = new StringBuilder();
      s.append("LOBBY Group List: ");
      ArrayList<Group> groupArrayList = MultipleServer.groupList;
      for (Group g : groupArrayList) {
        s.append(g.groupName).append("(").append(g.status).append(")").append(": ");
        ArrayList<Player> groupmembers = g.getGroupMembers();
        for (Player member : groupmembers) {
          s.append(member.getName()).append(" ");
        }
      }
      writeMsg(s.toString());

      StringBuilder str = new StringBuilder();
      str.append("LOBBY HighScore List: ");

      try {
      HighScore highScore = new HighScore();
      String highScoreList = highScore.getHighScoreList();
      LOGGER.debug("MultipleServer HighScoreList" + highScoreList);
      str.append(highScoreList);
      LOGGER.debug("MultipleServer HighScoreList StringBuilder " + str.toString());
      writeMsg(str.toString());
      } catch (IOException ioe) {
        writeMsg("CHAT Server: error sending high score list");
      }
    }

    /** This method setts the boolean keepgoing to false */
    public void stopClient() {
      keepGoing = false;
    }

    /**
     * This method sends a given message to the client
     *
     * @param msg the given message
     * @return true when program could send the message false when client isn't connected anymore
     */
    private synchronized boolean writeMsg(String msg) {
      // if Client isn't connected, close the connection
      if (!socket.isConnected()) {
        // if client isn't connected anymore to the server, the server closes the connection and
        // deletes the client from the arrayList
        close();
        deletePlayer(this.socket);
        return false;
      }
      // writes the message to the client
      try {
        out.write(msg.getBytes());
        out.write('\r');
        out.write('\n');
      } catch (IOException e) {
        // catches Exception that the message could not be written to the client
        String name = this.username;
        deletePlayer(this.socket);
        /*display("Error sending message to " + name);*/
        /*display(name + " lost connection.");*/
        broadcast(name + " lost connection.");

        try {
          // close output stream
          this.out.close();
          // close input stream
          this.in.close();
          // close buffered reader
          this.bu.close();
          // close connection to client
          this.socket = null;
          this.socket.close();
        } catch (IOException ioE) {
          /*display("Noooooo");*/
        } catch (NullPointerException ne) {
        }
        return false;
      } catch (Exception e) {
        // could not write to client
        String name = this.username;
        // as consequence, the client will be deleted from server
        deletePlayer(this.socket);
        // info message
        /*display("Error sending message to " + name);*/
        /*display(name + " lost connection.");*/
        broadcast(name + " lost connection.");

        try {
          // tries to close output stream
          this.out.close();
          // tries to close input stream
          this.in.close();
          // tries to close buffered reader
          this.bu.close();
          // tries to close socket
          this.socket.close();
        } catch (IOException ioE) {
          /*display("Noooooo");*/
        } catch (NullPointerException ne) {
        }
      }
      return true;
    }
  }
}
