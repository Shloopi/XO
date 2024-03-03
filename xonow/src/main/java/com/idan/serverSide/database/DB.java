package com.idan.serverSide.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.idan.PlayerType;
import com.idan.serverSide.entities.Game;
import com.idan.serverSide.entities.PlayerInfo;

public class DB {
    protected Connection connection;

    public DB() {
        this.connection = null;
        this.connectSql();
    }

    private void connectSql() {
        String url = "jdbc:mysql://localhost:3306/xoFinal?serverTimezone=Asia/Jerusalem";
        String username = "root";
        String password = "ENTER PASSWORD";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void disconnectSql() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    // public void createGameTable() {
    //     try {
    //         PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS game (id INT AUTO_INCREMENT PRIMARY KEY, size INT CHECK (size BETWEEN 1 AND 9), playerX INT, playerO INT, playerWon INT DEFAULT -1)");            
    //         statement.executeUpdate();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }

    // public void createPlayerTable() {
    //     try {
    //         PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS player (id INT AUTO_INCREMENT PRIMARY KEY, size INT CHECK (size BETWEEN 1 AND 9), name VARCHAR(20), type INT CHECK (type IN (1, -1))");
    //         statement.executeUpdate();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }

    // public int insertGame(Game game) {
    //     int gameID = -1;
    //     try {
    //         // try to insert the game.
    //         PreparedStatement statement = this.connection.prepareStatement("INSERT INTO game (size, playerX, playerO) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
    //         statement.setInt(1, game.getSize());
    //         statement.setInt(2, game.getXPlayer().getPlayerID());
    //         statement.setInt(3, game.getOPlayer().getPlayerID());
    //         statement.executeUpdate();
    
    //         // Retrieve the generated id.
    //         ResultSet resultSet = statement.getGeneratedKeys();
    //         if (resultSet.next()) {
    //             gameID = resultSet.getInt(1);
    //         }
    
    //         // Close resources
    //         resultSet.close();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    //     return gameID;
    // }

    // public int insertPlayer(PlayerInfo player) {
    //     int playerID = -1;
    //     try {
    //         // try to insert the player.
    //         PreparedStatement statement = this.connection.prepareStatement("INSERT INTO player (size, name, type) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
    //         statement.setInt(1, player.getSize());
    //         statement.setString(2, player.getName());
    //         statement.setInt(3, player.getType().getValue());
    //         statement.executeUpdate();
    
    //         // Retrieve the generated id.
    //         ResultSet resultSet = statement.getGeneratedKeys();
    //         if (resultSet.next()) {
    //             playerID = resultSet.getInt(1);
    //         }
    
    //         // Close resources
    //         resultSet.close();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    //     return playerID;
    // }

    // public void deleteGame(int gameID) {
    //     try {
    //         // try to delete the game with the gameID.
    //         PreparedStatement statement = this.connection.prepareStatement("DELETE FROM game WHERE gameID = ?");
    //         statement.setInt(1, gameID);
    //         statement.executeUpdate();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }
    // public void deletePlayer(int playerID) {
    //     try {
    //         this.deletePlayerGames(playerID);
            
    //         // try to delete the player with the playerID.
    //         PreparedStatement statement = this.connection.prepareStatement("DELETE FROM player WHERE playerID = ?");
    //         statement.setInt(1, playerID);
    //         statement.executeUpdate();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }
    // private void deletePlayerGames(int playerID) {
    //     try {
    //         // try to delete the game with the gameID.
    //         PreparedStatement statement = this.connection.prepareStatement("DELETE FROM game WHERE playerX = ? or playerO = ?");
    //         statement.setInt(1, playerID);
    //         statement.setInt(2, playerID);
    //         statement.executeUpdate();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }
    // public PlayerInfo getPlayer(String name) {
    //     PlayerInfo player = null;
    //     try {
    //         // select the player's information from the player
    //         PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM player WHERE name = ?");
    //         statement.setString(1, name);

    //         // get the result.
    //         ResultSet resultSet = statement.executeQuery();

    //         // if the result exsist.
    //         if (resultSet.next()) {
    //             // create the playerInfo from the information in the DB.
    //             player = new PlayerInfo(resultSet.getInt("playerID"), name, resultSet.getInt("size"), PlayerType.getType((byte)resultSet.getInt("type")));
    //         }

    //         resultSet.close();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    //     return player;
    // }
    // public void updateSise(int PlayerID, int size) {
    //     try {
    //         // update the size of the player.
    //         String updateQuery = "UPDATE player SET size = ? WHERE playerID = ?";
    //         PreparedStatement statement = connection.prepareStatement(updateQuery);
    //         statement.setInt(1, size);
    //         statement.setInt(2, PlayerID);
    //         statement.executeUpdate();

    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }
    // public PlayerInfo getPlayer(int playerID) {
    //     PlayerInfo player = null;
    //     try {
    //         // select the player's information from the player
    //         PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM player WHERE playerID = ?");
    //         statement.setInt(1, playerID);

    //         // get the result.
    //         ResultSet resultSet = statement.executeQuery();

    //         // if the result exsist.
    //         if (resultSet.next()) {
    //             // create the playerInfo from the information in the DB.
    //             player = new PlayerInfo(playerID, resultSet.getString("name"), resultSet.getInt("size"), PlayerType.getType((byte)resultSet.getInt("type")));
    //         }

    //         resultSet.close();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    //     return player;
    // }
    // public void updateWinner(int gameID, int playerID) {
    //     try {
    //         // update the winner of the game.
    //         String updateQuery = "UPDATE game SET playerWon = ? WHERE gameID = ?";
    //         PreparedStatement statement = connection.prepareStatement(updateQuery);
    //         statement.setInt(1, playerID);
    //         statement.setInt(2, gameID);
    //         statement.executeUpdate();

    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }
    // public Game getGame(int gameID) {
    //     Game game = null;
    //     try {
    //         // select the game from the id.
    //         PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM game WHERE gameID = ?");
    //         statement.setInt(1, gameID);

    //         // get the result.
    //         ResultSet resultSet = statement.executeQuery();

    //         // if the result exists.
    //         if (resultSet.next()) {
    //             System.out.println("get the players:" + resultSet.getInt(3) + " " + resultSet.getInt(4));
    //             // get the players' informations from the playerIDs.
    //             PlayerInfo playerX = this.getPlayer(resultSet.getInt(3));
    //             PlayerInfo playerO = this.getPlayer(resultSet.getInt(4));

    //             game = new Game(playerX, playerO);
    //         }

    //         resultSet.close();
    //         statement.close();
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    //     return game;
    // }
}
