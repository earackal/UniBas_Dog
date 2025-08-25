package rulesserver;

import org.junit.Test;
import server.RulesServer;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TestRulesServer {

  RulesServer rs;

  @Test
  public void testIsFull() {

    rs = new RulesServer();
    int[][] goal = new int[5][4];
    for (int i = 0; i < goal.length; i++) {
      Arrays.fill(goal[i], 0);
    }
    Arrays.fill(goal[1], 1);
    rs.setGoal(goal);

    assertEquals(true, rs.goalIsFull(1));
    assertEquals(false, rs.goalIsFull(2));
  }

  @Test
  public void testHasWon() {

    rs = new RulesServer();
    rs.setNumberOfPlayers(4);
    int[][] goal = new int[5][4];
    for (int i = 0; i < goal.length; i++) {
      Arrays.fill(goal[i], 0);
    }
    Arrays.fill(goal[2], 2);
    Arrays.fill(goal[3], 3);
    Arrays.fill(goal[4], 4);

    rs.setGoal(goal);

    assertEquals(2, rs.hasWon());
  }
}
