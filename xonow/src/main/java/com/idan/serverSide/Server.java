package com.idan.serverSide;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.idan.Move;
import com.idan.PlayerType;
import com.idan.serverSide.database.DBHandler;
import com.idan.serverSide.entities.Game;
import com.idan.serverSide.entities.PlayerInfo;

public class Server {
    private ServerSocket socket;
    private PlayerInfo[] waitingClients;
    private HashMap<Integer, ClientInfo> clients;
    private HashMap<Integer, Game> games;
    private final int PORT = 7486;
    private DBHandler dbHandler;

    public Server() {
        this.dbHandler = new DBHandler();

        this.games = new HashMap<>();
        this.clients = new HashMap<>();

        // create an array for waiting clients. keep them until we find them a game
        this.waitingClients = new PlayerInfo[9];
        for (int i = 0; i < this.waitingClients.length; i++) {
            this.waitingClients[i] = null;
        }

        // listen clients waiting to join in a thread.
        new Thread(() -> this.listenToClients()).start();
    }
    private void listenToClients() {
        // try to create the socket.
        try (ServerSocket serverSocket = new ServerSocket(this.PORT)) {
            this.socket = serverSocket;

            // listen and keep accepting clients.
            while (true) {
                Socket clientSocket = this.socket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // listen to the client's messages in a thread.
                new Thread(() -> this.handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleClient(Socket clientSocket) {
        // try to create the client's input and output streams.
        try (
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            ClientInfo clientInfo = new ClientInfo(clientSocket, inputStream, outputStream);
            String msg;
            
            // as long as the socket is open.
            while (!clientSocket.isClosed()) {
                // keep receiving messages.
                msg = this.receiveMessage(inputStream);

                // keep handling the messages received.
                this.handleMessage(clientInfo, msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
        finally {
        }
    }
    public void sendMessage(OutputStream outputStream, String message) {
        try {
            // Send a response to the client
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String receiveMessage(InputStream inputStream) throws IOException {
        String msg = "";
        byte[] buffer = new byte[1024];

        // try to read a message.
        int bytesRead = inputStream.read(buffer);

        // if we were able to read the message.
        if (bytesRead > 0) {
            msg = new String(buffer, 0, bytesRead);
        }
        return msg;
    }
    private void handleMessage(ClientInfo client, String msg) {
        if (msg != "") {
            // each parameter is divided by '~'. split the parameters in the message.
            String[] splittedMessage = msg.split("~");

            // code 0 means get name and board size.
            if (splittedMessage[0].equals("0") && splittedMessage.length == 3) {
                String name = splittedMessage[1];
                String sizeStr = splittedMessage[2]; 
                int size;

                // checks if the size is from 0-9.
                if (sizeStr.length() == 1 && Character.isDigit(sizeStr.charAt(0))) {
                    size = Integer.parseInt(sizeStr);

                    // size 0 is not possible.
                    if (size == 0) {
                        this.sendMessage(client.getOutput(), "Error: board size must be an integer 1 - 9");
                    }
                    // size is correct.
                    else {
                        // if the server can start a game.
                        boolean canStartGame = this.canStartGame(size, name, client);

                        // if the server can start a game. 
                        if (!canStartGame) {
                            this.sendMessage(client.getOutput(), "1~waiting");
                        }
                        else {
                            // get the player from the db.
                            PlayerInfo playerO = this.dbHandler.getPlayerDB().getPlayer(name);

                            // if the player does not exist.
                            if (playerO == null) {
                                // insert the player into the db.
                                playerO = this.dbHandler.getPlayerDB().getPlayer(this.dbHandler.getPlayerDB().insertPlayer(new PlayerInfo(name, size, PlayerType.getO())));
                            }

                            // add the client to the hashmap.
                            this.clients.put(playerO.getPlayerID(), client);

                            // get the playerInfo for X player.
                            PlayerInfo playerX = this.waitingClients[size - 1];

                            // insert the game into the db.
                            Game game = new Game(playerX, playerO);
                            int gameID = this.dbHandler.getGameDB().insertGame(game);
                            
                            // add the game to the games hashmap.
                            this.games.put(gameID, game);
                            
                            // send messages to the clients to start the game.
                            this.sendMessage(this.clients.get(playerX.getPlayerID()).getOutput(), "2~" + gameID + "~X~" + name);
                            this.sendMessage(client.getOutput(), "2~" + gameID + "~O~" + playerX.getName());
                        }
                    }
                }                
            }
            // if the server received a move.
            else if (splittedMessage[0].equals("3") && splittedMessage.length == 5) {
                System.out.println("RECEIVED MOVE");
                int gameID, row, column;

                // check if the gameID, row and columns are integers.
                if (Server.isNumeric(splittedMessage[1]) && Server.isNumeric(splittedMessage[2]) && Server.isNumeric(splittedMessage[3])) {
                    // save the variables.
                    gameID = Integer.parseInt(splittedMessage[1]);
                    row = Integer.parseInt(splittedMessage[2]);
                    column = Integer.parseInt(splittedMessage[3]);
                    String symbol = splittedMessage[4];

                    // create the move.
                    Move move = new Move(row, column, PlayerType.getType(symbol.charAt(0)));
                    
                    // get the game.
                    Game game = this.games.get(gameID);

                    // add the move to the game.
                    game.makeMove(move);

                    // check if a player has won.
                    PlayerType winner = game.checkWin();
                    if(winner != null) {
                        System.out.println("PLAYER WON");

                        // get the winner and loser info.
                        PlayerInfo playerWinner = winner.isX() ? game.getXPlayer() : game.getOPlayer();
                        PlayerInfo playerLoser = winner.isX() ? game.getOPlayer() : game.getXPlayer();

                        // update the winner in the game.
                        this.dbHandler.getGameDB().updateWinner(gameID, playerWinner.getPlayerID());

                        // get the output streams of the winner and the loser.
                        OutputStream winnerOutput = this.clients.get(playerWinner.getPlayerID()).getOutput();
                        OutputStream loserOutput = this.clients.get(playerLoser.getPlayerID()).getOutput();

                        // send a message to inform the players that the game is over and that one won.
                        this.sendMessage(winnerOutput, "9~W~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                        this.sendMessage(loserOutput, "9~L~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                        
                        // finish the game.
                        this.finishGame(gameID);
                    }
                    // check if the game is drawn.
                    else if (game.checkDraw()) {
                        System.out.println("DRAW");

                        // update the winner in the game.
                        this.dbHandler.getGameDB().updateWinner(gameID, 0);

                        // send a message to inform the players that the game is over and that it's a draw.
                        this.sendMessage(this.clients.get(game.getXPlayer().getPlayerID()).getOutput(), "9~D~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                        this.sendMessage(this.clients.get(game.getOPlayer().getPlayerID()).getOutput(), "9~D~" +  + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                        
                        // finish the game.
                        this.finishGame(gameID);
                    }
                    else {
                        // the move was accepted.
                        this.sendMessage(this.clients.get(game.getXPlayer().getPlayerID()).getOutput(), "4~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                        this.sendMessage(this.clients.get(game.getOPlayer().getPlayerID()).getOutput(), "4~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                    }             
                }
            }
        }
    }
    private void finishGame(int gameID) {
        // get the game.
        Game game = this.dbHandler.getGameDB().getGame(gameID);

        // finish the game.
        game.gameOver();

        // close the sockets.
        this.closeClient(this.clients.get(game.getXPlayer().getPlayerID()).getSocket());
        this.closeClient(this.clients.get(game.getOPlayer().getPlayerID()).getSocket());

        // remove the game from the dictionary.
        //this.games.remove(gameID);
    }
    public static boolean isNumeric(String str) {
        // check if the number is numeric.
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void closeClient(Socket clientSocket) {
        try {
            clientSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private boolean canStartGame(int size, String name, ClientInfo client) {
        boolean startGame = false;

        // checks if there is not a client waiting with the same board size.
        if (this.waitingClients[size - 1] == null) {
            // get the player from the db.
            PlayerInfo player = this.dbHandler.getPlayerDB().getPlayer(name);

            // if the player does not exist.
            if (player == null) {
                // insert the player into the db.
                player = this.dbHandler.getPlayerDB().getPlayer(this.dbHandler.getPlayerDB().insertPlayer(new PlayerInfo(name, size, PlayerType.getX())));
            }
            else {
                // if the player exists, update the size of the player.
                this.dbHandler.getPlayerDB().updateSise(player.getPlayerID(), size);
                player.setSize(size);
            }
            // add the player to the waiting players.
            this.waitingClients[size - 1] = player;

            // add the client to the hashmap.
            this.clients.put(player.getPlayerID(), client);
        }
        else {
            startGame = true;
        }
        return startGame;
    }
    public static void main(String[] args) {
        new Server();
    }
}