package com.idan.serverSide.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.idan.PlayerType;
import com.idan.serverSide.entities.BaseEntity;
import com.idan.serverSide.entities.Game;
import com.idan.serverSide.entities.PlayerInfo;
import com.idan.serverSide.entities.SqlEntity;

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

    public void insertGame(Game game) {
        this.insert(game);
        this.saveChanges();
    }
    public void deleteGame(Game game) {
        this.delete(game);
        this.saveChanges();
    }
    public void updateWinner(Game game) {
        this.update(game);
        this.saveChanges();
    }

    public List<BaseEntity> getGame(int gameID) {
        String sql = "SELECT * FROM games WHERE gameId = " + gameID;
        List<BaseEntity> gameList = this.select(sql);

        return gameList;
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

    @Override
    protected BaseEntity createModel(BaseEntity entity, ResultSet result) throws SQLException {
        Game game = (Game)entity;
        // if the result exists.
        if (result.next()) {
            // get the players' informations from the playerIDs.
            PlayerInfo playerX = this.getPlayer(result.getInt(3));
            PlayerInfo playerO = this.getPlayer(result.getInt(4));

            game.addInfo(playerX, playerO);
        }
        return game;
    }

    
    @Override
    protected BaseEntity newEntity() {
        return new Game();
    }

    @Override
    public String insertQuery(BaseEntity entity) {
        String sql = "";
        if (entity instanceof Game) {
            Game game = (Game) entity;

            sql = "INSERT INTO game (size, playerX, playerO) VALUES (" + game.getSize() + ", " + game.getXPlayer().getPlayerID() + ", " + game.getOPlayer().getPlayerID() + ")";
        }
        return sql;
    }

    @Override
    public String updateQuery(BaseEntity entity) {
        String sql = "";
        if (entity instanceof Game) {
            Game game = (Game) entity;
            PlayerInfo winner = game.checkWin().isX() ? game.getXPlayer() : game.getOPlayer();
            sql = "UPDATE game SET playerWon = "+ winner.getPlayerID() + " WHERE gameID = " + game.getID();
        }
        return sql;
    }

    @Override
    public String deleteQuery(BaseEntity entity) {
        String sql = "";
        if (entity instanceof Game) {
            Game game = (Game)entity;
            sql = "DELETE FROM game WHERE gameID = " + game.getID();
        }
        return sql;
    }
}
