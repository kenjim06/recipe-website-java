module DishItFX {

    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.net.http;
    requires com.google.gson;

    exports application;
    opens application to javafx.fxml;
    opens application.controllers to javafx.fxml;
    opens application.models to com.google.gson;

}
