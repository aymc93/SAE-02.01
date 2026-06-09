module com.example.sae2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.example.sae2 to javafx.fxml;
    exports com.example.sae2;
    exports com.example.sae2.controller;
    opens com.example.sae2.controller to javafx.fxml;
    exports com.example.sae2.vue.tours;
}