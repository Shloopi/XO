package com.idan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Platform;

public class Client implements Runnable {
    private Socket socket;
    private final int PORT = 7486;
    private final String ADDRESS = "localhost";
    private OutputStream output;
    private InputStream input;
    private byte[] buffer;
    private String name;
    private int boardSize;
    private Move move;
    private boolean isYourMove;
    private int gameID;
    private boolean isX;
    private graphics graphicsController;

    public Client(String name, int boardSize, graphics graphicsController) {
        this.graphicsController = graphicsController;
        this.name = name;
        this.boardSize = boardSize;
        this.buffer = new byte[1024];
        this.isYourMove = false;
    }
    @Override
    public void run() {
        // creates the connection with the server.
        try (Socket socket = new Socket(this.ADDRESS, this.PORT)) {
            this.socket = socket;
            
            // creates input and output streams.
            this.input = this.socket.getInputStream();
            this.output = this.socket.getOutputStream();

            // sending a message with the name and board size.
            this.sendMessage("0~" + this.name + "~" + this.boardSize);

            String msg;
            while (true) {
                msg = this.receiveMessage();
                this.handleMessage(msg);
            }

           
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void handleMessage(String msg) {
        if (msg != "") {
            String[] splittedMessage = msg.split("~");

            // if the message is 1, waiting.
            if (splittedMessage[0].equals("1") && splittedMessage.length == 2) {
                Platform.runLater(() -> {
                    try {
                        this.graphicsController.showWaitingScreen();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
            }
            // if the message 2, start game.
            else if (splittedMessage[0].equals("2") && splittedMessage.length == 4) {
                // get the game id, X or O and get the opponent's name.
                this.gameID = Integer.parseInt(splittedMessage[1]);
                this.isX = splittedMessage[2].equals("X");
                String opponentName = splittedMessage[3];

                // call the graphics to start the game.
                Platform.runLater(() -> {
                    try {
                        this.graphicsController.startGame(opponentName, isX);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });

                // if you are X, you start the first move of the game.
                if (this.isX) {
                    this.isYourMove = true;
                    this.waitForMove();
                }
            }
            // if the move was accpeted.
            else if (splittedMessage[0].equals("4") && splittedMessage.length == 4) {
                // get the row column and symbol from the message.
                int row = Integer.parseInt(splittedMessage[1]);
                int column = Integer.parseInt(splittedMessage[2]);
                String symbol = splittedMessage[3];
                
                // create the move from the variables.
                Move move = new Move(row, column, PlayerType.getType(symbol.charAt(0)));

                // call the graphics to show the move to the user.
                Platform.runLater(() -> this.graphicsController.showMove(move));

                // if this was the client's move.
                if ((this.isX == move.getPlayerType().isX())) {
                    this.isYourMove = false;
                }
                // if this was not the client's move.
                else {
                    this.isYourMove = true;
                    this.waitForMove();
                }
            }
            // if the game has ended.
            else if (splittedMessage[0].equals("9") && splittedMessage.length == 5) {
                String result = splittedMessage[1];
                
                // get the row column and symbol from the message.
                int row = Integer.parseInt(splittedMessage[2]);
                int column = Integer.parseInt(splittedMessage[3]);
                String symbol = splittedMessage[4];
                
                // create the move from the variables.
                Move move = new Move(row, column, PlayerType.getType(symbol.charAt(0)));
                
                Platform.runLater(() -> this.graphicsController.showMove(move));

                // if the game ended in a draw.
                if (result.equals("D")) {
                    Platform.runLater(() -> this.graphicsController.draw());

                }
                // if this player won the game.
                else if (result.equals("W")) {
                    Platform.runLater(() -> this.graphicsController.win());

                }
                // if this player lost the game.
                else if (result.equals("L")) {
                    Platform.runLater(() -> this.graphicsController.lose());

                }
            }
        }
    }
    private void waitForMove() {
        System.out.println("client: waiting for move from GUI");
        // wait until the move is set.
        while (this.move == null) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("client: Sending Move");
        // send the move to the server. 
        try {
            sendMessage("3~" + this.gameID + "~" + this.move.getRow() + "~" + this.move.getColumn() + "~" + this.move.getPlayerType().getSymbol());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // after sending the move, the turn is not yours.
        this.move = null;
        this.isYourMove = false;
    }
    public void sendMessage(String message) throws IOException {
        this.output.write(message.getBytes());
    }
    public String receiveMessage() throws IOException {
        String serverResponse = "";

        // try to read a message.
        int bytesRead = this.input.read(this.buffer);

        // if we were able to read a message.
        if (bytesRead > 0) {
            serverResponse = new String(this.buffer, 0, bytesRead);
        }
        return serverResponse;
    }
    public void setMove(Move move) {
        // if it's the client's turn to make a move.
        if (this.isYourMove) { 
            // set the move from the graphics.
            this.move = move;
        }
    }
}

