package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor.ISensor;

public  class ErrorsController extends BaseInterpretersController {

    private static final int  MAX_ELEMENTS = 24;
    @Override
    protected void buildVisualizationMap() {
        super.buildVisualizationMap();
        labelHashMap.clear();
        valueHashMap.clear();

        int layoutX = 14;
        int layoutY = 8;
        int labelWidth = 150;
        int valueWidth = 100;
        int height = 18;

        int element = 0;
        for (ISensor sensor : this.getSortedSensors()) {
            if(element < MAX_ELEMENTS) {
                if(element != 0 && element % 2 == 0) {
                    layoutY += 26;
                    layoutX -= 265;
                } else if(element % 2 != 0){
                    layoutX += 265;
                }

                Label label = new Label();
                Label value = new Label();

                label.setAlignment(Pos.CENTER);
                label.setLayoutX(layoutX);
                label.setLayoutY(layoutY);
                label.setPrefHeight(height);
                label.setPrefWidth(labelWidth);
                label.setVisible(true);

                value.setLayoutX(layoutX + 150);
                value.setLayoutY(layoutY);
                value.setPrefHeight(height);
                value.setPrefWidth(valueWidth);

                mainPanel.getChildren().add(label);
                mainPanel.getChildren().add(value);

                labelHashMap.put(sensor.getDestination(), label);
                valueHashMap.put(sensor.getDestination(), value);
            }

            element++;
        }

    }
}
