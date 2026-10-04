package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class DetachedTabController implements InvalidationListener {

    private Stage myStage;
    private Node content;

    private static final double initWidth = 1550.4;
    private static final double initHeight = 838.4;

    public void setStageAndContent(Stage stage, Node content) {
        this.myStage = stage;
        this.content = content;

        stage.widthProperty().addListener(this);
        stage.heightProperty().addListener(this);
    }

    @Override
    public void invalidated(Observable observable) {
        if (myStage.widthProperty().equals(observable) || myStage.heightProperty().equals(observable)) {
            scaleContent(myStage.getWidth() / initWidth, myStage.getHeight() / initHeight);
        }
    }

    private void scaleContent(double scaleX, double scaleY) {
        if (content != null) {
            if(!content.getTransforms().isEmpty()) {
                content.getTransforms().clear();
            }
            content.getTransforms().add(new Scale(scaleX, scaleY, 0, 0));
        }
    }

}
