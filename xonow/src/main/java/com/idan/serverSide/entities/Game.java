package com.idan.serverSide.entities;

import com.idan.Move;
import com.idan.PlayerType;

public class Game extends BaseEntity {
    private static int gameCounter = 0;
    private int size;
    private PlayerInfo xPlayer;
    private PlayerInfo oPlayer;
    private boolean isOver;

    private byte[][] board;

    public Game(PlayerInfo p1, PlayerInfo p2) {
        super();
        Game.gameCounter++;
        this.xPlayer = p1;
        this.oPlayer = p2;
        this.isOver = false;

        // get the size of the board.
        this.size = this.xPlayer.getSize();

        // set the board.
        board = new byte[this.size][this.size];

        for (int i = 0; i < this.size; i++)
            for (int j = 0; j < this.size; j++)
                board[i][j] = 0;

    }
    public static int getGameCounter() {
        return Game.gameCounter;
    }
    public int getSize() {
        return this.size;
    }
    public PlayerInfo getXPlayer() {
        return this.xPlayer;
    }
    public PlayerInfo getOPlayer() {
        return this.oPlayer;
    }
    public PlayerType checkWin() {
        int winner = 0; // 0 no winner, 1 X winner, -1 O winner.

        // counter1 counts for the row, counter2 counts for the column, 
        // counter3 counts for the main diagonal, counter4 counts for the second diagonal.
        int counter1, counter2, counter3 = 0, counter4 = 0;
        int i = 0, j;

        // run for the rows or until there is a winner.
        while (i < this.size && winner == 0) {
            j = 0;
            counter1 = 0;
            counter2 = 0;

            // run for the columns.
            while (j < this.size) {
                // count the values of the row.
                counter1 += this.board[i][j];

                // count the values of the column.
                counter2 += this.board[j][i];
                j++;
            }
            
            // if counter1 counted all squares as one symbol (x or o).
            if (Math.abs(counter1) == this.size) {
                winner = counter1 / this.size;
            }
            // if counter2 counted all squares as one symbol (x or o).
            else if (Math.abs(counter2) == this.size) {
                winner = counter2 / this.size;
            }

            // add the diagonal values to the counters. 
            counter3 += this.board[i][i];
            counter4 += this.board[this.size - i - 1][i];
            i++;
        }

        // if counter3 counted all squares as one symbol (x or o).
        if (winner == 0 && Math.abs(counter3) == this.size) {
            winner = counter3 / this.size;
        }
        // if counter4 counted all squares as one symbol (x or o).
        else if (winner == 0 && Math.abs(counter4) == this.size) {
            winner = counter4 / this.size;
        }

        return PlayerType.getType((byte) winner);
    }
    public boolean checkDraw() {
        boolean draw = true;
        int i = 0, j;

        // run for the rows or until it's not a draw.
        while (i < this.size && draw) {
            j = 0;

            // run for the columns or until it's not a draw.
            while (j < this.size && draw) {
                // if this spot is empty, it's not a draw.
                if (this.board[i][j] == 0) {
                    draw = false;
                }
                j++;
            }
            i++;
        }
        return draw;
    }

    public void gameOver() {
        this.isOver = true;
    }
    public boolean isGameOver() {
        return this.isOver;
    }
    private boolean isValid(Move move) {
        return 0 <= move.getRow() && move.getRow() < this.size && 0 <= move.getColumn() && move.getColumn() < this.size; 
    }
    public boolean makeMove(Move move) {
        // if the game is not over and if the move is valid.
        boolean moveMade = !this.isOver && this.isValid(move); 

        if (moveMade) {            
            // add the value to the grid. 
            this.board[move.getRow()][move.getColumn()] = move.getPlayerType().getValue();
        }

        return moveMade;
    }
}