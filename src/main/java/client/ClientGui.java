package client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This class is the starting Gui of the Client. It tries to start NewClient with the given
 * information like ip-address, username and port number. It sets up a Gui, where you can adjust
 * your inputs and tries to read and send the commands to NewClient
 */
@SuppressWarnings("CanBeFinal")
public class ClientGui extends JFrame implements ActionListener {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(ClientGui.class);

  /** will first hold "Username:", later on "Enter message" */
  private JLabel label;
  /** to hold the Username and later on the messages */
  private JTextField tf;
  /** to hold the server address, the port number, the name of the created lobby */
  private JTextField tfServer, tfPort, tfNameOfCreatedLobby, tfBroadcast, tfChangeName;
  /** to Logout and get the list of the users */
  private JButton login, logout, playerList, createLobby, joinLobby, tutorial;
  /** chat room, event room */
  private JTextArea taChat, taEvent;
  /** checks whether the client is still connected to the server */
  private boolean connected;
  /** the Client object */
  public NewClient client;
  /** the default port number */
  private int defaultPort;
  /** the default host address */
  private String defaultHost;
  /** name of the client */
  private String username;
  /** panel which appears when the button createLobby was activated */
  private JPanel lobby;
  /** boolean which informs whether button createLobby is activated */
  private boolean createIsActive = false;
  /** JFrame where the whole setting will be printed */
  private JFrame frame;
  /** buttons which represents the names of existing groups */
  private JButton[] buttons;
  /** JPanel which includes the array buttons */
  private JPanel groupList;
  /** JPanel on the east side */
  private JPanel center;
  /** names of the existing groups */
  private String[] nameOfButtons;

  /**
   * This is the constructor of the class
   *
   * @param hostaddress ip-address
   * @param port portnumber of the server
   * @param username name of the client
   */
  public ClientGui(String hostaddress, int port, String username) {
    super("Client");

    LOGGER.debug("CG: constructor-Successfully started the ClientGui constructor.");

    //initializing defaultPort, defaultHost and username
    defaultPort = port;
    defaultHost = hostaddress;
    this.username = username;
    // initializing main frame
    frame = new JFrame();
    // The NorthPanel:
    JPanel northPanel = new JPanel(new GridLayout(3, 1));
    // place of the server name and the port number
    JPanel serverAndPort = new JPanel(new GridLayout(1, 5, 1, 3));
    // the two JTextField with the value of the host address and port number
    tfServer = new JTextField(hostaddress);
    tfPort = new JTextField("" + port);
    tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
    // JLabel including the Message : 'Server Address' will be added to this specified JPanel
    serverAndPort.add(new JLabel("Server Address:  "));
    // JTextField with host address will be added this JPanel
    serverAndPort.add(tfServer);
    // JLabel including the Message : 'Port Number:' will be added to this specified JPanel
    serverAndPort.add(new JLabel("Port Number:  "));
    // JTextFiel with port number will be added this JPanel
    serverAndPort.add(tfPort);
    serverAndPort.add(new JLabel(""));
    // adds the Server an port field to the GUI
    northPanel.add(serverAndPort);
    // the Label and the TextField
    label = new JLabel("You can still change your name", SwingConstants.CENTER);
    // adding label to panel
    northPanel.add(label);
    tf = new JTextField(this.username);
    // background color is white
    tf.setBackground(Color.WHITE);
    // adds to the Gui
    northPanel.add(tf);
    // adding panel to frame on north position
    frame.add(northPanel, BorderLayout.NORTH);

    // JTextArea for the chat which will be activated after connection to the server
    taChat = new JTextArea("Welcome to the Chat room\n", 50, 50);
    // new panel
    JPanel centerPanel = new JPanel(new GridLayout(1, 1));
    // setting layout
    centerPanel.setLayout(new javax.swing.BoxLayout(centerPanel, javax.swing.BoxLayout.Y_AXIS));
    // adding chat to panel
    centerPanel.add(new JScrollPane(taChat));
    // can't be used now
    taChat.setEditable(false);

    // panel fro broadcast
    JPanel broadcast = new JPanel(new GridLayout(1, 3));
    // adding a label to panel
    broadcast.add(new JLabel("Broadcast: "));
    // initializing textfield in which the client writes the broadcast message
    tfBroadcast = new JTextField("");
    // can't be used at the beginning
    tfBroadcast.setEnabled(false);
    // adding textfield to panel
    broadcast.add(tfBroadcast);

    // panel fro broadcast
    JPanel changeName = new JPanel(new GridLayout(1, 3));
    // adding a label to panel
    changeName.add(new JLabel("New Name: "));
    // initializing textfield in which the client writes the broadcast message
    tfChangeName = new JTextField("");
    // can't be used at the beginning
    tfChangeName.setEnabled(false);
    // adding textfield to panel
    changeName.add(tfChangeName);
    // adding broadcast and changeName panels to centerPanel
    centerPanel.add(broadcast);
    centerPanel.add(changeName);
    // adding centerPanel to frame
    frame.add(centerPanel, BorderLayout.CENTER);

    // login button
    login = new JButton("Login");
    login.addActionListener(this);
    // sets size of button
    login.setPreferredSize(new Dimension(100, 30));
    // logout button
    logout = new JButton("Logout");
    logout.addActionListener(this);
    // you have to login before being able to logout
    logout.setEnabled(false);
    // sets size of button
    logout.setPreferredSize(new Dimension(100, 30));

    // new button for tutorial
    tutorial = new JButton("Tutorial");
    tutorial.addActionListener(this);
    // you have to login before being able to logout
    tutorial.setEnabled(false);
    // sets size of button
    tutorial.setPreferredSize(new Dimension(100, 30));

    // south panel
    JPanel southPanel = new JPanel();
    // adds to southpanel
    southPanel.add(login);
    southPanel.add(logout);
    southPanel.add(tutorial);
    // add to south position
    frame.add(southPanel, BorderLayout.SOUTH);

    // playerList button
    playerList = new JButton("List");
    // add it to action listener
    playerList.addActionListener(this);
    // you have to login before being able to see playerlist, grouplist and highscore list
    playerList.setEnabled(false);
    // sets size of button
    playerList.setPreferredSize(new Dimension(100, 30));
    // create new group button
    createLobby = new JButton("Create");
    // add it to action listener
    createLobby.addActionListener(this);
    // sets size of button
    createLobby.setPreferredSize(new Dimension(100, 30));
    // you have to login before being able to create a new group
    createLobby.setEnabled(false);
    // join an existing group button
    joinLobby = new JButton("Join");
    // add it to action listener
    joinLobby.addActionListener(this);
    // sets size of button
    joinLobby.setPreferredSize(new Dimension(100, 30));
    // you have to login before being able to join an existing group
    joinLobby.setEnabled(false);
    // new Jpanel
    JPanel data = new JPanel();
    // adds the buttons playerList, createLobby and joinLobby to JPanel
    data.add(playerList);
    data.add(createLobby);
    data.add(joinLobby);
    // setting layout of lobby
    lobby = new JPanel(new GridLayout(1, 1, 1, 3));
    // adding a JLabel to lobby
    lobby.add(new JLabel("Name:  "));
    // initializing JTextField
    tfNameOfCreatedLobby = new JTextField("");
    // adding text field to JPanel
    lobby.add(tfNameOfCreatedLobby);
    // making lobby invisible
    lobby.setVisible(false);
    // initializing event room with specific size
    taEvent = new JTextArea("Event room\n", 40, 30);
    // initializing panel
    groupList = new JPanel();
    // setting size
    groupList.setPreferredSize(new Dimension(0, 0));
    // initializing JPanel
    center = new JPanel();
    // setting layout
    center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.Y_AXIS));
    // adding event room and groupList as ScrollPanes to center
    center.add(new JScrollPane(taEvent));
    center.add(new JScrollPane(groupList));
    // can't be used now
    taEvent.setEditable(false);
    // east panel
    JPanel eastPanel = new JPanel();
    // setting layout of the east panel
    eastPanel.setLayout(new javax.swing.BoxLayout(eastPanel, javax.swing.BoxLayout.Y_AXIS));
    // adding panels to east panel
    eastPanel.add(data);
    eastPanel.add(lobby);
    // setting visible
    lobby.setVisible(false);
    // adding center to panel
    eastPanel.add(center);
    // add to east position
    frame.add(eastPanel, BorderLayout.EAST);

    // meaning of close button
    frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
    // size of the Gui
    frame.setSize(800, 600);
    // make Gui visible
    frame.setVisible(true);
    LOGGER.debug("CG: constructor - set frame visible");
    // focus should be on tf by changing size of window
    tf.requestFocus();
    findConnection(this.defaultHost, this.defaultPort, this.username);
  }

  /**
   * This method creates the GUI after a successful login to a server
   *
   * @param hostaddress ip address of server
   * @param portNum port of server
   * @param name name of the user
   */
  public void findConnection(String hostaddress, int portNum, String name) {
    // try creating a new NewClient
    client = new NewClient(hostaddress, portNum, name, this);
    LOGGER.debug("CG: actionPerformed-initializing client");
    // test if we can start the Client
    if (!client.start()) {
      return;
    }

    tf.setText("");
    label.setText("Enter your message below");
    // we got connection to a server
    connected = true;
    // disable login button
    login.setEnabled(false);
    // enable the buttons
    logout.setEnabled(true);
    tutorial.setEnabled(true);
    playerList.setEnabled(true);
    createLobby.setEnabled(true);
    joinLobby.setEnabled(true);
    // disable the Server and Port JTextField
    tfServer.setEditable(false);
    tfPort.setEditable(false);
    // enabling broadcast and change name fields
    tfBroadcast.setEnabled(true);
    tfChangeName.setEnabled(true);
    // Action listener for when the user enters a message
    tf.addActionListener(this);
    tfBroadcast.addActionListener(this);
    tfChangeName.addActionListener(this);
  }

  /** This method sets the frame of starting chat invisible It is especially used for unit test */
  public void setFrameInvisible() {
    // setts frame invisible
    frame.setVisible(false);
    LOGGER.debug("CG: setFrameInvisible-setted frame invisible");
  }

  /**
   * called by the Client to append text in the TextArea
   *
   * @param str String with the message which should be appended
   */
  public void append(String str) {
    // appends the message to the chat room
    taChat.append(str);
    taChat.setCaretPosition(taChat.getText().length() - 1);
  }

  /**
   * called by the Client to append events in the TextArea
   *
   * @param str String with the event which should be appended
   */
  void appendEvent(String str) {
    String[] split = str.split(" ");
    // if it is the names of the existing groups
    if (split[0].equals("GETLOBBY")) {
      // initializing array with all the buttons
      buttons = new JButton[split.length - 1];
      // initializing array with all the names of the groups
      nameOfButtons = new String[buttons.length];
      // removes everything which was already in panel
      groupList.removeAll();
      // setting layout
      groupList.setLayout(new GridLayout(4, 4));
      // setting size
      groupList.setPreferredSize(new Dimension(200, 30 * (buttons.length - 1) + 100));
      for (int i = 1; i < split.length; i++) {
        // try to create a button with the given name
        buttons[i - 1] = new JButton(split[i]);
        // setting size of button
        buttons[i - 1].setPreferredSize(new Dimension(800, 30));
        // adding it to action listener
        buttons[i - 1].addActionListener(this);
        // button can be clicked
        buttons[i - 1].setEnabled(true);
        // adds the button to the panel
        groupList.add(buttons[i - 1]);
        // saves name of the button
        nameOfButtons[i - 1] = split[i];
      }
      // make panel with the buttons visible
      groupList.setVisible(true);
      // updating groupList on frame
      groupList.revalidate();
      groupList.repaint();
      // updating center
      center.revalidate();
      center.repaint();
      // updating frame
      frame.revalidate();
      frame.repaint();
      LOGGER.debug("CG: appendEvent-group list as buttons");
    } else {
      // appends the event to the event room
      taEvent.append(str);
      taEvent.setCaretPosition(taEvent.getText().length() - 1);
    }
  }

  /** This method will be called due to connection failures */
  public void connectionFailed() {
    LOGGER.debug("CG: connectionFailed-stop connection activated");
    // client can log in to server
    login.setEnabled(true);
    // can't logout because the client isn't connected to a server
    logout.setEnabled(false);
    tutorial.setEnabled(false);
    // can't see the playerlist because the client isn't connected to the server
    playerList.setEnabled(false);
    // can't create a lobby because the client isn't connected to the server
    createLobby.setEnabled(false);
    // can't join a group because the client isn't connected to the server
    joinLobby.setEnabled(false);
    // can't write a broadcast message because the client isn't connected to the server
    tfBroadcast.setEnabled(false);
    tfChangeName.setEnabled(false);
    // label
    label.setText("Enter your username below");
    tf.setText(this.username);
    // reset port number and host name as a construction time, both are fix
    tfPort.setText("" + defaultPort);
    tfServer.setText(defaultHost);
    // let the user change them, because client is trying to reconnect
    tfServer.setEditable(true);
    tfPort.setEditable(true);
    // don't react to a <CR> after the username
    tf.removeActionListener(this);
    // client isn't connected to a server
    connected = false;
    setFrameVisible();
  }

  /** This method makes the starting frame visible */
  public void setFrameVisible() {
    // setts frame visible
    frame.setVisible(true);
  }

  /**
   * This method will be called when a button was clicked or when the client wrote a message
   *
   * @param e which includes the action of the client
   */
  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    // if it was the Logout button
    if (o == logout) {
      client.sendCommandToServer("LOGOUT");
      LOGGER.debug("CG: actionPerformed-client sends command LOGOUT");
      return;
    }
    // if it was the playerList button
    if (o == playerList) {
      client.sendCommandToServer("LOBBY");
      LOGGER.debug("CG: actionPerformed-client sends command LOBBY");
      return;
    }

    // if it was create lobby button
    if (o == createLobby) {
      // setts panel visible
      lobby.setVisible(true);
      // updating frame
      frame.revalidate();
      frame.repaint();
      // adding text field to action listener
      tfNameOfCreatedLobby.addActionListener(this);
      // setting boolean which says whether createLobby was activated true
      createIsActive = true;
      LOGGER.debug("CG: actionPerformed-activated text field for group name");
      return;
    }

    // if it was join lobby button
    if (o == joinLobby) {
      // sending command to Server to get group list
      client.sendCommandToServer("GETLOBBY");
      LOGGER.debug("CG: actionPerformed-client sends command GETLOBBY");
      return;
    }

    // if it was tutorial button
    if (o == tutorial) {
      // starting method in class Tutorial
      Tutorial.createTutorial();
      return;
    }

    // if it was a button of the group list
    if (o != login && o instanceof JButton) {
      // for-loop to get the right button
      for (int i = 0; i < buttons.length; i++) {
        if (o == buttons[i]) {
          LOGGER.debug("CG: actionPerformed-client clicked button with group name");
          // got the right button
          // tries to get the name of the clicked button
          String nameOfGroup = nameOfButtons[i];
          // sends command to join the group with the given name
          client.sendCommandToServer("JOINGROUP " + nameOfGroup);
          LOGGER.debug("CG: actionPerformed-client sends command JOINGROUP");
          // make panel invisible
          groupList.setVisible(false);
          // setting size to zero
          groupList.setPreferredSize(new Dimension(0, 0));
          // client can't join an other group
          joinLobby.setEnabled(false);
          // client can't create an other group
          createLobby.setEnabled(false);
          // updating center
          center.revalidate();
          center.repaint();
          // updating frame
          frame.revalidate();
          frame.repaint();
          LOGGER.debug("CG: actionPerformed-set group list invisible");
          return;
        }
      }
    }

    // it is a message coming from the JTextField
    if (connected && (!(o instanceof JButton))) {
      // save message in String
      if (createIsActive) {
        // if create button was already clicked
        // tries to get the name of the lobby
        String createdName = tfNameOfCreatedLobby.getText();
        if (createdName != null && createdName.length() > 0) {
          // if the text field of create lobby was activated
          // send the command and all the other important inputs to the server
          client.sendCommandToServer("CREATEGROUP " + createdName);
          LOGGER.debug("CG: actionPerformed-client sends command CREATEGROUP");
          // deleting name from text field
          tfNameOfCreatedLobby.setText("");
          // setting boolean equal to false
          createIsActive = false;
          // making lobby invisible
          lobby.setVisible(false);
          // client can't join an other group
          joinLobby.setEnabled(false);
          // client can't create an other group
          createLobby.setEnabled(false);
          // updating frame
          frame.revalidate();
          frame.repaint();
          return;
        }
      }
      // tries to get a possible message in tfBroadcast
      String broadcastMessage = tfBroadcast.getText();
      // if it was a broadcast message
      if (broadcastMessage != null && broadcastMessage.length() > 0) {
        // if there was a real message
        // sends message with command to server
        client.sendCommandToServer("BROADCAST " + broadcastMessage);
        LOGGER.debug("CG: actionPerformed-client sends command BROADCAST");
        // deletes message from text field
        tfBroadcast.setText("");
      }
      String newName = tfChangeName.getText();
      // if it was a new name
      if (newName != null && newName.length() > 0) {
        // if there was a real message
        // sends message with command to server
        client.sendCommandToServer("CHANGE " + newName);
        LOGGER.debug("CG: actionPerformed-client sends command CHANGE");
        // deletes message from text field
        tfChangeName.setText("");
      }
      // tries to get the message of chat text field
      String input = tf.getText();
      // if it was a chat message
      if (input != null && input.length() > 0) {
        // if it was a chat message
        // let String go through HumanModem to get the right protocol command
        String command = HumanModem.getCommand(input);
        // send the command and all the other important inputs to the server
        client.sendCommandToServer(command);
        LOGGER.debug("CG: actionPerformed-client sends command CHAT");
        // vanish text from text field
        tf.setText("");
      }
      return;
    }

    // if it was the Login button
    if (o == login) {
      LOGGER.debug("CG: actionPerformed-client clicked LOGIN-button");
      // get name of client
      String username = tf.getText().trim();
      // if empty username, ignore it
      if (username.length() == 0) {
        return;
      }
      this.username = username;
      // if empty host address, ignore it
      String server = tfServer.getText().trim();
      if (server.length() == 0) {
        return;
      }
      this.defaultHost = server;
      // if empty or invalid port number, ignore it
      String portNumber = tfPort.getText().trim();
      if (portNumber.length() == 0) {
        return;
      }
      int port = 0;
      try {
        // get the port number as an integer
        port = Integer.parseInt(portNumber);
      } catch (Exception en) {
        return;
      }
      this.defaultPort = port;
      findConnection(server, port, username);
    }
  }

  /**
   * This method enables the buttons createLobby and joinLobby. Called especially after the end of a
   * game
   */
  public void enableButtons() {
    // enabling buttons
    createLobby.setEnabled(true);
    joinLobby.setEnabled(true);
  }

  public NewClient getClient() {
    return this.client;
  }
}
