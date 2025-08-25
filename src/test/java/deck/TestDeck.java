package deck;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import general.cards.Card;
import general.cards.Deck;
import server.RulesServer;

import static org.junit.Assert.assertEquals;

public class TestDeck {

  @Test
  public void constructorWorks() {

    Deck deck = new Deck();

  }

  /** tests if a card can be drawn from the deck. */
  @Test
  public void testDrawCard() {

    Deck deck = new Deck();
    Card drawnCard = deck.drawCard();
    boolean isNotNull = (drawnCard != null);
    assertEquals(true, isNotNull);
  }

  /**
   * tests the shuffle function. Can theoretically fail if the deck is shuffled in the exact same
   * way. The chance of that happening is incredibly small, however. ~(1/110!)
   */
  @Test
  public void testShuffle() {

    Deck deck = new Deck();
    Deck shuffledDeck = new Deck();
    shuffledDeck.shuffle();

    int len = shuffledDeck.getCards().length;
    boolean[] isSameCard = new boolean[len];
    Card[] cards = deck.getCards();
    Card[] shuffledCards = shuffledDeck.getCards();

    for (int i = 0; i < len; i++) {
      boolean hasSameId = (cards[i].getId() == shuffledCards[i].getId());
      boolean hasSameVal = (cards[i].getValue() == shuffledCards[i].getValue());
      boolean hasSameSuit = (cards[i].getSuit() == shuffledCards[i].getSuit());
      isSameCard[i] = (hasSameId && hasSameVal && hasSameSuit);
    }

    boolean isSameDeck = true;
    for (int i = 0; i < len; i++) { // check if all cards are the same
      isSameDeck = (isSameDeck && isSameCard[i]);
    }

    assertEquals(false, isSameDeck);
  }

  @Test
  public void drawTooManyCards() {

    Deck deck = new Deck();
    Card card = null;

    for (int i = 0; i < 150; i++) {
      card = deck.drawCard();
    }
    boolean cardIsNull = true;
    if (card != null) {
      cardIsNull = false;
    }
    assertEquals(false, cardIsNull);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void constructorArgumentIsNull() {
    thrown.expect(IllegalArgumentException.class);
    RulesServer rs = null;
    Deck deck = new Deck(rs);
  }

  @Test
  public void dealTooManyCards() {

    thrown.expect(IllegalArgumentException.class);
    Deck deck = new Deck();
    deck.dealCards(42);
  }

  @Test
  public void dealNoCards() {

    thrown.expect(IllegalArgumentException.class);
    Deck deck = new Deck();
    deck.dealCards(0);
  }

  @Test
  public void dealOneCards() {

    thrown.expect(IllegalArgumentException.class);
    Deck deck = new Deck();
    deck.dealCards(1);
  }

  @Test
  public void dealNegativeCards() {

    thrown.expect(IllegalArgumentException.class);
    Deck deck = new Deck();
    deck.dealCards(-42);
  }

  @Test
  public void nullToString() {
    thrown.expect(NullPointerException.class);
    Deck deck = null;
    deck.toString();
  }


}
