package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Main extends Application {
    private static final int R = 15;

    private static int w;
    private static int h;
    private static int start;

    private static Map<Integer, Set<Integer>> graph = new HashMap<>();
    private static Map<Integer, Pair<Integer, Integer>> positions = new HashMap<>();
    private static Map<Integer, Circle> circles = new HashMap<>();
    private static Map<Pair<Integer, Integer>, Line> lines = new HashMap<>();
    private static List<Label> labels = new ArrayList<>();
    private static Map<Pair<Integer, Integer>, Label> distanceLabels = new HashMap<>();
    private static Group root;
    private static Timeline bfsAnimation;
    private static Stage stage;

    private static void clear() {
        graph.clear();
        positions.clear();
        circles.clear();
        lines.clear();
        labels.clear();
        distanceLabels.clear();
        bfsAnimation = null;
        root.getChildren().clear();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Main.stage = stage;

        root = new Group();
        Scene scene = new Scene(root, w, h);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        Button startAnimationBtn = new Button("Start");
        startAnimationBtn.setLayoutX(10);
        startAnimationBtn.setLayoutY(10);
        startAnimationBtn.setOnMouseClicked(e -> {
            if(circles.isEmpty()) {
                return;
            }

            circles.forEach((x, y) -> {
                if (x == start) {
                    y.setFill(Color.GREEN);
                } else {
                    y.setFill(Color.RED);
                }
            });

            if (bfsAnimation == null) {
                bfsAnimation = bfs();
            }

            bfsAnimation.play();
        });

        Button openFileBtn = new Button("Open");
        openFileBtn.setLayoutX(60);
        openFileBtn.setLayoutY(10);
        openFileBtn.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open input file");

            File file = fileChooser.showOpenDialog(stage);
            clear();
            root.getChildren().addAll(startAnimationBtn, openFileBtn);
            if (file != null) {
                initialize(file);
                drawCircles();
            }
        });

        root.getChildren().addAll(startAnimationBtn, openFileBtn);
    }


    public static void main(String[] args) {
        launch(args);
    }

    private static void initialize(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().filter(x -> !x.isEmpty()).forEach(x -> {
                String[] split = x.split(" ");

                switch (split[0]) {
                    case "size":
                        w = parseInt(split[1]);
                        h = parseInt(split[2]);
                        stage.setWidth(w);
                        stage.setHeight(h);
                        break;
                    case "start":
                        start = parseInt(split[1]);
                        break;
                    case "pos":
                        positions.put(parseInt(split[1]), new Pair<>(parseInt(split[2]), h - parseInt(split[3])));
                        break;
                    default:
                        int from = parseInt(split[0]);
                        graph.putIfAbsent(from, new HashSet<>());

                        for (int i = 1; i < split.length; ++i) {
                            graph.get(from).add(parseInt(split[i]));
                        }
                        break;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void drawCircles() {
        for (int u : positions.keySet()) {
            int x = positions.get(u).getKey();
            int y = positions.get(u).getValue();

            Circle circle = new Circle();
            circle.setCenterX(x);
            circle.setCenterY(y);
            circle.setRadius(R);
            circle.setFill(Color.RED);

            Label label = new Label(String.valueOf(u));
            label.setLayoutX(x - R / 2);
            label.setLayoutY(y - R / 2);
            label.setTextFill(Color.WHITE);

            if (u == start) {
                circle.setFill(Color.GREEN);
            }

            circle.setOnMouseDragged(e -> {
                circle.setCenterX(e.getX());
                circle.setCenterY(e.getY());

                label.setLayoutX(e.getX() - R / 2);
                label.setLayoutY(e.getY() - R / 2);
            });

            circles.put(u, circle);
            labels.add(label);

            for (int v : graph.get(u)) {
                Pair<Integer, Integer> vPos = positions.get(v);
                int toX = vPos.getKey();
                int toY = vPos.getValue();

                Line line;
                if (x <= toX && y <= toY) {
                    line = new Line(x, y, toX, toY);
                } else {
                    line = new Line(toX, toY, x, y);
                }

                lines.put(new Pair<>(min(u, v), max(u, v)), line);
            }
        }

        root.getChildren().addAll(lines.values());
        System.out.println(lines);

        lines.forEach((x, y) -> {
            Circle circleFrom = circles.get(x.getKey());
            Circle circleTo = circles.get(x.getValue());

            y.startXProperty().bind(circleFrom.centerXProperty());
            y.startYProperty().bind(circleFrom.centerYProperty());
            y.endXProperty().bind(circleTo.centerXProperty());
            y.endYProperty().bind(circleTo.centerYProperty());

            Label distanceLabel = new Label("∞");

            DoubleProperty fromX = y.startXProperty();
            DoubleProperty fromY = y.startYProperty();
            DoubleProperty toX = y.endXProperty();
            DoubleProperty toY = y.endYProperty();

            distanceLabel.layoutXProperty().bind(fromX.add(toX).divide(2));
            distanceLabel.layoutYProperty().bind(fromY.add(toY).divide(2));

            root.getChildren().add(distanceLabel);
            int from = min(x.getKey(), x.getValue());
            int to = max(x.getKey(), x.getValue());
            distanceLabels.put(new Pair<>(from, to), distanceLabel);
        });

        circles.values().stream().forEach(circle -> root.getChildren().add(circle));
        labels.forEach(label -> root.getChildren().add(label));
    }

    private Timeline bfs() {
        Queue<Integer> q = new LinkedList<>();
        q.add(start);
        Set<Integer> used = new HashSet<>();
        Map<Integer, Integer> d = new HashMap<>();

        d.put(start, 0);
        used.add(start);

        Timeline animation = new Timeline();
        final int[] duration = {1};

        while (!q.isEmpty()) {
            int from = q.poll();

            graph.get(from).stream().filter(to -> !used.contains(to)).forEach(to -> {
                used.add(to);
                q.add(to);
                d.put(to, d.get(from) + 1);

                animation.getKeyFrames().add(new KeyFrame(Duration.seconds(duration[0]), e -> {
                    circles.get(to).setFill(Color.GREEN);
                    distanceLabels.get(new Pair<>(min(from, to), max(from, to))).setText(String.valueOf(d.get(to)));
                }));
            });

            duration[0]+=3;
        }

        animation.setAutoReverse(false);
        animation.setCycleCount(1);
        return animation;
    }
}
