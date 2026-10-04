
module pl.edu.pwr.pwrinspace.poliwrocket {
    requires com.sothawo.mapjfx;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.slf4j;
    requires eu.hansolo.tilesfx;
    requires eu.hansolo.medusa;
    requires importer;
    requires annotations;
    requires java.logging;
    requires nrjavaserial;
    requires com.google.gson;
    requires javatuples;
    requires net.dv8tion.jda;
    requires com.fasterxml.jackson.core;
    requires java.desktop;
    requires freetts;
    requires jdk.jshell;
    requires com.jfoenix;
    requires protobuf.java;
    requires com.google.common;
    requires javafx.base;
    requires com.pi4j;

    opens pl.edu.pwr.pwrinspace.poliwrocket to javafx.fxml, javafx.controls, javafx.web, javafx.graphics, javafx.media, javafx.base, com.google.gson;
    opens pl.edu.pwr.pwrinspace.poliwrocket.Controller to javafx.fxml, javafx.controls, javafx.web, javafx.graphics, javafx.media, javafx.base;
    opens pl.edu.pwr.pwrinspace.poliwrocket.Model.Command to com.google.gson;
    opens pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor to com.google.gson;
    opens pl.edu.pwr.pwrinspace.poliwrocket.Model.Notification to com.google.gson;
    opens pl.edu.pwr.pwrinspace.poliwrocket.Model.Speech to com.google.gson;
    opens pl.edu.pwr.pwrinspace.poliwrocket.Service.Save to com.google.gson;

    exports pl.edu.pwr.pwrinspace.poliwrocket.Model.Configuration;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Model.Command;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Model.Sensor;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Model.MessageParser;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Model.Notification;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Service.Notification;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Thred;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Service.Save;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Event;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Model;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Controller to javafx.base, javafx.controls, javafx.fxml, javafx.graphics, javafx.media, javafx.web, pl.edu.pwr.pwrinspace.poliwrocket.Controller;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Model.Protobuf;
    opens pl.edu.pwr.pwrinspace.poliwrocket.Model.Protobuf to com.google.gson;
    exports pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Content;
    opens pl.edu.pwr.pwrinspace.poliwrocket.Model.Command.Content to com.google.gson;
}
