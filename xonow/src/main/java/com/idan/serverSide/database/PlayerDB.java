package com.idan.serverSide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.idan.PlayerType;
import com.idan.serverSide.entities.BaseEntity;
import com.idan.serverSide.entities.Game;
import com.idan.serverSide.entities.PlayerInfo;

public class PlayerDB extends DB {
    public int insertPlayer(PlayerInfo player) {
        int playerID = -1;
        try {
            // try to insert the player.
            PreparedStatement statement = this.connection.prepareStatement("INSERT INTO player (size, name, type) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1, player.getSize());
            statement.setString(2, player.getName());
            statement.setInt(3, player.getType().getValue());
            statement.executeUpdate();
    
            // Retrieve the generated id.
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                playerID = resultSet.getInt(1);
            }
    
            // Close resources
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return playerID;
    }
    public void createPlayerTable() {
        try {
            PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS player (id INT AUTO_INCREMENT PRIMARY KEY, size INT CHECK (size BETWEEN 1 AND 9), name VARCHAR(20), type INT CHECK (type IN (1, -1))");
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void deletePlayer(int playerID) {
        try {
            this.deletePlayerGames(playerID);
            
            // try to delete the player with the playerID.
            PreparedStatement statement = this.connection.prepareStatement("DELETE FROM player WHERE playerID = ?");
            statement.setInt(1, playerID);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    private void deletePlayerGames(int playerID) {
        try {
            // try to delete the game with the gameID.
            PreparedStatement statement = this.connection.prepareStatement("DELETE FROM game WHERE playerX = ? or playerO = ?");
            statement.setInt(1, playerID);
            statement.setInt(2, playerID);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public PlayerInfo getPlayer(String name) {
        PlayerInfo player = null;
        try {
            // select the player's information from the player
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM player WHERE name = ?");
            statement.setString(1, name);

            // get the result.
            ResultSet resultSet = statement.executeQuery();

            // if the result exsist.
            if (resultSet.next()) {
                // create the playerInfo from the information in the DB.
                player = new PlayerInfo(resultSet.getInt("playerID"), name, resultSet.getInt("size"), PlayerType.getType((byte)resultSet.getInt("type")));
            }

            resultSet.close();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return player;
    }
    public void updateSise(int PlayerID, int size) {
        try {
            // update the size of the player.
            String updateQuery = "UPDATE player SET size = ? WHERE playerID = ?";
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setInt(1, size);
            statement.setInt(2, PlayerID);
            statement.executeUpdate();

            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public PlayerInfo getPlayer(int playerID) {
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
    @Override
    protected BaseEntity createModel(BaseEntity entity, ResultSet result) throws SQLException {
        PlayerInfo player = (PlayerInfo)entity;
        // if the result exsist.
        if (result.next()) {
            // create the playerInfo from the information in the DB.
            player.addInfo(result.getInt("playerID"), result.getString("name"), result.getInt("size"), PlayerType.getType((byte)result.getInt("type")));
        }
        return player;
    }
    @Override
    public String insertQuery(BaseEntity entity) {
        String sql = "";
        if (entity instanceof PlayerInfo) {
            PlayerInfo player = (PlayerInfo) entity;

            sql = "INSERT INTO player (size, name, type) VALUES (" + player.getSize() + ", " + player.getName() + ", " + player.getType().getValue() + ")";
        }
        return sql;
    }
    @Override
    protected BaseEntity newEntity() {
        return new PlayerInfo();
    }
    @Override
    public String updateQuery(BaseEntity entity) {
        String sql = "";
        if (entity instanceof PlayerInfo) {
            PlayerInfo player = (PlayerInfo) entity;

            sql = "UPDATE player SET size = " + player.getSize() + "WHERE playerID = " + player.getPlayerID();
        }
        return sql;
    }
    @Override
    public String deleteQuery(BaseEntity entity) {
        String sql = "";
        if (entity instanceof PlayerInfo) {
            PlayerInfo player = (PlayerInfo) entity;

            sql = "DELETE FROM player WHERE playerID = " + player.getPlayerID();
        }
        return sql;
    }
}
