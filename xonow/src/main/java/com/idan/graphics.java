package com.idan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class graphics extends Application {

    private static Scene scene;
    private static Stage stage;
    private static String name;
    private static int size;
    private static gameController gc;
    private static Client client;
    private static graphics graphicsController;
    
    @SuppressWarnings("exports")
    @Override
    public void start(Stage stage) throws IOException {
        graphics.stage = stage;
        graphics.scene = new Scene(loadFXML("startScreen"), 640, 480);
        graphics.stage.setTitle("Game");
        graphics.stage.setScene(scene);
        graphics.stage.show();
    }
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(graphics.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    public static void createClient(int size, String name) {
        graphics.name = name;
        graphics.size = size;

        // save the controller for this graphics.
        graphics.graphicsController = new graphics();

        // create the client and run it as a thread.
        graphics.client = new Client(name, size, graphics.graphicsController);
        new Thread(graphics.client).start();
    }
    public void showWaitingScreen() throws IOException {
        // create the waiting window.
        FXMLLoader fxmlLoader = new FXMLLoader(graphics.class.getResource("waitingScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        graphics.stage.setScene(scene);
    }

    public void startGame(String opponentName, boolean isX) throws IOException {
        // create the waiting window.
        FXMLLoader fxmlLoader = new FXMLLoader(graphics.class.getResource("game.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // set the game.
        graphics.gc = fxmlLoader.getController();
        graphics.gc.setGame(graphics.graphicsController, graphics.name, opponentName, graphics.size, isX);
        
        // set the scene.
        graphics.stage.setScene(scene);
    }
    public void sendMove(int row, int column, boolean isX) {
        // get the type.
        PlayerType type = isX ? PlayerType.getX() : PlayerType.getO();
        
        // sends the move from the graphics to the client.
        graphics.client.setMove(new Move(row, column, type));
    }
    public void showMove(Move move) {
        // show the move to the user.
        graphics.gc.showMove(move);
    }
    public void win() {
        graphics.gc.win();
    }
    public void lose() {
        graphics.gc.lose();
    }
    public void draw() {
        graphics.gc.draw();
    }
    public static void main(String[] args) {
        launch();
    }

}