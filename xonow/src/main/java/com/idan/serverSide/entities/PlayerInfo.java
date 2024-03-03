package com.idan.serverSide.entities;

import com.idan.PlayerType;

public class PlayerInfo extends BaseEntity {
    private String name;
    private int size;
    private PlayerType type;
    private int gameID;

    public PlayerInfo() {
        super();
    }
    public PlayerInfo(String name, int size, PlayerType type) {
        super();
        this.name = name;
        this.size = size;
        this.type = type;
        this.gameID = -1;
    }
    public PlayerInfo(int playerID, String name, int size, PlayerType type) {
        super(playerID);
        this.name = name;
        this.size = size;
        this.type = type;
        this.gameID = -1;
    }
    public void addInfo(int playerID, String name, int size, PlayerType type) {
        this.setPlayerID(playerID);
        this.name = name;
        this.size = size;
        this.type = type;
        this.gameID = -1;
    }
    public int getPlayerID() {
        return this.getID();
    }
    public void setPlayerID(int playerID) {
        this.setID(playerID);
    }
    public PlayerType getType() {
        return type;
    }
    public String getName() {
        return this.name;
    }
    public int getSize() {
        return this.size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public int getGameID() {
        return gameID;
    }
    public void setGameID(int gameID) {
        this.gameID = gameID;
    } 
}
