package client;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import general.cards.Card;
import general.cards.Hand;

/**
 * This class is used for receiving information from GUI and sending it to other classes.
 * It is called "CommandLineInterface" because earlier it was used to get
 * user interaction from command line input but right now it used for managing
 * some types of information which is sent from GUI
 */
public class CommandLineInterface {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(CommandLineInterface.class);

  /* contains its own RulesClient object */
  final RulesClient rc;

  /* contains its own MainGui object */
  final MainGui mg;

  /* player's number who this CommandLineInterface object belongs to */
  int playerNumber;

  /* player's ID who this CommandLineInterface object belongs to */
  int playerID;

  /* reports whether CommandLineInterface object was created or not */
  private boolean cliCreated = false;

  /** constructor of this class
   *
   * @param rulesC object of RulesClient
   * @param mainGui object of MainGui
   */
  public CommandLineInterface(RulesClient rulesC, MainGui mainGui) {
    rc = rulesC;
    mg = mainGui;
    playerNumber = rc.getPlayerNumber();
    playerID = playerNumber;
  }

  /** when connection is standing, this is function is called */
  public void startCLI() {

    if (playerNumber == 0) {
      playerNumber = rc.getPlayerNumber();
      playerID = playerNumber;
    }
    System.out.println("Welcome to Unibas Dog!");

    /*
    System.out.println("You are player number " + playerNumber);

    String[] names = rc.getPlayerNames();
    System.out.println("Your name is: " + names[playerNumber]);
    */

  }

  /** gets called at start of turn, leads through the turn of the player */
  public void startTurn() {

    LOGGER.debug("CLI: startTurn()");

    try {
      if (rc.getPlayerHand().numOfCards() == 0) {
        System.out.println("Your hand was already discarded.");
        rc.endTurn(null);
      }
      printHand();
      printMarblePos(0);

      if (!rc.hasPlayableCard()) { // cannot play
        Hand hand = rc.getPlayerHand();
        hand.discardHand();
        System.out.println("You do not have any cards you can play. Your hand is discarded.");
        rc.endTurn(null);

      } else { // can play

        System.out.println("It is your turn!");

        Card playedCard = selectCard(rc.getPlayerHand());
        int mode = selectMode(playedCard);

        if (playedCard.getValue() == 0) { // joker is played

          System.out.println("You have selected to play your joker with the value " + mode);
          Card newCard = new Card(666, mode, 'j'); // create new card that is chosen card

          mode = selectMode(newCard);
          resolveCard(newCard, mode);

        } else { // card is not a joker

          resolveCard(playedCard, mode);
        }

        rc.endTurn(playedCard);
      }
    } catch (NullPointerException e) {
      System.out.println("Your hand was already discarded.");
      rc.endTurn(null);
    }
  }

  /**
   * prompts the player to play a card and returns the card the player wants to play checks for
   * legality of the card
   *
   * @param hand the hand of the player
   * @return the card to be played
   */
  public Card selectCard(Hand hand) {

    LOGGER.debug("CLI: selectCard(" + hand.valuesInHandToString() + ")");

    System.out.println("Please select a card to play:");
    printHand();
    System.out.println("Please enter a value between 0 and 13: ");
    // int value = getInputValue();
    int value = 0;
    synchronized (this) {
      try {
        cliCreated = true;
        if (!MainGui.isGuiCreated()) {
          wait();
        }
        GuiUpdater.refreshPlayerHand(rc);
        MainGui.selectCardRunnable.run();
        wait();
        value = GuiInteraction.getSelectedCardNumber();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    Card playedCard = hand.getCardWithVal(value);
    boolean isPlayable = rc.isPlayable(playedCard);
    boolean isInHand = !(playedCard == null);

    while (!isPlayable || !isInHand) {
      if (!isPlayable) {
        System.out.println("This card is not playable.");
      }
      if (!isInHand) {
        System.out.println("This card is not in your hand.");
      }
      System.out.println("Please select another card:");
      printHand();

      synchronized (this) {
        try {
          GuiUpdater.refreshPlayerHand(rc);
          MainGui.selectCardRunnable.run();
          wait();
          value = GuiInteraction.getSelectedCardNumber();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      // value = getInputValue();

      playedCard = hand.getCardWithVal(value);
      isPlayable = rc.isPlayable(playedCard);
      isInHand = !(playedCard == null);
    }

    System.out.println(
        "This card is playable. You have selected to play " + playedCard.cardAsString());

    return playedCard;
  }

  /**
   * returns the value of cliCreated
   *
   * @return the value of cliCreated
   */
  public boolean isCliCreated() {
    return cliCreated;
  }

  /**
   * prompts the player to chose which mode they want to play the card with
   *
   * @param card the card that is played
   * @return the mode selected
   */
  public int selectMode(Card card) {

    LOGGER.debug("CLI: selectMode(" + card.cardAsString() + ")");

    System.out.println("Please select the mode to be played: ");
    printModeMeanings(card);
    System.out.println("Please enter the desired value for mode: ");

    int mode = 0;
    synchronized (this) {
      try {
        MainGui.selectModeRunnable.run();
        wait();
        mode = GuiInteraction.getSelectedMode();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    // mode = getInputValue();

    boolean isLegalMode = rc.isLegalMode(card, mode, playerNumber);

    while (!isLegalMode) {
      System.out.println("This mode is not legal. Please select another mode:");
      printModeMeanings(card);
      GuiInteraction.setSelectedCardNumber(card.getValue());
      System.out.println("Please enter the desired value for mode: ");
      synchronized (this) {
        try {
          GuiInteraction.setIsModeLegal(false);
          MainGui.selectModeRunnable.run();
          wait();
          mode = GuiInteraction.getSelectedMode();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      // mode = getInputValue();
      isLegalMode = rc.isLegalMode(card, mode, playerNumber);
    }

    System.out.println("You have selected a legal mode. You have selected to play mode " + mode);

    return mode;
  }

  /**
   * prompts the player to select the position of the marble to be played. Not called if 7 or J is
   * played or marble moves out of cage.
   *
   * @param card the card that has been selected
   * @param mode the mode of the card to be used
   * @return the position of the marble to be moved
   */
  public int selectPos(Card card, int mode) {

    LOGGER.debug("CLI: selectPos(" + card.cardAsString() + "," + mode + ")");

    System.out.println("You have selected to play " + card.cardAsString());
    System.out.println("You have selected the mode " + mode);

    int i = 0;
    int[] marblePos = rc.fetchMarblePositions(playerNumber, rc.getFields(), rc.getGoal());

    if (marblePos[0] != -1) { // at least one marble is out
      printMarblePos(0);
    } else {
      System.out.println("None of your marbles are out!");
    }

    System.out.println("Please select the position of the marble you would like to move: ");
    System.out.println("Enter a number between 0 and 63 to move a marble on the field.");
    int goalBeginning = playerNumber * 100;
    int goalEnd = goalBeginning + 3;
    System.out.println(
        "Enter a number between "
            + goalBeginning
            + " and "
            + goalEnd
            + " to move a marble in the goal.");

    // int pos = getInputValue();

    int pos = 0;
    synchronized (this) {
      try {
        MainGui.selectMarbleRunnable.run();
        wait();
        pos = GuiInteraction.getSelectedMarble();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    boolean isLegalPos = rc.isLegalPos(card, pos, mode, playerNumber);

    while (!isLegalPos) {
      System.out.println("The position you selected is not legal. Please select a legal position:");
      System.out.println("Enter a number between 0 and 63 to move a marble on the field.");
      System.out.println(
          "Enter a number between "
              + goalBeginning
              + " and "
              + goalEnd
              + " to move a marble in the goal.");

      // pos = getInputValue();

      synchronized (this) {
        try {
          MainGui.selectMarbleRunnable.run();
          wait();
          pos = GuiInteraction.getSelectedMarble();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      isLegalPos = rc.isLegalPos(card, pos, mode, playerNumber);
    }

    System.out.println("You have selected a legal position!");
    System.out.println("You have selected position: " + pos);

    return pos;
  }

  /**
   * gets called for each card after mode and card are determined.
   *
   * @param card the card whose effect takes place
   * @param mode the mode selected
   */
  public void resolveCard(Card card, int mode) {

    LOGGER.debug("CLI: resolveCard(" + card.cardAsString() + "," + mode + ")");

    int value = card.getValue();
    int pos = -1;
    switch (value) {
      case 0:
        System.out.println(
            "ERROR in resolve card: Joker arrived here. Should have been handled before.");
        break;

      case 1:
        if (mode == 0) {
          pos = selectPos(card, mode);
          if (pos > 63) { // if in goal
            pos = pos % 100;
            rc.goalMove(pos, value, playerNumber);
          } else { // in field
            rc.simpleMove(pos, value);
          }

        } else if (mode == 1) {
          pos = selectPos(card, mode);
          rc.simpleMove(pos, 11);
        } else {
          rc.moveOut();
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
        pos = selectPos(card, mode);
        if (pos > 63) {
          pos = pos % 100;
          rc.goalMove(pos, value, playerNumber);
        } else {
          rc.simpleMove(pos, value);
        }
        break;

      case 4:
        pos = selectPos(card, mode);
        if (mode == 0) {
          rc.simpleMove(pos, value);
        } else {
          rc.simpleMove(pos, -value);
        }
        break;

      case 7:
        // FIXME if player enters goal with 7 when too many steps are left, game can never end
        for (int i = 1; i <= 7; i++) {
          pos = get7pos();
          if (pos > 63) {
            int player = pos / 100;
            pos = pos % 100;
            rc.goalMove(pos, 1, player);
          } else {
            rc.simpleMove(pos, 1);
          }
          GuiUpdater.refreshGUI(rc);
        }
        break;

      case 11:
        selectSwitchTargets();
        break;

      case 13:
        if (mode == 0) {
          pos = selectPos(card, mode);
          rc.simpleMove(pos, 13);
        } else {
          rc.moveOut();
        }
        break;
    }
  }

  /**
   * prints the modes and their explanations of selected card to console
   *
   * @param card the card of which the modes are to be explained
   */
  public void printModeMeanings(Card card) {

    LOGGER.debug("CLI: printModeMeanings(" + card.cardAsString() + ")");

    int value = card.getValue();
    switch (value) {
      case 0:
        System.out.println("0-13: becomes the card with that value");
        break;

      case 1:
        System.out.println("0: moves " + value);
        System.out.println("1: moves 11");
        System.out.println("2: one marble moves out of the cage");
        break;

      case 2:
      case 3:
      case 5:
      case 6:
      case 8:
      case 9:
      case 10:
      case 12:
        System.out.println("0: moves " + value);
        break;

      case 4:
        System.out.println("0: moves " + value + " forwards");
        System.out.println("1: moves " + value + " backwards");
        break;

      case 7:
        System.out.println("0: moves " + value + " but can be separated between marbles");
        break;

      case 11:
        System.out.println("0: switches the position of two marbles");
        break;

      case 13:
        System.out.println("0: moves " + value);
        System.out.println("1: one marble moves out of the cage");
        break;

      default:
        System.out.println("ERROR in printModeMeanings: not a legal card value");
        break;
    }
  }

  /**
   * called if a seven is played, gets a legal position to move
   *
   * @return the legal position
   */
  public int get7pos() {

    LOGGER.debug("CLI: get7Pos()");
    int goalBeginning = playerNumber * 100;
    int goalEnd = goalBeginning + 3;
    int pos = -1;
    int area;
    int[] fields = rc.getFields();
    int[][] goal = rc.getGoal();

    boolean isInField;
    boolean isInGoal;
    boolean isLegalPosition = false;
    boolean isMarble = false;
    boolean isBlocked = true;

    boolean isFirstIteration = true;

    while (!isLegalPosition || isBlocked || !isMarble || isFirstIteration) {

      if (isFirstIteration) {
        isFirstIteration = false;

      } else { // positions were illegal
        if (!isLegalPosition) {
          System.out.println("The position you have entered is not a legal position.");
        }
        if (!isMarble) {
          System.out.println(
              "The position you have selected does not contain one of your marbles.");
        }
        if (isBlocked) {
          System.out.println("The marble you have selected cannot move, its path is blocked.");
        }
        System.out.println("Please enter a valid position.");
      }
      System.out.println("Please select the marble to move one step.");
      printMarblePos(0);
      System.out.println("Enter a number between 0 and 63 to move a marble on the field.");
      System.out.println(
          "Enter a number between "
              + goalBeginning
              + " and "
              + goalEnd
              + " to move a marble in the goal.");

      // pos = getInputValue();

      synchronized (this) {
        try {
          MainGui.selectMarbleRunnable.run();
          wait();
          pos = GuiInteraction.getSelectedMarble();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      int goalPos = -1;

      area = rc.verifyPos(pos);
      isInField = false;
      isInGoal = false;
      if (area == 0) {
        isInField = true;
      } else if (area == 1) {
        isInGoal = true;
        goalPos = pos % 100;
      }
      isLegalPosition = (isInField || isInGoal);

      if (isInField) {
        isMarble = (fields[pos] != 0);
        isBlocked = rc.isBlocked(rc.coordinate(pos + 1));
      } else if (isInGoal) {
        isMarble = (goal[pos / 100][goalPos] != 0);

        if (goalPos >= 3) {
          isBlocked = true;
        } else {
          isBlocked = rc.isBlocked(pos + 1);
        }
      } else {
        isMarble = false;
      }
    }

    return pos;
  }

  /**
   * returns the first integer typed in by user
   *
   * @return an integer from user
   */
  public int getInputValue() {

    LOGGER.debug("CLI: getInputValue()");

    Scanner in = new Scanner(System.in);
    return in.nextInt();
  }

  /** outputs the hand to the command line */
  public void printHand() {

    LOGGER.debug("CLI: printHand()");

    System.out.println("Your hand is:");
    Hand hand = rc.getPlayerHand();
    System.out.println(hand.valuesInHandToString());
  }

  /** selects two targets for switches and exchanges them */
  public void selectSwitchTargets() {

    LOGGER.debug("CLI: selectSwitchTargets()");
    int ownPos = -1;
    int otherPos = -1;

    while (!rc.isLegalSwitch(ownPos, otherPos)) {

      if (ownPos != -1 || otherPos != -1) { // second try or later
        System.out.println("The selected positions are not legal. Please try again.");
      }
      printMarblePos(1);
      System.out.println("Please select your marble for the switch: ");
      // ownPos = getInputValue();

      synchronized (this) {
        try {
          MainGui.selectMarbleRunnable.run();
          wait();
          ownPos = GuiInteraction.getSelectedMarble();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      printMarblePos(2);
      System.out.println(
          "Please select the position of the other player's marble you would like to move: ");
      System.out.println("Enter a number between 0 and 63 to select a marble on the field.");
      // otherPos = getInputValue();

      synchronized (this) {
        try {
          MainGui.selectMarbleRunnable.run();
          wait();
          otherPos = GuiInteraction.getSelectedMarble();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    rc.switchOut(ownPos, otherPos);
  }

  /**
   * prints the positions of every player's marbles
   *
   * @param mode 0 means all output, 1 means only own marbles, 2 means only other player's marbles
   */
  public void printMarblePos(int mode) {

    LOGGER.debug("CLI: printMarblePos(" + mode + ")");

    int[] marblePos = rc.fetchMarblePositions(playerNumber, rc.getFields(), rc.getGoal());

    if (mode == 0 || mode == 1) { // your positions
      System.out.print("Your marbles are at positions: ");
      for (int i = 0; i < marblePos.length && marblePos[i] != -1; i++) {
        System.out.print(marblePos[i] + " ");
      }
      System.out.println(); // newline
    }

    if (mode == 0 || mode == 0) { // others' positions
      for (int j = 1; j <= rc.getNumberOfPlayers(); j++) {
        if (j != playerNumber) {
          marblePos = rc.fetchMarblePositions(j, rc.getFields(), rc.getGoal());
          System.out.print("Player " + j + "'s marbles are at positions: ");
          for (int i = 0; i < 4 && marblePos[i] != -1; i++) {
            System.out.print(marblePos[i] + " ");
          }
          System.out.println(); // newline
        }
      }
    }
  }
}
