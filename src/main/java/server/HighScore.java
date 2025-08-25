package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * This class manages the high score list by adding every winner in a HighScoreList.txt file This
 * class also sorts the high score list.
 */
public class HighScore {

  /** log4j Logger helping debug everything */
  private static final Logger LOGGER = LogManager.getLogger(HighScore.class);

  /** to specify the path to the file */
  File file;
  /** to get the file */
  FileInputStream fis;
  /** to get the input stream of the file */
  InputStream is;
  /** to read the file */
  BufferedReader bur;
  /** to write in the file */
  BufferedWriter myWriter;

  /** HighScore constructor gets the HighScoreList.txt from the resources
   *
   * @throws IOException when file not found
   * */
  public HighScore() throws IOException {
    LOGGER.debug("HS: constructor is called");
    file = new File("HighScoreList.txt");
    LOGGER.debug("HS: file ");

    //is = this.getClass().getClassLoader().getResourceAsStream("HighScoreList.txt");
    fis = new FileInputStream(file);
    LOGGER.debug("HS: is");

    InputStreamReader isr = new InputStreamReader(fis);
    LOGGER.debug("HS: isr");

    bur = new BufferedReader(isr);
    LOGGER.debug("HS: bur");

    myWriter = new BufferedWriter(new FileWriter(file, true));
    LOGGER.debug("HS: myWriter");
  }

  /**
   * This method adds the winner of the actual group to the txt file
   *
   * @param nameAndTurns this string contains the name, amount of turns of the winner and the local
   *     date
   */
  public void addScore(String nameAndTurns) {
    LOGGER.debug("HS: addScore-method called");
    LOGGER.debug("HS: name and turns " + nameAndTurns);
    try {
      myWriter.write("\n" + nameAndTurns);
      myWriter.close();
      LOGGER.debug("HS: addScore-method successful");
    } catch (IOException ioe) {
      LOGGER.debug("HS: addScore-method catch IOException probably file not found");
      ioe.printStackTrace();
    } catch (Exception e) {
      LOGGER.debug("HS: addScore-method catches undefined exception");
      e.printStackTrace();
    }
    LOGGER.debug("HS: end of addScore-method");
  }

  /**
   * This method reads the file and returns the sorted lists
   *
   * @return high score list
   */
  public String getHighScoreList() {
    LOGGER.debug("HS: started getHighScoreList");
    StringBuilder unsorted = new StringBuilder();
    try {
      String sLine = bur.readLine();
      while (sLine != null) {
        unsorted.append(sLine).append("\n");
        sLine = bur.readLine();
      }
    } catch (IOException e) {
      LOGGER.debug("HS: getHighScoreList - catch IOException");
      e.printStackTrace();
    }
    if(unsorted.length() < 1) {
      return "HighScoreList empty";
    } else {
      LOGGER.debug("HS: getHighScoreList - unsorted " + unsorted.toString());
      String highScoreList = sortScoreList(unsorted.toString());
      LOGGER.debug("HS: getHighScoreList - sorted " + highScoreList);
      return highScoreList;
    }
  }

  /**
   * This method actually sorts the entries in the file
   *
   * @param unsorted all the entries in txt file unsorted
   * @return a sorted String
   */
  public String sortScoreList(String unsorted) {
    LOGGER.debug("HS: sortedScoreList - started");
    // In strs is every index a row
    try {
      String[] strs = unsorted.split("\n");
      LOGGER.debug("HS: sortedScoreList unsorted strs[] ");
      for (String str : strs) {
        LOGGER.debug(str);
      }
      // in turns is the value after which we sort
      int[] turns = new int[strs.length];
      for (int i = 1; i < strs.length; i++) {
        String[] wordsOfLine = strs[i].split(" ");
        // lowest value first
        turns[i] = Integer.parseInt(wordsOfLine[0]);
      }
      LOGGER.debug("HS: sortedScoreList unsorted turns[] ");
      for (int turn : turns) {
        LOGGER.debug(turn);
      }

      // sort strs (selectionsort)
      for (int i = 1; i < turns.length - 1; i++) {
        for (int j = i + 1; j < turns.length; j++) {
          if (turns[i] > turns[j]) {
            int tmp = turns[i];
            String temp = strs[i];
            turns[i] = turns[j];
            strs[i] = strs[j];
            turns[j] = tmp;
            strs[j] = temp;
          }
        }
      }
      LOGGER.debug("HS: sortedScoreList sorted turns[] ");
      for (int turn : turns) {
        LOGGER.debug(turn);
      }
      LOGGER.debug("HS: sortedScoreList sorted strs[] ");
      for (String str : strs) {
        LOGGER.debug(str);
      }
      // strs is sorted
      // strs to string
      StringBuilder sb = new StringBuilder();
      for (String str : strs) {
        sb.append(str).append(" ");
      }
      LOGGER.debug("HS: sortedScoreList final String " + sb.toString());
      return sb.toString();
    } catch(NumberFormatException e) {
      e.printStackTrace();
      return "Highscore list is empty";
    } catch(NullPointerException e) {
      e.printStackTrace();
      return "Highscore list is empty";
    }
  }
}
