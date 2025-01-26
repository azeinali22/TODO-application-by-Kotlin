module org.example.amirhossein_zeinali_dehaghani_ass1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires java.sql;


    opens org.example.amirhossein_zeinali_dehaghani_ass1 to javafx.fxml;
    exports org.example.amirhossein_zeinali_dehaghani_ass1;
}