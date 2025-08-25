package general;

/**
 * protocol orders includes all important commands which can be necessary for the client to play the
 * game, to change settings(Username) or to communicate with other clients/server.
 */
public enum Protocol {

  /**
   * JOINGROUP : array[0] = "JOINGROUP", array[1] = name of the existing group, the rest will be
   * ignored
   */
  JOINGROUP,

  /**
   * CREATEGROUP : array[0] = "CREATEGROUP", array[1] = unchecked name of the group, the rest will
   * be ignored
   */
  CREATEGROUP,

  /** LOBBY : array[0] = "LOBBY", the rest will be ignored */
  LOBBY,

  /** CHANGE : array[0] = "CHANGE", array[1] = the new, unchecked name */
  CHANGE,

  /** STATE : array[0] = "STATE", the rest will be the encoded version of the board arrays */
  STATE,

  /** GETLOBBY : array[0] = "GETLOBBY", the rest will be all the group names in the lobby */
  GETLOBBY,

  /** PLAYEDCARDS : array[0] = "PLAYEDCARDS", the rest value of cards */
  PLAYEDCARDS,

  /** YOURTURN : array[0] = "YOURTURN", the rest will be ignored */
  YOURTURN,

  /** WHOSETURN : array[0] = "WHOSETURN", array[1] = "playerID of the current player" */
  WHOSETURN,

  /** HAND : array[0] = "HAND", the rest will be the encoded version of the cards in the hand */
  HAND,

  /** PLAYERID : array[0] = "PLAYERID", array[1] = id as String */
  PLAYERID,

  /** CHAT : array[0] = "CHAT", the rest: message */
  CHAT,

  /** BROADCAST : array[0] = "BROADCAST", the rest: message */
  BROADCAST,

  /**
   * WHISPER : array[0] = "WHISPER", array[1] name of client who should get the message, the rest:
   * the message
   */
  WHISPER,

  /**
   * PLAYERNAMES : array[0] = "PLAYERNAMES", the rest will be the names of the groupmembers as a
   * string
   */
  PLAYERNAMES,

  /**
   * NUMOFCARDS : array[0] = "NUMOFCARDS", the rest 0 x y z w where xyzw number of cards in hand of
   * players 1-4
   */
  NUMOFCARDS,

  /** CHAT Server: This command can not be executed by the server */
  DEFAULT,

  /** ENDGAME : array[0] = "ENDGAME" */
  ENDGAME,

  /**CLOSE : array[0] = "CLOSE" */
  CLOSE,

  /** GETOUT : array[0] = "GETOUT" */
  GETOUT,

  /** LOGOUT : array[0] = "LOGOUT" the rest will be ignored */
  LOGOUT
}
