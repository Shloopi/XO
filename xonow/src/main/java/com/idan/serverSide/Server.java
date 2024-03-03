package com.idan.serverSide;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
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
    private ArrayList<GameManager> games;
    private HashMap<Integer, PlayerInfo> players;
    private final int PORT = 7486;
    private DBHandler dbHandler;

    public Server() {
        this.dbHandler = new DBHandler();

        this.games = new ArrayList<>();
        this.clients = new HashMap<>();
        this.players = new HashMap<>();

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
            while (clientSocket.isConnected()) {
                // keep receiving messages.
                msg = this.receiveMessage(inputStream);

                // keep handling the messages received.
                this.handleMessage(clientInfo, msg);
            }
        } catch (IOException e) {
            this.closeClient(clientSocket);
            e.printStackTrace();
        } 
        finally {
        }
    }
    public void sendMessage(ClientInfo client, String message) {
        try {
            // Send a response to the client
            client.getOutput().write(message.getBytes());
        } catch (IOException e) {
            this.closeClient(client.getSocket());
            e.printStackTrace();
        }
    }

    private String receiveMessage(InputStream inputStream) throws IOException {
        String msg = "";
        byte[] buffer = new byte[1024];
        int bytesRead = 0;

        try {
            // try to read a message.
            bytesRead = inputStream.read(buffer);
        }
        catch (IOException e) {
        }
        finally {
            // if we were able to read the message.
            if (bytesRead > 0) {
                msg = new String(buffer, 0, bytesRead);
            }
        }
        return msg;
    }
    private void handleMessage(ClientInfo client, String msg) {
        if (msg != "") {
            System.out.println(msg);
            // each parameter is divided by '~'. split the parameters in the message.
            String[] splittedMessage = msg.split("~");

            // code 0 means get name and board size.
            if (splittedMessage[0].equals("0") && splittedMessage.length == 3) {
                String name = splittedMessage[1];
                if (this.hasClientJoined(name)) {
                    this.closeClient(client.getSocket());
                }
                else {
                    String sizeStr = splittedMessage[2]; 
                    int size;

                    // checks if the size is from 0-9.
                    if (sizeStr.length() == 1 && Character.isDigit(sizeStr.charAt(0))) {
                        size = Integer.parseInt(sizeStr);

                        // size 0 is not possible.
                        if (size == 0) {
                            this.sendMessage(client, "Error: board size must be an integer 1 - 9");
                        }
                        // size is correct.
                        else {
                            // if the server can start a game.
                            boolean canStartGame = this.canStartGame(size, name, client);

                            // if the server can start a game. 
                            if (!canStartGame) {
                                this.sendMessage(client, "1~waiting");
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
                                this.waitingClients[size - 1] = null;

                                // insert the game into the db.
                                Game game = new Game(playerX, playerO);

                                // add the game to the db.
                                this.dbHandler.getGameDB().insertGame(game);
                                
                                // add the game to the games hashmap.
                                this.games.add(new GameManager(game, this.clients.get(playerX.getPlayerID()), client));
                                
                                // send messages to the clients to start the game.
                                this.sendMessage(this.clients.get(playerX.getPlayerID()), "2~X~" + name);
                                this.sendMessage(client, "2~O~" + playerX.getName());

                                // add the gameID into the information about the players.
                                //playerO.setGameID(gameID);
                                //playerX.setGameID(gameID);

                                // add the players to the hash map.
                                this.players.put(playerX.getID(), playerX);
                                this.players.put(playerO.getID(), playerO);
                            }
                        }
                    }
                }                
            }
            // if the server received a move.
            else if (splittedMessage[0].equals("3") && splittedMessage.length == 4) {
                int row, column;

                // check if the gameID, row and columns are integers.
                if (Server.isNumeric(splittedMessage[1]) && Server.isNumeric(splittedMessage[2])) {
                    // save the variables.
                    row = Integer.parseInt(splittedMessage[1]);
                    column = Integer.parseInt(splittedMessage[2]);
                    String symbol = splittedMessage[3];

                    // create the move.
                    Move move = new Move(row, column, PlayerType.getType(symbol.charAt(0)));
                    
                    // get the game.
                    Game game = this.getGame(client);

                    // add the move to the game.
                    if (game.makeMove(move)) {

                        // check if a player has won.
                        PlayerType winner = game.checkWin();
                        if(winner != null) {
                            System.out.println("PLAYER WON");

                            // get the winner and loser info.
                            PlayerInfo playerWinner = winner.isX() ? game.getXPlayer() : game.getOPlayer();
                            PlayerInfo playerLoser = winner.isX() ? game.getOPlayer() : game.getXPlayer();

                            // update the winner in the game.
                            this.dbHandler.getGameDB().updateWinner(game);

                            // send a message to inform the players that the game is over and that one won.
                            this.sendMessage(this.clients.get(playerWinner.getPlayerID()), "9~W~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                            this.sendMessage(this.clients.get(playerLoser.getPlayerID()), "9~L~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                            
                            // finish the game.
                            this.finishGame(game);
                        }
                        // check if the game is drawn.
                        else if (game.checkDraw()) {
                            System.out.println("DRAW");

                            // update the winner in the game.
                            this.dbHandler.getGameDB().updateWinner(game);

                            // send a message to inform the players that the game is over and that it's a draw.
                            this.sendMessage(this.clients.get(game.getXPlayer().getPlayerID()), "9~D~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                            this.sendMessage(this.clients.get(game.getOPlayer().getPlayerID()), "9~D~" +  + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                            
                            // finish the game.
                            this.finishGame(game);
                        }
                        else {
                            // the move was accepted.
                            this.sendMessage(this.clients.get(game.getXPlayer().getPlayerID()), "4~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                            this.sendMessage(this.clients.get(game.getOPlayer().getPlayerID()), "4~" + move.getRow() + "~" + move.getColumn() + "~" + move.getPlayerType().getSymbol());
                        }             
                    }
                }
            }
            // if the message is disconnect.
            else if (splittedMessage[0].equals("7") && splittedMessage.length == 3) {
                // get the message.
                String message = splittedMessage[2];

                if (message.equals("waiting")) {
                    // get the size.
                    String sizeStr = splittedMessage[1];
                    int size;

                    // checks if the size is from 0-9.
                    if (sizeStr.length() == 1 && Character.isDigit(sizeStr.charAt(0))) {
                        size = Integer.parseInt(sizeStr);

                        // size 0 is not possible.
                        if (size == 0) {
                            this.sendMessage(client, "Error: board size must be an integer 1 - 9");
                        }
                        // size is correct.
                        else {
                            this.waitingClients[size - 1] = null;
                        }
                    }
                    // close the client.
                    this.closeClient(client.getSocket());
                }
                else {
                    // get the name of the player.
                    String name = splittedMessage[1];
                    
                    // get the playerID from the name.
                    PlayerInfo player = this.dbHandler.getPlayerDB().getPlayer(name);

                    if (player != null) {
                        // get the playerID from the name.
                        player = this.players.get(player.getID());

                        // if the player has a game going on.
                        if (player.getGameID() != -1) {

                            // get the game from the playerID.
                            Game game = this.getGame(client);

                            // get the winner and loser info.
                            PlayerInfo playerWinner = player.getType().isX() ? game.getOPlayer() : game.getXPlayer();

                            // send a message to inform the players that the game is over and that one won.
                            System.out.println(this.clients.get(playerWinner.getPlayerID()).getSocket().toString());
                            this.sendMessage(this.clients.get(playerWinner.getPlayerID()), "8~W~DC");
                            
                            // update the winner in the game.
                            this.dbHandler.getGameDB().updateWinner(game);
                            
                            // finish the game.
                            this.finishGame(game);
                        }
                    }
                }
            }
        }
    }
    private Game getGame(ClientInfo client) {
        Game game = null;
        for (GameManager gm : this.games) {
            game = gm.getGame(client);
            if (game != null) {
                return game;
            }
        }
        return game;
    }
    private void finishGame(Game game) {

        // finish the game.
        game.gameOver();

        // close the sockets.
        this.closeClient(this.clients.get(game.getXPlayer().getPlayerID()).getSocket());
        this.closeClient(this.clients.get(game.getOPlayer().getPlayerID()).getSocket());

        // remove the clients from the hash map.
        this.clients.remove(game.getXPlayer().getPlayerID());
        this.clients.remove(game.getOPlayer().getPlayerID());

        // remove the players from the hash map.
        this.players.remove(game.getXPlayer().getPlayerID());
        this.players.remove(game.getOPlayer().getPlayerID());

        // remove the game from the dictionary.
        this.games.remove(game.getID());
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
        String ip = clientSocket.getInetAddress().getHostAddress();
        try {
            clientSocket.close();
            System.out.println("Client disconnected: " + ip);
        } catch (IOException e) {
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
    private boolean hasClientJoined(String name) {
        boolean clientJoined = false;
        Collection<PlayerInfo> p = this.players.values();
        int i = 0;
        for (PlayerInfo player : p) {
            if (!clientJoined) {
                clientJoined = name.equals(player.getName());
            }
        }
        return clientJoined;
    }
    public static void main(String[] args) {
        new Server();
    }
}