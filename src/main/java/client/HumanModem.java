package client;

/**
 * This class tries to interpret the input, meaning that the user does not have to write the exact
 * command to get what they want
 */
public class HumanModem {

  /**
   * This method returns the transformed, correct command as String to the Client. Functionality:
   *
   * <p>CHAT : array[0] = options to call the command CHAT, the rest: message.
   *
   * <p>BROADCAST : array[0] = options to call the command BROADCAST, the rest: message.
   *
   * <p>LOBBY : array[0] = options to call the command LOBBY, the rest will be ignored.
   *
   * <p>LOGOUT : array[0] = options to call the command LOGOUT the rest will be ignored.
   *
   * <p>WHISPER : array[0] = options to call the command WHISPER, array[1] name of client who should
   * get the message, the rest: the message.
   *
   * <p>CHANGE : array[0] = options to call the command CHANGE, array[1] = the new, unchecked name.
   *
   * <p>CREATEGROUP: array[0] = options to call the command CREATEGROUP, array[1] = unchecked name
   * of the group, the rest will be ignored.
   *
   * <p>JOINGROUP : array[0] = options to call the command JOINGROUP, array[1] = name of the
   * existing group, the rest will be ignored.
   *
   * @param line includes the command and other important inputs like a message
   * @return command includes the correct protocol command and other important inputs
   */
  public static String getCommand(String line) {

    // splits at spaces and saves the words in an String array
    String[] array = line.split(" ");

    // changes every letter from the first word into a lower case one
    array[0] = array[0].toLowerCase();

    // removes everything, which is not a small letter
    array[0] = array[0].replaceAll("[^a-z]", "");
    String command = "";
    // switch to find out the right command
    try {
      switch (array[0]) {
          /*case "say":
          case "sy":
          case "chat":
          case "ch":
          case "cha":
          case "sag":
          case "write":
          case "send":
          case "sendall":
          case "sendmessage":
          case "sendmsg":
          case "tweet":
          case "print":
          case "tell":
          case "talk":

            // chat function activated, array[1] and following must contain the message
            command = "CHAT";
            for (int i = 1; i < array.length; i++) {
              command = command + " " + array[i];
            }
            break;

          case "create":
          case "creategroup":
          case "creategrp":
          case "crtgroup":
          case "crtgrp":
          case "crt":
          case "newgroup":
          case "addgroup":
          case "formgroup":
          case "startgroup":
          case "makegroup":
          case "setupgroup":

            // create group function activated, array[1] must be the name of the group
            command = "CREATEGROUP";
            for (int i = 1; i < array.length; i++) {
              command = command + " " + array[i];
            }
            break;

          case "gotogroup":
          case "join":
          case "joingroup":
          case "joingrp":
          case "addtogroup":
          case "addmetogroup":
          case "putmeingroup":

            // join group function activated, array[1] must be the name of the group
            command = "JOINGROUP";
            for (int i = 1; i < array.length; i++) {
              command = command + " " + array[i];
            }
            break;

          case "broadcast":
          case "toall":
          case "saytoall":
          case "brdcast":
          case "broad":
          case "bcast":
          case "cast":
          case "all":
          case "toeverybody":

            // broadcast function activated, array[1] and following must contain the message
            command = "BROADCAST";
            for (int i = 1; i < array.length; i++) {
              command = command + " " + array[i];
            }
            break;


          case "lobby":
          case "lob":
          case "lobb":
          case "lby":
          case "lb":
          case "playerlist":
          case "list":
          case "listofplayer":
          case "showlist":
          case "showplayerlist":
          case "connectionlist":
          case "connectedplayer":

            // lobby
            command = "LOBBY";
            break;


          case "logout":
          case "signout":
          case "log":
          case "logo":
          case "logou":
          case "sign":
          case "out":
          case "signo":
          case "close":
          case "signou":
          case "exit":
          case "quit":
          case "leave":
          case "terminate":

            // logout from server
            command = "LOGOUT";
            break;*/

          /*case "whisper":
          case "whsp":
          case "whi":
          case "whis":
          case "whisp":
          case "whispe":
          case "whsper":
          case "wper":*/
        case "w":

          // whisper-chat function activated, array[1] will be the name of the player
          // array[2] and following must contain the message
          command = "WHISPER " + array[1];
          for (int i = 2; i < array.length; i++) {
            command = command + " " + array[i];
          }
          break;

          /*case "change":
          case "changename":
          case "chnge":
          case "changenameto":
          case "newname":
          case "changeto":

            // array[1] must be the new name
            command = "CHANGE " + array[1];
            break;*/

        default:
          // array[0] = chat, and the rest will be the message
          command = "CHAT " + line;
          break;
      }
    } catch (IndexOutOfBoundsException e) {
      System.out.println("Missing Input. Please try again.");
    }
    // returns the command ClientGui
    return command;
  }
}
