package com.idan.serverSide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.idan.PlayerType;
import com.idan.serverSide.entities.Game;
import com.idan.serverSide.entities.PlayerInfo;

public class GameDB extends DB {

    public void createGameTable() {
        try {
            PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS game (id INT AUTO_INCREMENT PRIMARY KEY, size INT CHECK (size BETWEEN 1 AND 9), playerX INT, playerO INT, playerWon INT DEFAULT -1)");            
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int insertGame(Game game) {
        int gameID = -1;
        try {
            // try to insert the game.
            PreparedStatement statement = this.connection.prepareStatement("INSERT INTO game (size, playerX, playerO) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1, game.getSize());
            statement.setInt(2, game.getXPlayer().getPlayerID());
            statement.setInt(3, game.getOPlayer().getPlayerID());
            statement.executeUpdate();
    
            // Retrieve the generated id.
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                gameID = resultSet.getInt(1);
            }
    
            // Close resources
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return gameID;
    }
    public void deleteGame(int gameID) {
        try {
            // try to delete the game with the gameID.
            PreparedStatement statement = this.connection.prepareStatement("DELETE FROM game WHERE gameID = ?");
            statement.setInt(1, gameID);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void updateWinner(int gameID, int playerID) {
        try {
            // update the winner of the game.
            String updateQuery = "UPDATE game SET playerWon = ? WHERE gameID = ?";
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setInt(1, playerID);
            statement.setInt(2, gameID);
            statement.executeUpdate();

            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public Game getGame(int gameID) {
        Game game = null;
        try {
            // select the game from the id.
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM game WHERE gameID = ?");
            statement.setInt(1, gameID);

            // get the result.
            ResultSet resultSet = statement.executeQuery();

            // if the result exists.
            if (resultSet.next()) {
                System.out.println("get the players:" + resultSet.getInt(3) + " " + resultSet.getInt(4));
                // get the players' informations from the playerIDs.
                PlayerInfo playerX = this.getPlayer(resultSet.getInt(3));
                PlayerInfo playerO = this.getPlayer(resultSet.getInt(4));

                game = new Game(playerX, playerO);
            }

            resultSet.close();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return game;
    }
    private PlayerInfo getPlayer(int playerID) {
        PlayerInfo player = null;
        try {
            // select the player's information from the player
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM player WHERE playerID = ?");
            statement.setInt(1, playerID);

            // get the result.
            ResultSet resultSet = statement.executeQuery();

            // if the result exsist.
            if (resultSet.next()) {
                // create the playerInfo from the information in the DB.
                player = new PlayerInfo(playerID, resultSet.getString("name"), resultSet.getInt("size"), PlayerType.getType((byte)resultSet.getInt("type")));
            }

            resultSet.close();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return player;
    }
}
