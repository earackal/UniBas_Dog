package general.cards;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.RulesServer;

/** class for creating a deck object */
public class Deck {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(Deck.class);

  /** array for keeping cards in a deck */
  private Card[] cards;

  /** holds the index of the first card which has not been dealt */
  private int positionInDeck;

  /** holds the RulesClient object the deck is associated with */
  private RulesServer rs;

  /** constructor: creates a new array for keeping cards in a deck, cards get set */
  public Deck() {
    this.cards = new Card[110];
    create();
  }

  /**
   * constructor: creates new array of cards, also sets rs object
   *
   * @param rulesS a RulesServer object
   */
  public Deck(RulesServer rulesS) throws IllegalArgumentException{

    if (rulesS == null) {
      throw new IllegalArgumentException();
    }

    LOGGER.debug("DE: constructor was called");
    cards = new Card[110];
    create();
    rs = rulesS;
  }

  /** creates all the cards of a deck */
  public void create() {

    LOGGER.debug("DE: create()");

    int id = 0; // initialising the unique identifier of a card
    char suit; // 4 main card suits
    /* the game's card deck contains two standard 52-card decks */
    for (int k = 0; k < 2; k++) {
      /* card values from 2 to 10, Ace = 1, Jack = 11, Queen = 12, King = 13 */
      for (int i = 1; i < 14; i++) {
        /* 4 main card suits */
        for (int j = 1; j < 5; j++) {
          if (j == 1) {
            suit = 'c'; // clubs
          } else if (j == 2) {
            suit = 's'; // spades
          } else if (j == 3) {
            suit = 'h'; // hearts
          } else {
            suit = 'd'; // diamonds
          }
          /* creates a new card with its parameters, i indicates card's value */
          Card card = new Card(id, i, suit);
          cards[id] = card; // adds the newly created card to the card deck
          id++; // prepares the new id value for the next card
        }
      }
    }
    /* adds 6 Joker cards to the deck */
    for (int k = 0; k < 6; k++) {
      suit = 'j'; // Joker
      Card card = new Card(id, 0, suit); // creates a new Joker card
      cards[id] = card; // adds the new Joker card to the deck
      id++; // prepares the new id value for the next Joker card
    }

    positionInDeck = 0;

    StringBuilder sb = new StringBuilder();
    sb.append("DE: created Deck is: ");
    for (int i = 0; i < cards.length; i++) {
      sb.append(cards[i].valueToString());
    }
    LOGGER.debug(sb);
  }

  /**
   * gives each card in deck a random new position, by drawing random cards from original deck and
   * putting it in a new one
   */
  public void shuffle() {

    LOGGER.debug("DE: shuffle()");

    int index; // holds random index
    Random random = new Random();
    int len = cards.length;
    Card[] newDeck = new Card[len];

    for (int i = 0; i < newDeck.length; i++) { // pull cards until newDeck is full

      index = random.nextInt(len);
      newDeck[i] = cards[index]; // pull card from cards

      Card[] temp = new Card[len - 1];
      for (int j = 0; j < index; j++) { // copy previous cards
        temp[j] = cards[j];
      }
      for (int j = index; j < len - 1; j++) { // order past cards
        temp[j] = cards[j + 1];
      }
      cards = temp;
      len = cards.length;
    }

    cards = newDeck;

    positionInDeck = 0;

    StringBuilder stringBuilder = new StringBuilder("DE: shuffled Deck is: ");
    for (int i = 0; i < cards.length; i++) {
      stringBuilder.append(cards[i].valueToString());
    }
    LOGGER.debug(stringBuilder);
  }

  /**
   * hands out new cards to the players, jokers only if cheats are on
   *
   * @param numOfCards the number of cards to be dealt per hand
   */
  public void dealCards(int numOfCards) throws IllegalArgumentException {

    if (numOfCards < 2 || numOfCards > 6) {
      throw new IllegalArgumentException();
    }

    LOGGER.debug("DE: dealCards(" + numOfCards + ")");

    shuffle();
    // demoShuffle();

    rs.setPlayedCards(null); // reset card pile

    for (int player = 1; player <= rs.getNumberOfPlayers(); player++) { // iterate through players

      Hand newHand = new Hand();
      Card[] newCards = new Card[numOfCards];
      if (rs.cheatsAreOn[player]) { // always deals hand of jokers if cheats are on
        for (int j = 0; j < numOfCards; j++) { // iterate through cards in hand
          newCards[j] = new Card(666, 0, 'j');
        }

      } else {
        for (int j = 0; j < numOfCards; j++) { // iterate through cards in hand
          newCards[j] = drawCard();
        }
      }
      newHand.setHand(newCards);

      rs.setPlayerHand(player, newHand);
    }

    LOGGER.debug("Player hands are: ");
    for (int i = 1; i <= rs.getNumberOfPlayers(); i++) {
      LOGGER.debug("Player " + i + ": " + rs.getPlayerHand(i).valuesInHandToString());
    }
  }

  /**
   * draws the next card from the deck
   *
   * @return the card that has been drawn
   */
  public Card drawCard() {

    LOGGER.debug("DE: drawCard()");
    if (positionInDeck >= cards.length) {
      positionInDeck = 0;
    }
    Card card = cards[positionInDeck];
    positionInDeck++;
    LOGGER.debug("DE: drawn card: " + card.cardAsString());

    return card;
  }

  /**
   * returns the whole card deck
   *
   * @return array of cards in deck
   */
  public Card[] getCards() {
    return this.cards;
  }

  /**
   * returns the whole deck in one string with each card in a new line
   *
   * @return String of all cards, separated by newline
   */
  public String toString() {

    String nameOfCards = ""; // multiple-line String variable with all the cards listed in it
    for (int i = 0; i < 110; i++) {
      nameOfCards =
          nameOfCards
              + "ID: "
              + cards[i].getId()
              + ", suit: "
              + cards[i].getSuit()
              + ", value: "
              + cards[i].getValue()
              + "\n";
    }

    return nameOfCards;
  }
}
