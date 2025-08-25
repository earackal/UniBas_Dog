package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/** This class creates a thread for the GUI */
public class GuiThread extends Thread {
  /** This method runs the application */
  @Override
  public void run() {
    if (!MainGui.isOnceCreated()) {
      Application.launch(MainGui.class);
    }
    // this will be run if launch() method was already called in this session;
    // JavaFX window will be awaken from sleeping
    else {
      Platform.runLater(() -> {
        Stage stage = new Stage();
        MainGui.repeatableStart(stage);
      });
    }
  }
}
