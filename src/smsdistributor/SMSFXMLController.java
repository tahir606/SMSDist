package smsdistributor;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javacode.orcCon;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SMSFXMLController implements Initializable {

    @FXML
    private TextField txt_phone;
    @FXML
    private TextArea txt_body;
    @FXML
    private Button btn_send;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txt_phone.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    txt_phone.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    @FXML
    private void handleSendAction() {
        orcCon orc = new orcCon();
        String phone = txt_phone.getText();
        String body = txt_body.getText();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    orc.addQueue(phone, body);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

        Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION, "MESSAGE SENDING",
                ButtonType.OK);
        alert2.showAndWait();

        if (alert2.getResult() == ButtonType.OK) {
            Stage stage = (Stage) btn_send.getScene().getWindow();
            stage.close();
        }
    }
}
