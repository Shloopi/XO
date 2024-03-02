package com.idan;

public class Move {
    private int row;
    private int column;
    private PlayerType type;

    public Move(int row, int column, PlayerType type) {
        this.row = row;
        this.column = column;
        this.type = type;
    }
    public int getRow() {
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }
    public PlayerType getPlayerType() {
        return type;
    }
}
