import model.Color;
import model.GameState;
import org.junit.Test;

import static org.junit.Assert.*;

public class tests {

    @Test
    public void test() {

        GameState gameState = new GameState();

        gameState.getBoard();

        assertTrue(gameState.previousMoveColor == Color.BLACK);
        //тестирование правильного заполнения доски в начале игры
        assertTrue(
                //белые шашки
                gameState.board[0][7].hasChecker() && gameState.board[2][7].hasChecker() &&
                        gameState.board[4][7].hasChecker() && gameState.board[6][7].hasChecker() &&
                        gameState.board[1][6].hasChecker() && gameState.board[3][6].hasChecker() &&
                        gameState.board[5][6].hasChecker() && gameState.board[7][6].hasChecker() &&
                        gameState.board[0][5].hasChecker() && gameState.board[2][5].hasChecker() &&
                        gameState.board[4][5].hasChecker() && gameState.board[6][5].hasChecker() &&

                        //черные шашки
                        gameState.board[1][0].hasChecker() && gameState.board[3][0].hasChecker() &&
                        gameState.board[5][0].hasChecker() && gameState.board[7][0].hasChecker() &&
                        gameState.board[0][1].hasChecker() && gameState.board[2][1].hasChecker() &&
                        gameState.board[4][1].hasChecker() && gameState.board[6][1].hasChecker() &&
                        gameState.board[1][2].hasChecker() && gameState.board[3][2].hasChecker() &&
                        gameState.board[5][2].hasChecker() && gameState.board[7][2].hasChecker());

        //далее идут ходы которые приводят к победе ИИ
        //проверяем, совершается ли наш ход и совершается ли ход ИИ
        gameState.makeMove(1, 4, gameState.board[0][5].getChecker());
        assertTrue(!gameState.board[0][5].hasChecker() && gameState.board[1][4].hasChecker());
        gameState.makeIImove(Color.BLACK);

        //проверяем, совершается ли наш ход и совершается ли ход ИИ
        gameState.makeMove(7, 4, gameState.board[6][5].getChecker());
        assertTrue(!gameState.board[6][5].hasChecker() && gameState.board[7][4].hasChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(6, 3, gameState.board[7][4].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(6, 5, gameState.board[5][6].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(6, 5, gameState.board[4][7].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(1, 4, gameState.board[2][5].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(3, 4, gameState.board[4][5].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(5, 4, gameState.board[3][6].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(4, 3, gameState.board[5][4].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(4, 3, gameState.board[6][5].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(3, 2, gameState.board[4][3].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(2, 5, gameState.board[1][6].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(3, 4, gameState.board[2][5].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(3, 6, gameState.board[2][7].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(1, 6, gameState.board[0][7].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(6, 5, gameState.board[7][6].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(5, 4, gameState.board[6][5].getChecker());
        gameState.makeIImove(Color.BLACK);

        gameState.makeMove(5, 6, gameState.board[6][7].getChecker());
        gameState.makeIImove(Color.BLACK);

        //тестируем, что ИИ сделал правильный ход и выиграл игру
        assertEquals("Black won", gameState.gameover());

        //тест того, что после победы в следующей партии белые будут ходить первыми
        assertSame(gameState.previousMoveColor, Color.BLACK);
    }
}

