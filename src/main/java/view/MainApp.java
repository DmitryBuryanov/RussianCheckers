package view;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Checker;
import model.GameState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainApp extends Application {

    GameState gameState = new GameState();
    private final Group cells = new Group();
    private final Group checkers = new Group();
    public static int size = 100;
    Pane root = new Pane();
    private boolean friendGame = true;
    private model.Color iiColor = model.Color.NEITHRAL;
    private int rectanglesSize = 8; //количество клеток в ряду

    //отрисовка главного экрана приложения
    private Pane makeScreen() throws Exception {

        friendGame = true;
        iiColor = model.Color.NEITHRAL;
        Image image =
                new Image(new FileInputStream(new File("src\\main\\resources\\checkers-436285.jpg").getAbsolutePath()));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(rectanglesSize * size);
        imageView.setFitHeight(rectanglesSize * size);

        Text head = new Text("Welcome to Russian Checkers");

        Double headSize = 0.5;
        Double headX = 0.7;
        Double headY = 1.0;

        head.setFont(new Font(size * headSize));
        head.setFill(Color.WHITESMOKE);
        head.relocate(headX * size, headY);

        Double textSize = 0.2;
        Double textX = 0.7;
        Double textY = 7.5;

        Text down = new Text("Game by Dmitry Buryanov");
        down.setFont(new Font(size * textSize));
        down.setFill(Color.WHITESMOKE);
        down.relocate(textX * size, size * textY);

        Button but1 = new Button("Игра с другом");
        Button but2 = new Button("Игра с компьютером");

        Double but1X = 3.5;
        Double but1Y = 3.5;
        Double but2X = 3.3;
        Double but2Y = 4.0;

        but1.relocate(but1X * size, but1Y * size);
        but2.relocate(but2X * size, but2Y * size);
        root.setPrefSize(rectanglesSize * size, rectanglesSize * size);

        root.getChildren().addAll(imageView, but1, but2, head, down);

        but1.setOnMouseClicked(e -> {
            try {
                gameState.getBoard();
                friendGame = true;
                fillBoard();
                makeField();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        but2.setOnMouseClicked(e -> {
            try {
                choise();
                friendGame = false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return root;
    }

    //создание окна выбора, за какую сторону играть при игре с ИИ
    private void choise() throws FileNotFoundException {
        Image image =
                new Image(new FileInputStream(new File("src\\main\\resources\\checkers-436285.jpg").getAbsolutePath()));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(rectanglesSize * size);
        imageView.setFitHeight(rectanglesSize * size);

        Button but3 = new Button("Игра за белых");
        Button but4 = new Button("Игра за черных");

        Double but3X = 3.5;
        Double but3Y = 3.5;
        Double but4X = 3.45;
        Double but4Y = 4.0;

        but3.relocate(but3X * size, but3Y * size);
        but4.relocate(but4X * size, but4Y * size);
        root.setPrefSize(rectanglesSize * size, rectanglesSize * size);

        root.getChildren().clear();
        root.getChildren().addAll(imageView, but3, but4);

        but3.setOnMouseClicked(e -> {
            System.out.println("White");
            gameState.getBoard();
            try {
                fillBoard();
                makeField();
                iiColor = model.Color.BLACK;
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        but4.setOnMouseClicked(e -> {
            System.out.println("Black");
            gameState.getBoard();
            makeField();
            iiColor = model.Color.WHITE;
            gameState.makeIImove(iiColor);
            try {
                fillBoard();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });

    }

    //отрисовка поля
    private void makeField() {
        root.setPrefSize(rectanglesSize * size, rectanglesSize * size);
        for (int i = 0; i < rectanglesSize; i++) {
            for (int j = 0; j < rectanglesSize; j++) {
                Rectangle rectangle = createCell(i, j);
                cells.getChildren().add(rectangle);
            }
        }
        root.getChildren().clear();
        root.getChildren().addAll(cells, checkers);
    }

    //заполнение доски шашками
    private void fillBoard() throws FileNotFoundException {
        checkers.getChildren().clear();
        for (int i = 0; i < rectanglesSize; i++) {
            for (int j = 0; j < rectanglesSize; j++) {
                Checker checker;
                if (gameState.board[i][j].hasChecker()) {
                    checker = gameState.board[i][j].getChecker();
                    CheckerModel checkerModel = createChecker(i, j, checker, checker.isDamka);
                    checkers.getChildren().add(checkerModel);
                }
            }
        }
    }

    //запуск приложения
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(makeScreen(), rectanglesSize * size, rectanglesSize * size);
        stage.setScene(scene);
        stage.setTitle("Checkers");
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    //отрисовка клетки
    private Rectangle createCell(int i, int j) {
        Rectangle rectangle = new Rectangle();
        rectangle.setHeight(size);
        rectangle.setWidth(size);
        if (gameState.board[i][j].color == model.Color.BROWN) rectangle.setFill(Color.BROWN);
        else rectangle.setFill(Color.BEIGE);
        rectangle.relocate(i * size, j * size);
        return rectangle;
    }

    //создание модели шашки, с которой можно взаимодействовать
    private CheckerModel createChecker(int i, int j, Checker checker, boolean isDamka) throws FileNotFoundException {
        CheckerModel checkerModel = new CheckerModel(i, j, checker, isDamka);
        checkerModel.setOnMouseReleased(e -> {
            int newX = (int) Math.floor(e.getSceneX() / size);
            int newY = (int) Math.floor(e.getSceneY() / size);

            //если мы ходим не своей шашкой
            if (!friendGame) {
                if (checker.color == gameState.previousMoveColor) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Warning");
                    alert.setHeaderText(null);
                    alert.setContentText("It is not your checker");
                    alert.showAndWait();
                }
            }

            //если бить надо, а мы не бьем
            if (gameState.previousMoveColor == model.Color.BLACK && gameState.needtobyteforWhite() &&
                    checker.color == model.Color.WHITE && gameState.canMove(newX, newY, checker) != 2) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("Now you must byte black checker!");
                alert.showAndWait();
            }

            //если бить надо, а мы не бьем
            if (gameState.previousMoveColor == model.Color.WHITE && gameState.needtobyteforBlack() &&
                    checker.color == model.Color.BLACK && gameState.canMove(newX, newY, checker) != 2) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("Now you must byte white checker!");
                alert.showAndWait();
            }

            int moveType = gameState.canMove(newX, newY, checker);

            if ((gameState.needtobyteforWhite() && checker.color == model.Color.WHITE || gameState.needtobyteforBlack()
                    && checker.color == model.Color.BLACK) && moveType != 2) {
                moveType = 0;
            }
            //делаем ход
            if (moveType != 0) gameState.makeMove(newX, newY, checker);


            //проверка на окончанеи партии
            String str = gameState.gameover();
            if (str.equals("White won") || str.equals("Black won")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("We have winner");
                alert.setHeaderText(null);
                alert.setContentText("Game is over." + str);
                alert.showAndWait();
                try {
                    root.getChildren().clear();
                    makeScreen();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            //если играем с ИИ, его ход
            if (!friendGame && moveType != 0 && model.Color.NEITHRAL != iiColor) {
                gameState.makeIImove(iiColor);

                //проверка на конец партии после хода ИИ
                String str1 = gameState.gameover();
                if (str1.equals("White won") || str1.equals("Black won")) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("We have winner");
                    alert.setHeaderText(null);
                    alert.setContentText("Game is over." + str1);
                    alert.showAndWait();
                    try {
                        root.getChildren().clear();
                        makeScreen();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            //отображение именений
            checkers.getChildren().clear();
            try {
                fillBoard();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return checkerModel;
    }

}
