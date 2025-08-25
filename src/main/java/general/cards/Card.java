package general.cards;

/** class for creating a card object */
public class Card {

  /** unique identifier of a card */
  private final int id;

  /** from 2 to 10, Ace = 1, Jack = 11, Queen = 12, King = 13, Joker = 0 */
  private final int value;

  /** 4 main card suits: 'c'-clubs, 's'-spades, 'h'-hearts, 'd'-diamonds */
  private final char suit;

  /**
   * defines card's id, number and suit when creating a new card
   *
   * @param id unique number of the card
   * @param value value of the card
   * @param suit the suit of the card, as a char
   */
  public Card(int id, int value, char suit) {

    this.value = value;
    this.suit = suit;
    this.id = id;
  }

  /**
   * returns card's main properties (variables) in a string
   *
   * @return String of the cards properties
   */
  public String toString() {
    return "ID: " + id + ", suit: " + suit + ", value: " + value;
  }

  /**
   * outputs a string of how a human would call the card
   *
   * @return the name of the card
   */
  public String cardAsString() {

    if (this == null) {
      return "null";
    }

    String message = "";

    if (value > 10 || value == 0 || value == 1) {

      switch (value) {
        case 1:
          message += "an ace ";
          break;

        case 11:
          message += "the jack ";
          break;

        case 12:
          message += "the queen ";
          break;

        case 13:
          message += "the king ";
          break;

        case 14:
          message += "the ace ";
          break;

        case 0:
          message += "a joker";
          break;
      }

    } else {

      message += "a " + value + " ";
    }

    switch (suit) {
      case 'c':
        message += "of clubs";
        break;

      case 'h':
        message += "of hearts";
        break;

      case 's':
        message += "of spades";
        break;

      case 'd':
        message += "of diamonds";
        break;

      case 'j':
        break;
    }

    return message;
  }

  /**
   * returns card's id
   *
   * @return the id field of the card as int
   */
  public int getId() {

    return this.id;
  }

  /**
   * returns card's value
   *
   * @return the value of the card as int
   */
  public int getValue() {

    return this.value;
  }

  /**
   * returns card's suit
   *
   * @return the char describing the suit
   */
  public char getSuit() {

    return this.suit;
  }

  /**
   * returns the letter/number corresponding to card value
   *
   * @return letter/number of card
   */
  public String valueToString() {

    if (this == null) {
      return "null";
    }

    switch (value) {
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 9:
      case 10:
        return value + " ";

      case 0:
        return "Joker ";

      case 1:
        return "Ace ";

      case 11:
        return "Jack ";

      case 12:
        return "Queen ";

      case 13:
        return "King ";

      default:
        return "not a legal card ";
    }
  }
}
