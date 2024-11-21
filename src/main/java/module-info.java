module se233.teamnoonkhem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;
    requires javafx.swing;


    opens se233.teamnoonkhem to javafx.fxml;
    exports se233.teamnoonkhem;
    exports se233.teamnoonkhem.Controller;
    opens se233.teamnoonkhem.Controller to javafx.fxml;
}