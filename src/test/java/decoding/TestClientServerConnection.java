package decoding;

import org.junit.Test;
import client.ClientGui;
import client.NewClient;
import static org.junit.Assert.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.MultipleServer;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestClientServerConnection {

  private static final Logger LOGGER = LogManager.getLogger(TestClientServerConnection.class);

  @Test
  public synchronized void testProtocolCommands() {

    // creates new thread which carries the server
    TestServerThread serverThread = new TestServerThread();
    // starts server
    serverThread.startServer();
    LOGGER.debug("TCSC: started serverThread");

    try {
      InetAddress ia = InetAddress.getLocalHost();
      String ipAddress = ia.getHostAddress();
      // creating new ClientGui
      ClientGui cgui1 = new ClientGui(ipAddress, 8090, "Kapilas");
      // setting frame of starting Gui to invisible
      cgui1.setFrameInvisible();
      // creating object of NewClient
      // NewClient client1 = new NewClient(ipAddress, 8090, "Kapilas", cgui1);
      // starting client
      // client1.start();
      NewClient client1 = cgui1.getClient();
      LOGGER.debug("TCSC: created and started client 1");

      // creating new ClientGui
      ClientGui cgui2 = new ClientGui(ipAddress, 8090, "Lars");
      // setting frame of starting Gui to invisible
      cgui2.setFrameInvisible();
      // creating object of NewClient
      // NewClient client2 = new NewClient(ipAddress, 8090, "Lars", cgui2);
      // starting client
      // client2.start();
      NewClient client2 = cgui2.getClient();
      LOGGER.debug("TCSC: created and started client 2");

      // creating new ClientGui
      ClientGui cgui3 = new ClientGui(ipAddress, 8090, "Gustas");
      // setting frame of starting Gui to invisible
      cgui3.setFrameInvisible();
      // creating object of NewClient
      // NewClient client3 = new NewClient(ipAddress, 8090, "Gustas", cgui3);
      // starting client
      // client3.start();
      NewClient client3 = cgui3.getClient();
      LOGGER.debug("TCSC: created and started client 3");

      // tries to create a new server through thread
      MultipleServer server = serverThread.getServer();
      LOGGER.debug("TCSC: created and started server");

      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // gotConnectionTest
      assertEquals("Kapilas", server.playerList.get(0).getName());
      LOGGER.debug("TCSC: passed firstNameTest");
      assertEquals("Lars", server.playerList.get(1).getName());
      LOGGER.debug("TCSC: passed secondNameTest");
      assertEquals("Gustas", server.playerList.get(2).getName());
      LOGGER.debug("TCSC: passed thirdNameTest");

      // startingGroupTest
      assertEquals("NotInAGroup", server.playerList.get(0).getGroup());
      LOGGER.debug("TCSC: client1 (1/3) passed starting groupNameTest");
      assertEquals("NotInAGroup", server.playerList.get(1).getGroup());
      LOGGER.debug("TCSC: client2 (2/3) passed starting groupNameTest");
      assertEquals("NotInAGroup", server.playerList.get(2).getGroup());
      LOGGER.debug("TCSC: client3 (3/3) passed starting groupNameTest");

      // first client sends a chat message
      client1.sendCommandToServer("CHAT Hello World");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }

      // gotChatMessageTest
      assertEquals("Kapilas: Hello World ", client2.messageOfServer);
      LOGGER.debug("TCSC: client2 (1/2) passed chatMessageTest");
      assertEquals("Kapilas: Hello World ", client3.messageOfServer);
      LOGGER.debug("TCSC: client3 (2/2) passed chatMessageTest");

      // client 2 activates whisper-chat
      client2.sendCommandToServer("WHISPER Gustas Hello Client");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // whisperChatTest
      assertEquals("Kapilas: Hello World ", client1.messageOfServer);
      LOGGER.debug("TCSC: client1 (1/2) passed gotWhisperChatTest");
      assertEquals("Lars: *private* Hello Client ", client3.messageOfServer);
      LOGGER.debug("TCSC: client3 (2/2) passed didNotGetWhisperChatTest");

      // client 3 tries to change the name
      client3.sendCommandToServer("CHANGE Emmanuel");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // changeNameTest
      assertEquals("Emmanuel", server.playerList.get(2).getName());
      LOGGER.debug("TCSC: passed changeNameTest");

      // client 1 tries to create a group called group007
      client1.sendCommandToServer("CREATEGROUP group007");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // createdGroupTest
      assertEquals("group007", server.groupList.get(0).getGroupName());
      LOGGER.debug("TCSC: passed newGroupNameTest");
      assertEquals(1, server.groupList.get(0).getGroupSize(server.groupList.get(0)));
      LOGGER.debug("TCSC: passed newGroupSizeTest");
      assertEquals("Kapilas", server.groupList.get(0).getGroupMembers().get(0).getName());
      LOGGER.debug("TCSC: passed newGroupMemberTest");

      // client 2 sends a chat message
      client2.sendCommandToServer("CHAT Hello World");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // memberOfGroupDoesNotGetNormalChatMessageTest
      assertEquals("Lars: Hello World ", client3.messageOfServer);
      LOGGER.debug("TCSC: passed playerGotChatMessageTest");
      assertNotEquals("Lars: Hello World ", client1.messageOfServer);
      LOGGER.debug("TCSC: passed memberOfGroupDoesNotGetNormalChatMessageTest");

      // client 1 sends a chat message
      client1.sendCommandToServer("CHAT Do you hear me?");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }

      // playersOutsideOfGroupDoesNotGetMessageTest
      assertNotEquals("Kapilas: Do you hear me? ", client2.messageOfServer);
      LOGGER.debug("TCSC: client 2 (1/2) passed playersOutsideOfGroupDoesNotGetMessageTest");
      assertNotEquals("Kapilas: Do you hear me? ", client3.messageOfServer);
      LOGGER.debug("TCSC: client 3 (2/2) passed playersOutsideOfGroupDoesNotGetMessageTest");

      // client 1 activates broadcast function
      client1.sendCommandToServer("BROADCAST Someone here?");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // broadcastTest
      assertEquals("Kapilas: Someone here? ", client2.messageOfServer);
      LOGGER.debug("TCSC: client 2 (1/2) passed broadcastTest");
      assertEquals("Kapilas: Someone here? ", client3.messageOfServer);
      LOGGER.debug("TCSC: client 3 (2/2) passed broadcastTest");

      // new object of CLientGui
      ClientGui cgui4 = new ClientGui(ipAddress, 8090, "Emmanuel");
      // setting frame to invisible
      cgui4.setFrameInvisible();
      // creating object of NewClient
      // NewClient client4 = new NewClient(ipAddress, 8090, "Emmanuel", cgui4);
      // starting client
      // client4.start();
      NewClient client4 = cgui4.getClient();
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // checkNameTest
      assertEquals("EmmanuelI", server.playerList.get(3).getName());
      LOGGER.debug("TCSC: passed checkNameTest");

      // client 4 changes the name
      client4.sendCommandToServer("CHANGE Gustas");

      // client 4 tries to join group group007
      client4.sendCommandToServer("JOINGROUP group007");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // joinGroupTest
      assertEquals(2, server.groupList.get(0).getGroupSize(server.groupList.get(0)));
      LOGGER.debug("TCSC: passed joinedGroupSizeTest");
      assertEquals("Gustas", server.groupList.get(0).getGroupMembers().get(1).getName());
      LOGGER.debug("TCSC: passed joinedGroupMembersTest");

      // client 2 tries to create group called group007
      client2.sendCommandToServer("CREATEGROUP group007");
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // existingGroupNameTest
      assertEquals("group007I", server.groupList.get(1).getGroupName());
      LOGGER.debug("TCSC: passed existingGroupNameTest");
      assertEquals(1, server.groupList.get(1).getGroupSize(server.groupList.get(1)));
      LOGGER.debug("TCSC: passed existingGroupSizeTest");
      assertEquals("Lars", server.groupList.get(1).getGroupMembers().get(0).getName());
      LOGGER.debug("TCSC: passed existingGroupMemberTest");

      // client 3 tries to sign out from server
      client3.sendCommandToServer("LOGOUT");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // logoutTest
      assertEquals(3, server.playerList.size());
      LOGGER.debug("TCSC: passed sizeOfPlayerListAfterLogoutTest");
      assertEquals("Gustas", server.playerList.get(2).getName());
      LOGGER.debug("TCSC: passed nameOfPlayerAfterLogoutInPlayerListTest");

      // client 2 wants to get the player, group and high score list
      client2.sendCommandToServer("LOBBY");
      // tries to wait for 200 milliseconds
      try {
        wait(200);
      } catch (InterruptedException e) {
      }
      // lobbyTest
      assertEquals("LOBBY Player List: Kapilas Lars Gustas ", client2.playerListOfServer);
      LOGGER.debug("TCSC: passed playerListTest");
      assertEquals(
          "LOBBY Group List: group007(open): Kapilas Gustas group007I(open): Lars ",
          client2.gameListOfServer);
      LOGGER.debug("TCSC: passed groupListTest");
      // assertEquals("LOBBY Highscore List: ", client2.highscorelistOfServer);
      // LOGGER.debug("TCSC: passed highscoreListTest");
    } catch (UnknownHostException e) {
    }
  }
}
