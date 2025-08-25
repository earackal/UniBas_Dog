package server;

import java.net.*;
import java.io.*;

/** In this class, a player will be created with its socket and name. */
public class Player {

  /** contains the socket of the client */
  Socket clientSocket;
  /** contains the name of the client */
  String clientName;
  /** contains the name of the client */
  String groupName;
  /** output stream to player */
  OutputStream out;

  /**
   * This is the constructor which creates an object of Player.
   *
   * @param clientSocket the socket to be used for the connection
   * @param clientName the name of the client
   * @param groupName the name of the group they want to play in
   * @param out the output stream of the player.
   */
  public Player(Socket clientSocket, String clientName, String groupName, OutputStream out) {
    // initialize socket of client
    this.clientSocket = clientSocket;
    // initialize name of the player
    this.clientName = clientName;
    // initialize name of the group
    this.groupName = groupName;
    // initialize output stream to the client
    this.out = out;
  }

  /**
   * This method returns the name of the player
   *
   * @return the clientName
   */
  public String getName() {
    return this.clientName;
  }

  /**
   * This method returns the socket of the player
   *
   * @return the clientSocket
   */
  public Socket getClient() {
    return this.clientSocket;
  }

  /**
   * This method returns the group in which the player wants to play
   *
   * @return groupName
   */
  public String getGroup() {
    return this.groupName;
  }

  /**
   * sets the clients name
   *
   * @param name string of the checked name
   */
  public void setName(String name) {
    this.clientName = name;
  }

  /**
   * This method sets the clients group
   *
   * @param groupName Name of the group you want to play in
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }
}
