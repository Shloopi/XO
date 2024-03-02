package com.idan;

public class PlayerType {
    private static PlayerType x = new PlayerType(1);
    private static PlayerType o = new PlayerType(-1);
    private static int typesCounter = 0;
    private byte value;
    private char symbol;

    public PlayerType(int value) {
        // can only create 2 playertypes. one for X and one for O.
        if (PlayerType.typesCounter < 2) {
            // save the value.
            this.value = (byte) value;
            
            // save the symbol (either X or O).
            if (this.value == 1) {
                this.symbol = 'X';
            }
            else if (this.value == -1) {
                this.symbol = 'O';
            }

            PlayerType.typesCounter++;
        }
    }
    public static PlayerType getType(byte value) {
        // get the player type depending on the values given.
        PlayerType p = null;
        if (value == 1) {
            p = PlayerType.x;
        }
        else if (value == -1) {
            p = PlayerType.o;
        }
        return p;
    }
    public static PlayerType getType(char symbol) {
        // get the player type depending on the symbol given.
        PlayerType p = null;
        if (symbol == 'X') {
            p = PlayerType.x;
        }
        else if (symbol == 'O') {
            p = PlayerType.o;
        }
        return p;
    }
    public static PlayerType getX() {
        return PlayerType.x;
    }
    public static PlayerType getO() {
        return PlayerType.o;
    }
    public byte getValue() {
        return this.value;
    }
    public char getSymbol() {
        return symbol;
    }
    public boolean isX() {
        return this.value == 1;
    }
    public boolean isO() {
        return this.value == -1;
    }



}
