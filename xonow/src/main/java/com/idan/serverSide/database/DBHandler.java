package com.idan.serverSide.database;

public class DBHandler {
    private DB[] tables;

    public DBHandler() {
        this.tables = new DB[2];
        this.tables[0] = new GameDB();
        this.tables[1] = new PlayerDB();
    }
    public GameDB getGameDB() {
        return (GameDB)this.tables[0];
    }
    public PlayerDB getPlayerDB() {
        return (PlayerDB)this.tables[1];
    }
}
