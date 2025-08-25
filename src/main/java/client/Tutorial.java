package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * This class is used for displaying tutorial/game rules
 * in ClientGui
 */
public class Tutorial extends JFrame {
  /** This method creates a window with a manual*/
  public static void createTutorial() {
    // tries to get image
    BufferedImage icon1;
    BufferedImage icon2;
    ImageIcon imageIcon1;
    ImageIcon imageIcon2;

    try {
      icon1 = ImageIO.read(Tutorial.class.getClassLoader().getResourceAsStream("pictures/startingGui.png"));
      imageIcon1 = new ImageIcon(icon1);
      JLabel pic1 = new JLabel();
      // setting image to label
      pic1.setIcon(imageIcon1);
      //ImageIcon icon1 = new ImageIcon("pictures/startingGui.png");

      // tries to get second image
      icon2 =  ImageIO.read(Tutorial.class.getClassLoader().getResourceAsStream("pictures/gameGui.PNG"));
      imageIcon2 = new ImageIcon(icon2);
      JLabel pic2 = new JLabel();
      // setting image to label
      pic2.setIcon(imageIcon2);
      // text
      JLabel text1 = new JLabel("");
      text1.setText(
              "<html><br>UniBas Dog \u002D Spiel"
                      + "<br>"
                      + "<br><b><u>Hauptmen\u00FC</u></b>"
                      + "<br>"
                      + "<br>Das Hauptmen\u00FC besteht aus vielerlei Funktionen:"
                      + "<br>"
                      + "<br><b><u>Broadcast</u></b>"
                      + "<br>"
                      + "<br>Der Broadcast ist eine Chat-Funktion. Im Gegensatz zum normalen Chat k\u00F6nnen alle Spieler, die zum Server verbunden sind,"
                      + "<br>die Nachricht erhalten. Dabei spielt es keine Rolle, ob die Spieler sich im Hauptmen\u00FC oder innerhalb eines Spiels sich befinden."
                      + "<br><br><b><u>Change Name</u></b>"
                      + "<br>"
                      + "<br>Wie der Name bereits sagt, k\u00F6nnen Sie mit dieser Funktion Ihren Namen \u00E4ndern."
                      + "<br>Hierbei m\u00FCssen Sie neben dem im Textfeld neben \u00ABNew Name\u00BB Ihren neuen Namen angeben."
                      + "<br>"
                      + "<br><br><b><u>Gruppe erstellen</u></b>"
                      + "<br>"
                      + "<br>Um eine Gruppe zu erstellen, muss auf dem Button \u00ABCreate\u00BB gedr\u00FCckt werden. Daraufhin erscheint ein Textfeld,"
                      + "<br>indem der Name der zu erstellenden Gruppe eingegeben werden kann. Es ist zu beachten, dass Sie beim Erstellen"
                      + "<br>einer Gruppe automatisch zur Gruppe hinzugef\u00FCgt werden und diese nicht mehr verlassen k\u00F6nnen."
                      + "<br>"
                      + "<br><br><b><u>Gruppe joinen</u></b>"
                      + "<br>"
                      + "<br>Um einer Gruppe zu joinen, muss der Button \u00ABJoin\u00BB angeklickt werden. Daraufhin werden bereits"
                      + "<br>existierende Gruppen angezeigt. Sie k\u00F6nnen nun eine Gruppe anklicken und sie joinen. Falls keine Gruppen existieren,"
                      + "<br>wird nichts angezeigt. Falls Sie einer Gruppe beitreten, k\u00F6nnen Sie diese nicht mehr verlassen."
                      + "<br>"
                      + "<br<br><b><u>List</u></b>"
                      + "<br>"
                      + "<br>Anhand des Buttons \u00ABList\u00BB k\u00F6cnnen Sie die Player List, die Group List und die High Score List betrachten."
                      + "<br>Diese werden im Eventroom dargestellt."
                      + "<br>"
                      + "<br><br><b><u>Logout</u></b>"
                      + "<br>"
                      + "<br>Mit dem Button \u00ABLogout\u00BB k\u00F6nnen Sie jederzeit den Server verlassen. Falls dies der Fall ist, k\u00F6nnen Sie einem"
                      + "<br>neuen Server beitreten. Oben werden zwei Textfelder aktiviert, in denen Sie den Port und die IP-Adresse eingeben k\u00F6nnen."
                      + "<br>Wenn Sie daraufhin auf den Button \u00ABLogin\u00BB klicken, versucht das Programm, sich mit dem Server mit den angegebenen"
                      + "<br>Informationen zu verbinden."
                      + "<br><br></html>");
      // second text
      JLabel text2 = new JLabel("");
      text2.setText(
              "<html><br><br><b><u>Spielstart</u></b>"
                      + "<br>"
                      + "<br>Das Spiel startet automatisch, wenn sich 4 Spieler in einer Gruppe befinden. Daraufhin wird das Hauptmen\u00FC ausgeblendet und"
                      + "<br>ein neues Fenster erscheint. Dieses Fenster beinhaltet das Spielbrett mit den Spielfiguren und Ihrer Hand."
                      + "<br>Auf der rechten Seite k\u00F6nnen die Chat-, Whisper-, List und Change-Name-Funktion aktiviert werden."
                      + "<br>"
                      + "<br><br><b><u>Ihr Zug</u></b>"
                      + "<br>"
                      + "<br>Oben rechts steht, welcher Spieler am Zug ist. Wenn Sie am Zug sind, wird nicht Nachricht: \u00ABIt\u00B8s your turn!\u00BB abgebildet."
                      + "<br>Daraufhin m\u00FCssen Sie die auszuspielende Karte aussuchen, indem Sie auf die Karte klicken. Falls die Karte nicht spielbar ist,"
                      + "<br>erscheint ein Text, welcher Sie dazu auffordert, eine neue Karte auszuw\u00E4hlen. Falls eine Karte ausgew\u00E4hlt wurde,"
                      + "<br>m\u00FCssen Sie nun den Modus ausw\u00E4hlen. Die verschiedenen Modi der Karte werden als Buttons oben rechts dargestellt."
                      + "<br>Sie k\u00F6nnen Ihren gew\u00FCnschten Modus durch das Klicken des Buttons aussuchen. Zu allerletzt m\u00FCssen Sie die Murmel ausw\u00E4hlen."
                      + "<br>Wenn keine Ihrer Murmeln sich auf dem Feld befinden, wird dieser Schritt \u00FCbersprungen. Falls doch, dann m\u00FCssen Sie die Murmel"
                      + "<br>anklicken, welche Sie bewegen m\u00F6chten."
                      + "<br>"
                      + "<br>Wenn Sie eine 7 ausspielen, m\u00FCssen Sie mit Ihren Murmeln 7 Schritte vorw\u00E4hrts gehen. Dementsprechend m\u00FCssen Sie"
                      + "<br>7 Mal ihre Murmeln anklicken."
                      + "<br>"
                      + "<br>Wenn Sie einen Buben spielen, dann m\u00FCssen Sie zuerst Ihre Murmel anklicken, welche Sie tauschen m\u00F6chten. Danach m\u00FCssen Sie"
                      + "<br>die Murmel eines Gegenspielers anklicken, mit der Sie Ihre Murmel tauschen wollen."
                      + "<br>"
                      + "<br><br><b><u>Spielende</u></b>"
                      + "<br>"
                      + "<br>Nachdem das Spiel zu Ende ist, werden Sie aus der Gruppe entfernt und das Hauptmen\u00FC erscheint."
                      + "<br>Das Spielbrett bleibt git weiterhin offen. Diese k\u00F6nnen Sie jederzeit schliessen."
                      + "<br><br></html>");
      JFrame frame = new JFrame();
      // setting layout of panel
      JPanel panel = new JPanel(new GridLayout(1, 1));
      panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
      // adding elements to panel
      panel.add(pic1);
      panel.add(text1);
      panel.add(pic2);
      panel.add(text2);
      // adding panel as scroll pane to frame
      frame.add(new JScrollPane(panel));
      // meaning of close button
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      // size of the frame
      frame.setSize(800, 600);
      //frame.setResizable(false);
      // make Gui visible
      frame.setVisible(true);
    } catch (IOException e) {
      e.printStackTrace();
    }


  }
}
