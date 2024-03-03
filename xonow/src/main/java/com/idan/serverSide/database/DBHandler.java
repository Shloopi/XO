package com.idan.serverSide.database;

import java.util.HashMap;

public class DBHandler {
    private HashMap<String, DB> tables;

    public DBHandler() {
        this.tables = new HashMap<>();
        this.tables.put("game", new GameDB());
        this.tables.put("player", new PlayerDB());
    }
    public GameDB getGameDB() {
        return (GameDB)(this.tables.get("game"));
    }
    public PlayerDB getPlayerDB() {
        return (PlayerDB)(this.tables.get("player"));
    }
}
