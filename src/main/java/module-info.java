module com.tcxconverter {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;

    opens com.tcxconverter to javafx.fxml;
    opens com.tcxconverter.controller to javafx.fxml;
}
