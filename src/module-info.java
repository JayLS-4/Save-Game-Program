module module_name {

    exports main to javafx.graphics;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens gui to javafx.fxml;
}