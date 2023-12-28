module com.example.roomfactors2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.json;
    requires android.json;
    requires opencv;

    opens com.example.roomfactors2 to javafx.fxml;
    exports com.example.roomfactors2;
}