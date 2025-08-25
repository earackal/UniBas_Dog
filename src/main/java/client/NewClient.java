package client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import general.Main;
import general.cards.Card;
import general.cards.Hand;
import general.Protocol;

import java.io.*;
import java.net.*;
import java.lang.*;

/** This class starts a client, who tries to connect to a specific server */
public class NewClient {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(NewClient.class);

  /** contains the socket of the server */
  private Socket socket;
  /** contains the unchecked username of the client */
  private String username;
  /** to use ClientGui */
  private ClientGui cGUI;
  /** ip-address of server */
  private String hostAddress;
  /** port number of server */
  private int port;
  /** outgoing stream to the server */
  private OutputStream out;
  /** ingoing stream from the server */
  private InputStream in;
  /** the buffered reader */
  private BufferedReader br;
  /** object of RulesClient */
  private RulesClient rc;
  /** says whether it is your turn */
  private boolean myTurn;
  /** player ID during the game */
  private int playerID;
  /** players hand during a game */
  private Hand hand;
  /** messages that the client gets, especially used for unit tests */
  public String messageOfServer;
  /** player list that the client gets, especially used for unit tests */
  public String playerListOfServer;
  /** game list that the client gets, especially used for unit tests */
  public String gameListOfServer;
  /** high score list that the client gets, especially used for unit tests */
  public String highScoreListOfServer;
  /** its own GUI object */
  private MainGui mg;

  /** after creating GUI is set to false */
  private boolean isGuiCreated = false;

  /**
   * constructor of NewClient. Tries to connect to a server and maintain the connection.
   *
   * @param hostAddress the String containing the IP-address of the server
   * @param port the number of the port used by the server
   * @param username the name of the client
   * @param cGUI is the object of ClientGui
   */
  public NewClient(String hostAddress, int port, String username, ClientGui cGUI) {

    LOGGER.debug("NC: constructor is called");

    this.hostAddress = hostAddress;
    this.port = port;
    this.username = username;
    this.cGUI = cGUI;
  }

  /** constructor method of NewClient without any parameters */
  public NewClient() {}

  /**
   * This method tries to connect to a server.
   *
   * @return true if the program could connect to a server
   */
  public boolean start() {

    LOGGER.debug("NC: start()");

    try {
      // tries to find the server with an ip-address and port
      socket = new Socket(this.hostAddress, this.port);
      // initialization of the output stream
      out = socket.getOutputStream();
      // initialization of the input stream
      in = socket.getInputStream();
      br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      // creates a new InThread object for the client
      InThread th = new InThread();
      // starts thread
      th.start();
      // sends as first message the username of the client to the server
      out.write(this.username.getBytes());
      out.write('\r');
      out.write('\n');
      display("Client got Connection with Server.");

    } catch (UnknownHostException e) {
      e.printStackTrace();
      display("Such an IP-address does not exist.");
      return false;
    } catch (ConnectException e) {
      e.printStackTrace();
      display("The connection to desired server could not be established.");
      return false;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      display("The formatting of your input is not correct.");
      return false;
    } catch (NullPointerException e) {
      e.printStackTrace();
      display("You did not enter anything.");
      return false;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      display("You entered the wrong port.");
      return false;
    } catch (EOFException e) {
      e.printStackTrace();
      return false;
    } catch (SocketException e) {
      e.printStackTrace();
      display("Connection to server was already terminated.");
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      display("Connection to server was lost");
      return false;
    }
    return true;
  }

  /**
   * To send a message to ClientGui or MainGui
   *
   * @param msg the message which should be appended to ClientGui and MainGui
   */
  private void display(String msg) {
    // appends the message to ClientGui JTextArea
    cGUI.appendEvent(msg + "\n");
    // appends event to MainGui
    if (mg != null) {
      // if game gui already exists
      if (mg.isGuiCreated()) {
        // send message to game gui
        mg.setMessage(msg + "\n");
        mg.appendTextRunnable.run();
      }
    }
  }

  /**
   * This method tries to write the command to the server
   *
   * @param command String which includes the command and all necessary inputs
   */
  public void sendCommandToServer(String command) {

    LOGGER.debug("NC: sendCommandToServer(" + command + ")");

    try {
      out = socket.getOutputStream();
      // trying to write the message to server
      out.write(command.getBytes());
      out.write('\r');
      out.write('\n');
      if (command.equals("LOGOUT")) {
        // closes client because of logout command
        LOGGER.debug("NC: sendCommandToServer-activated disconnect method");
        disconnect();
      }
    } catch (IOException e) {
      e.printStackTrace();
      LOGGER.debug("catch IOEXCEPTION in sendCommand to server");
      display("Exception writing to server: " + e);
    } catch (NullPointerException e) {
      e.printStackTrace();
      LOGGER.debug("NC: sendCommandToServer - got Nullpointerexception");
    }
  }

  /**
   * This method prints out the given lobby list
   *
   * @param str String including the lobby
   */
  public void showLobby(String str) {

    // Structure of String: LOBBY, player list with all players,
    // group list with all groups or high score list

    LOGGER.debug("NC: showLobby(" + str + ")");
    // splits string at whitespace
    String[] split = str.split(" ");
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    if (split[1].equals("HighScore")) {
      LOGGER.debug("NC: showLobby got HighScoreList");
      // 1 turns Kapilas datum 2 turns name datum...
      sb.append(split[1] + " " + split[2] + " \n");
      if(split[3].equals("HighScoreList")) {
        sb.append(split[3] + " " + split[4]);
      } else {
        int length = split.length - 3;
        // trying to decode string to list
        for (int i = 3; i < length; i = i + 4) {
          sb.append(split[i])
              .append(" ")
              .append(split[i + 1])
              .append(" ")
              .append(split[i + 2])
              .append(" ")
              .append(split[i + 3])
              .append("\n");
        }
      }
      LOGGER.debug("NC: showLobby HighScoreList " + sb.toString());
    } else {
      // first two words are group list/ highscore list/ player list
      sb.append(split[1] + " " + split[2] + " \n");
      // tries to get the words in list
      for (int i = 3; i < split.length; i++) {
        sb.append(split[i] + "\n");
      }
    }
    sb.append("\n");
    if (mg != null) {
      // if game gui exists
      if (mg.isGuiCreated()) {
        // write list to game gui
        mg.setMessage(sb.toString() + "\n");
        mg.appendTextRunnable.run();
      }
    }
    // add list to event room
    cGUI.appendEvent(sb.toString());
  }

  /**
   * This method gets the essential information of the board from the server Furthermore, it encodes
   * the string to the individual arrays.
   *
   * @param str String with all the information
   */
  public void setState(String str) {
    // for testing cases
    StringBuilder sb = new StringBuilder();
    LOGGER.debug("NC: setState(" + str + ")");

    // Structure of String: STATE, fields[], blockade[], cage[], goal[][],
    // numberOfPlayers

    try {
      int index = 1;
      LOGGER.debug("The index is " + index);
      if (rc == null) {
        // create a new object of RulesClient
        rc = new RulesClient();
        rc.setNewClient(this);
        rc.setClientGui(cGUI);
      }
      int[] fields = new int[64];
      // splits the given string at the spaces
      String[] state = str.split(" ");
      // the first 64 array values after the very first on should be the array values
      // from the array fields
      for (int i = 0; i < fields.length; i++) {
        fields[i] = Integer.parseInt(state[index]);
        index++; // 1,2,3,4
      }

      LOGGER.debug("setState: Fields has been transferred");
      LOGGER.debug(index);
      sb.append("setState: Fields ");
      // debug
      for (int i = 0; i < fields.length; i++) {
        sb.append(fields[i] + " ");
      }
      LOGGER.debug(sb.toString());

      // sets the array fields in RulesClient
      rc.setFields(fields);
      // array of the blockades
      boolean[] blockade = new boolean[5];
      // the next 5 array values should be the array values of the array blockade
      for (int i = 0; i < blockade.length; i++) {
        blockade[i] = Boolean.parseBoolean(state[index]);
        index++;
      }
      LOGGER.debug("setState: blockade has been transferred");
      LOGGER.debug(index);
      sb = new StringBuilder();
      sb.append("setState: Blockades ");
      // debug
      for (int i = 0; i < blockade.length; i++) {
        sb.append(blockade[i] + " ");
      }
      LOGGER.debug(sb.toString());
      // setts the array blockade in RulesClient
      rc.setBlockade(blockade);
      // array of the blockades
      int[] cage = new int[5];
      // the next 5 array values should be the array values of the array cage
      for (int i = 0; i < cage.length; i++) {
        cage[i] = Integer.parseInt(state[index]);
        index++;
      }
      LOGGER.debug("setState: cage has been transferred");
      LOGGER.debug(index);
      sb = new StringBuilder();
      sb.append("setState: Cage ");
      // debug
      for (int i = 0; i < cage.length; i++) {
        sb.append(cage[i] + " ");
      }
      LOGGER.debug(sb.toString());
      // sets the array cage in RulesClient
      rc.setCage(cage);
      // array of the goals
      int[][] goal = new int[5][4];
      // the next 20 array values should be the array values of the array goal
      for (int i = 0; i < goal.length; i++) {
        for (int j = 0; j < goal[i].length; j++) {
          goal[i][j] = Integer.parseInt(state[index]);
          index++;
        }
      }
      LOGGER.debug("setState: goal has been transferred");
      LOGGER.debug("Index after goal " + index);
      sb = new StringBuilder();
      sb.append("setState: Goal ");
      // debug
      for (int i = 0; i < goal.length; i++) {
        for (int j = 0; j < goal[i].length; j++) {
          sb.append(goal[i][j] + " ");
        }
      }
      LOGGER.debug(sb.toString());
      // setts the array goal in RulesClient
      rc.setGoal(goal);
      // the last value is the amount of the players
      int numberOfPlayers = Integer.parseInt(state[index]);
      index++;
      LOGGER.debug("setState: numberOfPlayers has been transferred");
      LOGGER.debug(index);

      // setts integer numberOfPlayers in RulesClient
      rc.setNumberOfPlayers(numberOfPlayers);
      LOGGER.debug("setState: Number of players " + numberOfPlayers);
      // calls method refreshBoard from class RulesClient
      rc.refreshMarblePos();
    } catch (ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
      System.out.println("Caught ArrayIndexOOB exception in setState()");
    }
  }

  /**
   * This method encodes all arrays as a string to be sent from client to server
   *
   * @param rulesC the RulesClient object containing all the relevant info
   */
  public void sendState(RulesClient rulesC) {

    // Structure of String: STATE, fields[], blockade[], cage[], goal[][],
    // numberOfPlayers

    LOGGER.debug("NC: sendState(RC)");

    // initializing board
    int[] fields = rulesC.getFields();
    boolean[] blockade = rulesC.getBlockade();
    int[] cage = rulesC.getCage();
    int[][] goal = rulesC.getGoal();
    int numberOfPlayers = rulesC.getNumberOfPlayers();

    // StringBuilder which is used to encode the arrays
    StringBuilder sb = new StringBuilder();
    // first word is the protocol command
    sb.append("STATE ");
    // appends all elements of fields into the StringBuilder
    for (int i = 0; i < fields.length; i++) {
      sb.append(fields[i] + " ");
    }
    // appends all elements of blockade into the StringBuilder
    for (int i = 0; i < blockade.length; i++) {
      sb.append(blockade[i] + " ");
    }
    // appends all elements of cage into the StringBuilder
    for (int i = 0; i < cage.length; i++) {
      sb.append(cage[i] + " ");
    }
    // appends all elements of goal into the StringBuilder
    for (int i = 0; i < goal.length; i++) {
      for (int j = 0; j < goal[0].length; j++) {
        sb.append(goal[i][j] + " ");
      }
    }
    // appends numberOfPlayers into the StringBuilder
    sb.append(numberOfPlayers + " ");

    LOGGER.debug("NC: sendState() sends string: " + sb.toString());

    // sends new board to server
    sendCommandToServer(sb.toString());
  }

  /**
   * This method makes a String of the Hand object and sends it to the server
   *
   * @param hand a hand object
   */
  public void sendMyHand(Hand hand) {

    // Structure of String: HAND, id, value and suit of card

    LOGGER.debug("NC: This is the sendMyHand method");
    try {
      if (hand.numOfCards() == 0) {
        // if hand is empty
        LOGGER.debug("NC: sendMyHand() is null");
        sendCommandToServer("HAND " + this.playerID + " null");
      } else {
        LOGGER.debug("NC: sendMyHand(" + hand.valuesInHandToString() + ")");

        StringBuilder sb = new StringBuilder();
        sb.append("HAND " + this.playerID + " ");
        // trying to decode hand
        for (int i = 0; i < hand.numOfCards(); i++) {
          Card c = hand.getCard(i);
          sb.append(c.getId()).append(" ");
          sb.append(c.getValue()).append(" ");
          sb.append(c.getSuit()).append(" ");
        }
        // sends StringBuilder as String to method sendCommand
        sendCommandToServer(sb.toString());
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      LOGGER.debug("NC: sendMyHand-hand is null");
      sendCommandToServer("HAND " + this.playerID + " null");
    }
  }

  /**
   * This method sets the new hand according to info given through protocol
   *
   * @param str String which includes all information about the hand
   */
  public void setMyHand(String str) {

    // Structure of String: HAND, id, value and suit of card

    LOGGER.debug("NC: setMyHand(" + str + ")");

    try {
      // splits string at spaces
      String[] splitString = str.split(" ");

      if (splitString[1].equals("null")) {
        rc.setPlayerHand(null);
      } else {
        // splitString[0] = "HAND", then in three packs:
        // splitString[i] = id, splitString[i+1] = value, splitString[i+2] = suit
        // for i = 3^k-2 with k = 1,2,....

        // get amount of card in the given hand
        int amountOfCardsInHand = (splitString.length - 1) / 3;
        // create new array to save the cards
        Card[] cardsOfHand = new Card[amountOfCardsInHand];
        int count = 0;

        for (int i = 1; i < splitString.length; i = i + 3) {
          // tries to get the id of the card
          int id = Integer.parseInt(splitString[i]);
          // tries to get the value of the card
          int value = Integer.parseInt(splitString[i + 1]);
          // tries to get the suit of the card
          char suit = (splitString[i + 2]).charAt(0);
          // creates card with the given information
          Card c = new Card(id, value, suit);
          // adds card to array
          cardsOfHand[count] = c;
          count++;
        }
        // creates new hand
        hand = new Hand(playerID, cardsOfHand);
        // sets hand in RulesClient
        rc.setPlayerHand(hand);
        // creates MainGui
        if (!isGuiCreated) {
          isGuiCreated = true;
          rc.createMainGui();
        }
        String handVal = rc.getPlayerHand().valuesInHandToString();

        LOGGER.debug("NC: setMyHand(): handVal = " + handVal);
      }

    } catch (ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
      System.out.println(("ArrayIndexOutOfBoundsException in NewClient/setMyHand"));
    }
  }

  /**
   * This method gets the id of the player during a game and sets it in RulesClient
   *
   * @param str string including the player's ID
   */
  public void setIDOfPlayer(String str) {

    LOGGER.debug("NC: setIDOfPlayer(" + str + ")");

    try {
      // Structure splitString[0] = "PLAYERID", splitString[1] = id from player
      // the rest will be ignored.

      String[] splitString = str.split(" ");
      // setting id
      this.playerID = Integer.parseInt(splitString[1]);
      rc.setPlayerNumber(this.playerID);

      LOGGER.debug("NC: setIDOfPlayer " + this.playerID);

    } catch (ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
      System.out.println(("ArrayIndexOutOfBoundsException in NewClient/setIDOfPlayer"));
    }
  }

  /**
   * This method sets the decodes given string to the names of the players
   *
   * @param str String with names of the players
   */
  public void setNamesOfPlayer(String str) {

    // Structure of String: PLAYERNAMES, names of player

    // splits string
    String[] split = str.split(" ");
    // array with names of the player
    String[] playerNames = new String[5];
    playerNames[0] = "";
    for (int i = 1; i < split.length; i++) {
      // tries to get the name
      playerNames[i] = split[i];
    }
    LOGGER.debug("NC setNamesOfPlayer " + str);
    // sends it to RulesClient
    rc.setPlayerNames(playerNames);
  }

  /**
   * This method takes a string of the played Cards from the server and sets the played card field
   * in RulesClient
   *
   * @param str contains the playedCard array as String
   */
  public void setPlayedCards(String str) {

    // Structure of String: PLAYEDCARDS, id, value and suit of card

    LOGGER.debug("NC: setPlayedCards(" + str + ")");

    // array of played cards
    String[] state = str.split(" ");
    Card[] playedCards = new Card[(state.length - 1) / 3];
    LOGGER.debug("NC: string's length: " + playedCards.length);
    LOGGER.debug("NC: The single components of state: ");
    for (int i = 0; i < state.length; i++) {
      LOGGER.debug("\t" + state[i]);
    }
    int index = 0;
    for (int i = 1; i < state.length; i = i + 3) {
      // try to get the id of the card
      int id = Integer.parseInt(state[i]);
      // try to get the value of the card
      int value = Integer.parseInt(state[i + 1]);
      // try to get the suit of the card
      char suit = (state[i + 2]).charAt(0);
      // save card in array
      playedCards[index] = new Card(id, value, suit);
      index++;
    }

    LOGGER.debug("NC: setPlayedCards( ");
    // debug
    for (int i = 0; playedCards != null && i < playedCards.length; i++) {
      LOGGER.debug(playedCards[i].valueToString());
    }
    LOGGER.debug(" )");

    // sets the updated array in RulesClient
    rc.setPlayedCards(playedCards);
  }

  /**
   * this method sets the amount of cards every player has in RulesClient
   *
   * @param str contains an array with amount of cards
   */
  public void setNumOfCards(String str) {

    // Structure of String: NUMOFCARDS, amount of cards in hand for each player

    String[] num = str.split(" ");
    int[] numOfCards = new int[5];
    for (int i = 0; i < num.length - 1; i++) {
      numOfCards[i] = Integer.parseInt(num[i + 1]);
    }
    // sending numOfCards to RulesClient and to GuiUpdater
    rc.setNumOfCards(numOfCards);
    GuiUpdater.setNumOfCards(numOfCards);
    GuiUpdater.refreshPlayerHand(rc);
    // debug
    for (int i = 0; i < numOfCards.length; i++) {
      LOGGER.debug("From setNumOfCards in NewClient: numOfCards is: " + i + " " + numOfCards[i]);
    }
  }

  /**
   * This method returns the object of RulesClient
   *
   * @return rc object of rulesclient
   */
  public RulesClient getRulesClient() {
    return rc;
  }

  /**
   * This method sets its own MainGui object
   *
   * @param mainGui its own MainGui object
   */
  public void setMainGui(MainGui mainGui) {
    this.mg = mainGui;
    // setting frame invisible through calling method
    cGUI.setFrameInvisible();
  }

  /**
   * this method receives a card[] playedCards and form this to a String and sends it to the Server
   *
   * @param playedCards contains the Card array.
   */
  public void sendPlayedCards(Card[] playedCards) {

    // Structure of String: PLAYEDCARDS, id, value and suit of card

    StringBuilder s = new StringBuilder();
    for (int i = 0; i < playedCards.length; i++) {
      s.append(playedCards[i].cardAsString());
    }
    LOGGER.debug("NC: sendPlayedCards(CARDS)");

    try {
      StringBuilder sb = new StringBuilder();
      sb.append("PLAYEDCARDS ");
      if (playedCards != null) {
        for (Card playedCard : playedCards) {
          sb.append(playedCard.getId())
              .append(" ")
              .append(playedCard.getValue())
              .append(" ")
              .append(playedCard.getSuit())
              .append(" ");
        }
      }
      LOGGER.debug("NC: sendPlayerCards " + sb.toString());
      sendCommandToServer(sb.toString());
    } catch (ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
      System.out.println("cards array is wrong");
    }
  }

  /**
   * When there is a disconnect due to unknown reasons, this method closes the Input/Output streams
   * and the socket itself
   */
  private void disconnect() {

    LOGGER.debug("NC: disconnect()");

    try {
      if (in != null) {
        in.close(); // closes InputStream
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      if (out != null) {
        out.close(); // closes OutputStream
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      if (socket != null) {
        socket = null; // closes Socket
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    // informs the GUI that the connection failed
    if (cGUI != null) {
      cGUI.connectionFailed();
    }
    try {
      if (mg != null) {
        mg = null;
      }
      if (rc != null) {
        rc = null;
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  public void setGuiCreated(boolean guiCreated) {
    isGuiCreated = guiCreated;
  }

  /**
   * This is a class, which tries to wait for the message from the server. When it gets messages it
   * appends them to the JTextArea
   */
  class InThread extends Thread {

    /** This method tries to get all ingoing messages and appends them to ClientGui */
    public synchronized void run() {

      LOGGER.debug("NCIT: run()");

      while (true) {
        try {
          // reads all ingoing messages and saves them as a String
          String str = br.readLine();
          // appends String to chat room
          if (str == null) {
            LOGGER.debug("NCIT: run() is disconnecting, str == null");
            sendCommandToServer("LOGOUT");
            cGUI.setFrameVisible();
            disconnect();
            cGUI.connectionFailed();
            break;
          } else {
            String[] commands = str.split(" ");
            switch (Protocol.valueOf(commands[0])) {
              case CHAT:
                // commands[0] = "CHAT", the rest: message

                // print it out as a message
                messageOfServer = str.substring(5, str.length());
                cGUI.append(str.substring(5, str.length()) + "\n");
                if (mg != null) {
                  // if game gui exists
                  if (MainGui.isGuiCreated()) {
                    // sending message to game gui
                    mg.setMessage(messageOfServer + "\n");
                    mg.appendTextRunnable.run();
                  }
                }
                break;

              case STATE:
                // commands[0] = "STATE", the rest = array values as String
                // for more information, please look at the method setState

                LOGGER.debug("newClient " + str);
                LOGGER.debug(str.length());

                // start method setState to get the updated arrays
                setState(str);
                break;

              case YOURTURN:
                // commands[0] = "YOURTURN", the rest will be ignored.

                // starting thread which carries communication between client
                // and gui
                YourTurnThread yourTurnThread = new YourTurnThread();
                yourTurnThread.start();
                break;

              case WHOSETURN:
                //  commands[0] = "WHOSETURN", commands[1] = idOfCurrentPlayer

                int id = Integer.parseInt(commands[1]);
                // setting id
                rc.setWhoseTurnID(id);
                MainGui.setWhoseTurnID(id);
                if (MainGui.isGuiCreated()) {
                  LOGGER.debug("It is " + id + " player's turn from NC");
                  MainGui.whoseTurnRunnable.run();
                }
                break;

              case PLAYERNAMES:
                // commands[0] = "PLAYERNAMES", the rest: names of the players

                setNamesOfPlayer(str);
                break;

              case GETLOBBY:
                // commands[0] = "GETLOBBY", the rest: name of the already existing groups

                cGUI.appendEvent(str);
                break;

              case HAND:
                // commands[0] = "HAND", the rest: information about the cards in the hand.
                // for more information, please look at the method setMyHand

                LOGGER.debug("received hand is: " + str);
                // start method setMyHand to get the given hand
                setMyHand(str);
                break;

              case PLAYERID:
                // commands[0] = "PLAYERID", commands[1] = id
                // the rest will be ignored

                // start method setIDOfPlayer to set the player's id
                setIDOfPlayer(str);
                break;

              case ENDGAME:
                // commands[0] = "ENDGAME", command[1] = amount of turns, command[3] = name of winner
                isGuiCreated = false;
                String name = commands[3];
                int turns = Integer.parseInt(commands[1]);
                // sending name of the winner to game gui
                GuiInteraction.setWinner(name);
                MainGui.playerHasWonRunnable.run();
                // sending command to server to set object inLobby to false
                sendCommandToServer("GETOUT ");
                break;

              case LOBBY:
                // commands[0] = "LOBBY", the rest: list and its information
                // could be player list, group list or high score list

                String[] getLobby = str.split(" ");
                if (getLobby[1].equals("Player")) {
                  // got player list
                  playerListOfServer = str;
                } else if (getLobby[1].equals("Group")) {
                  // got group list
                  gameListOfServer = str;
                } else {
                  // got high score list
                  highScoreListOfServer = str;
                }
                showLobby(str);
                break;

              case PLAYEDCARDS:
                // commands[0] = "PLAYEDCARDS", the rest: information about the cards
                // which are already played.

                setPlayedCards(str);
                break;

              case NUMOFCARDS:
                // commands[0] = "NUMOFCARDS", the rest: amounts of cards in hand per each player

                setNumOfCards(str);

                if (mg.isGuiCreated()) {
                  // calls refreshOtherCardsRunnable to update cards on the board in JavaFX Thread
                  mg.refreshOtherCardsRunnable.run();
                }
                break;

              default:
                break;
            }
          }
        } catch (SocketException e) {
          e.printStackTrace();
          display("Server has closed the connection. Please try again.");
          disconnect();
          break;
        } catch (IOException e) {
          e.printStackTrace();
          display("Server has closed the connection. Please try again");
          if (cGUI != null) {
            cGUI.connectionFailed();
          }
          disconnect();
          break;
        }
      }
    }
  }

  /**
   * this thread carries out the communication between the user and the program during that user's
   * turn
   */
  class YourTurnThread extends Thread {
    /* starts the turn of the player */
    public synchronized void run() {
      rc.startTurn();
    }
  }
}
