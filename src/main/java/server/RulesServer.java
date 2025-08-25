package server;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import client.MainGui;
import general.cards.Card;
import general.cards.Deck;
import general.cards.Hand;

/**
 * contains most of the things that make the game run on the server's side contains data structures
 * representing the game state and functions to change or read them
 */
public class RulesServer {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(RulesServer.class);

  /**
   * fields contains all the places on the board (except for goal and cage) the number determines
   * whose player's marble is on the spot, 0 is empty
   */
  private int[] fields = new int[64];

  /**
   * cage contains the number of marbles which are outside the game position (player number) in the
   * array contains the number of marbles in the player's cage, usage: cage[player];
   */
  private int[] cage = new int[5];

  /** goal contains the 16 positions of the goals. usage is goal[player][pos]; */
  private int[][] goal = new int[5][4];

  /** is one at position of player's number if player has blocking marble */
  private boolean[] blockade = new boolean[5];

  /** number of players is determined via input at start */
  private int numberOfPlayers = 0;

  /** contains the start points of each player, usage: startingPoint[player]; */
  private final int[] startingPoint = {0, 0, 16, 32, 48};

  /** counts the number of times cards have been dealt */
  private int turnNumber = 1;

  /** contains the deck used */
  private Deck deck = new Deck(this);

  /** the players hands, usage: hand[player] */
  private Hand[] hands = new Hand[5];

  /** keeps track of cards that have been played, reset when dealing new hands */
  private Card[] playedCards;

  /** contains the amount of Cards every player has */
  private int[] numOfCards = new int[5];

  /** the object needed for communication */
  private Group gr;

  /** contains the number of the player whose turn it is */
  private int playerAtTurn;

  /** contains the index of the player */
  public int winner = 0;

  /** determines if certain player has activated cheats */
  public boolean[] cheatsAreOn = new boolean[5];

  /** constructor for rulesServer */
  public RulesServer() {

    LOGGER.debug("RS: constructor is called");

    // fill the cages
    for (int i = 1; i < cage.length; i++) {
      cage[i] = 4;
    }
    // clear the board
    Arrays.fill(fields, 0);

    // clear blockades
    Arrays.fill(blockade, false);

    // clear the goals
    for (int[] ints : goal) {
      Arrays.fill(ints, 0);
    }
  }

  /**
   * sets up the board for a game to begin clears all the fields, and puts one marble out if
   * necessary
   */
  public void setup() {

    LOGGER.debug("RS: setup()");

    // one marble is moved out for each player
    for (int i = 1; i <= numberOfPlayers; i++) {
      moveOut(i);
    }

    gr.sendStateToGroupMembers(this);
    gr.sendPlayedCards(playedCards);
    sendPlayerNames();

    for (int i = 1; i < gr.getGroupSize(gr) + 1; i++) {
      String out = "PLAYERID " + i;
      gr.sendCommandToOne(out, i);
    }

    // deal first hand
    dealCards();

    for (int i = 0; i < numOfCards.length; i++) {
      LOGGER.debug(i + " Number of cards: " + numOfCards[i]);
    }
    LOGGER.debug("");

    // send game state
    for (int i = 1; i < hands.length; i++) {
      setPlayerHand(i, hands[i]);
      if (i == 1) {
        LOGGER.debug("setPlayerHand was run");
      }
    }

    for (int i = 0; i < numOfCards.length; i++) {
      LOGGER.debug(i + " Number of cards: " + numOfCards[i]);
    }
    LOGGER.debug("");
    // send amount of cards
    sendNumOfCards();
    // start time measuring
    playerAtTurn = 1;
    callForTurn(playerAtTurn);
  }

  /**
   * checks if own goal is full
   *
   * @param playerNumber id of player
   * @return true if own goal is full
   */
  public boolean goalIsFull(int playerNumber) {

    boolean isFull = true;
    for (int i = 0; i < goal[playerNumber].length; i++) {
      if (goal[playerNumber][i] == 0) {
        isFull = false;
      }
    }
    return isFull;
  }

  /**
   * used when marble gets out of cage might eat other marble, also sets up a blockade
   *
   * @param player number of the player whose marble gets out of its cage
   */
  public void moveOut(int player) {

    LOGGER.debug("RS: moveOut()");

    int victim = fields[startingPoint[player]];
    if (victim != 0) {
      cage[victim]++;
    }
    fields[startingPoint[player]] = player;
    cage[player]--;
    blockade[player] = true;
  }

  /**
   * deals fixed amount of cards to players called after all cards have been played, uses turnNumber
   * to determine amount of cards to be dealt
   */
  public void dealCards() {

    LOGGER.debug("RS: dealCards()");
    int amountOfCards = 6;
    int t = turnNumber;
    LOGGER.debug("RS: dealCards - turnNumber = " + turnNumber);
    while (t > 1) { // determine number of cards to be dealt
      if (amountOfCards > 2) {
        amountOfCards--;
      } else {
        amountOfCards = 6;
      }
      t--;
    }
    turnNumber++;
    for (int i = 1; i < numberOfPlayers; i++) {
      this.numOfCards[i] = amountOfCards;
    }
    LOGGER.debug(amountOfCards + "dealt");
    deck.dealCards(amountOfCards);
  }

  /** gets called after player ends their turn, calls next player's turn */
  public synchronized void handOverTurn() {

    LOGGER.debug("RS: handOverTurn()");

    int winner = hasWon();
    if (winner != 0) { // somebody has won
      String winnerString = getWinner();
      LOGGER.debug("winnerString " + winnerString);
      gr.sendStateToGroupMembers(this);
      LOGGER.debug("after sendStateToGroupMembers");
      gr.sendPlayedCards(playedCards);
      LOGGER.debug("after sendPlayedCards");
      gr.sendNumOfCards(numOfCards);
      LOGGER.debug("after sendNumOfCards");
      System.out.println("Player " + winner + " has won!");
      gr.setStatus("closed");
      LOGGER.debug("after setStatus");
      try {
        HighScore hs = new HighScore();
        LOGGER.debug("after HighScore constructor");
        hs.addScore(winnerString);
        LOGGER.debug("after hs.addScore()");
      } catch (IOException ioe) {
        LOGGER.debug("couldnt start highscore or write in txt");
      }
      try {
        wait(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      LOGGER.debug("after 200 milliSeconds later");
      sendEndGame();
      LOGGER.debug("after sendEndgame()");
      gr.emptyGroup(gr);
      LOGGER.debug("after emptyGroup()");
    } else { // game continues
      System.out.println("RS: handOverTurn-Player at turn before = " + this.playerAtTurn);
      // int playerAtTurn = (this.playerAtTurn + 1) % numberOfPlayers;
      int atTurn = (this.playerAtTurn + 1) % (numberOfPlayers + 1);
      this.playerAtTurn = atTurn;
      System.out.println("RS: handOverTurn-Player at turn after = " + this.playerAtTurn);
      if (this.playerAtTurn == 0) {
        this.playerAtTurn = 1;
      }

      // send game state
      for (int i = 0; i < numOfCards.length; i++) {
        LOGGER.debug(i + " Number of cards before setPlayerHand: " + numOfCards[i]);
      }
      LOGGER.debug("");
      setPlayerHand(this.playerAtTurn, hands[this.playerAtTurn]);
      for (int i = 0; i < numOfCards.length; i++) {
        LOGGER.debug(i + " Number of cards after sendNumOfCards: " + numOfCards[i]);
      }
      LOGGER.debug("");
      gr.sendStateToGroupMembers(this);
      sendPlayerNames();
      gr.sendPlayedCards(playedCards);
      boolean aHandHasCards = false;
      for (int i = 1; i <= numberOfPlayers; i++) {
        LOGGER.debug("RS: handoverTurn start for-loop");
        try {
          aHandHasCards = (aHandHasCards || hands[i].containsCards());
        } catch (NullPointerException e) {
          continue;
        }
      }
      if (!aHandHasCards) { // all hands are empty
        dealCards();
      }
      for (int i = 0; i < numOfCards.length; i++) {
        LOGGER.debug(i + " Number of cards before sendNumOfCards: " + numOfCards[i]);
      }
      LOGGER.debug("");
      gr.sendNumOfCards(numOfCards);

      callForTurn(this.playerAtTurn);
    }
  }

  /**
   * calls the player to make their turn via protocol
   *
   * @param player number of the player
   */
  public void callForTurn(int player) {

    LOGGER.debug("RS: callForTurn(" + player + ")");

    gr.sendCommandToOne("YOURTURN", player);
    gr.sendCommand("WHOSETURN " + player);
  }

  /**
   * determines if a player/team has won
   *
   * @return returns number of player who has won or 0 (if teams exist, returns number of one of the
   *     winning members)
   */
  public int hasWon() {

    LOGGER.debug("RS: hasWon()");

    boolean[] isFull = new boolean[numberOfPlayers + 1];
    isFull[0] = false;
    /* counts whose goal is full */
    for (int player = 1; player <= numberOfPlayers; player++) {
      isFull[player] = goalIsFull(player);
    }
    /* checks whose goal is full
    if they have a partner, their goal is checked too
    if full, they win
     */
    for (int player = 1; player <= numberOfPlayers; player++) {
      if (isFull[player]) {
        winner = player;
        return player;
      }
    }
    return 0;
  }

  /**
   * sets player's hand to hand given, uses the protocol
   *
   * @param player the number of the respective player
   * @param hand the hand it should be set to
   */
  public void setPlayerHand(int player, Hand hand) {

    try {
      if (!hand.containsCards()) {
        hands[player] = null;
        numOfCards[player] = 0;
        gr.sendCommandToOne("HAND null", player);
      } else {

        LOGGER.debug("RS: setPlayerHand(" + player + "," + hand.valuesInHandToString() + ")");

        hands[player] = hand;
        numOfCards[player] = hand.numOfCards();
        StringBuilder sb = new StringBuilder();
        sb.append("HAND ");
        for (int i = 0; i < hand.numOfCards(); i++) {
          Card c = hand.getCard(i);
          sb.append(c.getId()).append(" ");
          sb.append(c.getValue()).append(" ");
          sb.append(c.getSuit()).append(" ");
        }
        String handString = sb.toString();

        LOGGER.debug("RS: sent hand string is: " + handString);

        gr.sendCommandToOne(handString, player);
      }
    } catch (NullPointerException e) {
      hands[player] = null;
      gr.sendCommandToOne("HAND null", player);
    }
  }

  /**
   * This mehtod setts the hand of a player
   *
   * @param id id of player
   * @param hand new updated hand of player
   */
  public void setHand(int id, Hand hand) {
    try {
      if (!hand.containsCards()) {
        // if hand has no cards
        // hand is null
        hands[id] = null;
        // no cards left
        numOfCards[id] = 0;
      } else {
        hands[id] = hand;
        numOfCards[id] = hands[id].numOfCards();
      }
    } catch (NullPointerException e) {
      hands[id] = null;
      numOfCards[id] = 0;
    }
  }

  /**
   * returns the hand of selected player
   *
   * @param player the player whose hand is desired
   * @return hand object
   */
  public Hand getPlayerHand(int player) {
    return hands[player];
  }

  /**
   * sets numberOfPlayers to desired value
   *
   * @param num the number of players
   */
  public void setNumberOfPlayers(int num) {
    numberOfPlayers = num;
  }

  /**
   * sets the player's goal to newGoal, typically called via protocol
   *
   * @param newBlockade the new state of the blockade
   */
  public void setBlockade(boolean[] newBlockade) {
    blockade = newBlockade;
  }

  /**
   * sets the fields array, typically called from server via protocol
   *
   * @param newFields the new state of the fields
   */
  public void setFields(int[] newFields) {
    fields = newFields;
  }

  /**
   * sets the player's goal to newGoal, typically called via protocol
   *
   * @param newGoal the new state of the goal
   */
  public void setGoal(int[][] newGoal) {
    goal = newGoal;
  }

  /**
   * sets the player's cage to newCage, typically called via protocol order STATE
   *
   * @param newCage the new value for Cage
   */
  public void setCage(int[] newCage) {
    cage = newCage;
  }

  /**
   * sets the playedCards array to desired value
   *
   * @param newPile the new playedCards array
   */
  public void setPlayedCards(Card[] newPile) {
    playedCards = newPile;
  }

  /**
   * sets the Group object to provided value
   *
   * @param group the object to be set
   */
  public void setGroup(Group group) {
    gr = group;
  }

  /**
   * This method makes a String of all playerNames in the group and sends it to the groupMembers via
   * Protocol
   */
  public void sendPlayerNames() {
    ArrayList<Player> player = gr.getGroupMembers();
    StringBuilder sb = new StringBuilder();
    sb.append("PLAYERNAMES ");
    for (int i = 0; i < player.size(); i++) {
      sb.append(player.get(i).getName() + " ");
    }
    gr.sendCommand(sb.toString());
  }

  /** this method only sends the numOfCards to group */
  public void sendNumOfCards() {
    gr.sendNumOfCards(numOfCards);
  }

  /**
   * returns the number of players of the server
   *
   * @return number of players
   */
  public int getNumberOfPlayers() {
    return numberOfPlayers;
  }

  /**
   * gets the fields array
   *
   * @return fields[]
   */
  public int[] getFields() {
    return fields;
  }

  /**
   * gets the goal array
   *
   * @return goal[][]
   */
  public int[][] getGoal() {
    return goal;
  }

  /**
   * gets the cage array
   *
   * @return cage[]
   */
  public int[] getCage() {
    return cage;
  }

  /**
   * gets the blockade array
   *
   * @return blockade[]
   */
  public boolean[] getBlockade() {
    return blockade;
  }

  /**
   * gets the playedCards array
   *
   * @return playedCards[]
   */
  public Card[] getPlayedCards() {
    return playedCards;
  }

  /**
   * this method gets the amount of cards every player has
   *
   * @return amount of cards every player has
   */
  public int[] getNumOfCards() {
    return numOfCards;
  }

  /**
   * this method sets the cheating player true
   *
   * @param id id of the cheating player
   */
  public void setCheatsAreOn(int id) {
    cheatsAreOn[id] = true;
  }

  /**
   * gets the amount of turns until there was a winner, the name of the winner and the date
   *
   * @return an array with turnNumber, winnerName and the actuacl date
   */
  public String getWinner() {
    LocalDate today = LocalDate.now();
    String name = gr.getWinnerName(winner);
    String s = turnNumber + " " + "turns" + " " + name + " " + today;
    return s;
  }

  /** this method sends the ENDGAME to the clients to get them out of their group */
  public void sendEndGame() {
    String w = getWinner();
    gr.sendCommand("ENDGAME " + w);
  }
}
