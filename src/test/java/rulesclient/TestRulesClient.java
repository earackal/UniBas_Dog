package rulesclient;

import org.junit.Test;
import client.RulesClient;
import general.cards.Card;
import general.cards.Hand;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TestRulesClient {

  RulesClient rc;

  /** tests that marble is moved out of cage */
  @Test
  public void testMoveOut() {

    rc = new RulesClient();
    int testNumber = 1;
    rc.setPlayerNumber(testNumber);
    rc.moveOut();
    int[] startingPoints = rc.getStartingPoint();
    int[] fields = rc.getFields();
    int numberAtStart = fields[startingPoints[testNumber]];
    assertEquals(1, numberAtStart);
  }

  /** tests if a hand with all non-playable cards is discarded when no marble is out */
  @Test
  public void testDiscardHandWithoutStart() {

    rc = new RulesClient();
    int testNumber = 1;
    rc.setPlayerNumber(testNumber);
    rc.fetchMarblePositions(testNumber, rc.getFields(), rc.getGoal());
    Card[] cards = new Card[11];
    for (int i = 0; i < cards.length; i++) {
      cards[i] = new Card(i, i + 2, 'h');
    }
    Hand hand = new Hand(1, cards);
    rc.setPlayerHand(hand);
    System.out.println(hand.valuesInHandToString());
    boolean hasAPlayableCard = rc.hasPlayableCard();
    assertEquals(false, hasAPlayableCard);
  }

  @Test
  public void testNextSpaceIsBlocked() {

    rc = new RulesClient();
    int testNumber = 1;
    rc.setPlayerNumber(testNumber);
    Card card = new Card(0, 7, 'h');
    int[] fields = new int[64];
    Arrays.fill(fields, 0);
    fields[16] = 2;
    fields[15] = 1; // 2 steps
    rc.setFields(fields);

    boolean[] blockade = new boolean[5];
    Arrays.fill(blockade, false);
    blockade[2] = true;
    rc.setBlockade(blockade);

    assertEquals(true, rc.nextSpaceIsBlocked(15, rc.getFields(), rc.getGoal(), rc.getBlockade()));

    int[][] goal = new int[5][4];
    for (int j = 0; j < goal.length; j++) {
      Arrays.fill(goal[j], 0);
    }
    goal[1][3] = 1; // 1 step
    goal[1][2] = 1; // 1 step
    rc.setGoal(goal);
    assertEquals(true, rc.nextSpaceIsBlocked(103, rc.getFields(), rc.getGoal(), rc.getBlockade()));
    assertEquals(true, rc.nextSpaceIsBlocked(102, rc.getFields(), rc.getGoal(), rc.getBlockade()));
  }

  /** test that a 7 is playable when it has exactly 7 steps to move */
  @Test
  public void test7IfExactlyPlayable() {

    rc = new RulesClient();
    int testNumber = 1;
    rc.setPlayerNumber(testNumber);
    Card card = new Card(0, 7, 'h');

    int[] fields = new int[64];
    Arrays.fill(fields, 0);
    fields[16] = 2;
    fields[13] = 1; // 2 steps
    fields[32] = 3;
    fields[28] = 1; // 3 steps
    rc.setFields(fields);

    boolean[] blockade = new boolean[5];
    Arrays.fill(blockade, false);
    blockade[2] = true;
    blockade[3] = true;
    rc.setBlockade(blockade);

    int[][] goal = new int[5][4];
    for (int j = 0; j < goal.length; j++) {
      Arrays.fill(goal[j], 0);
    }
    goal[1][1] = 1; // 1 step
    goal[1][2] = 1; // 1 step
    rc.setGoal(goal);

    assertEquals(true, rc.isPlayable(card));
  }

  /** tests that a seven is not playable when not enough steps are around */
  @Test
  public void test7IfNotPlayable() {

    rc = new RulesClient();
    int testNumber = 1;
    rc.setPlayerNumber(testNumber);
    Card card = new Card(0, 7, 'h');

    int[] fields = new int[64];
    Arrays.fill(fields, 0);
    rc.setFields(fields);

    int[][] goal = new int[5][4];
    for (int j = 0; j < goal.length; j++) {
      Arrays.fill(goal[j], 0);
    }
    goal[1][0] = 1; // 2 steps
    goal[1][3] = 1; // 0 steps
    rc.setGoal(goal);

    assertEquals(false, rc.isPlayable(card));
  }

  @Test
  public void testJack() {

    rc = new RulesClient();
    int testNumber = 1;
    rc.setPlayerNumber(testNumber);
    Card card = new Card(0, 11, 'h');

    int[] fields = new int[64];
    Arrays.fill(fields, 0);
    fields[16] = 2;
    fields[0] = 1;
    rc.setFields(fields);

    boolean[] blockade = new boolean[5];
    Arrays.fill(blockade, false);
    blockade[2] = true;
    rc.setBlockade(blockade);

    rc.refreshMarblePos();

    assertEquals(false, rc.isPlayable(card));

    blockade[2] = false;
    rc.setBlockade(blockade);
    assertEquals(true, rc.isPlayable(card));

    blockade[1] = true;
    rc.setBlockade(blockade);
    assertEquals(false, rc.isPlayable(card));

    int[][] goal = new int[5][4];
    for (int j = 0; j < goal.length; j++) {
      Arrays.fill(goal[j], 0);
    }
    goal[1][0] = 1;
    rc.setGoal(goal);

    fields[0] = 0;
    rc.setFields(fields);

    rc.refreshMarblePos();
    assertEquals(false, rc.isPlayable(card));

    fields[32] = 1;
    fields[48] = 3;
    rc.setFields(fields);
    rc.switchOut(32, 48);
    fields = rc.getFields();
    assertEquals(3, fields[32]);
    assertEquals(1, fields[48]);
  }

  @Test
  public void test4Mode1Legality() {
    rc = new RulesClient();
    int testNumber = 1;
    rc.setPlayerNumber(testNumber);
    Card card = new Card(0, 4, 'h');

    int[] fields = new int[64];
    Arrays.fill(fields, 0);
    fields[16] = 2;
    fields[15] = 1;
    rc.setFields(fields);

    boolean[] blockade = new boolean[5];
    Arrays.fill(blockade, false);
    blockade[2] = true;
    rc.setBlockade(blockade);

    rc.refreshMarblePos();

    assertEquals(true, rc.isPlayable(card));
    assertEquals(false, rc.isLegalMode(card, 0, testNumber));
    assertEquals(true, rc.isLegalMode(card, 1, testNumber));
  }
}
