module DishItFX {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    exports application;
    opens application to javafx.fxml;
    opens application.controllers to javafx.fxml;

}
