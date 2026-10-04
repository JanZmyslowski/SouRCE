package pl.edu.pwr.pwrinspace.poliwrocket.Controller;

import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseController implements InvalidationListener {

    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public String getControllerName() {
        return getClass().getSimpleName().replace("Controller", "");
    }

    @FXML
    protected void initialize() {};
}
