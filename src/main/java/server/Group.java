package server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import general.cards.Card;
import general.cards.Hand;

/** Class to form the group */
public class Group {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(Group.class);

  /** PlayerList */
  ArrayList<Player> groupMembers;

  /** Group name */
  String groupName;

  /** status of group */
  String status;

  /** reference to the rulesServer object it is associated with */
  private RulesServer rs;

  /**
   * constructor for Group
   *
   * @param groupName the name given to the group and used to reference it in char
   */
  public Group(String groupName) {

    // sets given group name
    this.groupName = groupName;
    // creates new ArrayList for the players
    this.groupMembers = new ArrayList<Player>();
    // status of the group
    this.status = "open";
  }

  /**
   * This method returns the groupname
   *
   * @return groupName name of the group
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * This method returns the ArrayList including all players of the group
   *
   * @return groupMembers ArrayList including all players of the group
   */
  public ArrayList<Player> getGroupMembers() {
    return this.groupMembers;
  }

  /**
   * This method returns the size of the group
   *
   * @param g Group
   * @return g.groupMembers.size() size of group
   */
  public int getGroupSize(Group g) {
    return g.groupMembers.size();
  }

  /**
   * this method sets the new group status "open", "inGame", "closed"
   *
   * @param newStatus the new Status of the group
   */
  public void setStatus(String newStatus) {
    this.status = newStatus;
  }

  /**
   * this method empties the group after the game has a winner
   *
   * @param group group to empty
   */
  public void emptyGroup(Group group) {
    for (int i = group.groupMembers.size() - 1; i >= 0; i--) {
      // setting group name
      group.groupMembers.get(i).setGroupName("NotInAGroup");
      // removing player from group
      group.groupMembers.remove(i);
    }
  }

  /**
   * sends the state or the chat to all the groupmembers
   *
   * @param line includes all informations about the state or the chat message
   */
  public void sendCommand(String line) {

    LOGGER.debug("group " + line);
    LOGGER.debug("GR: sendCommand(LINE), length: " + line.length());

    // counting amount of spaces
    int countSpace = 0;
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) == ' ') {
        countSpace++;
      }
    }

    LOGGER.debug("GR: sendCommand(LINE), spaces " + countSpace);
    if (countSpace > 1) {
      String[] str = line.split(" ");
      // chat message or command to activate cheat code
      if (str[0].equals("CHAT") && str[3].equals("ezwin")) {
        LOGGER.debug("GR: sendCommand(LINE) activated cheating");
        // trying to get name of player
        String userName = str[1];
        int id = 1;
        for (Player groupMember : this.groupMembers) {
          if (groupMember.getName().equals(userName)) {
            sendCommand("CHAT "+ userName +" is a dirty cheater!");
            // found player with same name
            LOGGER.debug("GR: sendCommand(LINE) cheating in if case player id " + id);
            // activating cheat code for the player
            rs.setCheatsAreOn(id);
            LOGGER.debug("GR: sendCommand(LINE) setCheatsAreOn");
          }
          id++;
        }
      } else {
        int count = 1;
        try {
          // tries to find all player through a for loop
          for (Player groupMember : this.groupMembers) {
            // writing message to group members
            groupMember.out.write(line.getBytes());
            groupMember.out.write('\r');
            groupMember.out.write('\n');
            count++;
          }
        } catch (IOException e) {
          e.printStackTrace();
          LOGGER.debug(count);
          connectionLoss(count);
        }
      }
    } else {
      // sends chat messages and the encoded version of the board the the players
      try {
        // tries to find all player through a for loop
        for (Player groupMember : this.groupMembers) {
          groupMember.out.write(line.getBytes());
          groupMember.out.write('\r');
          groupMember.out.write('\n');
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * This method deletes all players from the group and marks the group as cancelled. Is especially
   * called after a connection loss of player
   *
   * @param playerId id of the player who got the connection loss
   */
  public void connectionLoss(int playerId) {
    for (int i = this.groupMembers.size(); i > 0; i--) {
      if (i != playerId) {
        // sending command to all the other players
        sendCommandToOne("ENDGAME 0 0 No_Winner_Due_To_ConnectionLoss", i);
      }
    }
    // setting new status
    this.setStatus("cancelled");
    // deleting all players from group
    emptyGroup(this);
  }

  /**
   * sends the information to only one player choosen by the PlayerId
   *
   * @param line includes the message
   * @param playerId is the number of the player
   */
  public void sendCommandToOne(String line, int playerId) {
    // tries to get the player with the given id
    LOGGER.debug("sendCommandToOne " + line);
    Player p = groupMembers.get(playerId - 1);
    try {
      // tries to write to that player through an output stream
      OutputStream out = p.clientSocket.getOutputStream();
      out.write(line.getBytes());
      out.write('\r');
      out.write('\n');
    } catch (ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method encodes all arrays as a string
   *
   * @param rulesS where the values should be taken from
   */
  public void sendStateToGroupMembers(RulesServer rulesS) {

    // Structure of String: STATE, fields[], blockade[], cage[], goal[][],
    // numberOfPlayers

    // initializing arrays of board
    int[] fields = rulesS.getFields();
    boolean[] blockade = rulesS.getBlockade();
    int[] cage = rulesS.getCage();
    int[][] goal = rulesS.getGoal();
    int numberOfPlayers = rulesS.getNumberOfPlayers();

    // StringBuilder which is used to encode the arrays
    StringBuilder sb = new StringBuilder();
    // first word is the protocol command
    sb.append("STATE ");
    // appends all elements of fields into the StringBuilder
    for (int field : fields) {
      sb.append(field);
      sb.append(" ");
    }
    // appends all elements of blockade into the StringBuilder
    for (boolean b : blockade) {
      sb.append(b);
      sb.append(" ");
    }
    // appends all elements of cage into the StringBuilder
    for (int value : cage) {
      sb.append(value);
      sb.append(" ");
    }
    // appends all elements of goal into the StringBuilder
    for (int i = 0; i < goal.length; i++) {
      for (int j = 0; j < goal[i].length; j++) {
        sb.append(goal[i][j]);
        sb.append(" ");
      }
    }
    // appends numberOfPlayers into the StringBuilder
    sb.append(numberOfPlayers).append(" ");
    // sends StringBuilder as String to method sendCommand
    LOGGER.debug(sb.toString().length());
    LOGGER.debug("GR: sendStateToGroupMembers(RULESSERVER)");

    sendCommand(sb.toString());
  }

  public void sendPlayedCards(Card[] playedCards) {

    // Structure of String: PLAYEDCARDS, id, value and suit of card

    // appends all elements of playedCards including the cards id, value and suit into the
    // StringBuilder
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
    System.out.println(sb.toString());
    // sending command
    sendCommand(sb.toString());
  }

  /**
   * this sends the amount of cards every player has to MultipleServer
   *
   * @param numOfCards array index is player value is amount of Cards in Hand
   */
  public void sendNumOfCards(int[] numOfCards) {

    // Structure of String: NUMOFCARDS, amount of cards in hand for each player

    StringBuilder sb = new StringBuilder();
    sb.append("NUMOFCARDS ");
    for (int numOfCard : numOfCards) {
      sb.append(numOfCard).append(" ");
    }
    sendCommand(sb.toString());
  }

  /**
   * This method gets the essential information of the board from the server Furthermore, it encodes
   * the string to the individual arrays.
   *
   * @param str String with all the information from ccHandler
   */
  public void setState(String str) {
    try {

      // Structure of String: STATE, fields[], blockade[], cage[], goal[][],
      // numberOfPlayers

      int[] fields = new int[64];
      // splits the given string at the spaces
      String[] state = str.split(" ");
      // the first 64 array values after the very first on should be the array values
      // from the array fields
      for (int i = 1; i < 65; i++) {
        fields[i - 1] = Integer.parseInt(state[i]);
      }
      // setts the array fields in RulesServer
      rs.setFields(fields);
      // array of the blockades
      boolean[] blockade = new boolean[5];
      // the next 5 array values should be the array values of the array blockade
      for (int i = 0; i < 5; i++) {
        blockade[i] = Boolean.parseBoolean(state[i + 65]);
      }
      // setts the array blockade in RulesServer
      rs.setBlockade(blockade);
      // array of the blockades
      int[] cage = new int[5];
      // the next 5 array values should be the array values of the array cage
      for (int i = 0; i < 5; i++) {
        cage[i] = Integer.parseInt(state[i + 70]);
      }
      // setts the array cage in RulesServer
      rs.setCage(cage);
      // array of the goals
      int[][] goal = new int[5][4];
      int count = 75;
      // the next 20 array values should be the array values of the array goal
      for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 4; j++) {
          goal[i][j] = Integer.parseInt(state[count]);
          count++;
        }
      }
      // setts the array goal in RulesServer
      rs.setGoal(goal);
      // the last value is the amount of the players
      int numberOfPlayers = Integer.parseInt(state[95]);
      // setts integer numberOfPlayers in RulesServer
      rs.setNumberOfPlayers(numberOfPlayers);

      rs.handOverTurn();
    } catch (ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method gets a String with played cards from the client and sets it in the RulesServer
   *
   * @param str String of played Cards
   */
  public void setPlayedCards(String str) {

    // Structure of String: PLAYEDCARDS, id, value and suit of card

    LOGGER.debug("GR: setPlayedCards()");
    // array of played cards
    String[] state = str.split(" ");
    Card[] playedCards = new Card[(state.length - 1) / 3];
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

    LOGGER.debug("GR: setPlayedCards() has finished setting");
    String s = "";
    if (playedCards != null) {
      for (Card playedCard : playedCards) {
        if (playedCard == null) {
          s = s + "null";
        } else {
          s = s + playedCard.valueToString() + " ";
        }
      }
    } else {
      s = s + "null";
    }
    LOGGER.debug("GR: !!!!!!!!! playedCards is now: " + s);

    // sets the updated array in RulesClient
    rs.setPlayedCards(playedCards);
  }

  /**
   * This method gets a String with a hand of the player and his Id and sets it in the RulesServer
   *
   * @param line String of a hand and the player id
   */
  public void setMyHand(String line) {

    // commands[0] = "HAND", the rest: information about the cards in the hand.

    LOGGER.debug("GR: setMyHand(" + line + ")");

    try {
      // splits string at spaces
      String[] splitString = line.split(" ");
      if (splitString[2].equals("null")) {
        rs.setHand(Integer.parseInt(splitString[1]), null);
      } else {
        // splitString[0] = "HAND", then in three packs:
        // splitString[1] = id of player
        // splitString[i] = id, splitString[i+1] = value, splitString[i+2] = suit
        // for i = 3^k-2 with k = 1,2,....
        // second input is id of player
        int idOfPlayer = Integer.parseInt(splitString[1]);
        // get amount of card in the given hand
        int amountOfCardsInHand = (splitString.length - 1) / 3;
        // create new array to save the cards
        Card[] cardsOfHand = new Card[amountOfCardsInHand];
        int count = 0;

        for (int i = 2; i < splitString.length; i = i + 3) {
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
        Hand hand = new Hand(idOfPlayer, cardsOfHand);
        // sets hand in RulesClient
        rs.setHand(idOfPlayer, hand);
        // String handVal = rs.getPlayerHand().valuesinHandtoString();

        /*
          LOGGER.debug("NC: setMyHand(): handVal = " + handVal);
        */
      }

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println(("ArrayIndexOutOfBoundsException in NewClient/setMyHand"));
    }
  }

  /**
   * This method gets the name of the winner must be called before closing the group, else it throws an IndexOutOfBoundsException
   *
   * @param w index of winner
   * @return the name of the winner
   */
  public synchronized String getWinnerName(int w) {
    if (w <= groupMembers.size()) {
      Player winner = groupMembers.get(w - 1);
      return winner.getName();
    } else {
      return "NameNotFound";
    }
  }

  /**
   * This method returns the status of the Group
   *
   * @return status of the group
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * This method is called by MultipleServer if the group is full. As consequence RulesServer will
   * be started
   */
  public void startGame() {
    // initialize object of RulesServer
    rs = new RulesServer();
    rs.setGroup(this);
    rs.setNumberOfPlayers(getGroupSize(this));
    // start method setup in RulesServer
    rs.setup();
  }
}
