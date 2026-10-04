package pl.edu.pwr.pwrinspace.poliwrocket.Model.Skin;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.skins.TileSkin;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class TimerSkin extends TileSkin {
    private Rectangle background;
    private Text countdownText;
    private Timeline countdownTimeline;

    private int remainingSeconds;

    public TimerSkin(final Tile tile, int countdownDurationInSeconds) {
        super(tile);
        this.remainingSeconds = countdownDurationInSeconds;
        initGraphics();
        registerListeners();
        startCountdown();
    }

    @Override
    protected void initGraphics() {
        super.initGraphics();

        background = new Rectangle();
        background.setFill(Color.web("#2B2C2D"));
        getPane().getChildren().add(background);

        countdownText = new Text();
        Color countdownColor = remainingSeconds > 10 ? Color.WHITE : Color.RED;
        countdownText.setFill(countdownColor);
        countdownText.setFont(Font.font(24));
        getPane().getChildren().add(countdownText);

        updateCountdown();
    }

    @Override
    protected void registerListeners() {
        tile.widthProperty().addListener((obs, oldWidth, newWidth) -> resize());
        tile.heightProperty().addListener((obs, oldHeight, newHeight) -> resize());
    }

    @Override
    protected void resize() {
        super.resize();

        double width = tile.getWidth();
        double height = tile.getHeight();

        background.setWidth(width);
        background.setHeight(height);

        countdownText.setX((width - countdownText.getLayoutBounds().getWidth()) / 2);
        countdownText.setY((height + countdownText.getLayoutBounds().getHeight()) / 2);
    }

    private void startCountdown() {
        countdownTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, event -> {
                    if (remainingSeconds > 0) {
                        updateCountdown();
                        remainingSeconds--;
                    } else {
                        countdownTimeline.stop();
                        countdownText.setText("00:00");
                    }
                }),
                new KeyFrame(Duration.seconds(1))
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateCountdownColor() {
        Color countdownColor = remainingSeconds > 10 ? Color.WHITE : Color.RED;
        countdownText.setFill(countdownColor);
    }

    private void updateCountdown() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        countdownText.setText(String.format("%02d:%02d", minutes, seconds));
        updateCountdownColor();
        resize();
    }

    public void setCountdown(int countdownDurationInSeconds) {
        this.remainingSeconds = countdownDurationInSeconds;

        if (remainingSeconds > 0) {
            countdownTimeline.stop();
            startCountdown();
        }
        updateCountdown();
    }
}
