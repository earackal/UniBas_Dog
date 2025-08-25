package client;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import general.cards.Card;
import general.cards.Hand;

import java.io.*;
import java.util.ArrayList;

/**
 * This class handles the way in which GUI elements are updated but it doesn't include no user
 * interaction. The methods in this class also import different GUI elements.
 */
public class GuiUpdater {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(MainGui.class);

  /** the main window */
  private static Stage window;

  /** height of the line containing close, hide and minimize buttons */
  private static DoubleProperty windowBarHeight;

  /** contains the number of cards in every player's hand */
  private static int[] numOfCards = new int[5];

  /** gives the player number of this object */
  private static int playerNumber;

  /** contains previously played cards */
  private static Card[] playedCards;

  /** contains all 53 card images */
  private static ArrayList<Image> cardImages = new ArrayList<Image>();

  /** contains the order of other arrays depending on the player number */
  private static int[] order = {0, 1, 2, 3, 4};

  /** keeps the updated status of all fields */
  private static int[] guiFields = new int[96];

  /** the starting points of the individual players */
  private static final int[] startingPoint = {0, 0, 16, 32, 48};

  /** array for keeping field coordinates */
  private static double[][] coords = new double[2][96];

  /** array of double properties for updating the X coordinates of the marbles */
  private static ArrayList<DoubleProperty> coordsPropertyX = new ArrayList<DoubleProperty>();

  /** array of double properties for updating the Y coordinates of the marbles */
  private static ArrayList<DoubleProperty> coordsPropertyY = new ArrayList<DoubleProperty>();

  /** contains every player's name in the primary order */
  private static String[] playerNamesNotChanged = new String[5];

  /** imports the coordinate arrangement of the fields from a file */
  public static void getCoordinates() {

    LOGGER.debug("MG: getCoordinates()");

    InputStream fi = GuiUpdater.class.getClassLoader().getResourceAsStream("fieldCoordinates.bin");
    ObjectInputStream os = null;
    try {
      os = new ObjectInputStream(fi);
      coords = (double[][]) os.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }


    /**
   * updates marble coordinates
   *
   * @param marblePane pane which contains all the marbles
   */
  public static void setMarbleCoordinates(Pane marblePane) {
    for (int i = 0; i < 16; i++) {
      Node ballNode = marblePane.getChildren().get(i);
      // manages the X coordinate of the marble
      Circle ball = (Circle) ballNode;
      ball.centerXProperty()
          .bind(window.heightProperty().subtract(windowBarHeight).multiply(coordsPropertyX.get(i)));
      // manages the Y coordinate of the marble
      ball.centerYProperty()
          .bind(window.heightProperty().subtract(windowBarHeight).multiply(coordsPropertyY.get(i)));
      // manages marble's size
      ball.radiusProperty().bind(window.heightProperty().subtract(windowBarHeight).multiply(0.016));
    }
  }

  /**
   * initializes all 16 marbles
   *
   * @param marblePane pane which contains all the marbles
   */
  public static void initializeMarbles(Pane marblePane) {

    LOGGER.debug("MG: setMarbles(PANE)");

    // initializes the variables for containing images
    Image ballImage = null;
    Image ballImage1;
    Image ballImage2;
    Image ballImage3;
    Image ballImage4;

    // creates all the balls and assigns them their colours
    for (int i = 0; i < 16; i++) {
      Circle ball = new Circle();
      switch (i) {
          // creates the first blue ball
        case 0:
          //ballImage1 = new Image("file:./src/main/resources/pictures/ball_pictures/blueBall.png");
          ballImage1 = new Image(GuiUpdater.class.getClassLoader().getResource("pictures/ball_pictures/blueBall.png").toString());

          ball.setFill(new ImagePattern(ballImage1));
          marblePane.getChildren().add(ball);
          ballImage = ballImage1;
          break;
          // creates the first green ball
        case 4:
          ballImage2 = new Image(GuiUpdater.class.getClassLoader().getResource("pictures/ball_pictures/greenBall.png").toString());
          //ballImage2 = new Image("file:./src/main/resources/pictures/ball_pictures/greenBall.png");
          ball.setFill(new ImagePattern(ballImage2));
          marblePane.getChildren().add(ball);
          ballImage = ballImage2;
          break;
          // creates the first red ball
        case 8:
          ballImage3 = new Image(GuiUpdater.class.getClassLoader().getResource("pictures/ball_pictures/redBall.png").toString());
          //ballImage3 = new Image("file:./src/main/resources/pictures/ball_pictures/redBall.png");
          ball.setFill(new ImagePattern(ballImage3));
          marblePane.getChildren().add(ball);
          ballImage = ballImage3;
          break;
          // creates the first yellow ball
        case 12:
          ballImage4 = new Image(GuiUpdater.class.getClassLoader().getResource("pictures/ball_pictures/yellowBall.png").toString());
          //ballImage4 = new Image("file:./src/main/resources/pictures/ball_pictures/yellowBall.png");
          ball.setFill(new ImagePattern(ballImage4));
          marblePane.getChildren().add(ball);
          ballImage = ballImage4;
          break;
          // creates all the other balls with appropriate colours
        default:
          ball.setFill(new ImagePattern(ballImage));
          marblePane.getChildren().add(ball);
          break;
      }
    }
  }

  /**
   * reports to the player whose turn it is when it is not your turn
   *
   * @param infoPane pane which displays different information for the user
   * @param playerNumber this player's number
   * @param whoseTurnID number of the player whose turn it is
   */
  public static void whoseTurn(int whoseTurnID, int playerNumber, VBox infoPane) {
    LOGGER.debug("whoseTurnID = " + whoseTurnID);
    if (whoseTurnID != playerNumber) {
      LOGGER.debug("It is " + whoseTurnID + " player's turn from whoseTurn()");
      infoPane.getChildren().clear();
      LOGGER.debug("infoPane.getChildren().clear() from whoseTurn()");
      Label whoseTurnLabel = new Label("It is " + playerNamesNotChanged[whoseTurnID] + "'s turn!");
      MainGui.setWhoseTurnLabel(whoseTurnLabel);
      for (int i = 0; i < playerNamesNotChanged.length; i++) {
        LOGGER.debug("PlayerNamesNotChanged in whoseTurn: " + i + " " + playerNamesNotChanged[i]);
      }
      infoPane.getChildren().add(whoseTurnLabel);
    }
  }

  /**
   * adds the message to the chat box
   *
   * @param str the message which will be added
   * @param chatBox TextArea in which all the messages are displayed
   */
  public static void appendText(String str, TextArea chatBox) {
    // appends the message to the chat room
    chatBox.appendText(str);
    chatBox.positionCaret(chatBox.getText().length());
  }

  /**
   * updates player's hand visually on the board
   *
   * @param cardsBottomPane pane used for displaying player's hand
   * @param indexInCardImages contains indices of cards in cardImages array
   */
  public static void setHand(HBox cardsBottomPane, int[] indexInCardImages) {

    // deletes all previous cards from the GUI
    cardsBottomPane.getChildren().clear();

    for (int i = 0; i < indexInCardImages.length; i++) {
      LOGGER.debug("From setHand: indexInCardImages is: " + i + " " + indexInCardImages[i]);
    }
    // displays updated player's hand
    for (int i = 0; i < indexInCardImages.length; i++) {
      ImageView cardImageView = new ImageView(cardImages.get(indexInCardImages[i]));
      cardImageView.setPreserveRatio(true);
      cardImageView
          .fitHeightProperty()
          .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.15));
      cardsBottomPane.getChildren().add(cardImageView);
    }
  }

  /**
   * checks whether there are any players without cards and then displays an appropriate message if
   * there is one
   *
   * @param indexInCardImages contains indices of cards in cardImages array
   * @param whoseTurnID number of the player whose turn it is
   * @param cardsBottomPane pane in which your cards are displayed
   * @param cardsLeftPane pane in which the cards of the player on your left are displayed
   * @param cardsRightPane pane in which the cards of the player on your right are displayed
   * @param cardsTopPane pane in which the cards of the player in front of you are displayed
   * @param playerNames contains players' names
   */
  public static void noCards(
      HBox cardsBottomPane,
      HBox cardsTopPane,
      VBox cardsLeftPane,
      VBox cardsRightPane,
      int whoseTurnID,
      String[] playerNames,
      int[] indexInCardImages) {

    // imports necessary images to display when a player's hand is discarded
    //Image maintainImage = new Image(GuiUpdater.class.getResource("file:./src/main/resources/pictures/maintainFrameRotated.png"));

    //Image maintainImage = new Image("file:./src/main/resources/pictures/maintainFrame.png");
    //Image maintainImage = new Image("/pictures/maintainFrameRotated.png");
    Image maintainImage = new Image(GuiUpdater.class.getClassLoader().getResource("pictures/maintainFrame.png").toString());

    //Image maintainRotatedImage = new Image("file:./src/main/resources/pictures/maintainFrameRotated.png");
    Image maintainRotatedImage =
            new Image(GuiUpdater.class.getClassLoader().getResource("pictures/maintainFrameRotated.png").toString());


    // creates and sets appropriate labels to show when a player's hand is discarded
    Label yourDiscardedLabel = new Label("Your hand was discarded");
    Label otherDiscardedLabel2 = new Label(playerNames[2] + "'s hand was discarded");
    otherDiscardedLabel2.setRotate(90);
    Label otherDiscardedLabel3 = new Label(playerNames[3] + "'s hand was discarded");
    Label otherDiscardedLabel4 = new Label(playerNames[4] + "'s hand was discarded");
    otherDiscardedLabel4.setRotate(270);
    for (int i = 0; i < numOfCards.length; i++) {
      LOGGER.debug("From noCards: numOfCards is: " + i + " " + numOfCards[i]);
    }
    for (int j = 1; j < numOfCards.length; j++) {
      // checks whether a player doesn't have any cards
      if (numOfCards[j] == 0 || (j == 1 && indexInCardImages.length == 0)) {
        switch (j) {
            // displays appropriate info whether your hand was discarded
          case 1:
            cardsBottomPane.getChildren().clear();
            for (int i = 0; i < 2; i++) {
              ImageView maintainImageView1 = new ImageView(maintainImage);
              maintainImageView1.setPreserveRatio(true);
              maintainImageView1
                  .fitHeightProperty()
                  .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.15));
              cardsBottomPane.getChildren().add(maintainImageView1);
              if (i == 0) cardsBottomPane.getChildren().add(yourDiscardedLabel);
              LOGGER.debug("whoseTurnID from noCards is " + whoseTurnID);
            }
            break;
            // displays appropriate info whether the player's hand was discarded who is sitting on
            // your right
          case 2:
            cardsRightPane.getChildren().clear();
            for (int i = 0; i < 2; i++) {
              ImageView maintainImageView2 = new ImageView(maintainRotatedImage);
              maintainImageView2.setPreserveRatio(true);
              maintainImageView2
                  .fitWidthProperty()
                  .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.15));
              cardsRightPane.getChildren().add(maintainImageView2);
              if (i == 0) {
                Group labelHolder = new Group(otherDiscardedLabel2);
                cardsRightPane.getChildren().add(labelHolder);
              }
            }
            break;
            // displays appropriate info whether the player's hand was discarded who is sitting in
            // front of you
          case 3:
            cardsTopPane.getChildren().clear();
            for (int i = 0; i < 2; i++) {
              ImageView maintainImageView3 = new ImageView(maintainImage);
              maintainImageView3.setPreserveRatio(true);
              maintainImageView3
                  .fitHeightProperty()
                  .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.15));
              cardsTopPane.getChildren().add(maintainImageView3);
              if (i == 0) cardsTopPane.getChildren().add(otherDiscardedLabel3);
            }
            break;
            // displays appropriate info whether the player's hand was discarded who is sitting on
            // your left
          case 4:
            cardsLeftPane.getChildren().clear();
            for (int i = 0; i < 2; i++) {
              ImageView maintainImageView4 = new ImageView(maintainRotatedImage);
              maintainImageView4.setPreserveRatio(true);
              maintainImageView4
                  .fitWidthProperty()
                  .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.15));
              cardsLeftPane.getChildren().add(maintainImageView4);
              if (i == 0) {
                Group labelHolder2 = new Group(otherDiscardedLabel4);
                cardsLeftPane.getChildren().add(labelHolder2);
              }
            }
            break;
        }
      }
    }
  }

  /**
   * refreshes cards around the table
   *
   * @param marblePane basic pane designated to display marbles
   * @param playerNames contains players' names
   * @param cardsTopPane pane in which the cards of the player in front of you are displayed
   * @param cardsRightPane pane in which the cards of the player on your right are displayed
   * @param cardsLeftPane pane in which the cards of the player on your left are displayed
   */
  public static void refreshOtherCards(
      Pane marblePane,
      HBox cardsTopPane,
      VBox cardsLeftPane,
      VBox cardsRightPane,
      String[] playerNames) {

    // clears the panes whether a player doesn't have any cards in his hand
    if (numOfCards[2] != 0) cardsRightPane.getChildren().clear();
    if (numOfCards[3] != 0) cardsTopPane.getChildren().clear();
    if (numOfCards[4] != 0) cardsLeftPane.getChildren().clear();

    for (int i = 0; i < 5; i++) {
      LOGGER.debug("Number of cards is: " + i + " " + numOfCards[i]);
    }

    // adds labels displaying player names
    for (int j = 2; j < 5; j++) {
      Label playerNameLabel = new Label(playerNames[j] + ":");
      switch (j) {
        case 2:
          cardsRightPane.getChildren().add(playerNameLabel);
          break;
        case 3:
          cardsTopPane.getChildren().add(playerNameLabel);
          break;
        case 4:
          cardsLeftPane.getChildren().add(playerNameLabel);
          break;
      }
      // adds appropriate number of covered cards to every player's hand
      for (int i = 0; i < numOfCards[j]; i++) {

        if (j == 3) {
          Image cardImage = new Image(GuiUpdater.class.getClassLoader().getResource("pictures/cardBack.png").toString());
          ImageView cardImageView = new ImageView(cardImage);
          cardImageView.setPreserveRatio(true);
          cardImageView
              .fitHeightProperty()
              .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.15));
          cardsTopPane.getChildren().add(cardImageView);
        } else {
          Image cardImage = new Image(GuiUpdater.class.getClassLoader().getResource("pictures/cardBackRotated.png").toString());
          ImageView cardImageView = new ImageView(cardImage);
          cardImageView.setPreserveRatio(true);
          cardImageView
              .fitWidthProperty()
              .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.15));
          if (j == 4) cardsLeftPane.getChildren().add(cardImageView);
          else cardsRightPane.getChildren().add(cardImageView);
        }
      }
    }

    // creates and sets the card in the center of the board
    Image cardImage = new Image(GuiUpdater.class.getClassLoader().getResource("pictures/cardBack.png").toString());
    ImageView cardImageView = new ImageView(cardImage);

    cardImageView.setPreserveRatio(true);
    cardImageView
        .fitHeightProperty()
        .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.15));
    cardImageView
        .xProperty()
        .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.24));
    cardImageView
        .yProperty()
        .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.28));
    marblePane.getChildren().add(cardImageView);
  }

  /**
   * sets the number of cards in players' hands
   *
   * @param numberOfCards new value of number of cards in player's hands
   */
  public static void setNumOfCards(int[] numberOfCards) {
    int temp2;
    for (int j = 1; j < playerNumber; j++) {
      temp2 = numberOfCards[1];
      for (int i = 1; i < 4; i++) {
        numberOfCards[i] = numberOfCards[i + 1];
      }
      numberOfCards[4] = temp2;
      // sets the number of cards which all players have in their hands
      LOGGER.debug("from setNumOfCards: order of numberOfCards changed");
    }
    for (int i = 0; i < numberOfCards.length; i++) {
      LOGGER.debug("from setNumOfCards: numOfCards is " + i + " " + numberOfCards[i]);
    }
    GuiUpdater.numOfCards = numberOfCards;
  }

  /** imports card images */
  public static void initializeCards() {
    // imports "Joker" card image and adds it to cardImages
    Image jokerImage = new Image(GuiUpdater.class.getClassLoader().getResource("pictures/cards/0.jpg").toString());
    cardImages.add(jokerImage);

    // imports all the other card images and adds it to cardImages
    char suit = 0;
    for (int i = 1; i < 14; i++) {
      for (int j = 0; j < 4; j++) {
        switch (j) {
          case 0:
            suit = 'c';
            break;
          case 1:
            suit = 'd';
            break;
          case 2:
            suit = 'h';
            break;
          case 3:
            suit = 's';
            break;
        }
        StringBuilder stringBuilder =
            new StringBuilder("pictures/cards/");
        stringBuilder.append(i).append(suit).append(".png");
        //Image cardImage = new Image(String.valueOf(stringBuilder));
        Image cardImage = new Image(GuiUpdater.class.getClassLoader().getResource(String.valueOf(stringBuilder)).toString());

        cardImages.add(cardImage);
      }
    }
  }

  /**
   * updates player's hand
   *
   * @param rc the rulesClient object it gets its data from
   */
  public static void refreshPlayerHand(RulesClient rc) {

    // imports cards in hand
    int[] indexInCardImages;
    Hand oldHand = rc.getPlayerHand();
    if (oldHand != null) {
      Card[] oldCardsInHand = oldHand.getCardsInHand();
      if (oldCardsInHand != null) {
        // creates new variable for storing cards in hand
        Card[] cardsInHand = new Card[oldCardsInHand.length];

        // rewrites cards in the hand to new variables
        for (int i = 0; i < oldCardsInHand.length; i++) {
          cardsInHand[i] = oldCardsInHand[i];
        }

        // converts hand values to values in indexInCardImages
        indexInCardImages = new int[cardsInHand.length];
        for (int i = 0; i < cardsInHand.length; i++) {
          indexInCardImages[i] = getCardIndex(cardsInHand[i].getSuit(), cardsInHand[i].getValue());
        }
        MainGui.setIndexInCardImages(indexInCardImages);
        // actually updates the cards in MainGui
        if (MainGui.refreshHandRunnable != null) {
          LOGGER.debug("refreshHand.run() is about to be called");
          MainGui.refreshHandRunnable.run();
          LOGGER.debug("refreshHand.run() was called");
        }
      } else {
        indexInCardImages = new int[0];
        MainGui.setIndexInCardImages(indexInCardImages);
        LOGGER.debug("first exceptional else triggered");
      }
    } else {
      indexInCardImages = new int[0];
      MainGui.setIndexInCardImages(indexInCardImages);
      LOGGER.debug("second exceptional else triggered");
    }
  }

  /**
   * gets the appropriate card index for selecting a card from cardImages
   *
   * @param suit contains the cards suit
   * @param value contains the cards value
   * @return the index for selecting a card from cardImages
   */
  public static int getCardIndex(char suit, int value) {
    int addMe = 0;
    switch (suit) {
      case 'j':
        addMe = 0;
        value = 1;
        break;
      case 'c':
        addMe = 1;
        break;
      case 'd':
        addMe = 2;
        break;
      case 'h':
        addMe = 3;
        break;
      case 's':
        addMe = 4;
        break;
    }
    int index = (value - 1) * 4 + addMe;
    return index;
  }

  /**
   * refreshes last played card in the middle of the board
   *
   * @param marblePane basic pane designated to display marbles
   */
  public static void refreshPlayedCard(Pane marblePane) {
    LOGGER.debug(" refreshPlayedCard was run but only in part");
    if (playedCards.length != 0) {
      LOGGER.debug(" refreshPlayedCardRunnable was run (there are cards in playedCards)");
      Card playedCard = playedCards[playedCards.length - 1];
      int index = getCardIndex(playedCard.getSuit(), playedCard.getValue());
      Image cardImage = cardImages.get(index);
      ImageView cardImageView = new ImageView(cardImage);
      cardImageView.setPreserveRatio(true);
      cardImageView
          .fitHeightProperty()
          .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.147));
      cardImageView
          .xProperty()
          .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.35));
      cardImageView
          .yProperty()
          .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.28));
      marblePane.getChildren().remove(cardImageView);
      marblePane.getChildren().add(cardImageView);
    }
  }

  /**
   * displays all 12 cards when Joker is played
   *
   * @param cardPane pane which displays all 12 cards
   */
  public static void displayCardsAfterJoker(FlowPane cardPane) {
    // setting 13 cards which will be displayed when Joker is selected
    for (int i = 1; i < 14; i++) {
      ImageView card = new ImageView((cardImages.get(i * 4)));
      card.setPreserveRatio(true);
      card.fitHeightProperty()
          .bind(window.heightProperty().subtract(windowBarHeight).multiply(0.15));
      cardPane.getChildren().add(card);
    }
  }

  /**
   * updates the marble arrangement on the board
   *
   * @param rc the rulesClient object it gets its data from
   */
  public static void refreshGUI(RulesClient rc) {

    LOGGER.debug("MG: refreshGUI(RC)");

    // sets player names in a primary order
    setPlayerNamesNotChanged(rc.getPlayerNames());

    // imports the arrays with new marble positions
    int[] oldFields = rc.getFields(); // keeps the status of the main 64 fields
    int[] oldCage = rc.getCage(); // keeps the status of the cage
    int[][] oldGoal = rc.getGoal(); // keeps the status of the goal

    // creates arrays for working with marble positions
    int[] fields = new int[oldFields.length]; // keeps the status of the main 64 fields
    int[] cage = new int[oldCage.length]; // keeps the status of the cage
    int[][] goal = new int[oldGoal.length][oldGoal[0].length]; // keeps the status of the goal

    int[] fieldNumbers = new int[96];

    // assigns oldFields values to fields
    for (int i = 0; i < oldFields.length; i++) {
      fields[i] = oldFields[i];
      fieldNumbers[i] = i;
    }

    // rearranges fieldNumbers values
    for (int i = 0; i < 4; i++) {
      int k = 0;
      for (int j = 80 + i * 4; j < 80 + (i + 1) * 4; j++) {
        fieldNumbers[j] = (i + 1) * 100 + k;
        k++;
      }
    }
    int tempField;
    for (int j = 0; j < (playerNumber - 1) * 4; j++) {
      tempField = fieldNumbers[80];
      for (int i = 80; i < 95; i++) {
        fieldNumbers[i] = fieldNumbers[i + 1];
      }
      fieldNumbers[95] = tempField;
    }
    for (int i = startingPoint[playerNumber]; i < 64; i++) {
      fieldNumbers[i - startingPoint[playerNumber]] = i;
    }
    for (int i = 0; i < startingPoint[playerNumber]; i++) {
      fieldNumbers[i + 64 - startingPoint[playerNumber]] = i;
    }

    // assigns oldCage values to cage
    for (int i = 0; i < oldCage.length; i++) {
      cage[i] = oldCage[i];
      LOGGER.debug("cage values are: " + i + " " + cage[i]);
    }

    // assigns oldGoal values to goals
    for (int i = 0; i < oldGoal.length; i++) {
      for (int j = 0; j < oldGoal[i].length; j++) {
        goal[i][j] = oldGoal[i][j];
      }
    }

    // sets initial guiFields values to 0
    for (int i = 0; i < 96; i++) {
      guiFields[i] = 0;
    }

    // takes the values from "fields" and puts them in "guiFields"
    for (int i = startingPoint[playerNumber]; i < 64; i++) {
      guiFields[i - startingPoint[playerNumber]] = fields[i];
    }
    for (int i = 0; i < startingPoint[playerNumber]; i++) {
      guiFields[i + 64 - startingPoint[playerNumber]] = fields[i];
    }

    // shifts values of arrays to the left according to the player number
    int temp0;
    int[] temp1;
    for (int j = 1; j < playerNumber; j++) {
      temp0 = cage[1];
      temp1 = goal[1];
      for (int i = 1; i < 4; i++) {
        cage[i] = cage[i + 1];
        goal[i] = goal[i + 1];
      }
      cage[4] = temp0;
      goal[4] = temp1;
    }

    // takes the values from "cage" and puts them in "guiFields"
    for (int i = 1; i < 5; i++) {
      for (int j = 0; j < cage[i]; j++) {
        guiFields[64 + (i - 1) * 4 + j] = order[i];
      }
    }

    // takes the values from "goal" and puts them in "guiFields"
    int k = 80;
    for (int i = 1; i < 5; i++) {
      for (int j = 0; j < 4; j++) {
        if (goal[i][j] == order[i]) guiFields[k] = order[i];
        else guiFields[k] = 0;
        k++;
      }
    }

    // creates a 0-value object for setting the default values for further creation of two arrays
    // containing zeros
    DoubleProperty temp = new SimpleDoubleProperty(0);

    // fills up with zeros two arrays for keeping coordinates
    for (int i = 0; i < 16; i++) {
      coordsPropertyX.add(temp);
      coordsPropertyY.add(temp);
    }

    // helps to fill up the arrays with coordinates in a proper way
    int j1 = 0, j2 = 4, j3 = 8, j4 = 12;
    int[] marblePositions = new int[16];

    // converts info from "guiFields" into two arrays for coordinates: "coordsPropertyX" and
    // "coordsPropertyY"
    for (int i = 0; i < 96; i++) {
      switch (guiFields[i]) {
        case 1:
          marblePositions[j1] = fieldNumbers[i];
          temp = new SimpleDoubleProperty(coords[0][i]);
          coordsPropertyX.set(j1, temp);
          temp = new SimpleDoubleProperty(coords[1][i]);
          coordsPropertyY.set(j1, temp);
          j1++;
          break;
        case 2:
          marblePositions[j2] = fieldNumbers[i];
          temp = new SimpleDoubleProperty(coords[0][i]);
          coordsPropertyX.set(j2, temp);
          temp = new SimpleDoubleProperty(coords[1][i]);
          coordsPropertyY.set(j2, temp);
          j2++;
          break;
        case 3:
          marblePositions[j3] = fieldNumbers[i];
          temp = new SimpleDoubleProperty(coords[0][i]);
          coordsPropertyX.set(j3, temp);
          temp = new SimpleDoubleProperty(coords[1][i]);
          coordsPropertyY.set(j3, temp);
          j3++;
          break;
        case 4:
          marblePositions[j4] = fieldNumbers[i];
          temp = new SimpleDoubleProperty(coords[0][i]);
          coordsPropertyX.set(j4, temp);
          temp = new SimpleDoubleProperty(coords[1][i]);
          coordsPropertyY.set(j4, temp);
          j4++;
          break;
      }
    }

    // sets new marble positions in GUI
    MainGui.setMarblePositions(marblePositions);

    LOGGER.debug("It is " + rc.whoseTurnID + " player's turn");

    // calls refreshMarbles to change marble positions in JavaFX Thread
    if (MainGui.refreshMarblesRunnable != null) {
      MainGui.refreshMarblesRunnable.run();
    }

    LOGGER.debug("about to run refreshPlayedCardRunnable");
    // calls refreshPlayedCardRunnable to change last played card in JavaFX Thread
    if (MainGui.refreshPlayedCardRunnable != null) {
      MainGui.refreshPlayedCardRunnable.run();
      LOGGER.debug(" refreshPlayedCardRunnable was run");
    }
  }

  /** rearranges all necessary arrays depending on player's number */
  public static void rearrangeOrder() {
    int temp;
    for (int j = 1; j < playerNumber; j++) {
      temp = order[1];
      for (int i = 1; i < 4; i++) {
        order[i] = order[i + 1];
      }
      order[4] = temp;
    }
  }

  /**
   * sets the stage of GUI for this class
   *
   * @param window the stage of GUI
   */
  public static void setWindow(Stage window) {
    GuiUpdater.window = window;
  }

  /**
   * sets the windowBarHeight of GUI for this class
   *
   * @param windowBarHeight the windowBarHeight of GUI
   */
  public static void setWindowBarHeight(DoubleProperty windowBarHeight) {
    GuiUpdater.windowBarHeight = windowBarHeight;
  }

  /**
   * sets the player number whom the GUI belong to
   *
   * @param playerNumber the player number whom the GUI belong to
   */
  public static void setPlayerNumber(int playerNumber) {
    GuiUpdater.playerNumber = playerNumber;
  }

  /**
   * sets the order to thi class
   *
   * @param order this player's order array
   */
  public static void setOrder(int[] order) {
    GuiUpdater.order = order;
  }

  /**
   * sets the player names in a primary order
   *
   * @param playerNamesNotChanged names of all players in a primary order
   */
  public static void setPlayerNamesNotChanged(String[] playerNamesNotChanged) {
    for (int i = 0; i < playerNamesNotChanged.length; i++) {
      GuiUpdater.playerNamesNotChanged[i] = playerNamesNotChanged[i];
    }
  }

  /**
   * updates previously played cards
   *
   * @param playedCards previously played cards
   */
  public static void setPlayedCards(Card[] playedCards) {
    GuiUpdater.playedCards = playedCards;
  }
}
