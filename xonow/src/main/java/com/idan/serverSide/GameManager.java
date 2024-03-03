package com.idan.serverSide;

import com.idan.serverSide.entities.Game;

public class GameManager {
    private ClientInfo client1;
    private ClientInfo client2;
    private Game game; 

    public GameManager(Game game, ClientInfo client1, ClientInfo client2) {
        this.game = game;
        this.client1 = client1;
        this.client2 = client2;
    }

    public Game getGame(ClientInfo client) {
        Game game = null;

        if (client.equals(client1) || client.equals(client2)) {
            game = this.game;
        }

        return game;
    }
}
