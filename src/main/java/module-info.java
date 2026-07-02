module com.github.tzesti.tcxconverter {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;

    opens com.github.tzesti.tcxconverter to javafx.fxml, javafx.graphics;
    opens com.github.tzesti.tcxconverter.controller to javafx.fxml;
}
