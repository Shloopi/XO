module com.idan {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    opens com.idan to javafx.fxml;
    exports com.idan;
}
