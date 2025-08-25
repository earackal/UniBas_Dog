package general.cards;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Hand {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(Hand.class);

  /** the number of the owner */
  private int playerNumber;

  /** the array of the cards in the hand */
  private Card[] cardsInHand;

  /**
   * constructor
   *
   * @param player Number of the player
   * @param dealtCards an array of the dealt cards
   */
  public Hand(int player, Card[] dealtCards) {

    playerNumber = player;
    cardsInHand = dealtCards;
  }

  /** constructor */
  public Hand() {
    playerNumber = -1;
    cardsInHand = null;
  }

  /**
   * gets the number of cards in the player's hand
   *
   * @return the length of cardsInHand
   */
  public int numOfCards() {
    try {
      if (cardsInHand != null) {
        return cardsInHand.length;
      } else {
        return 0;
      }
    } catch (NullPointerException e) {
      return 0;
    }
  }

  /**
   * gets the card in position pos from hand
   *
   * @param pos the array index of cardsInHand
   * @return the card at said position
   */
  public Card getCard(int pos) {

    if (cardsInHand == null) {
      return null;
    } else {
      return cardsInHand[pos];
    }
  }

  /**
   * gets the number of the owner of the hand
   *
   * @return player number (1 to 4) of the hand's owner
   */
  public int getOwner() {

    return playerNumber;
  }

  /** deletes cardsInHand and sets it to null */
  public void discardHand() {

    LOGGER.debug("HA: discardHand()");

    cardsInHand = null;
  }

  /**
   * checks if provided card is in hand
   *
   * @param card the card to be checked against cards in hand
   * @return true if card is in hand
   */
  public boolean isInHand(Card card) {

    boolean isInHand = false;
    for (int i = 0; i < cardsInHand.length; i++) {
      boolean sameId = (card.getId() == cardsInHand[i].getId());
      boolean sameVal = (card.getValue() == cardsInHand[i].getValue());
      boolean sameSuit = (card.getSuit() == cardsInHand[i].getSuit());
      isInHand = isInHand || (sameId && sameVal && sameSuit);
    }
    return isInHand;
  }

  /**
   * checks if hand contains a card with chosen value
   *
   * @param value number between 0 and 13
   * @return true if value exists in hand
   */
  public boolean isInHand(int value) {

    boolean isInHand = false;
    for (int i = 0; i < cardsInHand.length; i++) {
      boolean sameValue = (value == cardsInHand[i].getValue());
      isInHand = isInHand || sameValue;
    }
    return isInHand;
  }

  /**
   * removes card from hand and returns it
   *
   * @param card the card to be removed
   */
  public void loseCard(Card card) {

    LOGGER.debug("HA: loseCard(" + card.valueToString() + ")");

    // iterate to card in hand
    int i = 0; // position of played card in hand
    LOGGER.debug(card.cardAsString());
    for (i = 0; i < cardsInHand.length; i++) {
      // boolean sameId = (card.getId() == cardsInHand[i].getId());
      boolean sameVal = (card.getValue() == cardsInHand[i].getValue());
      boolean sameSuit = (card.getSuit() == cardsInHand[i].getSuit());

      LOGGER.debug(cardsInHand[i].cardAsString());
      LOGGER.debug(sameVal + " " + sameSuit);
      if (sameVal && sameSuit) {
        LOGGER.debug("!!!!!!!!!!i = " + i);
        break;
      }
    }

    LOGGER.debug("!!!!!! out of for loop");
    // sculpt new hand
    Card[] newHand = new Card[cardsInHand.length - 1];
    for (int j = 0; j < i; j++) {
      newHand[j] = cardsInHand[j];
    }
    LOGGER.debug("!!!losing card = " + cardsInHand[i].cardAsString());
    for (int j = i + 1; j < cardsInHand.length; j++) {
      newHand[j - 1] = cardsInHand[j];
    }
    cardsInHand = newHand; // new hand with card played

    StringBuilder stringBuilder = new StringBuilder("HA: Cards in hand at end of loseCard(): ");
    for (int k = 0; k < cardsInHand.length; k++) {
      stringBuilder.append(cardsInHand[k].valueToString() + " ");
    }

    LOGGER.debug(stringBuilder.toString());
  }

  /**
   * adds given card to cardsInHand
   *
   * @param card card to be added to hand
   */
  public void addCard(Card card) {

    int length = cardsInHand.length + 1;

    Card[] newHand = new Card[length];

    for (int i = 0; i < length - 1; i++) {
      newHand[i] = cardsInHand[i];
    }
    newHand[length - 1] = card;
    cardsInHand = newHand;
  }

  /**
   * puts the cards provided in the cardsInHand array, usually called via protocol
   *
   * @param newHand the new cards in hand, replace old cards
   */
  public void setHand(Card[] newHand) {

    cardsInHand = newHand;
  }

  /**
   * checks if hand holds any cards
   *
   * @return true if at least one card in hand
   */
  public boolean containsCards() {
    try {
      if (cardsInHand != null) {
        return true;
      } else {
        return false;
      }
    } catch (NullPointerException e) {
      return false;
    }
  }

  /**
   * prints basic representation of values in hand
   *
   * @return String describing the values of the cards in hand
   */
  public String valuesInHandToString() {

    String valuesInHand = "";

    for (int i = 0; i < cardsInHand.length; i++) {
      if (cardsInHand[i] != null) {
        valuesInHand += cardsInHand[i].valueToString();
      }
    }

    return valuesInHand;
  }

  /**
   * gets the first card in hand with the same value as the passed integer
   *
   * @param value the value to be searched for
   * @return a card matching the card, or null if there is none
   */
  public Card getCardWithVal(int value) {

    Card card;
    for (int i = 0; i < cardsInHand.length; i++) {
      card = cardsInHand[i];
      if (value == card.getValue()) {
        return card;
      }
    }
    return null; // no such card in hand
  }

  /**
   * returns the cards in hand of a hand object
   *
   * @return the cardsInHand array
   */
  public Card[] getCardsInHand() {

    if (cardsInHand == null) {
      return null;
    } else {
      return cardsInHand;
    }
  }
}
