package com.idan;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class gameController {

    @FXML
    private VBox mainVbox;

    @FXML
    private Text oPlayerText;

    @FXML
    private Text xPlayerText;

    @FXML
    private Text youText;

    private boolean isX;

    private boolean isTurnX;

    private GridPane gridPane;

    private Button[][] buttons;

    private graphics graphicsController;

    private int size;

    private boolean gameOver;

    public void setGame(graphics graphicsController, String name, String opponentName, int size, boolean isX) {
        this.gameOver = false;
        this.graphicsController = graphicsController;
        this.isX = isX;
        this.isTurnX = true;
        this.size = size;
        this.mainVbox.setFillWidth(true);

        // get the x player and o player.
        String xName = this.isX ? name : opponentName;
        String oName = this.isX ? opponentName : name;

        // set the names of the players.
        this.youText.setText(this.youText.getText() + name);
        this.xPlayerText.setText(this.xPlayerText.getText() + xName);
        this.oPlayerText.setText(this.oPlayerText.getText() + oName);

        // create the grid pane.
        this.gridPane = new GridPane();
        
        // set row constraints for the rows in the gridpane.
        for (int r = 0; r < size; r++) {
            // always grow.
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            this.gridPane.getRowConstraints().add(rc);
        }
        // set column constraints for the columns in the gridpane.
        for (int c = 0; c < size; c++) {
            // always grow.
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);

            this.gridPane.getColumnConstraints().add(cc);
        }

        // create the buttons.
        this.buttons = new Button[this.size][this.size];

        // create buttons for each position in the gridpane.
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                // create the button.
                Button button = new Button();
                button.setStyle("-fx-font-size: 32px;");

                // create an event for when the user presses the button.
                final int row = i;
                final int column = j;
                button.setOnAction(event -> buttonClicked(row, column));
                GridPane.setRowIndex(button, i);
                GridPane.setColumnIndex(button, j);

                // add the button to the gridpane.
                this.gridPane.getChildren().add(button);

                // Set preferred width for the button to make them bigger
                button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                this.buttons[i][j] = button;
            }
        }

        VBox.setVgrow(this.gridPane, Priority.ALWAYS);
        this.mainVbox.getChildren().add(this.gridPane);


        this.setTurn();
    }
    public void setTurn() {
        if (this.isTurnX) {
            this.xPlayerText.setFill(Color.GREEN);
            this.oPlayerText.setFill(Color.BLACK);
        }
        else {
            this.oPlayerText.setFill(Color.GREEN);
            this.xPlayerText.setFill(Color.BLACK);
        }
    }
    public void buttonClicked(int row, int column) {
        // if the turn is of this player.
        if (!this.gameOver && (this.isX == this.isTurnX)) {
            // send the move to the graphics controller.
            this.graphicsController.sendMove(row, column, isX);
        }
    }
    public void showMove(Move move) {
        // switch the turn.
        this.isTurnX = this.isTurnX ? false : true;

        // convert the symbol from character to string.
        String symbol = Character.toString(move.getPlayerType().getSymbol());

        // change the button's text to the symbol.
        this.buttons[move.getRow()][move.getColumn()].setText(symbol);

        this.setTurn();
    }

    public void draw() {
        this.youText.setText("DRAW");
        this.gameOver = true;
    }
    public void win() {
        this.youText.setText("WIN");
        this.gameOver = true;
    }
    public void lose() {
        this.youText.setText("LOSE");
        this.gameOver = true;
    }
}
