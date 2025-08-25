package client;

import java.util.Arrays;

import general.cards.Hand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import general.cards.Card;
import general.cards.Hand;

/** This class manages the rules of the game */
public class RulesClient {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(RulesClient.class);

  /** copy of the server's game state */
  private int[] fields;

  /** copy of the server's cages */
  private int[] cage;

  /** copy of the server's goals, usage: goal[player][pos] */
  private int[][] goal;

  /** copy of the server's blockades */
  private boolean[] blockade;

  /** copy of the server's number of players */
  private int numberOfPlayers;

  /** the starting points of the individual players */
  private final int[] startingPoint = {0, 0, 16, 32, 48};

  /** contains the player's hand */
  private Hand playerHand;

  /** number of the marbles the client is currently playing with */
  private int playerNumber;

  /** the number of the player whose client this is */
  private int playerID;

  /** contains copy of played cards */
  private Card[] playedCards;

  /** commandLineInterface assigned to this client */
  private CommandLineInterface cli;

  /** boolean which checks whether RulesClient has updated the arrays */
  private boolean refresh;

  /** contains the names of each player, usage: playerNames[player]; */
  private String[] playerNames = new String[5];

  /** contains amount of cards every player has */
  private int[] numOfCards = new int[5];

  /** contains the object, which represents the client gui */
  private ClientGui cg;

  /** contains the MainGui object used for displaying information */
  private MainGui mg;

  /** contains the NewClient object used for communication with the server */
  private NewClient nc;

  /** the id of the current player */
  public int whoseTurnID = 0;

  /**
   * contains the positions of all the marbles of player 1 in the field, if in goal: 100 * player +
   * pos. usage is marblePos[playerNumber][marbleNumber]
   */
  private int[][] marblePos;

  /** constructor */
  public RulesClient() {

    LOGGER.debug("RC: constructor is called.");

    refresh = false;

    numberOfPlayers = 4;

    fields = new int[64];
    Arrays.fill(fields, 0);

    goal = new int[5][4];
    for (int i = 0; i < goal.length; i++) {
      Arrays.fill(goal[i], 0);
    }

    marblePos = new int[5][4];
    for (int i = 0; i < marblePos.length; i++) {
      Arrays.fill(marblePos[i], -1);
    }
    cage = new int[5];
    Arrays.fill(cage, 4);

    blockade = new boolean[5];
    Arrays.fill(blockade, false);

    playerHand = null;
    playerNumber = -1;
    playerID = -1;
    playedCards = new Card[0];
  }

  /**
   * checks if own goal is full
   *
   * @param player player id
   * @return true if own goal is full
   */
  public boolean goalIsFull(int player) {

    boolean isFull = true;
    for (int i = 0; i < goal[player].length; i++) {
      if (goal[player][i] == 0) {
        isFull = false;
      }
    }
    return isFull;
  }

  /** refreshes current board state, called at start of turn */
  public void refreshMarblePos() {

    LOGGER.debug("RC: refreshMarblePos()");

    for (int player = 1; player <= numberOfPlayers; player++) {
      Arrays.fill(marblePos[player], -1);
      marblePos[player] = fetchMarblePositions(player, fields, goal);
    }

    if (mg != null) {
      GuiUpdater.setPlayedCards(playedCards);
      for (int i = 0; i < playedCards.length; i++) {
        LOGGER.debug("Played cards: " + i + " " + playedCards[i].getSuit());
      }
      for (int i = 0; i < numOfCards.length; i++) {
        LOGGER.debug(i + " Number of cards: " + numOfCards[i]);
      }
      LOGGER.debug("");
      GuiUpdater.refreshGUI(this);
      GuiUpdater.refreshPlayerHand(this);
    }
  }

  /**
   * gets called via protocol as turn starts for player checks if player can play a card, if they
   * cannot, they discard their hand else they play a card for their turn ends turn
   */
  public void startTurn() {

    LOGGER.debug("RC: startTurn()");
    LOGGER.debug("About to call refreshGui().");

    for (int i = 0; i < playedCards.length; i++) {
      LOGGER.debug("Played cards: " + i + " " + playedCards[i].getSuit());
    }
    GuiUpdater.setPlayedCards(playedCards);

    // refreshes marble arrangement
    for (int i = 0; i < numOfCards.length; i++) {
      LOGGER.debug(i + " Number of cards: " + numOfCards[i]);
    }
    LOGGER.debug("");

    LOGGER.debug("startTurn() has moved past the refreshGui().");
    LOGGER.debug("About to call refreshMarblePos().");

    refreshMarblePos();

    LOGGER.debug("startTurn() has moved past the refreshMarblePos().");
    LOGGER.debug("About to call cli.startTurn.");

    cli.startTurn();
  }

  /**
   * ends the turn, transfers the played card (if any) and board state to server
   *
   * @param playedCard Card object with played Cards
   */
  public void endTurn(Card playedCard) {

    if (playedCard == null) {
      LOGGER.debug("RC: start of endTurn, length of playedCards: " + playedCards.length);
      LOGGER.debug("RC: endTurn(null)");

      nc.sendMyHand(playerHand);

      LOGGER.debug("RC: sended hand which is null");
      LOGGER.debug("RC: calling nc.sendState()");

    } else {

      LOGGER.debug("RC: start of endTurn, length of playedCards: " + playedCards.length);
      LOGGER.debug("RC: endTurn(" + playedCard.cardAsString() + ")");

      // refresh playedCards
      Card[] tempPile = new Card[playedCards.length + 1];

      LOGGER.debug("RC: number of cards in playedCards: " + playedCards.length);
      LOGGER.debug("RC: number of cards in tempPile: " + tempPile.length);

      for (int i = 0; i < playedCards.length; i++) {
        tempPile[i] = playedCards[i];
      }

      tempPile[tempPile.length - 1] = playedCard;
      playedCards = new Card[tempPile.length];
      for (int i = 0; i < tempPile.length; i++) {
        playedCards[i] = tempPile[i];
        System.out.println(playedCards[i].cardAsString());
      }
      playerHand.loseCard(playedCard); // remove card from hand

      StringBuilder stringBuilder = new StringBuilder("RC: playerHand is: ");
      for (int i = 0; i < playerHand.numOfCards(); i++) {
        stringBuilder.append(playerHand.getCard(i).valueToString() + " ");
      }
      LOGGER.debug(stringBuilder);

      nc.sendMyHand(playerHand);

      stringBuilder = new StringBuilder("RC: sendPlayedCards(");
      for (int i = 0; playedCards != null && i < playedCards.length; i++) {
        stringBuilder.append(playedCards[i].valueToString() + " ");
      }
      LOGGER.debug(stringBuilder);

      nc.sendPlayedCards(playedCards);

      LOGGER.debug("RC: calling nc.sendState()");
    }

    nc.sendState(this); // should start next turn
    LOGGER.debug("Your turn has ended, please wait for your next one.");
  }

  /** used when marble gets out of cage might eat other marble, also sets up a blockade */
  public void moveOut() {

    LOGGER.debug("RC: moveOut()");

    int victim = fields[startingPoint[playerNumber]];
    if (victim != 0) {
      cage[victim]++;
    }
    fields[startingPoint[playerNumber]] = playerNumber;
    cage[playerNumber]--;
    blockade[playerNumber] = true;
  }

  /**
   * switches the position of two marbles
   *
   * @param pos1 position on field of first marble
   * @param pos2 position on field of second marble
   */
  public void switchOut(int pos1, int pos2) {

    LOGGER.debug("RC: switchOut(" + pos1 + "," + pos2 + ")");

    int player1 = playerNumber;
    int player2 = fields[pos2];
    fields[pos1] = player2;
    fields[pos2] = player1;
  }

  /**
   * used whenever a marble moves inside the field enables it to move into goal if possible, might
   * eat other marble. If marble was blocking, releases blockade
   *
   * @param pos the position of the moving marble on the field
   * @param length amount of spaces it moves
   */
  public void simpleMove(int pos, int length) {

    LOGGER.debug("RC: simpleMove(" + pos + "," + length + ")");

    int movingPlayer = fields[pos];
    int finish = coordinate(pos + length);
    int victim = fields[finish];

    // reset starting position
    fields[pos] = 0;

    // player can move into goal
    int goalCoordinate = coordinate(finish - startingPoint[movingPlayer] - 1);
    boolean startIsBeforeGoal = false;
    if (pos <= startingPoint[movingPlayer]) {
      startIsBeforeGoal = true;
    } else if (movingPlayer == 1 && pos > startingPoint[4]) { // wrap around
      startIsBeforeGoal = true;
    }

    if (goalCoordinate >= 0 && goalCoordinate < 4 && startIsBeforeGoal) {
      boolean goalIsClear = true;
      for (int i = 0; i <= goalCoordinate; i++) {
        if (goal[movingPlayer][i] != 0) {
          goalIsClear = false;
        }
      }
      // not the first move of a new marble
      if (!blockade[movingPlayer] && goalIsClear) { // moving marble has moved before
        System.out.println("You can move into the goal. Do you want to? y=1, n=0");
        // int choice = cli.getInputValue(); // was used before creating GUI
        int choice = 0;
        // gets required choice value from user interaction via GUI
        synchronized (this) {
          try {
            MainGui.chooseGoalRunnable.run();
            // waits for the user to click the button and
            // in such way this will be notified
            wait();
            choice = GuiInteraction.getGoalChosen();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        while (choice != 0 && choice != 1) { // until input is valid
          System.out.println("Please enter a valid value. Type 1 for yes and 0 for no.");
          // gets required choice value from user interaction via GUI
          synchronized (this) {
            try {
              MainGui.chooseGoalRunnable.run();
              // waits for the user to click the button and
              // in such way this will be notified
              wait();
              choice = GuiInteraction.getGoalChosen();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          // choice = cli.getInputValue(); // was used before creating GUI
        }
        if (choice == 1) { // move into goal
          goal[movingPlayer][goalCoordinate] = movingPlayer;
          return;
        }
      }
    }

    // marble gets eaten
    if (victim != 0) {
      cage[victim]++;
    }

    // release blockade
    if (pos == startingPoint[movingPlayer] && blockade[movingPlayer]) {
      blockade[movingPlayer] = false;
    }

    fields[finish] = movingPlayer;
  }

  /**
   * used if marble already in goal is moved
   *
   * @param pos the position inside goal (0..3)
   * @param length amount of spaces moved
   * @param player the player whose goal is concerned
   */
  public void goalMove(int pos, int length, int player) {

    LOGGER.debug("RC: goalMove(" + pos + "," + length + ")");
    goal[player][pos] = 0;
    goal[player][pos + length] = playerNumber;
  }

  /**
   * checks if player has a card they can play in hand
   *
   * @return true if player has a playable card
   */
  public boolean hasPlayableCard() {

    LOGGER.debug("RC: hasPlayableCard()");

    if (playerHand.getCardsInHand() == null) {
      return false;
    }

    boolean hasPlayableCard = false;
    for (int i = 0; i < playerHand.numOfCards(); i++) {
      hasPlayableCard = (hasPlayableCard || isPlayable(playerHand.getCard(i)));
    }
    return hasPlayableCard;
  }

  /**
   * checks if a card is playable
   *
   * @param card the card one wishes to play
   * @return true if playable
   */
  public boolean isPlayable(Card card) {

    if (card == null) {
      return false;
    }

    LOGGER.debug("RC: isPlayable(" + card.cardAsString() + ")");

    int number = card.getValue();
    boolean mode0isPlayable;
    boolean mode1isPlayable;
    boolean mode2isPlayable;

    switch (number) {
      case 0: // Joker -> any other card
        // always playable
        return true;

      case 1: // Ace -> 1, 11 or start
        mode0isPlayable = isLegalMode(card, 0, playerNumber);
        mode1isPlayable = isLegalMode(card, 1, playerNumber);
        mode2isPlayable = isLegalMode(card, 2, playerNumber);
        return (mode0isPlayable || mode1isPlayable || mode2isPlayable);

      case 4: // forwards or backwards

      case 13: // King -> 13 or start
        mode0isPlayable = isLegalMode(card, 0, playerNumber);
        mode1isPlayable = isLegalMode(card, 1, playerNumber);
        return (mode0isPlayable || mode1isPlayable);

      case 2:

      case 3:

      case 5:

      case 6:

      case 8:

      case 9:

      case 10:

      case 11: // Jack -> exchange

      case 12: // Queen -> 12
        mode0isPlayable = isLegalMode(card, 0, playerNumber);
        return mode0isPlayable;

      case 7: // split
        LOGGER.debug("isPlayable(7) has started");

        boolean marbleIsOut = false;
        for (int i = 0; i < fields.length; i++) {
          marbleIsOut = marbleIsOut || (fields[i] != 0);
        }
        if (marbleIsOut) {
          return true;
        }

        int[][] goalCopy = new int[goal.length][goal[0].length];
        for (int i = 0; i < goalCopy.length; i++) {
          for (int j = 0; j < goalCopy[i].length; j++) {
            goalCopy[i][j] = goal[i][j];
          }
        }

        LOGGER.debug("has copied goal");

        int numOfSteps = 0;

        for (int i = 1; numOfSteps < 7 && i <= numberOfPlayers; i++) {
          boolean hasMoved = true;
          while (hasMoved) {
            hasMoved = false;

            for (int j = 0;
                numOfSteps < 7 && j <= goalCopy[i].length - 1;
                j++) { // no need to check for last space

              if (goalCopy[i][j] != 0
                  && !nextSpaceIsBlocked(i * 100 + j, fields, goalCopy, blockade)) {
                goalCopy[i][j] = 0;
                goalCopy[i][j + 1] = i;
                numOfSteps++;
                hasMoved = true;
              }
            }
          }
        }

        if (numOfSteps >= 7) {
          LOGGER.debug("7 steps are possible");

          return true;
        }

        LOGGER.debug("all marbles are blocked");
        return false;

      default:
        return false;
    }
  }

  /**
   * a function used for testing if the next space is occupied
   *
   * @param pos position in fields/goal
   * @param fieldsArray an array of fields or a copy
   * @param goalArray an array of goal or a copy
   * @param blockadeArray an array of blockade or a copy
   * @return true if next space is blocked
   */
  public boolean nextSpaceIsBlocked(
      int pos, int[] fieldsArray, int[][] goalArray, boolean[] blockadeArray) {

    if (pos < fieldsArray.length && pos >= 0) { // in fields
      boolean isBlocked = false;

      for (int i = 1; i < numberOfPlayers; i++) {
        if (pos + 1 == startingPoint[i] && blockadeArray[i]) {
          isBlocked = true;
        }
      }
      return isBlocked;

    } else { // in goal
      int playerNum = pos / 100;
      if (playerNum < 1 || playerNum > 4) {
        return true;
      }

      int goalPos = pos % 100;

      if (goalPos > 2 || goalPos < 0) {
        return true;
      }
      if (goalArray[playerNum][goalPos + 1] == 0) {
        return false;
      } else {
        return true;
      }
    }
  }

  /**
   * fetches the positions of a player's, marbles 0 - 63 on field 100*player - 100*number+3 for goal
   *
   * @param player the player whose marbles are searched for
   * @param fieldsArray contains fields or copy
   * @param goalArray contains goal or copy
   * @return array of positions of marbles. -1 means no further marbles were found
   */
  public int[] fetchMarblePositions(int player, int[] fieldsArray, int[][] goalArray) {

    LOGGER.debug("RC: fetches the marbles' positions.");

    int[] marblePos = new int[4];
    Arrays.fill(marblePos, -1);
    int marbleNum = 0;
    // go through field
    for (int fieldPos = 0; fieldPos < fieldsArray.length; fieldPos++) {
      if (fieldsArray[fieldPos] == player) {
        if (marbleNum < marblePos.length) {
          marblePos[marbleNum] = fieldPos;
          marbleNum++;
        } else {
          // System.out.println("ERROR in fetchMarblePositions: too many marbles on the field");
          cg.append("ERROR in fetchMarblePositions: too many marbles on the field");
        }
      }
    }
    // go through goal
    for (int goalPos = 0; goalPos < goalArray[player].length; goalPos++) {
      if (goalArray[player][goalPos] == player) {
        if (marbleNum < marblePos.length) {
          marblePos[marbleNum] = goalPos + player * 100;
          marbleNum++;

        } else {
          // System.out.println("ERROR in fetchMarblePositions: too many marbles on field/goal");
          cg.append("ERROR in fetchMarblePositions: too many marbles on field/goal");
        }
      }
    }
    return marblePos;
  }

  /**
   * checks if the position selected is legal
   *
   * @param card the card to be played
   * @param pos the position to be checked
   * @param mode the mode to be used
   * @param player the player wanting to play the card
   * @return true if position is legal
   */
  public boolean isLegalPos(Card card, int pos, int mode, int player) {

    LOGGER.debug("RC: isLegalPos(" + card.cardAsString() + "," + mode + "," + pos + ")");

    if (pos < 0) { // too low
      return false;
    } else if (pos > playerNumber * 100 + 3) { // above goal
      return false;
    } else if (pos > 63 && pos < playerNumber * 100) { // between goal and field
      return false;
    }

    if (pos < 63) {
      if (fields[pos] != player) {
        return false;
      }
    } else {
      int pl = pos / 100;
      if (pl != player) {
        return false;
      }
      int ps = pos % 100;
      if (goal[player][ps] != player) {
        return false;
      }
    }

    int value = card.getValue();
    int length;

    if (pos == -1) {
      return false;
    }
    if (pos <= player * 100 + 3 && pos >= player * 100) { // in goal

      switch (value) {
        case 0:
        case 7:
          return true;

        case 1:
          if (mode == 0) {
            length = 1;
          } else {
            return false;
          }
          break;

        case 2:
          length = 2;
          break;

        case 3:
          length = 3;
          break;

        default:
          return false;
      }

      if (length == -1) {
        return false;
      }
      return pos + length <= player * 100 + 3;

    } else { // in field

      switch (value) {
        case 1:
          if (mode == 0) {
            length = 1;
          } else if (mode == 1) {
            length = 11;
          } else {
            // System.out.println("ERROR in isLegalPos: mode 2/Ace somehow came through to here");
            cg.append("ERROR in isLegalPos: mode 2/Ace somehow came through to here");

            return false;
          }
          break;

        case 2:

        case 3:

        case 5:

        case 6:

        case 8:

        case 9:

        case 10:

        case 12:

        case 13:
          if (mode == 0) {
            length = value;
          } else {
            // System.out.println("ERRROR in isLegalPos: mode 1 in card with only mode 0 or King");
            cg.append("ERRROR in isLegalPos: mode 1 in card with only mode 0 or King");
            return false;
          }
          break;

        case 4:
          if (mode == 0) {
            length = 4;
          } else if (mode == 1) {
            // going through the fields and checking if they are blocked
            for (int i = 1; i <= 4; i++) {
              int tempPos = coordinate(pos - i);
              // checking for each players starting point, and if it is blocked
              for (int j = 1; j <= numberOfPlayers; j++) {
                if (tempPos == startingPoint[j] && blockade[j]) {
                  // blocked!
                  return false;
                }
              }
            }
            // has never been blocked
            return true;
          } else {
            // System.out.println("ERROR in isLegalPos: not a legal mode for card 4");
            cg.append("ERROR in isLegalPos: not a legal mode for card 4");

            return false;
          }
          break;

        case 11:
          if (fields[pos] == player) {
            boolean isBlocked = false;
            if (pos == startingPoint[player] && blockade[player]) {
              isBlocked = true;
            }
            return !isBlocked;
          }

        case 7:
          // special case, already resolved in isPlayable
          return false;

        default:
          // System.out.println("ERROR in isLegalPos: card with illegal value");
          cg.append("ERROR in isLegalPos: card with illegal value");

          return false;
      }

      // go through all the steps after pos
      for (int i = 1; i < length; i++) {
        // go through every possible blockade
        for (int j = 1; j <= numberOfPlayers; j++) {
          if (coordinate(pos + i) == startingPoint[j] && blockade[j]) {
            // blocked!
            return false;
          }
        }
      }
      // has never been blocked
      return true;
    }
  }

  /**
   * checks if the selected mode is legal
   *
   * @param card the card to be played
   * @param mode the mode to be checked
   * @param player the player wanting to select the mode
   * @return true if mode is legal
   */
  public boolean isLegalMode(Card card, int mode, int player) {

    LOGGER.debug("RC: isLegalMode(" + card.cardAsString() + "," + mode + ")");

    if (mode < 0 || mode > 13) {
      return false;
    }

    int value = card.getValue();

    boolean isLegal = false;

    switch (value) {
      case 0:
        if (mode == 0) { // joker copying joker, infinite loop
          return false;
        }
        Card dummyCard = new Card(666, mode, 'h');
        return isPlayable(dummyCard);

      case 1: // Ace
        if (mode == 0 || mode == 1) { // 1 or 11
          for (int i = 0; i < marblePos[player].length && marblePos[player][i] != -1; i++) {
            isLegal = (isLegal || isLegalPos(card, marblePos[player][i], mode, player));
          }
          return isLegal;

        } else if (mode == 2) { // start
          if (cage[player] > 0) {
            return true;
          }
        } else { // illegal mode
          break;
        }

      case 2:
        if (mode == 0) {
          // go through each marble and check if it is legal with said mode
          for (int i = 0; i < marblePos[player].length && marblePos[player][i] != -1; i++) {
            isLegal = (isLegal || isLegalPos(card, marblePos[player][i], mode, player));
          }
          return isLegal;
        } else {
          return false;
        }

      case 3:

      case 5:

      case 6:

      case 8:

      case 9:

      case 10:

      case 12:
        if (mode == 0) {
          // go through each marble and check if it is legal with said mode
          for (int i = 0; i < marblePos[player].length && marblePos[player][i] != -1; i++) {
            isLegal = (isLegal || isLegalPos(card, marblePos[player][i], mode, player));
          }
          return isLegal;
        } else { // illegal mode
          break;
        }

      case 11:
        if (mode == 0) {
          // go through each marble and check if one is playable
          int pos1 = -1;
          for (int i = 0; i < marblePos[player].length && marblePos[player][i] != -1; i++) {
            if (!isBlocked(marblePos[playerNumber][i])) {
              pos1 = marblePos[playerNumber][i];
            }
          }

          if (pos1 == -1) {
            break;
          }

          // go through all other marbles and check if they are playable
          for (int i = 0; i < marblePos.length; i++) {

            if (i != playerNumber) {
              for (int j = 0; j < marblePos[i].length; j++) {

                if (isLegalSwitch(pos1, marblePos[i][j])) {
                  return true;
                }
              }
            }
          }
        }
        break;

      case 4:
        if (mode == 0 || mode == 1) {
          // go through each marble and check if it is legal with said mode
          for (int i = 0; i < marblePos[player].length && marblePos[player][i] != -1; i++) {
            isLegal = (isLegal || isLegalPos(card, marblePos[player][i], mode, player));
          }
          return isLegal;
        } else { // illegal mode
          break;
        }

      case 13:
        if (mode == 0) { // 13
          // go through each marble and check if it is legal with said mode
          for (int i = 0; i < marblePos[player].length && marblePos[player][i] != -1; i++) {
            isLegal = (isLegal || isLegalPos(card, marblePos[player][i], mode, player));
          }
          return isLegal;
        } else if (mode == 1) {
          if (cage[player] > 0) {
            return true;
          }
        } else { // illegal mode
          break;
        }

      case 7:
        if (mode == 0) {
          return true;
        } else {
          break;
        }

      default:
        // System.out.println("ERROR in isLegalMode: card value is illegal.");
        cg.append("ERROR in isLegalMode: card value is illegal.");

        break;
    }
    return false;
  }

  /**
   * for a given position, returns if said position is blocked
   *
   * @param pos the position to be checked, pos + player * 100 for goal coordinate
   * @return true if it is blocked or illegal value
   */
  public boolean isBlocked(int pos) {

    LOGGER.debug("RC: isBlocked(" + pos + ")");

    int area = verifyPos(pos);
    if (area == 0) { // in field

      if (fields[pos] != 0) { // not necessary but most probable point to stop

        int numOfStartingPoint = -1;
        for (int i = 1; i < startingPoint.length; i++) {
          if (pos == startingPoint[i]) {
            numOfStartingPoint = i;
          }
        }

        if (numOfStartingPoint != -1) {

          return blockade[numOfStartingPoint]; // next field is a starting point and is blocked
        }
      }

      return false;

    } else if (area == 1) { // in goal

      pos = pos % 100;
      int player = pos / 100;
      return goal[player][pos] != 0;

    } else {

      return true;
    }
  }

  /**
   * checks if entered number is in field, in goal or illegal
   *
   * @param pos the position (goalPos = playerNumber * 100 + pos)
   * @return -1 if illegal, 0 if in field, 1 if in goal
   */
  public int verifyPos(int pos) {

    LOGGER.debug("RC: verifyPos(" + pos + ")");

    if (pos <= 63 && pos >= 0) { // in field
      return 0;

    } else if (pos <= playerNumber * 100 + 3 && pos >= 100 * playerNumber) { // in goal
      return 1;

    } else { // illegal
      return -1;
    }
  }

  /**
   * determines if the target is legal
   *
   * @param pos1 position of own marble
   * @param pos2 position of other player's marble
   * @return true if switch is legal
   */
  public boolean isLegalSwitch(int pos1, int pos2) {

    LOGGER.debug("RC: isLegalSwitch(" + pos1 + "," + pos2 + ")");

    if (pos1 < 0 || pos1 > 63 || pos2 < 0 || pos2 > 63) {
      return false;
    }

    boolean firstIsOwnMarble = (fields[pos1] == playerNumber);
    boolean sameMarble = (pos2 == pos1);
    boolean noTargetMarble = (fields[pos2] == 0);
    boolean secondIsOwnMarble = (fields[pos2] == playerNumber);
    boolean otherIsBlockedMarble = false;
    int victim = fields[pos2];
    if (pos2 == startingPoint[victim] && blockade[victim]) {
      otherIsBlockedMarble = true;
    }
    boolean ownIsBlockedMarble = (pos1 == startingPoint[playerNumber] && blockade[playerNumber]);

    return !sameMarble
        && !noTargetMarble
        && !secondIsOwnMarble
        && !ownIsBlockedMarble
        && !otherIsBlockedMarble
        && firstIsOwnMarble;
  }

  /**
   * sets the fields array, typically called from server via protocol
   *
   * @param newFields from the server
   */
  public void setFields(int[] newFields) {
    fields = newFields;
  }

  /**
   * sets the player's cage to newCage, typically called via protocol
   *
   * @param newCage the new value for Cage
   */
  public void setCage(int[] newCage) {
    cage = newCage;
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
   * sets the player's goal to newGoal, typically called via protocol
   *
   * @param newBlockade the new state of the blockade
   */
  public void setBlockade(boolean[] newBlockade) {
    blockade = newBlockade;
  }

  /**
   * gets called from server via protocol and updates the number of players
   *
   * @param newNum the new number of players
   */
  public void setNumberOfPlayers(int newNum) {
    numberOfPlayers = newNum;
  }

  /**
   * sets playerNumber to desired value, typically called via protocol at start of game also sets
   * playerID to said value
   *
   * @param num the new number of the player
   */
  public void setPlayerNumber(int num) {
    playerNumber = num;
    playerID = num;
  }

  /**
   * sets playedCards to desired value, typically called via protocol
   *
   * @param newPlayedCards the new pile of cards
   */
  public void setPlayedCards(Card[] newPlayedCards) {

    playedCards = new Card[newPlayedCards.length];
    for (int i = 0; i < newPlayedCards.length; i++) {
      playedCards[i] = newPlayedCards[i];
      LOGGER.debug("Played cards: " + i + " " + playedCards[i].cardAsString());
    }
    try {
      GuiUpdater.setPlayedCards(playedCards);
      GuiUpdater.refreshGUI(this);
    } catch (NullPointerException e) {

    }
  }

  /**
   * sets new hand, typically called via protocol
   *
   * @param newHand the new hand object
   */
  public void setPlayerHand(Hand newHand) {
    playerHand = newHand;
  }

  /**
   * sets new number of Cards via protocol
   *
   * @param noc new numOfCards
   */
  public void setNumOfCards(int[] noc) {
    numOfCards = noc;
  }

  /**
   * creates a new MainGui and CommandLineInterface object
   */
  public void createMainGui() {

    LOGGER.debug("RulesClient is creating its MainGui.");

    // creating new MainGui object
    setMainGui(new MainGui());
    // imports a file with field coordinates in it and returns them in an array
    GuiUpdater.getCoordinates();
    // sets this rules client for MainGui
    mg.setRulesClient(this);
    // sets this player's number for MainGui
    mg.setPlayerNumber(playerNumber);
    GuiUpdater.setPlayerNumber(playerNumber);
    // set all player names in MainGui
    //noinspection AccessStaticViaInstance
    for (int i = 0; i < playerNames.length; i++) {
      LOGGER.debug("PlayerNames in RC: " + i + " " + playerNames[i]);
    }
    GuiUpdater.setPlayerNamesNotChanged(playerNames);
    MainGui.setPlayerNames();
    // sets MainGui object for NewClient
    nc.setMainGui(mg);
    // sets NewClient object for MainGui
    mg.setNewClient(nc);
    // sets ClientGui object for MainGui
    MainGui.setClientGui(cg);
    //sets order in GuiUpdater
    int[] order = {0, 1, 2, 3, 4};
    GuiUpdater.setOrder(order);
    // rearranges necessary arrays depending on player number
    GuiUpdater.rearrangeOrder();
    // refreshes played cards in MainGui
    GuiUpdater.setPlayedCards(playedCards);
    for (int i = 0; i < playedCards.length; i++) {
      LOGGER.debug("Played cards: " + i + " " + playedCards[i].getSuit());
    }
    for (int i = 0; i < numOfCards.length; i++) {
      LOGGER.debug(i + " Number of cards: " + numOfCards[i]);
    }
    // refreshes marble arrangement
    GuiUpdater.refreshGUI(this);
    // refreshes player hand
    GuiUpdater.refreshPlayerHand(this);

    LOGGER.debug("RulesClient is creating its CLI.");

    // creating new CommandLineInterface
    setCommandLineInterface(new CommandLineInterface(this, mg));
    // assigning cli to MainGui
    mg.setCli(cli);
    // starting CommandLineInterface
    cli.startCLI();
    // creates and starts a new thread to launch start(); method in MainGui
    client.GuiThread gt = new client.GuiThread();
    gt.start();
  }

  /**
   * sets the playerNames to provided names
   *
   * @param names array of strings, which start at 1.
   */
  public void setPlayerNames(String[] names) {
    for (int i = 0; i < names.length; i++) {
      playerNames[i] = names[i];
    }
  }

  /**
   * returns the number of player whose client this is
   *
   * @return the number of the player (1 to 4)
   */
  public int getPlayerNumber() {
    return playerNumber;
  }

  /**
   * gets the players hand
   *
   * @return the Hand object owned by player
   */
  public Hand getPlayerHand() {
    return playerHand;
  }

  /**
   * gets the cages
   *
   * @return the array of cages
   */
  public int[] getCage() {
    return cage;
  }

  /**
   * gets the blockade array
   *
   * @return boolean array blockade
   */
  public boolean[] getBlockade() {
    return blockade;
  }

  /**
   * gets the fields
   *
   * @return the array of fields
   */
  public int[] getFields() {
    return fields;
  }

  /**
   * gets the goals
   *
   * @return the array of goals
   */
  public int[][] getGoal() {
    return goal;
  }

  /**
   * gets the starting points
   *
   * @return the array of starting points
   */
  public int[] getStartingPoint() {
    return startingPoint;
  }

  /**
   * sets the ClientGui object to the provided value
   *
   * @param cg the cg to be set
   */
  public void setClientGui(ClientGui cg) {
    this.cg = cg;
  }

  /**
   * sets the NewClient object to the provided value
   *
   * @param nc the nc to be set
   */
  public void setNewClient(NewClient nc) {
    this.nc = nc;
  }

  /**
   * sets the CommandLineInterface object to the provided value
   *
   * @param cli the cli to be set
   */
  public void setCommandLineInterface(CommandLineInterface cli) {
    this.cli = cli;
  }

  /**
   * gets the array of player names
   *
   * @return array of strings conataining player names
   */
  public String[] getPlayerNames() {
    return playerNames;
  }

  /**
   * sets the mainGui to be associated with this client
   *
   * @param mainGUI the object to be set
   */
  public void setMainGui(MainGui mainGUI) {

    this.mg = mainGUI;
  }

  /**
   * gets the number of players in the game
   *
   * @return numberOfPlayers field
   */
  public int getNumberOfPlayers() {
    return numberOfPlayers;
  }

  /**
   * gets the rulesClient
   *
   * @return this rulesClient object
   */
  public RulesClient getRulesClient() {
    return this;
  }

  /**
   * sets the refresh boolean
   *
   * @param b new refresh status
   */
  public void setRefresh(boolean b) {
    refresh = b;
  }

  /**
   * converts entered coordinate to a legal index of fields
   *
   * @param oldCoordinate the non-converted coordinate
   * @return a legal coordinate in fields
   */
  public int coordinate(int oldCoordinate) {

    return (64 + oldCoordinate) % 64;
  }

  /**
   * returns number of cards in each player's hand
   *
   * @return number of cards in each player's hand
   */
  public int[] getNumOfCards() {
    return numOfCards;
  }

  /**
   * This method sets the id of the player
   *
   * @param id id of the current player
   */
  public void setWhoseTurnID(int id) {
    whoseTurnID = id;
  }

  /** This method sets the state to its original form */
  public void setOriginState() {
    fields = new int[64];
    cage = new int[5];
    goal = new int[5][4];
    blockade = new boolean[5];
    playerHand = new Hand();
    numberOfPlayers = 0;
    numOfCards = new int[5];
  }
}
