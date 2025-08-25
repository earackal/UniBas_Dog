package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;

/**
 * This class provides the main graphical user interface for the game. It launches a window in which
 * the board, marbles, cards and other elements of the game are displayed.
 */
@SuppressWarnings({"FieldCanBeLocal", "RedundantThrows"})
public class MainGui extends Application {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(MainGui.class);

  /** the main window */
  private static Stage window;

  /** used for changing height and width of the stage */
  private static boolean a;

  /** used for changing height and width of the stage */
  private static boolean b;

  /** height of the window */
  private static DoubleProperty height = new SimpleDoubleProperty();

  /** width of the left side of the window */
  private static DoubleProperty westWidth = new SimpleDoubleProperty();

  /** height of the line containing close, hide and minimize buttons */
  private static DoubleProperty windowBarHeight = new SimpleDoubleProperty();

  /** contains field positions where marbles are situated */
  private static int[] marblePositions = new int[16];

  /** gives the player number of this object */
  private static int playerNumber;

  /** keeps the rulesClient object it needs for the refreshGUI */
  private static RulesClient rc;

  /** keeps the commandLineInterface object which is associated with this GUI */
  private static CommandLineInterface cli;

  /** pane for keeping all 16 marbles */
  private static Pane marblePane;

  /** refreshes Marbles in JavaFX Thread */
  public static Runnable refreshMarblesRunnable;

  /** refreshes Player's hand in JavaFX Thread */
  public static Runnable refreshHandRunnable;

  /** lets the player select a card in JavaFX Thread */
  public static Runnable selectCardRunnable;

  /** lets the player select the mode in JavaFX Thread */
  public static Runnable selectModeRunnable;

  /** lets the player whether to move into the goal or not in JavaFX Thread */
  public static Runnable chooseGoalRunnable;

  /** lets the player select the marble in JavaFX Thread */
  public static Runnable selectMarbleRunnable;

  /** displays message in ChatBox in JavaFX Thread */
  public static Runnable appendTextRunnable;

  /** refreshes last played card in JavaFX Thread */
  public static Runnable refreshPlayedCardRunnable;

  /** refreshes other players card number in JavaFX Thread */
  public static Runnable refreshOtherCardsRunnable;

  /** donates a card chose by the player in JavaFX Thread */
  public static Runnable donateCardRunnable;

  /** displays a message that some player has won */
  public static Runnable playerHasWonRunnable;

  /** displays whose turn it is */
  public static Runnable whoseTurnRunnable;

  /** contains indices in cardImages array linking to user's hand */
  private static int[] indexInCardImages = new int[6];

  /** message which will be showed in ChatBox */
  private static String message;

  /** text area which contains received messages */
  private static TextArea chatBox;

  /** contains this player's NewClient object */
  private static NewClient newClient;

  /** reports whether GUI was created or not */
  private static boolean guiCreated = false;

  /** contains every player's name */
  private static String[] playerNames = new String[5];

  /** contains every player's name */
  private static ArrayList<StringProperty> playerNamesProp = new ArrayList<StringProperty>();

  /** the id of the current player */
  private static int whoseTurnID;

  /** reports whether whisper button was clicked or not */
  private static boolean whisperClicked;

  /** pane which contains covered cards of the player sitting against you */
  private static HBox cardsTopPane;

  /** pane which contains player's own hand */
  private static HBox cardsBottomPane;

  /** pane which contains covered cards of the player sitting on your left */
  private static VBox cardsLeftPane;

  /** pane which contains covered cards of the player sitting on your right */
  private static VBox cardsRightPane;

  /** pane which contains all game elements on the left of the window */
  private static BorderPane gamePane;

  /** pane which contains the board and the marbles */
  private static StackPane boardPane;

  /** pane which contains info and chat elements ont he right of the window */
  private static VBox infoAndChatPane;

  /** pane which contains all the temporarily displayed information to the user */
  private static VBox infoPane;

  /** pane which displays all the elements relevant to chatting */
  private static HBox chatPane;

  /** pane which contains the chat box and the field to enter player's message */
  private static VBox chatBoxPane;

  /** pane which contains all the buttons on the right of the chat box */
  private static VBox chatButtonsPane;

  /** button which sends a chat message when clicked */
  private static Button chatButton;

  /** button which sends a private message when clicked */
  private static Button whisperButton;

  /** button which sends a broadcast message when clicked */
  private static Button broadcastButton;

  /** button which displays the high score list when clicked */
  private static Button listButton;

  /** button which lets you change your name to the desired one */
  private static Button changeNameButton;

  /** ImageView which contains board image */
  private static ImageView boardImageView;

  /** Label which shows whose turn it is */
  private static Label whoseTurnLabel;

  /** contains its own ClientGui object */
  private static ClientGui clientGui;

  /** sound which will be played when a button is clicked */
  public static Sound buttonClickSound;

  /** sound which will be played when a card is clicked */
  public static Sound cardClickSound;

  /** sound which will be played when a marble is clicked */
  public static Sound marbleClickSound;

  /** appears when an illegal card is selected after playing "Joker" */
  private static Label notLegalModeJokerLabel;

  /** shows whether it is the first GUI was created in this session */
  private static boolean onceCreated = false;

  /** a whisper message will be sent to the player on your right when this button is clicked */
  private static Button player2Button;

  /** a whisper message will be sent to the player in front of you when this button is clicked */
  private static Button player3Button;

  /** a whisper message will be sent to the player on your left when this button is clicked */
  private static Button player4Button;

  /**
   * opens the window with its functionality; this will be launched only if GUI is being created for
   * the first time in the session
   *
   * @param stage the stage to be drawn upon
   */
  @Override
  public void start(Stage stage) throws Exception {
    onceCreated = true;
    repeatableStart(stage);
  }

  /**
   * actually opens the window with its functionality; this will be launched everytime a player
   * enters a game
   *
   * @param stage the stage to be drawn upon
   */
  public static void repeatableStart(Stage stage) {

    LOGGER.debug("MG: start(STAGE)");

    // this makes that JavaFX thread won't be closed
    // after closing GUI window
    Platform.setImplicitExit(false);

    // creates all required variables
    player2Button = new Button();
    player3Button = new Button();
    player4Button = new Button();
    a = true;
    b = true;
    cardsTopPane = new HBox();
    cardsBottomPane = new HBox();
    cardsLeftPane = new VBox();
    cardsRightPane = new VBox();
    gamePane = new BorderPane();
    boardPane = new StackPane();
    infoAndChatPane = new VBox();
    infoPane = new VBox();
    chatPane = new HBox();
    chatBoxPane = new VBox();
    chatButtonsPane = new VBox();
    whisperClicked = false;
    marblePane = new Pane();
    GuiInteraction.setIsModeLegal(true);
    GuiInteraction.setDonationHappened(false);

    // initializes sound library and imports sound files
    TinySound.init();
    buttonClickSound = TinySound.loadSound("sounds/button_click.wav");
    cardClickSound = TinySound.loadSound("sounds/card_click.wav");
    marbleClickSound = TinySound.loadSound("sounds/marble_click.wav");

    // label which is displayed when a player chooses an illegal card
    // after choosing Joker
    notLegalModeJokerLabel =
        new Label("You have selected the card incorrectly. Please select again.");

    // assigns the stage value to the reference "window"
    window = stage;

    // sets window and windowBarHeight variables to GuiUpdater
    GuiUpdater.setWindow(window);
    GuiUpdater.setWindowBarHeight(windowBarHeight);

    // manages the action when the MainGui is closed
    window.setOnCloseRequest(
        event -> {
          newClient.sendCommandToServer("CLOSE ");
          closeGui();
        });

    // sets the central alignment for the information and buttons displayed in the infoPane
    infoPane.setAlignment(Pos.TOP_CENTER);

    // creates the buttons with appropriate text in them
    chatButton = new Button("Chat");
    whisperButton = new Button("Whisper");
    broadcastButton = new Button("Broadcast");
    listButton = new Button("List");
    changeNameButton = new Button("Change Name");

    // creates all necessary layouts
    HBox mainPane = new HBox();

    // this pane is showed when joker is selected
    FlowPane cardPane = new FlowPane();

    // actually refreshes the marbles via JavaFX Thread
    refreshMarblesRunnable =
        () -> {
          Platform.runLater(
              () -> {
                GuiUpdater.setMarbleCoordinates(marblePane);
              });
        };

    // actually refreshes player's hand via JavaFX Thread
    refreshHandRunnable =
        () -> {
          Platform.runLater(
              () -> {
                LOGGER.debug("setHand will be called");
                GuiUpdater.setHand(cardsBottomPane, indexInCardImages);
                LOGGER.debug("setHand was called, noCards will be called");
                GuiUpdater.noCards(
                    cardsBottomPane,
                    cardsTopPane,
                    cardsLeftPane,
                    cardsRightPane,
                    whoseTurnID,
                    playerNames,
                    indexInCardImages);
                LOGGER.debug("noCards was called");
              });
        };

    // let's the user select the card via JavaFX Thread
    selectCardRunnable =
        () -> {
          Platform.runLater(
              () -> {
                GuiInteraction.selectCard(infoPane, indexInCardImages, cardsBottomPane);
              });
        };

    // let's the user select the mode via JavaFX Thread
    selectModeRunnable =
        () -> {
          Platform.runLater(
              () -> {
                GuiInteraction.selectMode(infoPane, cardPane, notLegalModeJokerLabel);
              });
        };

    // let's the user select the marble via JavaFX Thread
    selectMarbleRunnable =
        () -> {
          Platform.runLater(
              () -> {
                GuiInteraction.selectMarble(marblePane, infoPane, marblePositions);
              });
        };

    // let's the user to choose whether to move into the goal or not via JavaFX Thread
    chooseGoalRunnable =
        () -> {
          Platform.runLater(
              () -> {
                GuiInteraction.chooseGoal(infoPane);
              });
        };

    // prints out the message in the chat box via JavaFX Thread
    appendTextRunnable =
        () -> {
          Platform.runLater(
              () -> {
                GuiUpdater.appendText(message, chatBox);
              });
        };

    // refreshes last played card in the middle of the board via JavaFX Thread
    // also refreshes player names in case someone has changed his/her name
    refreshPlayedCardRunnable =
        () -> {
          Platform.runLater(
              () -> {
                setPlayerNames();
                GuiUpdater.refreshPlayedCard(marblePane);
                LOGGER.debug(" refreshPlayedCard was run");
              });
        };

    // refreshes other players' cards via JavaFX Thread
    refreshOtherCardsRunnable =
        () -> {
          Platform.runLater(
              () -> {
                GuiUpdater.refreshOtherCards(
                    marblePane, cardsTopPane, cardsLeftPane, cardsRightPane, playerNames);
                GuiUpdater.noCards(
                    cardsBottomPane,
                    cardsTopPane,
                    cardsLeftPane,
                    cardsRightPane,
                    whoseTurnID,
                    playerNames,
                    indexInCardImages);
              });
        };

    // lets the player to choose which card to donate via JavaFX Thread
    donateCardRunnable =
        () -> {
          Platform.runLater(
              () -> {
                GuiInteraction.setDonationHappened(false);
                GuiInteraction.donateCard(infoPane, cardsBottomPane, indexInCardImages);
              });
        };

    // displays the message that one of the player's has won via JavaFX Thread
    playerHasWonRunnable =
        () -> {
          Platform.runLater(
              () -> {
                GuiInteraction.hasWonWindow();
              });
        };

    // displays the information whose turn it is via JavaFX Thread
    whoseTurnRunnable =
        () -> {
          Platform.runLater(
              () -> {
                LOGGER.debug("It is " + whoseTurnID + " player's turn from whoseTurnRunnable");
                GuiUpdater.whoseTurn(whoseTurnID, playerNumber, infoPane);
              });
        };

    // imports and sets board image
    Image boardImage =
        new Image(
            MainGui.class.getClassLoader().getResource("pictures/board.jpg").toString());
    // Image boardImage = new Image("file:./src/main/resources/pictures/board.jpg");
    boardImageView = new ImageView(boardImage);
    boardImageView.setPreserveRatio(true);

    // aligns the layout that all cards would be properly aligned
    cardsTopPane.setAlignment(Pos.CENTER);
    cardsBottomPane.setAlignment(Pos.CENTER);
    cardsLeftPane.setAlignment(Pos.CENTER);
    cardsRightPane.setAlignment(Pos.CENTER);

    // creates all 16 marbles
    GuiUpdater.initializeMarbles(marblePane);

    // stacks board and marbles on each other and adds them to the boardPane
    boardPane.getChildren().addAll(boardImageView, marblePane);

    // puts card layouts and board layout in certain sections (BorderPane sections used for that)
    gamePane.setCenter(boardPane);
    gamePane.setTop(cardsTopPane);
    gamePane.setBottom(cardsBottomPane);
    gamePane.setLeft(cardsLeftPane);
    gamePane.setRight(cardsRightPane);

    // sets the style of the mainPane (basically the whole scene)
    mainPane.setStyle("-fx-background-color: #D2EBE8;");

    // creates the text area where messages will be displayed
    chatBox = new TextArea();

    // makes it not possible to write in the chat box
    chatBox.setEditable(false);

    // creates and sets all buttons and labels required for chatting
    Label messageLabel = new Label("Enter your message:");
    Label chatMethodLabel = new Label("Choose the way to send your message:");
    Label recipientLabel = new Label("Choose the player who will get the message:");

    // text field in which the player can write his/her message
    TextField messageField = new TextField();

    for (int i = 0; i < playerNames.length; i++) {
      LOGGER.debug("PlayerNames in start(): " + i + " " + playerNames[i]);
    }

    // broadcasts the message to all groups when clicked
    broadcastButton.setOnAction(
        event -> {
          buttonClickSound.play();
          String messageText = messageField.getText();
          if (messageText != null && messageText.length() > 0) {
            newClient.sendCommandToServer("BROADCAST " + messageText);
            messageField.setText("");
          }
        });

    // new name of player
    changeNameButton.setOnAction(
        event -> {
          buttonClickSound.play();
          String messageText = messageField.getText();
          if (messageText != null && messageText.length() > 0) {
            newClient.sendCommandToServer("CHANGE " + messageText);
            messageField.setText("");
          }
        });

    // sends the message to the members of this group when clicked
    chatButton.setOnAction(
        event -> {
          buttonClickSound.play();
          String messageText = messageField.getText();
          if (messageText != null && messageText.length() > 0) {
            newClient.sendCommandToServer("CHAT " + messageText);
            messageField.setText("");
          }
        });

    // shows the high score list when clicked
    listButton.setOnAction(
        event -> {
          buttonClickSound.play();
          newClient.sendCommandToServer("LOBBY");
        });
    Label listLabel = new Label("Click to see the list:");

    // expands or minimizes player list to choose from when clicked
    whisperButton.setOnAction(
        event -> {
          buttonClickSound.play();
          if (!whisperClicked) {
            chatButtonsPane
                .getChildren()
                .removeAll(broadcastButton, changeNameButton, listLabel, listButton);
            chatButtonsPane
                .getChildren()
                .addAll(
                    recipientLabel,
                    player2Button,
                    player3Button,
                    player4Button,
                    broadcastButton,
                    changeNameButton,
                    listLabel,
                    listButton);
            whisperClicked = true;
          } else {
            chatButtonsPane
                .getChildren()
                .removeAll(recipientLabel, player2Button, player3Button, player4Button);
            whisperClicked = false;
          }
        });

    // creates a button which sends a private message to the second player when clicked
    whisperMessage(messageField, player2Button, 2);

    // creates a button which sends a private message to the third player when clicked
    whisperMessage(messageField, player3Button, 3);

    // creates a button which sends a private message to the fourth player when clicked
    whisperMessage(messageField, player4Button, 4);

    // adds all required nodes to chatBoxPane
    chatBoxPane.getChildren().addAll(chatBox, messageLabel, messageField);

    // adds all required nodes to chatButtonsPane
    chatButtonsPane
        .getChildren()
        .addAll(
            chatMethodLabel,
            chatButton,
            whisperButton,
            broadcastButton,
            changeNameButton,
            listLabel,
            listButton);

    // adds all required nodes to chatPane
    chatPane.getChildren().addAll(chatBoxPane, chatButtonsPane);
    infoAndChatPane.getChildren().addAll(infoPane, chatPane);

    // creates required panes for displaying UniBas logos
    // and imports Unibas logo
    StackPane stackPane = new StackPane();
    Pane logoPane = new Pane();
    Image logoImage = new Image(MainGui.class.getClassLoader().getResource("pictures/logo.jpg").toString());


    // adds logoPane and gamePane to stackPane
    stackPane.getChildren().addAll(logoPane, gamePane);
    // adds gamePane and infoAndChatPane to the mainPane
    mainPane.getChildren().addAll(stackPane, infoAndChatPane);

    // sets the scene
    Scene scene = new Scene(mainPane, 1024, 576);

    // sets the title of the window
    window.setTitle("UniBas Dog");

    // adds the main scene to the window
    window.setScene(scene);

    // shows the window
    window.show();

    // manages the position of different elements in GUI when full screen is turned on or off
    window
        .fullScreenProperty()
        .addListener(
            (observable) -> {
              windowBarHeight.set(window.getHeight() - scene.getHeight());

              LOGGER.debug("Fullscreen was enabled or disabled");
              LOGGER.debug("windowBarHeight is " + windowBarHeight);
            });

    // rotates the board according to the player number
    boardImageView.setRotate((playerNumber - 1) * 90);

    // displays marbles in correct starting position
    GuiUpdater.setMarbleCoordinates(marblePane);

    // imports card images and puts them into an array
    GuiUpdater.initializeCards();

    // displays all 12 cards when joker is played
    GuiUpdater.displayCardsAfterJoker(cardPane);

    // sets appropriate positions for all 4 UniBas logos
    double logoPositionX = 0;
    double logoPositionY = 0;
    for (int i = 0; i < 4; i++) {
      switch (i) {
        case 0:
          logoPositionX = 0.01;
          logoPositionY = 0.01;
          break;
        case 1:
          logoPositionX = 0.01;
          logoPositionY = 0.89;
          break;
        case 2:
          logoPositionX = 0.89;
          logoPositionY = 0.01;
          break;
        case 3:
          logoPositionX = 0.89;
          logoPositionY = 0.89;
          break;
      }
      ImageView logoImageView = new ImageView(logoImage);
      logoImageView.setPreserveRatio(true);
      logoImageView.xProperty().bind(height.multiply(logoPositionX));
      logoImageView.yProperty().bind(height.multiply(logoPositionY));
      logoImageView.fitHeightProperty().bind(height.multiply(0.1));
      logoPane.getChildren().add(logoImageView);
    }

    // refreshes other players' cards for the first time
    GuiUpdater.refreshOtherCards(
        marblePane, cardsTopPane, cardsLeftPane, cardsRightPane, playerNames);

    // sets player's cards for the first time
    GuiUpdater.setHand(cardsBottomPane, indexInCardImages);

    // shows if any players have their cards discarded
    GuiUpdater.noCards(
        cardsBottomPane,
        cardsTopPane,
        cardsLeftPane,
        cardsRightPane,
        whoseTurnID,
        playerNames,
        indexInCardImages);

    // changes window's height if width is changed
    stage
        .widthProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (b) {
                stage.setHeight(stage.getWidth() / 16 * 9 - windowBarHeight.get());
                a = false;
              } else b = true;
            });

    // changes window's width if height is changed
    stage
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (a) {
                stage.setWidth((stage.getHeight() + windowBarHeight.get()) / 9 * 16);
                b = false;
              } else a = true;
            });

    // binds all elements of GUI so that it would change accordingly
    // when the size of the window is changed
    windowBarHeight.set(window.getHeight() - scene.getHeight());
    height.bind(window.heightProperty().subtract(windowBarHeight));
    westWidth.bind(window.widthProperty().subtract(height));
    stackPane.prefHeightProperty().bind(height);
    stackPane.prefWidthProperty().bind(height);
    logoPane.prefHeightProperty().bind(height);
    logoPane.prefWidthProperty().bind(height);
    boardImageView.fitHeightProperty().bind(height.multiply(0.7));
    gamePane.prefHeightProperty().bind(height);
    gamePane.prefWidthProperty().bind(height);
    boardPane.prefHeightProperty().bind(height.multiply(0.7));
    boardPane.prefWidthProperty().bind(height.multiply(0.7));
    cardsTopPane.prefHeightProperty().bind(height.multiply(0.15));
    cardsTopPane.prefWidthProperty().bind(height);
    cardsBottomPane.prefHeightProperty().bind(height.multiply(0.15));
    cardsBottomPane.prefWidthProperty().bind(height);
    cardsLeftPane.prefHeightProperty().bind(height.multiply(0.7));
    cardsLeftPane.prefWidthProperty().bind(height.multiply(0.15));
    cardsRightPane.prefHeightProperty().bind(height.multiply(0.7));
    cardsRightPane.prefWidthProperty().bind(height.multiply(0.15));
    chatPane.prefHeightProperty().bind(height.multiply(0.5));
    infoPane.prefHeightProperty().bind(height.multiply(0.5));
    chatBoxPane.prefWidthProperty().bind(westWidth.divide(2));
    chatButtonsPane.prefWidthProperty().bind(westWidth.divide(2));
    chatButton.prefWidthProperty().bind(westWidth.divide(2));
    broadcastButton.prefWidthProperty().bind(westWidth.divide(2));
    changeNameButton.prefWidthProperty().bind(westWidth.divide(2));
    whisperButton.prefWidthProperty().bind(westWidth.divide(2));
    listButton.prefWidthProperty().bind(westWidth.divide(2));

    // updates names displayed on the buttons in case a player changes
    // his/her name
    player2Button.textProperty().bind(playerNamesProp.get(2));
    player3Button.textProperty().bind(playerNamesProp.get(3));
    player4Button.textProperty().bind(playerNamesProp.get(4));

    // whoseTurnID = 1;
    LOGGER.debug("whoseTurnID from start() is " + whoseTurnID);
    GuiUpdater.whoseTurn(whoseTurnID, playerNumber, infoPane);

    // notifies CommandLineInterface object that the GUI was created
    if (cli.isCliCreated()) {
      synchronized (cli) {
        cli.notify();
      }
    }

    // GUI window is now created
    guiCreated = true;

    LOGGER.debug("MG: got to end of start()");
  }

  /** runs some required commands before GUI window is closed */
  public static void closeGui() {
    TinySound.shutdown();
    clientGui.setFrameVisible();
    clientGui.enableButtons();
    guiCreated = false;
    newClient.setGuiCreated(false);
    window.close();
  }

  /**
   * sets the player number
   *
   * @param player the player which this Gui belongs to
   */
  public void setPlayerNumber(int player) {
    playerNumber = player;
  }

  /* returns whether GUI was created or not */
  public static boolean isGuiCreated() {
    return guiCreated;
  }

  /* sets new message value*/
  public static void setMessage(String msg) {
    message = msg;
  }

  /* sets the new client which belongs to this player*/
  public void setNewClient(NewClient newClient) {
    MainGui.newClient = newClient;
  }

  /* sets the player names*/
  public static void setPlayerNames() {
    String[] oldPlayerNames = rc.getPlayerNames();
    String[] playerNames = new String[oldPlayerNames.length];
    for (int i = 0; i < oldPlayerNames.length; i++) {
      playerNames[i] = oldPlayerNames[i];
    }
    String temp;
    for (int j = 1; j < playerNumber; j++) {
      temp = playerNames[1];
      for (int i = 1; i < 4; i++) {
        playerNames[i] = playerNames[i + 1];
      }
      playerNames[4] = temp;
    }
    LOGGER.debug("start of setPlayerNames in MainGui");
    LOGGER.debug("playerNames.length is " + playerNames.length);
    if (playerNamesProp.size() == 0) {
      for (int i = 0; i < playerNames.length; i++) {
        StringProperty temp1 = new SimpleStringProperty();
        playerNamesProp.add(temp1);
      }
    }
    for (int i = 0; i < playerNames.length; i++) {
      MainGui.playerNames[i] = playerNames[i];
      playerNamesProp.get(i).set(playerNames[i]);
    }
    for (int i = 0; i < playerNames.length; i++) {
      LOGGER.debug("playerNamesProp is " + i + " " + playerNamesProp.get(i).getValue());
      LOGGER.debug("playerNames is " + i + " " + playerNames[i]);
    }
  }

  /**
   * sends when private message to the chosen player when called
   *
   * @param messageField the message which will be sent
   * @param playerButton the button which will be cicked
   * @param playerNumber the player number which the message will be sent to
   */
  public static void whisperMessage(TextField messageField, Button playerButton, int playerNumber) {
    playerButton.setOnAction(
        event -> {
          MainGui.buttonClickSound.play();
          String messageText = messageField.getText();
          if (messageText != null && messageText.length() > 0) {
            MainGui.getNewClient()
                .sendCommandToServer("WHISPER " + playerNames[playerNumber] + " " + messageText);
            messageField.setText("");
          }
        });
  }

  /**
   * sets whoseTurnLabel its new value
   *
   * @param whoseTurnLabel show whose turn it is
   */
  public static void setWhoseTurnLabel(Label whoseTurnLabel) {
    MainGui.whoseTurnLabel = whoseTurnLabel;
  }

  /**
   * gets the newClient object
   *
   * @return newClient object
   */
  public static NewClient getNewClient() {
    return newClient;
  }

  /**
   * gets whoseTurnLabel value
   *
   * @return whoseTurnLabel value
   */
  public static Label getWhoseTurnLabel() {
    return whoseTurnLabel;
  }

  /**
   * sets the new value to indexInCardImages
   *
   * @param indexInCardImages the new value of indexInCardImages
   */
  public static void setIndexInCardImages(int[] indexInCardImages) {
    MainGui.indexInCardImages = indexInCardImages;
  }

  /**
   * sets the new value to marblePositions
   *
   * @param marblePositions the new value of marblePositions
   */
  public static void setMarblePositions(int[] marblePositions) {
    MainGui.marblePositions = marblePositions;
  }

  /**
   * report whether GUI was once created in the same session
   *
   * @return the answer whether GUI was created or not
   */
  public static boolean isOnceCreated() {
    return onceCreated;
  }

  /**
   * sets the ID number of the player whose turn it is
   *
   * @param whoseTurnID player's ID whose turn it is
   */
  public static void setWhoseTurnID(int whoseTurnID) {
    MainGui.whoseTurnID = whoseTurnID;
  }

  /**
   * sets the rulesClient object to the provided one
   *
   * @param rulesClient the new RulesClient object the MainGui is to be associated with
   */
  public void setRulesClient(RulesClient rulesClient) {

    LOGGER.debug("MG: setRulesClient()");

    this.rc = rulesClient;
    GuiInteraction.setRulesClient(rulesClient);
  }

  /**
   * sets the CommandLineInterface object to the provided one
   *
   * @param cli the new CommandLineInterface object the MainGui is to be associated with
   */
  public void setCli(CommandLineInterface cli) {
    this.cli = cli;
    GuiInteraction.setCli(cli);
  }

  /**
   * returns this player's Rules Client
   *
   * @return this MainGui's RulesClient
   */
  public RulesClient getRulesClient() {
    return rc;
  }

  /**
   * sets its own ClientGui
   *
   * @param cg MainGui's own ClientGui object
   */
  public static void setClientGui(ClientGui cg) {
    clientGui = cg;
  }
}
