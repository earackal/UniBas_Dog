package decoding;

import org.junit.Test;

import java.util.Random;

import client.NewClient;
import client.RulesClient;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import general.cards.Card;
import general.cards.Hand;

public class TestDecodedState {

  private static final Logger LOGGER = LogManager.getLogger(TestDecodedState.class);
  /** arrays of state */
  int[] fields;

  boolean[] blockade;
  int[] cage;
  int[][] goal;
  int numberOfPlayers;
  Hand playerHand;
  /** object of RulesClient */
  RulesClient rc;
  /** object of NewClient */
  NewClient nc;

  /**
   * This method creates a basic state and encodes it. Then it controls whether the array that was
   * created here was also set correctly in RulesClient
   */
  @Test
  public void testStartingState() {
    // initializing object of RulesClient
    rc = new RulesClient();
    setBasicState();
    // creating string
    String startingState = encode();
    String handAsString = handToString();
    // creating object of NewClient
    nc = new NewClient();
    // setting the state in NewClient
    nc.setState(startingState);
    nc.setMyHand(handAsString);
    rc = nc.getRulesClient();
    // tests
    assertArrayEquals(fields, rc.getFields());
    LOGGER.debug("TestEncoding: testStartingState - finished fields lul");
    assertArrayEquals(blockade, rc.getBlockade());
    LOGGER.debug("TestEncoding: testStartingState - finished blockades");
    assertArrayEquals(cage, rc.getCage());
    LOGGER.debug("TestEncoding: testStartingState - finished cage");
    assertArrayEquals(goal, rc.getGoal());
    LOGGER.debug("TestEncoding: testStartingState - finished goal");
    assertEquals(numberOfPlayers, rc.getNumberOfPlayers());
    LOGGER.debug("TestEncoding: testStartingState - finished numberOfPlayers");
    assertEquals(playerHand, rc.getPlayerHand());
    LOGGER.debug("TestEncoding: testStartingState - finished hand");
  }

  /**
   * This method creates a random state and encodes it. Then it controls whether the array that was
   * created here was also set correctly in class RulesClient
   */
  @Test
  public void testStateRandom() {
    // initializing object of RulesClient
    rc = new RulesClient();
    setBasicState();
    Random random = new Random();
    int marblesInCage = 0;
    for (int i = 1; i <= 4; i++) {
      int amountOfMarbles = 4;
      int marblesInField = random.nextInt(5);
      for (int j = 0; j < marblesInField; j++) {
        int position = random.nextInt(64);
        fields[position] = i;
      }
      amountOfMarbles = amountOfMarbles - marblesInField;
      if (amountOfMarbles > 0) {
        marblesInCage = random.nextInt(amountOfMarbles);
        for (int j = 0; j < marblesInCage; j++) {
          int position = random.nextInt(5);
          cage[position] = i;
        }
      }
      amountOfMarbles = amountOfMarbles - marblesInCage;
      if (amountOfMarbles > 0) {
        for (int j = 0; j < amountOfMarbles; j++) {
          int position = random.nextInt(4);
          goal[i][position] = i;
        }
      }
    }

    numberOfPlayers = 4;
    // creating string
    String startingState = encode();
    // creating object of NewClient
    nc = new NewClient();
    // setting the state in NewClient
    nc.setState(startingState);
    // tests
    rc = nc.getRulesClient();
    assertArrayEquals(fields, rc.getFields());
    LOGGER.debug("TestEncoding: testStartingState - finished fields");
    assertArrayEquals(blockade, rc.getBlockade());
    LOGGER.debug("TestEncoding: testStartingState - finished blockades");
    assertArrayEquals(cage, rc.getCage());
    LOGGER.debug("TestEncoding: testStartingState - finished cage");
    assertArrayEquals(goal, rc.getGoal());
    LOGGER.debug("TestEncoding: testStartingState - finished goal");
    assertEquals(numberOfPlayers, rc.getNumberOfPlayers());
    LOGGER.debug("TestEncoding: testStartingState - finished numberOfPlayers");
  }

  /**
   * This method encodes the arrays through a StringBuilder to a String
   *
   * @return str String which includes the values of the state
   */
  public String encode() {
    StringBuilder sb = new StringBuilder();
    sb.append("STATE ");
    for (int i = 0; i < fields.length; i++) {
      sb.append(fields[i] + " ");
    }
    for (int i = 0; i < blockade.length; i++) {
      sb.append(blockade[i] + " ");
    }
    for (int i = 0; i < cage.length; i++) {
      sb.append(cage[i] + " ");
    }
    for (int i = 0; i < goal.length; i++) {
      for (int j = 0; j < goal[i].length; j++) {
        sb.append(goal[i][j] + " ");
      }
    }
    sb.append(numberOfPlayers);
    String str = sb.toString();
    return str;
  }

  public String handToString() {
    StringBuilder sb = new StringBuilder();
    if (!playerHand.containsCards()) {
      playerHand = null;
      sb.append("HAND null");
      return sb.toString();
    } else {
      sb.append("HAND "); // playerHand
      for (int i = 0; i < playerHand.numOfCards(); i++) {
        Card c = playerHand.getCard(i);
        sb.append(c.getId()).append(" ");
        sb.append(c.getValue()).append(" ");
        sb.append(c.getSuit()).append(" ");
      }
      String handString = sb.toString();
      return handString;
    }
  }

  /** This method creates the basic state which means that all array values are zero */
  public void setBasicState() {
    // initializing state
    fields = new int[64];
    blockade = new boolean[5];
    cage = new int[5];
    goal = new int[5][4];
    numberOfPlayers = 4;
    playerHand = new Hand();
  }
}
