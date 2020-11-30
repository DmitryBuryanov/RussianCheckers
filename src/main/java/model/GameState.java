package model;

import java.util.ArrayList;

public class GameState {
    private int fieldSize = 100;
    public Cell[][] board = new Cell[8][8];
    public Color previousMoveColor = Color.BLACK;
    public int moveCount = 0;
    int maxDepth = 2;

    public void getBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Color color;
                if ((i + j) % 2 == 0) color = Color.BEIGE;
                else color = Color.BROWN;
                board[i][j] = new Cell(i, j, color);
                Checker checker = null;
                board[i][j].setChecker(null);
                if (j < 3 && board[i][j].color == Color.BROWN) {
                    checker = new Checker(i, j, Color.BLACK, 0, false);
                }
                if (j > 4 && board[i][j].color == Color.BROWN) {
                    checker = new Checker(i, j, Color.WHITE, 0, false);
                }
                if (checker != null) {
                    board[i][j].setChecker(checker);
                }
            }
        }
        moveCount = 0;
    }

    //оценка возможности хода
    public int canMove(int newX, int newY, Checker checker) {
        //значению хода присваивается 0, если совершить данный ход невозможно по разным причинам
        // например, в клетке уже есть шашка или сейчас ход соперника
        if (checker.color == previousMoveColor) return 0;
        if (!cellExist(newX, newY) || board[newX][newY].hasChecker() || (newX + newY) % 2 != 1) return 0;
        else {
            int nowX = (int) Math.floor(checker.getOldX() / fieldSize);
            int nowY = (int) Math.floor(checker.getOldY() / fieldSize);
            if (!checker.isDamka) {
                // здесь идет проверка того, что шашка(не дамка) походила не более чем на одну клетку и обязательно
                // вперед (в случае, если это так, ее ходу присваивается значение 1(ход без взятия))
                if (checker.color == Color.BLACK && Math.abs(newX - nowX) == 1 && newY - nowY == 1 || // what is this
                        checker.color == Color.WHITE && Math.abs(newX - nowX) == 1 && newY - nowY == -1) {
                    return 1;
                }
                // проверка того, что шашка(не дамка) бьет шашку соперника(тогда ее ходу присваивается 2)
                int evilX = (newX + nowX) / 2;
                int evilY = (newY + nowY) / 2;
                if (board[evilX][evilY].hasChecker() && board[evilX][evilY].getChecker().color != checker.color &&
                        Math.abs(newX - nowX) == 2 && Math.abs(newY - nowY) == 2) { // what

                    return 2;
                }
            } else {
                //обработка хода дамки, подсчет сколько шашек она встретила на пути
                if (Math.abs(newX - nowX) == 0 || Math.abs(newY - nowY) == 0) return 0;

                int lx = (newX - nowX) / Math.abs(newX - nowX);
                int ly = (newY - nowY) / Math.abs(newY - nowY);
                int xx = nowX + lx;
                int yy = nowY + ly;
                int countChecker = 0;
                while (xx != newX && yy != newY) {
                    if (board[xx][yy].hasChecker() && board[xx][yy].getChecker().color == checker.color) return 0;
                    if (board[xx][yy].hasChecker() && board[xx][yy].getChecker().color != checker.color)
                        countChecker += 1;
                    xx += lx;
                    yy += ly;
                }
                //соответственно, если ни одной - ход без взятия(значение 1), если одну - ход со взятием(значение 2)
                if (countChecker == 0) return 1;
                if (countChecker == 1) return 2;
            }
        }
        return 0;
    }

    //совершение самого хода
    public void makeMove(int newX, int newY, Checker checker) {
        moveCount++;
        int moveResult;
        //если попытаться совершить ход за пределы доски, он будет отменен
        if (newX < 0 || newY < 0 || newX > 7 || newY > 7) moveResult = 0; // размер поля
        else moveResult = canMove(newX, newY, checker);

        // так как взятие является обязательным, в случае, если оно возможно, но игрок его не делает, его ход отменяется
        // (ему присваивается значение 0)
        if ((needtobyteforWhite() && checker.color == Color.WHITE || needtobyteforBlack()
                && checker.color == Color.BLACK) && moveResult != 2) {
            moveResult = 0;
        }

        //создание списка, в который входят начальные координаты шашки, ее конечные координаты, координаты
        //битой шашки(если она имееется, в противном случае туда подаются -1, является ли шашка дамкой(1, если нет - 0),
        //является ли битая шашка дамкой(аналогично, как выше), если она имеется, цвет шашки, совершающей ход
        // черный - 0, белый - 1. Лист Integer, чтобы было удобнее заносить туда координаты, так как они наиболее важны
        int nowX = (int) Math.floor(checker.getOldX() / 100);
        int nowY = (int) Math.floor(checker.getOldY() / 100);

        if (moveResult != 0) {
            checker.go(newX, newY);
            board[nowX][nowY].setChecker(null);
            board[newX][newY].setChecker(checker);
            if (moveResult == 1) {

                if (checker.color == Color.BLACK && newY == 7) checker.isDamka = true;
                if (checker.color == Color.WHITE && newY == 0) checker.isDamka = true;

                //при успешном ходе переключаем данное поле на ход второго игрока
                previousMoveColor = checker.color;

            } else if (moveResult == 2) {
                if (!checker.isDamka) {
                    int evilX = (newX + nowX) / 2;
                    int evilY = (newY + nowY) / 2;

                    board[evilX][evilY].setChecker(null);

                } else {
                    int lx = (newX - nowX) / Math.abs(newX - nowX);
                    int ly = (newY - nowY) / Math.abs(newY - nowY);
                    int xx = nowX + lx;
                    int yy = nowY + ly;
                    while (!board[xx][yy].hasChecker()) {
                        xx += lx;
                        yy += ly;
                    }

                    board[xx][yy].setChecker(null);
                }

                previousMoveColor = checker.color;

                if (checker.color == Color.BLACK && newY == 7) checker.isDamka = true;
                if (checker.color == Color.WHITE && newY == 0) checker.isDamka = true;

                if (gameover().equals("White won") || gameover().equals("Black won")) {
                    previousMoveColor = Color.BLACK;
                }
            }

            if (checker.color == Color.BLACK && newY == 7 || checker.color == Color.WHITE && newY == 0)
                checker.isDamka = true;

        }
        checker.moveType = moveResult;
    }

    //функция проверяет, при данном раскладе сил на доске, выиграл ли кто-то или еще нет
    public String gameover() {
        int black = 0;
        int white = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j].hasChecker() && board[i][j].getChecker().color == Color.BLACK) black += 1;
                if (board[i][j].hasChecker() && board[i][j].getChecker().color == Color.WHITE) white += 1;
            }
        }
        if (black == 0) {
            return "White won";
        }
        if (white == 0) {
            return "Black won";
        }
        return "";
    }

    private boolean cellExist(int x, int y) {
        return x >= 0 && x <= 7 && y >= 0 && y <= 7;
    }

    //проверяет, может ли шашка в данной клетке побить вражескую шашку
    private boolean canByte(Cell cell) {
        if (cell.hasChecker()) {
            int i = cell.x;
            int j = cell.y;
            if (!cell.getChecker().isDamka) {
                if (cellExist(i + 2, j + 2) && board[i + 1][j + 1].hasChecker() && !board[i + 2][j + 2].hasChecker()
                        && board[i + 1][j + 1].getChecker().color != cell.getChecker().color) return true;
                if (cellExist(i + 2, j - 2) && board[i + 1][j - 1].hasChecker() && !board[i + 2][j - 2].hasChecker()
                        && board[i + 1][j - 1].getChecker().color != cell.getChecker().color) return true;
                if (cellExist(i - 2, j + 2) && board[i - 1][j + 1].hasChecker() && !board[i - 2][j + 2].hasChecker()
                        && board[i - 1][j + 1].getChecker().color != cell.getChecker().color) return true;
                if (cellExist(i - 2, j - 2) && board[i - 1][j - 1].hasChecker() && !board[i - 2][j - 2].hasChecker()
                        && board[i - 1][j - 1].getChecker().color != cell.getChecker().color) return true;
            } else {
                int nowX = i;
                int nowY = j;
                while (nowX != 0 && nowY != 0) {
                    nowX--;
                    nowY--;
                    if (cellExist(nowX, nowY)) {
                        if (board[nowX][nowY].hasChecker()) {
                            if (board[nowX][nowY].getChecker().color == cell.getChecker().color) break;
                            if (board[nowX][nowY].getChecker().color != cell.getChecker().color &&
                                    cellExist(nowX - 1, nowY - 1) && !board[nowX - 1][nowY - 1].hasChecker())
                                return true;
                            else break;
                        }
                    }
                }

                nowX = i;
                nowY = j;
                while (nowX != 7 && nowY != 0) {
                    nowX++;
                    nowY--;
                    if (cellExist(nowX, nowY)) {
                        if (board[nowX][nowY].hasChecker()) {
                            if (board[nowX][nowY].getChecker().color == cell.getChecker().color) break;
                            if (board[nowX][nowY].getChecker().color != cell.getChecker().color &&
                                    cellExist(nowX + 1, nowY - 1) && !board[nowX + 1][nowY - 1].hasChecker())
                                return true;
                            else break;
                        }
                    }
                }

                nowX = i;
                nowY = j;
                while (nowX != 7 && nowY != 7) {
                    nowX++;
                    nowY++;
                    if (cellExist(nowX, nowY)) {
                        if (board[nowX][nowY].hasChecker()) {
                            if (board[nowX][nowY].getChecker().color == cell.getChecker().color) break;
                            if (board[nowX][nowY].getChecker().color != cell.getChecker().color &&
                                    cellExist(nowX + 1, nowY + 1) && !board[nowX + 1][nowY + 1].hasChecker())
                                return true;
                            else break;
                        }
                    }
                }

                nowX = i;
                nowY = j;
                while (nowX != 0 && nowY != 7) {
                    nowX--;
                    nowY++;
                    if (cellExist(nowX, nowY)) {
                        if (board[nowX][nowY].hasChecker()) {
                            if (board[nowX][nowY].getChecker().color == cell.getChecker().color) break;
                            if (board[nowX][nowY].getChecker().color != cell.getChecker().color &&
                                    cellExist(nowX - 1, nowY + 1) && !board[nowX - 1][nowY + 1].hasChecker())
                                return true;
                            else break;
                        }
                    }
                }
            }
        }
        return false;
    }

    //проверка, необходимо ли белым бить
    public boolean needtobyteforWhite() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j].hasChecker() && board[i][j].getChecker().color == Color.WHITE && canByte(board[i][j]))
                    return true;
            }
        }
        return false;
    }

    //проверка, необходимо ли черным бить
    public boolean needtobyteforBlack() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j].hasChecker() && board[i][j].getChecker().color == Color.BLACK && canByte(board[i][j]))
                    return true;
            }
        }
        return false;
    }

    // алгоритм хождения ИИ
    // насколько я смог заметить, алгоритм альфа-бета отсечений в русских шашках не является
    // особо эффективным по сравнению с минимаксом
    // это связано с обязательным взятием, и из этого следует, что основной процесс игры проходит так:
    // оба игрока не имеют никакой возможности сделать какой то свой ход, так как на поле,
    // как это происходит чаще всего, одна из вражеских шашек берет удар на себя, подставляясь и лишая
    // соперника возможности сделать нормальный ход. Именно поэтому алгоритм, реализованный ниже будет постоянно
    // отсекать ненужные ходы, не просчитывая их, так как он просто не сможет походить так
    // (аналогично альфа-бета отсечениям)

    public void makeIImove(Color color) {
        Integer[] minimax = minimax(0, color);
        makeMove(minimax[2], minimax[3], board[minimax[0]][minimax[1]].getChecker());
    }

    //функция, где текущее состояние доски копируется и сохраняется
    public Cell[][] copyBoard() {
        Cell[][] oldboard = new Cell[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Color color;
                if ((i + j) % 2 == 0) color = Color.BEIGE;
                else color = Color.BROWN;
                oldboard[i][j] = new Cell(i, j, color);
                oldboard[i][j].setChecker(null);
                Checker checker = board[i][j].getChecker();
                if (board[i][j].hasChecker()) oldboard[i][j].setChecker(new Checker(i, j, checker.color,
                        checker.moveType, checker.isDamka));
            }
        }
        return oldboard;
    }

    //процедура отмены хода, используется замена текущего состояния доски на предыдущее
    public void makeOldBoard(Cell[][] oldBoard, Color color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j].setChecker(null);
                Checker checker = oldBoard[i][j].getChecker();
                if (oldBoard[i][j].hasChecker()) board[i][j].setChecker(new Checker(i, j, checker.color,
                        checker.moveType, checker.isDamka));
            }
        }
        previousMoveColor = color;
    }

    //оценочная функция
    //положение примерной ничьей будет равно 0. В случае близости белых к победе число будет увеличиваться,
    // в случае черных - наоборот, уменьшаться

    //дамка расценивается как 3 простых шашки(оценка взята из самоучителя по шашкам)

    public int getEvaluation() {
        int whiteCount = 0;
        int blackCount = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j].hasChecker()) {
                    Checker checker = board[i][j].getChecker();
                    if (board[i][j].getChecker().color == Color.BLACK) {
                        if (checker.isDamka) blackCount += 3;
                        else
                        blackCount++;
                    }
                    if (board[i][j].getChecker().color == Color.WHITE) {
                        if (checker.isDamka) whiteCount += 3;
                        else
                        whiteCount++;
                    }
                }
            }
        }

        // и, конечно, учитывая обязательность взятия, будем снижать оценку, если алгоритм подставит шашку под удар
        // без какой-либо выгоды после этого
        if (previousMoveColor == Color.BLACK && needtobyteforWhite()) blackCount--;
        else if (previousMoveColor == Color.WHITE && needtobyteforBlack()) whiteCount--;
        return whiteCount - blackCount;
    }

    // ну и, собственно, сам алгоритм минимакс
    // он такой объемный лишь на первый взгляд, на самом деле в нем очень много повторяющихся строк, которые
    // отличаются лишь конечными координатами, что делает не очень удобным вынос их в отдельную функцию. Кроме того,
    // на мой взгляд, при таком виде алгоритма он выглядит более структурированно и понятно, где какая шашка что делает
    // и какой ход совершается
    // по сути, алгоритм получился большим из-за объемной логики шашек и большого количества возможностей хода, которые,
    // хоть и часто отсекаются по причиным описанным выше, в игре бывают ситуации, когда они возможны, и поэтому их
    // нельзя было не описать
    public Integer[] minimax(int depth, Color color) {

        //создаем список, который покажет нам, откуда куда ходить и оценку этого хода
        Integer[] currentDepth = new Integer[]{0, 0, 0, 0, 0};

        //сразу проверим, нужно ли нам совершать ход дальше, или игра уже проиграна или выиграна

        //так как шашек на поле 24(по 12 каждого цвета) в случае победы за оценку возьмем просто
        // очень большое(по сравнению с 24) для белых, или очень малое для черных число, например +-1000
        if (gameover().equals("White won")) currentDepth[4] = 1000;
        if (gameover().equals("Black won")) currentDepth[4] = -1000;
        if (currentDepth[4] == 1000 || currentDepth[4] == -1000) return currentDepth;


        if (color == Color.BLACK) currentDepth[4] = Integer.MAX_VALUE;
        else currentDepth[4] = Integer.MIN_VALUE;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                //проверка цвета, чей игрок сейчас ходит
                if (color == Color.BLACK) {
                    //проверка взятия
                    if (needtobyteforBlack()) {
                        //проверка наличия в клетке шашки и ее возможностей взятия
                        if (board[i][j].hasChecker() && board[i][j].getChecker().color == Color.BLACK && canByte(board[i][j])) {
                            // если шашка - дамка, она может сделать ход в любую сторону до края доски
                            //проверям ходы в 4 направлениях
                            if (board[i][j].getChecker().isDamka) {
                                int nowX = i;
                                int nowY = j;
                                while (nowX != 0 && nowY != 0) {
                                    nowX--;
                                    nowY--;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 2) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            //здесь если задаваемая глубина просчета достигла максимума оцениваем
                                            // состояние поля, если нет - считаем дальше
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.WHITE);
                                                if (score[4] <= currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score <= currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            //отменяем совершенный ранее ход для возможности просчета остальных
                                            makeOldBoard(oldBoard, Color.WHITE);
                                        }
                                    }
                                }

                                //здес и ниже действуем по тому же принципу, только в других направлениях
                                nowX = i;
                                nowY = j;
                                while (nowX != 7 && nowY != 0) {
                                    nowX++;
                                    nowY--;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 2) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.WHITE);
                                                if (score[4] <= currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score <= currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.WHITE);
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 7 && nowY != 7) {
                                    nowX++;
                                    nowY++;
                                    if (cellExist(nowX, nowY)) {
                                        if (cellExist(nowX, nowY)) {
                                            if (canMove(nowX, nowY, board[i][j].getChecker()) == 2) {
                                                Cell[][] oldBoard = copyBoard();
                                                makeMove(nowX, nowY, board[i][j].getChecker());
                                                if (depth != maxDepth) {
                                                    Integer[] score = minimax(depth + 1, Color.WHITE);
                                                    if (score[4] <= currentDepth[4]) {
                                                        currentDepth[0] = i;
                                                        currentDepth[1] = j;
                                                        currentDepth[2] = nowX;
                                                        currentDepth[3] = nowY;
                                                        currentDepth[4] = score[4];
                                                    }
                                                } else {
                                                    int score = getEvaluation();
                                                    if (score <= currentDepth[4]) {
                                                        currentDepth[0] = i;
                                                        currentDepth[1] = j;
                                                        currentDepth[2] = nowX;
                                                        currentDepth[3] = nowY;
                                                        currentDepth[4] = score;
                                                    }
                                                }
                                                makeOldBoard(oldBoard, Color.WHITE);
                                            }
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 0 && nowY != 7) {
                                    nowX--;
                                    nowY++;
                                    if (cellExist(nowX, nowY)) {
                                        if (cellExist(nowX, nowY)) {
                                            if (canMove(nowX, nowY, board[i][j].getChecker()) == 2) {
                                                Cell[][] oldBoard = copyBoard();
                                                makeMove(nowX, nowY, board[i][j].getChecker());
                                                if (depth != maxDepth) {
                                                    Integer[] score = minimax(depth + 1, Color.WHITE);
                                                    if (score[4] <= currentDepth[4]) {
                                                        currentDepth[0] = i;
                                                        currentDepth[1] = j;
                                                        currentDepth[2] = nowX;
                                                        currentDepth[3] = nowY;
                                                        currentDepth[4] = score[4];
                                                    }
                                                } else {
                                                    int score = getEvaluation();
                                                    if (score <= currentDepth[4]) {
                                                        currentDepth[0] = i;
                                                        currentDepth[1] = j;
                                                        currentDepth[2] = nowX;
                                                        currentDepth[3] = nowY;
                                                        currentDepth[4] = score;
                                                    }
                                                }
                                                makeOldBoard(oldBoard, Color.WHITE);
                                            }
                                        }
                                    }
                                }
                            } else {
                                //если не дамка, она может сделать ходы со взятием только на 2 клетки в любом
                                // направлении, так же проверим каждое из них
                                if (canMove(i - 2, j - 2, board[i][j].getChecker()) == 2) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i - 2, j - 2, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.WHITE);
                                        if (score[4] < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 2;
                                            currentDepth[3] = j - 2;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score <= currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 2;
                                            currentDepth[3] = j - 2;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.WHITE);
                                }
                                if (canMove(i + 2, j + 2, board[i][j].getChecker()) == 2) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i + 2, j + 2, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.WHITE);
                                        if (score[4] < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 2;
                                            currentDepth[3] = j + 2;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 2;
                                            currentDepth[3] = j + 2;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.WHITE);
                                }
                                if (canMove(i - 2, j + 2, board[i][j].getChecker()) == 2) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i - 2, j + 2, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.WHITE);
                                        if (score[4] < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 2;
                                            currentDepth[3] = j + 2;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 2;
                                            currentDepth[3] = j + 2;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.WHITE);
                                }
                                if (canMove(i + 2, j - 2, board[i][j].getChecker()) == 2) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i + 2, j - 2, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.WHITE);
                                        if (score[4] < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 2;
                                            currentDepth[3] = j - 2;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 2;
                                            currentDepth[3] = j - 2;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.WHITE);
                                }
                            }
                        }
                    } else {
                        //возможности взятия нет
                        if (board[i][j].hasChecker() && board[i][j].getChecker().color == Color.BLACK) {
                            if (board[i][j].getChecker().isDamka) {
                                //шашка - дамка - проверяем все 4 стороны до края доски
                                int nowX = i;
                                int nowY = j;
                                while (nowX != 0 && nowY != 0) {
                                    nowX--;
                                    nowY--;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 1) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.WHITE);
                                                if (score[4] < currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score < currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.WHITE);
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 7 && nowY != 0) {
                                    nowX++;
                                    nowY--;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 1) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.WHITE);
                                                if (score[4] < currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score < currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.WHITE);
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 7 && nowY != 7) {
                                    nowX++;
                                    nowY++;
                                    if (cellExist(nowX, nowY)) {
                                        if (cellExist(nowX, nowY)) {
                                            if (canMove(nowX, nowY, board[i][j].getChecker()) == 1) {
                                                Cell[][] oldBoard = copyBoard();
                                                makeMove(nowX, nowY, board[i][j].getChecker());
                                                if (depth != maxDepth) {
                                                    Integer[] score = minimax(depth + 1, Color.WHITE);
                                                    if (score[4] < currentDepth[4]) {
                                                        currentDepth[0] = i;
                                                        currentDepth[1] = j;
                                                        currentDepth[2] = nowX;
                                                        currentDepth[3] = nowY;
                                                        currentDepth[4] = score[4];
                                                    }
                                                } else {
                                                    int score = getEvaluation();
                                                    if (score < currentDepth[4]) {
                                                        currentDepth[0] = i;
                                                        currentDepth[1] = j;
                                                        currentDepth[2] = nowX;
                                                        currentDepth[3] = nowY;
                                                        currentDepth[4] = score;
                                                    }
                                                }
                                                makeOldBoard(oldBoard, Color.WHITE);
                                            }
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 0 && nowY != 7) {
                                    nowX--;
                                    nowY++;
                                    if (cellExist(nowX, nowY)) {
                                        if (cellExist(nowX, nowY)) {
                                            if (canMove(nowX, nowY, board[i][j].getChecker()) == 1) {
                                                Cell[][] oldBoard = copyBoard();
                                                makeMove(nowX, nowY, board[i][j].getChecker());
                                                if (depth != maxDepth) {
                                                    Integer[] score = minimax(depth + 1, Color.WHITE);
                                                    if (score[4] < currentDepth[4]) {
                                                        currentDepth[0] = i;
                                                        currentDepth[1] = j;
                                                        currentDepth[2] = nowX;
                                                        currentDepth[3] = nowY;
                                                        currentDepth[4] = score[4];
                                                    }
                                                } else {
                                                    int score = getEvaluation();
                                                    if (score < currentDepth[4]) {
                                                        currentDepth[0] = i;
                                                        currentDepth[1] = j;
                                                        currentDepth[2] = nowX;
                                                        currentDepth[3] = nowY;
                                                        currentDepth[4] = score;
                                                    }
                                                }
                                                makeOldBoard(oldBoard, Color.WHITE);
                                            }
                                        }
                                    }
                                }
                            } else {
                                //шашка - не дамка, проверяем лишь два направления хода на клетку
                                // вперед-влево или вперед-вправо
                                if (canMove(i + 1, j + 1, board[i][j].getChecker()) == 1) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i + 1, j + 1, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.WHITE);
                                        if (score[4] < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 1;
                                            currentDepth[3] = j + 1;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 1;
                                            currentDepth[3] = j + 1;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.WHITE);
                                }
                                if (canMove(i - 1, j + 1, board[i][j].getChecker()) == 1) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i - 1, j + 1, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.WHITE);
                                        if (score[4] < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 1;
                                            currentDepth[3] = j + 1;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score < currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 1;
                                            currentDepth[3] = j + 1;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.WHITE);
                                }
                            }
                        }
                    }
                } else if (color == Color.WHITE) {
                    //для хода белой шашки применяются те же алгоритмы, что и для хода черной, только одно различие:
                    // направление вперед у нее противоположно направлению вперед у черной
                    //в остальном все похоже
                    if (needtobyteforWhite()) {
                        if (board[i][j].hasChecker() && board[i][j].getChecker().color == Color.WHITE && canByte(board[i][j])) {
                            if (board[i][j].getChecker().isDamka) {
                                int nowX = i;
                                int nowY = j;
                                while (nowX != 0 && nowY != 0) {
                                    nowX--;
                                    nowY--;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 2) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.BLACK);
                                                if (score[4] > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.BLACK);
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 7 && nowY != 0) {
                                    nowX++;
                                    nowY--;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 2) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.BLACK);
                                                if (score[4] > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.BLACK);
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 7 && nowY != 7) {
                                    nowX++;
                                    nowY++;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 2) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.BLACK);
                                                if (score[4] > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.BLACK);
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 0 && nowY != 7) {
                                    nowX--;
                                    nowY++;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 2) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.BLACK);
                                                if (score[4] > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.BLACK);
                                        }
                                    }
                                }
                            } else {
                                if (canMove(i - 2, j - 2, board[i][j].getChecker()) == 2) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i - 2, j - 2, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.BLACK);
                                        if (score[4] > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 2;
                                            currentDepth[3] = j - 2;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 2;
                                            currentDepth[3] = j - 2;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.BLACK);
                                }
                                if (canMove(i + 2, j + 2, board[i][j].getChecker()) == 2) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i + 2, j + 2, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.BLACK);
                                        if (score[4] > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 2;
                                            currentDepth[3] = j + 2;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 2;
                                            currentDepth[3] = j + 2;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.BLACK);
                                }
                                if (canMove(i - 2, j + 2, board[i][j].getChecker()) == 2) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i - 2, j + 2, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.BLACK);
                                        if (score[4] > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 2;
                                            currentDepth[3] = j + 2;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 2;
                                            currentDepth[3] = j + 2;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.BLACK);
                                }
                                if (canMove(i + 2, j - 2, board[i][j].getChecker()) == 2) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i + 2, j - 2, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.BLACK);
                                        if (score[4] > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 2;
                                            currentDepth[3] = j - 2;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 2;
                                            currentDepth[3] = j - 2;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.BLACK);
                                }
                            }
                        }
                    } else {
                        if (board[i][j].hasChecker() && board[i][j].getChecker().color == Color.WHITE) {
                            if (board[i][j].getChecker().isDamka) {
                                int nowX = i;
                                int nowY = j;
                                while (nowX != 0 && nowY != 0) {
                                    nowX--;
                                    nowY--;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 1) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.BLACK);
                                                if (score[4] > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.BLACK);
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 7 && nowY != 0) {
                                    nowX++;
                                    nowY--;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 1) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.BLACK);
                                                if (score[4] > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.BLACK);
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 7 && nowY != 7) {
                                    nowX++;
                                    nowY++;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 1) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.BLACK);
                                                if (score[4] > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.BLACK);
                                        }
                                    }
                                }

                                nowX = i;
                                nowY = j;
                                while (nowX != 0 && nowY != 7) {
                                    nowX--;
                                    nowY++;
                                    if (cellExist(nowX, nowY)) {
                                        if (canMove(nowX, nowY, board[i][j].getChecker()) == 1) {
                                            Cell[][] oldBoard = copyBoard();
                                            makeMove(nowX, nowY, board[i][j].getChecker());
                                            if (depth != maxDepth) {
                                                Integer[] score = minimax(depth + 1, Color.BLACK);
                                                if (score[4] > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score[4];
                                                }
                                            } else {
                                                int score = getEvaluation();
                                                if (score > currentDepth[4]) {
                                                    currentDepth[0] = i;
                                                    currentDepth[1] = j;
                                                    currentDepth[2] = nowX;
                                                    currentDepth[3] = nowY;
                                                    currentDepth[4] = score;
                                                }
                                            }
                                            makeOldBoard(oldBoard, Color.BLACK);
                                        }
                                    }
                                }
                            } else {
                                if (canMove(i - 1, j - 1, board[i][j].getChecker()) == 1) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i - 1, j - 1, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.BLACK);
                                        if (score[4] > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 1;
                                            currentDepth[3] = j - 1;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i - 1;
                                            currentDepth[3] = j - 1;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.BLACK);
                                }
                                if (canMove(i + 1, j - 1, board[i][j].getChecker()) == 1) {
                                    Cell[][] oldBoard = copyBoard();
                                    makeMove(i + 1, j - 1, board[i][j].getChecker());
                                    if (depth != maxDepth) {
                                        Integer[] score = minimax(depth + 1, Color.BLACK);
                                        if (score[4] > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 1;
                                            currentDepth[3] = j - 1;
                                            currentDepth[4] = score[4];
                                        }
                                    } else {
                                        int score = getEvaluation();
                                        if (score > currentDepth[4]) {
                                            currentDepth[0] = i;
                                            currentDepth[1] = j;
                                            currentDepth[2] = i + 1;
                                            currentDepth[3] = j - 1;
                                            currentDepth[4] = score;
                                        }
                                    }
                                    makeOldBoard(oldBoard, Color.BLACK);
                                }
                            }
                        }
                    }
                }
            }
        }
        //возвращаем список, содержащий наилучший ход и его оценку
        return currentDepth;
    }

}
