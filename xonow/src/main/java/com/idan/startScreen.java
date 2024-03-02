package com.idan;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class startScreen {

    @FXML
    private TextField sizeText;

    @FXML
    private Button startButton;

    @FXML
    private TextField usernameText;

    @FXML
    void startGame(ActionEvent event) throws IOException {
        // check if the username is not empty and if the text is 0-9.
        if (!this.usernameText.getText().isEmpty() && this.sizeText.getText().length() == 1 && Character.isDigit(this.sizeText.getText().charAt(0))) {
            // get the integer size.
            int size = Integer.parseInt(this.sizeText.getText());
            
            // check if the size is not 0.
            if (size != 0) {
                graphics.createClient(size, this.usernameText.getText());
            }
        }
    }

}