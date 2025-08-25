package client;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import general.cards.Card;
import general.cards.Hand;

/**
 * This class manages the interaction between the user and the game. It also updates some GUI
 * elements because there are cases where user interaction and updating of the GUI happens in the
 * same methods (that's the reason why such functionality is not in the class "GuiUpdater")
 */
public class GuiInteraction {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(MainGui.class);

  /** player's number who won the game */
  private static String winner;

  /** shows if user decided to move into the goal or not */
  private static int goalChosen;

  /** keeps the rulesClient object it needs for the refreshGUI */
  private static RulesClient rc;

  /** keeps the commandLineInterface object which is associated with this GUI */
  private static CommandLineInterface cli;

  /** contains the value of the selected marble */
  private static int selectedMarble;

  /** tells whether a card was donated */
  private static boolean donationHappened;

  /** a card which will be donated */
  private static Card cardToDonate;

  /** contains the value of the selected card */
  private static int selectedCardNumber;

  /** tells if the mode is legal */
  private static boolean isModeLegal;

  /** contains the value of the selected mode */
  private static int selectedMode;

  /**
   * this will be called whenever one of the players wins the game or one of the player's closes
   * MainGui window
   */
  public static void hasWonWindow() {

    Stage hasWonStage = new Stage();
    hasWonStage.setOnCloseRequest(event -> MainGui.closeGui());
    hasWonStage.initModality(Modality.APPLICATION_MODAL);
    hasWonStage.setTitle("Game over!");
    Label hasWonLabel = new Label();
    // different label will be displayed when one of the players loses connection
    if (winner.equals("No_Winner_Due_To_ConnectionLoss")) {
      hasWonLabel.setText("No winner due to connection loss");
    }
    // this will be displayed when one player wins the game
    else {
      hasWonLabel.setText(winner + " has just won the game!");
    }
    LOGGER.debug("Winner" + winner + "Winner");
    Label voidLabel = new Label();
    Button returnButton = new Button("Return to Lobby");
    // clicking this button will return you to the Lobby
    returnButton.setOnAction(
        event -> {
          MainGui.buttonClickSound.play();
          hasWonStage.close();
          MainGui.closeGui();
        });
    VBox hasWonLayout = new VBox();
    hasWonLayout.setAlignment(Pos.CENTER);
    hasWonLayout.getChildren().addAll(hasWonLabel, voidLabel, returnButton);
    Scene hasWonScene = new Scene(hasWonLayout, 250, 150);
    hasWonStage.setScene(hasWonScene);
    hasWonStage.show();
  }

  /**
   * sets the winning message with the winner's name in it
   *
   * @param winner the player's name who has won the game
   */
  public static void setWinner(String winner) {
    GuiInteraction.winner = winner;
  }

  /**
   * asks the player whether to move to the goal or not
   *
   * @param infoPane the pane where the question will be asked
   */
  public static void chooseGoal(VBox infoPane) {

    // creating and adding all the required buttons and labels
    Label turnLabel = new Label("It is your turn!");
    infoPane.getChildren().add(turnLabel);
    Label chooseGoalLabel = new Label("You can move into the goal. Do you want to?");
    infoPane.getChildren().add(chooseGoalLabel);
    Button yesButton = new Button("Yes");
    Button noButton = new Button("No");
    infoPane.getChildren().add(yesButton);
    infoPane.getChildren().add(noButton);

    // moves the marble into the goal if chosen
    yesButton.setOnAction(
        event -> {
          MainGui.buttonClickSound.play();
          goalChosen = 1;
          synchronized (rc) {
            rc.notify();
          }
          infoPane.getChildren().clear();
          LOGGER.debug("infoPane.getChildren().clear() from yesButton()");
        });

    // doesn't move the marble into the goal if chosen
    noButton.setOnAction(
        event -> {
          MainGui.buttonClickSound.play();
          goalChosen = 0;
          synchronized (rc) {
            rc.notify();
          }
          infoPane.getChildren().clear();
          LOGGER.debug("infoPane.getChildren().clear() from noButton()");
        });
  }

  /**
   * gets if user decided to get into the goal
   *
   * @return user's decision whether to move into the goal
   */
  public static int getGoalChosen() {
    return goalChosen;
  }

  /**
   * lets the player select a marble to play
   *
   * @param marblePane contains all the marbles
   * @param infoPane contains information useful to the user
   * @param marblePositions contains updated marble positions
   */
  public static void selectMarble(Pane marblePane, VBox infoPane, int[] marblePositions) {
    Label turnLabel = new Label("It is your turn!");
    Label selectMarbleLabel = new Label("Please select a marble you want to move");
    infoPane.getChildren().addAll(turnLabel, selectMarbleLabel);
    for (int i = 0; i < 16; i++) {
      Node ballNode = marblePane.getChildren().get(i);
      Circle ball = (Circle) ballNode;
      ball.setDisable(false);
      int finalI = i;
      ball.setOnMouseClicked(
          event -> {
            MainGui.marbleClickSound.play();
            for (int j = 0; j < 16; j++) {
              Node insideBallNode = marblePane.getChildren().get(j);
              Circle insideBall = (Circle) insideBallNode;
              insideBall.setDisable(true);
            }
            selectedMarble = marblePositions[finalI];
            synchronized (cli) {
              cli.notify();
            }
            infoPane.getChildren().clear();
            LOGGER.debug("infoPane.getChildren().clear() from selectMarble()");
          });
    }
  }

  /* gets selected marble */
  public static int getSelectedMarble() {
    return selectedMarble;
  }

  /**
   * donates the card to some other player
   *
   * @param infoPane contains the information useful to the player
   * @param cardsBottomPane pane which contains all your cards
   * @param indexInCardImages contains indices of cards in cardImages array
   */
  public static void donateCard(VBox infoPane, HBox cardsBottomPane, int[] indexInCardImages) {
    Label donateLabel = new Label("Select a card you want to donate!");
    infoPane.getChildren().clear();
    LOGGER.debug("infoPane.getChildren().clear() from donateCard()");
    infoPane.getChildren().add(donateLabel);
    LOGGER.debug("indexInCardImages.length in donateCard is " + indexInCardImages.length);
    for (int i = 0; i < indexInCardImages.length; i++) {
      Node cardNode = cardsBottomPane.getChildren().get(i);
      ImageView card = (ImageView) cardNode;
      card.setDisable(false);
      int finalI = i;
      card.setOnMouseClicked(
          event -> {
            MainGui.cardClickSound.play();
            for (int j = 0; j < indexInCardImages.length; j++) {
              Node insideCardNode = cardsBottomPane.getChildren().get(j);
              ImageView insideCard = (ImageView) insideCardNode;
              insideCard.setDisable(true);
            }
            int remainder = indexInCardImages[finalI] % 4;
            Hand hand = rc.getPlayerHand();
            int value;
            if (remainder != 0) value = (indexInCardImages[finalI] - (remainder)) / 4 + 1;
            else value = (indexInCardImages[finalI] - (remainder)) / 4;
            cardToDonate = hand.getCardWithVal(value);
            LOGGER.debug("going to set CardToDonate");
            LOGGER.debug(cardToDonate.cardAsString());
            donationHappened = true;
          });
    }
  }

  /**
   * gets the card which will be donated
   *
   * @return the card which will be donated
   */
  public static synchronized Card getCardToDonate() {
    LOGGER.debug("started method getCardToDonate");
    while (!donationHappened) {}
    LOGGER.debug("donationHappend is true ");
    donationHappened = false;
    return cardToDonate;
  }

  /**
   * reports whether card donation happened or not
   *
   * @return true or false whether card donation happened or not
   */
  public static boolean isDonationHappened() {
    return donationHappened;
  }

  /**
   * sets that donation happened or did not
   *
   * @param donationHappened reports whether donation happened
   */
  public static void setDonationHappened(boolean donationHappened) {
    GuiInteraction.donationHappened = donationHappened;
  }

  /**
   * updates selectedCardNumber by user interaction, in other words - lets the user select a card
   *
   * @param infoPane pane used for displaying game messages and interacting with the player
   * @param indexInCardImages contains indices of cards in cardImages array
   * @param cardsBottomPane pane which contains all your cards
   */
  public static void selectCard(VBox infoPane, int[] indexInCardImages, HBox cardsBottomPane) {
    if (infoPane.getChildren().contains(MainGui.getWhoseTurnLabel())) {
      LOGGER.debug("whoseTurnLabel was recognized as being stored in infoPane");
      infoPane.getChildren().clear();
      LOGGER.debug("infoPane.getChildren().clear() from selectCard()");
    }
    LOGGER.debug("SELECT CARD CALLED");
    Label turnLabel = new Label("It is your turn!");
    Label notPlayableLabel = new Label("This card is not playable. Choose another one...");
    Label alsoNotPlayableLabel = new Label("This card is also not playable. Choose another one...");
    if (infoPane.getChildren().isEmpty()) {
      infoPane.getChildren().add(turnLabel);
      LOGGER.debug("INFOPANESIZE: " + infoPane.getChildren().size());
    } else if (infoPane.getChildren().size() == 1) {
      infoPane.getChildren().add(notPlayableLabel);
      LOGGER.debug("INFOPANESIZE: " + infoPane.getChildren().size());
    } else if (infoPane.getChildren().size() == 2) {
      infoPane.getChildren().add(alsoNotPlayableLabel);
    }
    for (int i = 0; i < indexInCardImages.length; i++) {
      LOGGER.debug("From selectedCard: indexInCardImages is: " + i + " " + indexInCardImages[i]);
    }
    for (int i = 0; i < indexInCardImages.length; i++) {
      Node cardNode = cardsBottomPane.getChildren().get(i);
      ImageView card = (ImageView) cardNode;
      card.setDisable(false);
      int finalI = i;
      card.setOnMouseClicked(
          event -> {
            MainGui.cardClickSound.play();
            for (int j = 0; j < indexInCardImages.length; j++) {
              Node insideCardNode = cardsBottomPane.getChildren().get(j);
              ImageView insideCard = (ImageView) insideCardNode;
              insideCard.setDisable(true);
            }
            int remainder = indexInCardImages[finalI] % 4;
            if (remainder != 0)
              selectedCardNumber = (indexInCardImages[finalI] - (remainder)) / 4 + 1;
            else selectedCardNumber = (indexInCardImages[finalI] - (remainder)) / 4;
            synchronized (cli) {
              cli.notify();
            }
          });
    }
  }

  /**
   * lets the user select the mode to be played
   *
   * @param infoPane contains the information useful to the player
   * @param cardPane displays 13 cards if joker is chosen
   * @param notLegalModeJokerLabel this will be displayed when a player chooses an illegal card
   *     after playing Joker
   */
  public static void selectMode(VBox infoPane, FlowPane cardPane, Label notLegalModeJokerLabel) {

    if (infoPane.getChildren().size() == 0) {
      Label turnLabel = new Label("It is your turn!");
      infoPane.getChildren().add(turnLabel);
    }
    if (infoPane.getChildren().size() > 1) {
      infoPane.getChildren().remove(1, infoPane.getChildren().size());
    }

    Label chooseModeLabel = new Label("Please select the mode to be played:");
    if (selectedCardNumber != 0) {
      infoPane.getChildren().add(chooseModeLabel);
    }
    Label notLegalModeLabel = new Label("You have selected an illegal mode. Please select again.");
    Button modeButton0 = new Button();
    Button modeButton1 = new Button();
    Button modeButton2 = new Button();

    switch (selectedCardNumber) {
      case 0:
        Label jokerLabel = new Label("Choose a card:");

        infoPane.getChildren().add(jokerLabel);
        infoPane.getChildren().add(cardPane);
        if (!isModeLegal && !infoPane.getChildren().contains(notLegalModeJokerLabel)) {
          infoPane.getChildren().add(notLegalModeJokerLabel);
        }
        for (int i = 1; i < 14; i++) {
          Node cardNode = cardPane.getChildren().get(i - 1);
          ImageView card = (ImageView) cardNode;
          card.setDisable(false);
          int finalI = i;
          card.setOnMouseClicked(
              event -> {
                MainGui.cardClickSound.play();
                for (int j = 1; j < 14; j++) {
                  Node insideCardNode = cardPane.getChildren().get(j - 1);
                  ImageView insideCard = (ImageView) insideCardNode;
                  insideCard.setDisable(true);
                }

                selectedCardNumber = finalI;
                selectedMode = finalI;
                synchronized (cli) {
                  cli.notify();
                }
                infoPane.getChildren().clear();
                LOGGER.debug("infoPane.getChildren().clear() from jokerSelected()");
              });
        }
        break;
      case 1:
        modeButton0.setText("Move " + selectedCardNumber);
        infoPane.getChildren().add(modeButton0);
        modeButton1.setText("Move 11");
        infoPane.getChildren().add(modeButton1);
        modeButton2.setText("Move a marble out of cage");
        infoPane.getChildren().add(modeButton2);
        break;

      case 2:
      case 3:
      case 5:
      case 6:
      case 8:
      case 9:
      case 10:
      case 12:
        {
          modeButton0.setText("Move " + selectedCardNumber);
          infoPane.getChildren().add(modeButton0);
          break;
        }

      case 4:
        modeButton0.setText("Move " + selectedCardNumber + " forwards");
        infoPane.getChildren().add(modeButton0);
        modeButton1.setText("Move " + selectedCardNumber + " backwards");
        infoPane.getChildren().add(modeButton1);
        break;

      case 7:
        modeButton0.setText(
            "Move " + selectedCardNumber + " but you can separate it between marbles");
        infoPane.getChildren().add(modeButton0);
        break;

      case 11:
        modeButton0.setText("Switch the position of two marbles");
        infoPane.getChildren().add(modeButton0);
        break;

      case 13:
        modeButton0.setText("Move " + selectedCardNumber);
        infoPane.getChildren().add(modeButton0);
        modeButton1.setText("Move a marble out of cage");
        infoPane.getChildren().add(modeButton1);
        break;
    }
    if (!isModeLegal
        && !infoPane.getChildren().contains(notLegalModeLabel)
        && selectedCardNumber != 0) {
      infoPane.getChildren().add(notLegalModeLabel);
    }
    isModeLegal = true;

    modeButton0.setOnAction(
        event -> {
          MainGui.buttonClickSound.play();
          selectedMode = 0;
          synchronized (cli) {
            cli.notify();
          }
          infoPane.getChildren().clear();
          LOGGER.debug("infoPane.getChildren().clear() from modButton0()");
        });
    modeButton1.setOnAction(
        event -> {
          MainGui.buttonClickSound.play();
          selectedMode = 1;
          synchronized (cli) {
            cli.notify();
          }
          infoPane.getChildren().clear();
          LOGGER.debug("infoPane.getChildren().clear() from modeButton1()");
        });
    modeButton2.setOnAction(
        event -> {
          MainGui.buttonClickSound.play();
          selectedMode = 2;
          synchronized (cli) {
            cli.notify();
          }
          infoPane.getChildren().clear();
          LOGGER.debug("infoPane.getChildren().clear() from modeButton2()");
        });
  }

  /**
   * gets selected mode
   *
   * @return selected mode
   */
  public static int getSelectedMode() {
    return selectedMode;
  }

  /**
   * sets isModeLegal value
   *
   * @param isModeLegal shows whether the chose mode is legal
   */
  public static void setIsModeLegal(boolean isModeLegal) {
    GuiInteraction.isModeLegal = isModeLegal;
  }

  /**
   * gets the number of the selected card
   *
   * @return number of the selected card
   */
  public static int getSelectedCardNumber() {
    return selectedCardNumber;
  }

  /**
   * returns the selected card number
   *
   * @param selectedCardNumber player's selected card number
   */
  public static void setSelectedCardNumber(int selectedCardNumber) {
    GuiInteraction.selectedCardNumber = selectedCardNumber;
  }

  /**
   * sets the CommandLineInterface object to the provided one
   *
   * @param commandLineInterface the new CommandLineInterface object the GuiInteraction is to be
   *     associated with
   */
  public static void setCli(CommandLineInterface commandLineInterface) {
    cli = commandLineInterface;
  }

  /**
   * sets its own RulesClient object
   *
   * @param rulesClient RulesClient object which belongs to this player
   */
  public static void setRulesClient(RulesClient rulesClient) {
    rc = rulesClient;
  }
}
