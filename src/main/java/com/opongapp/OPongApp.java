package com.opongapp;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Random;
import javax.sound.midi.*;

class BinkBonkSound {

    private static final int MAX_PITCH_BEND = 16383;
    private static final int MIN_PITCH_BEND = 0;
    private static final int REVERB_LEVEL_CONTROLLER = 91;
    private static final int MIN_REVERB_LEVEL = 0;
    private static final int DRUM_MIDI_CHANNEL = 9;
    private static final int CLAVES_NOTE = 76;
    private static final int NORMAL_VELOCITY = 100;

    Instrument[] instrument;
    MidiChannel[] midiChannels;
    boolean playSound;

    public BinkBonkSound(){
        playSound=true;
        try{
            Synthesizer gmSynthesizer = MidiSystem.getSynthesizer();
            gmSynthesizer.open();
            instrument = gmSynthesizer.getDefaultSoundbank().getInstruments();
            midiChannels = gmSynthesizer.getChannels();

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    void play(boolean hiPitch){
        if(playSound) {
            midiChannels[DRUM_MIDI_CHANNEL]
                    .setPitchBend(hiPitch ? MAX_PITCH_BEND : MIN_PITCH_BEND);

            midiChannels[DRUM_MIDI_CHANNEL]
                    .controlChange(REVERB_LEVEL_CONTROLLER, MIN_REVERB_LEVEL);

            midiChannels[DRUM_MIDI_CHANNEL]
                    .noteOn(CLAVES_NOTE, NORMAL_VELOCITY);
        }
    }

    public void toggleSound() {
        playSound = !playSound;
    }
}

class ScoreDisplay extends Pane {

    private static final int SCORE_FONT_SIZE = 25;
    private static final FontWeight SCORE_FONT_WEIGHT = FontWeight.BOLD;
    private static final Color SCORE_FONT_COLOR = Color.BLUE;

    ScoreDisplay(int displayW, int displayH) {  

        setTranslateX(displayW / 2 - SCORE_FONT_SIZE / 2);
        setTranslateY(displayH / 2 - SCORE_FONT_SIZE / 2);
        update(0);
    }

    void update(int score) {

        if (!getChildren().isEmpty())
            getChildren().remove(0);

        Label l = createLabel(Integer.toString(score));
        getChildren().add(l);
    
    }

    Label createLabel(String score) {

        Label l = new Label(score);
        l.setTextFill(SCORE_FONT_COLOR);
        l.setFont(Font.font("Arial", SCORE_FONT_WEIGHT, SCORE_FONT_SIZE));
        return l;
    }
}

class GameTimer extends HBox {

    private static final Color FPS_FONT_COLOR = Color.RED;
    private static final FontWeight FPS_FONT_WEIGHT = FontWeight.NORMAL;
    private static final int FPS_FONT_SIZE = 15;
    private static final Insets MARGINS = new Insets(0, 5, 0,0);
 
    boolean showFlag = false;

    Label l1, l2, l3;
    
    GameTimer() {
        l1 = new Label("");
        l2 = new Label("");
        l3 = new Label("");
        setTranslateX(0);
        setTranslateY(-1000);
        getChildren().addAll(l1, l2, l3);
    }

    void update(double info, String desc, int pos) {

        getChildren().remove(pos);
        String s = String.format(String.format("%.2f ", info) + desc);
        Label label = createLabel(s);
        Pane pane = new Pane(label);
        getChildren().add(pos, pane);
        setMargin(getChildren().get(pos), MARGINS);
    }

    Label createLabel(String s) {
        Label l = new Label(s);
        l.setTextFill(FPS_FONT_COLOR);
        l.setFont(Font.font("Arial", FPS_FONT_WEIGHT, FPS_FONT_SIZE));
        return l;
    }

    void show() {
        if (showFlag) {
            setTranslateY(-1000);
            showFlag = false;
        } else {
            setTranslateY(0);
            showFlag = true;
        }
    }
}
class Ball extends Rectangle {

    Ball(Point2D pos, int width, int height, Color color) {
        super(width, height, color);
        setTranslateX(pos.getX());
        setTranslateY(pos.getY());
    }

}
class Bat extends Rectangle {

    Bat(Point2D pos, int width, int height, Color color) {
        super(width, height, color);     
        setTranslateX(pos.getX());
        setTranslateY(pos.getY());
    }
}
class Bounds extends Rectangle {

    Bounds(Point2D pos, int width, int height) {
        super(width, height);
        setTranslateX(pos.getX());
        setTranslateY(pos.getY());
    }
}
class Pong extends Group {

    public static final int GAME_HEIGHT = 600;
    public static final int GAME_WIDTH = 400;
    public static final int BAT_WIDTH = GAME_WIDTH / 3;
    public static final int BAT_HEIGHT = GAME_HEIGHT / 25;
    public static final int MIN_BAT_WIDTH = 5;
    public static final int BALL_SIZE = GAME_WIDTH / 10;
    public static final int BASE_SPEED = 4;
    public static final int BASE_ANGLE = 5;
    public static final int UPDATE_TIME = 2500;
    public static final int BAT_REDUCTION_VALUE = 2;
    public static final Color BAT_COLOR = Color.TEAL;
    public static final Color BALL_COLOR = Color.BLUE;

    Ball ball;
    Bat bat;
    Bounds topBounds, leftBounds, rightBounds;
    GameTimer gameTimer = new GameTimer();
    ScoreDisplay scoreDisplay = new ScoreDisplay(GAME_WIDTH, GAME_WIDTH);
    Random r = new Random();
    BinkBonkSound sound = new BinkBonkSound();

    boolean fpsPaneFlag = false;

    double gravityX = 0;
    double gravityY = BASE_SPEED;
    double batVelocity = 0;
    double angle = 0;
    int points = 0;    

    public Pong() {
        ball = new Ball(new Point2D(r.nextInt(GAME_WIDTH - BALL_SIZE),0), BALL_SIZE, BALL_SIZE, BALL_COLOR);
        bat = new Bat(new Point2D(0, GAME_HEIGHT - BAT_HEIGHT), 
                BAT_WIDTH, BAT_HEIGHT, BAT_COLOR);

        topBounds = new Bounds(new Point2D(0,0), GAME_WIDTH, 0);
        leftBounds = new Bounds(new Point2D(0,0), 0, GAME_HEIGHT);
        rightBounds = new Bounds(new Point2D(GAME_WIDTH, 0 ), 0, GAME_HEIGHT);

        getChildren().addAll(ball, bat, scoreDisplay, gameTimer,
        topBounds, leftBounds, rightBounds);
        startAnimation();
    }

    public void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {
            double old = -1;
            double elapsedTime = 0;
            double speed = 0;
            double acceleration = 0.1;
            double initTime = 0;
            double rotationValue = 5;
            double ft = 0;
            int difficulty = 5;
            boolean bouncedBack = false;
            int rotation = 0;
            Point2D lastBatPos = new Point2D(0, 0);
            Point2D lastBallPos = new Point2D(0, 0);

            @Override
            public void handle(long now) {
                if (old < 0)
                    old = now;
                double delta = (now - old) / 1e9;

                old = now;
                elapsedTime += delta;

                double frames = 1 / delta;
                ft = (1 / frames) * 1000;

                if (now % UPDATE_TIME == 0) {
                    updateFpsDescription(frames, "FPS (avg)", 0);
                    updateFpsDescription(ft, "FT (ms avg)", 1);
                    updateFpsDescription(elapsedTime, "GT (s)", 2);
                }

                if (now % (UPDATE_TIME / 2) == 0) {
                    lastBatPos = new Point2D(bat.getTranslateX(),
                            bat.getTranslateY());
                    lastBallPos = new Point2D(ball.getTranslateX(),
                            ball.getTranslateY());
                    initTime = elapsedTime;
                }

                updateRotation();

                ball.setTranslateX(ball.getTranslateX() + gravityX);
                ball.setTranslateY(ball.getTranslateY() + gravityY);
                
                stuckBall();
                batOutOfBounds();
                
                if(collisionDetection(ball, bat)) {
                    updateVelocity();
                    bounceBat(lastBallPos, speed);
                    gravityX *= -1;
                    gravityY *= -1;
                }

                if (collisionDetection(ball, topBounds)) {
                    bounceWall(lastBallPos, speed);
                    gravityX *= -1;
                    gravityY *= -1;
                }

                if (collisionDetection(ball, leftBounds) || 
                    collisionDetection(ball, rightBounds)) {
                        bounceWall(lastBallPos, speed);
                        rotation+=rotationValue;
                }

                if (ball.getTranslateY() + ball.getHeight() >= GAME_HEIGHT) {
                    ballHasFallen();
                }
            }

            public void batOutOfBounds() {
                if (collide(bat, rightBounds) || collide(bat, leftBounds)) {
                    bat.setFill(Color.RED);
                } else {
                    bat.setFill(BAT_COLOR);
                }
            }

            public boolean collisionDetection(Rectangle a, Rectangle b) {
                if (collide(a, b)) {
                    scoredAPoint();
                    sound.play(true);
                    return true;
                }
                return false;
            }

            public void scoredAPoint() {
                if (points % difficulty == 0)
                    shrinkBat();
                
                bouncedBack = true;
                points++;
                speed += acceleration;
                rotation += rotationValue;
                addNewScore(points);
                bounceWall(lastBallPos, speed);

            }

            public void updateRotation() {
                ball.setRotate(rotation);

                if (bouncedBack) {
                    rotation += batVelocity;

                    if (batVelocity < 0) 
                        ball.setRotate(rotation);
                    else
                        ball.setRotate(-rotation);
                }
            }

            public void updateVelocity() {
                batVelocity = (bat.getTranslateX() - lastBatPos.getX()) / 
                            ((elapsedTime + 1) - initTime);
                batVelocity *= ft;
            }

            public void shrinkBat() {
                if (bat.getWidth() > MIN_BAT_WIDTH)
                    bat.setWidth(bat.getWidth() - BAT_REDUCTION_VALUE);
                else {
                    bat.setWidth(MIN_BAT_WIDTH);
                }
            }

            public void stuckBall() {

                double ballH = ball.getTranslateY() + ball.getHeight();
                double ballW = ball.getTranslateX() + ball.getWidth();
                double batW = bat.getTranslateX() + bat.getWidth();
                double batH = bat.getTranslateY() + bat.getHeight();
                
                if (ballH > bat.getTranslateY() && 
                ballH < batH && 
                ballW > bat.getTranslateX() &&
                ballW < batW) {
                        ball.setTranslateY(ballH - bat.getHeight());
                        ball.setTranslateX(ballW - bat.getTranslateX());
                }
            }

            public void ballHasFallen() {
                bouncedBack = false;
                points = 0;
                speed = 0;
                bat.setWidth(BAT_WIDTH);
                ball.setRotate(0);
                rotation = 0;
                addNewScore(points);
                gravityX = 0;
                gravityY = BASE_SPEED;
                ball.setTranslateX(r.nextInt(GAME_WIDTH - BALL_SIZE));
                ball.setTranslateY(0);

            }
        };
        loop.start();
    }

    public void bounceWall(Point2D lastPos, double speed) {
        if (lastPos.getX() > ball.getTranslateX()) {
            gravityX = Math.cos(Math.toRadians(BASE_ANGLE));
            gravityX += (BASE_SPEED + speed);
        } else {
            gravityX = -Math.cos(Math.toRadians(BASE_ANGLE));
            gravityX += (-BASE_SPEED + (-speed));
        }
    }

    public void bounceBat(Point2D lastPos, double speed) {

        if (batVelocity == 0) {
            gravityX = Math.cos(Math.toRadians(90));
            gravityY += (BASE_SPEED + speed);
        } else if (batVelocity < 0) {
            gravityX = Math.cos(Math.toRadians(BASE_ANGLE));
            gravityX += (BASE_SPEED + speed);
        } else {
            gravityX = -Math.cos(Math.toRadians(BASE_ANGLE));
            gravityX += (-BASE_SPEED + (-speed));
        }
    }

    public Rectangle createBat(int w, int h, Color c) {
        Rectangle r = new Rectangle(w, h);
        r.setTranslateX(GAME_WIDTH / 30);
        r.setTranslateY(GAME_HEIGHT - h);
        r.setFill(c);

        return r;
    }

    public boolean collide(Rectangle a, Rectangle b) {
        if (a.getTranslateY() + a.getHeight() >= b.getTranslateY() &&
                a.getTranslateY() <= b.getTranslateY() + b.getHeight() &&
                a.getTranslateX() + a.getWidth() >= b.getTranslateX() &&
                a.getTranslateX() <= b.getTranslateX() + b.getWidth()) {
            return true;
        }

        return false;
    }

    public void addNewScore(int points) {
        scoreDisplay.update(points);
    }

    public void updateFpsDescription(double frames, String description, int pos) {
        gameTimer.update(frames, description, pos);
    }

    void handleMouseMove(MouseEvent e) {
        bat.setTranslateX(e.getX() - (bat.getWidth() / 2));
    }

    void handleFpsPaneShow() {
        gameTimer.show();
    }

    void handleSound() {
        sound.toggleSound();
    }
}

public class OPongApp extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        Pong root = new Pong();
        scene = new Scene(root, Pong.GAME_WIDTH, Pong.GAME_HEIGHT);
        stage.setScene(scene);

        scene.setOnMouseMoved(e -> root.handleMouseMove(e));
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.I) {
                root.handleFpsPaneShow();
            } else if (e.getCode() == KeyCode.S) {
                root.handleSound();
            }

        });

        stage.setTitle("PONG!");
        stage.setResizable(false);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

}