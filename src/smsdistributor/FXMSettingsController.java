package smsdistributor;

import java.net.URL;
import java.util.ResourceBundle;
import javacode.FileHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMSettingsController implements Initializable {
    
    @FXML
    private ComboBox<String> combo_port;
    @FXML
    private ComboBox<Integer> combo_bitrate;
    @FXML
    private TextField txt_modem;
    @FXML
    private TextField txt_pin;
    @FXML
    private TextField txt_smsc;
    @FXML
    private TextField txt_smsno;
    @FXML
    private TextField txt_time;
    @FXML
    private TextField txt_ip;
    @FXML
    private TextField txt_user;
    @FXML
    private TextField txt_pass;
    @FXML
    private Button btn_save;
    
    @FXML
    private CheckBox check_trans;
    @FXML
    private CheckBox check_rec;
    @FXML
    private CheckBox check_auto;
    @FXML
    private CheckBox check_allRep;
    
    static String COMPORT;
    static int BITRATE;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        FileHelper fh = new FileHelper();
        String[] settings = fh.getSettings();
        String id = "modem.com1";
        String port = settings[0];
        int bitRate = Integer.parseInt(settings[1]);
        String modemName = settings[2];
        String modemPin = settings[3];
        String SMSC = settings[4];
        String number = settings[5];
        String interval = settings[6];
        String ip = settings[7];
        String user = settings[8];
        String pass = settings[9];
        
        txt_modem.setText(modemName);
        txt_pin.setText(modemPin);
        txt_smsc.setText(SMSC);
        txt_smsno.setText(number);
        txt_time.setText(interval);
        txt_ip.setText(ip);
        txt_user.setText(user);
        txt_pass.setText(pass);
        
        combo_port.getItems().setAll("COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10",
                                     "COM11", "COM12", "COM13", "COM14", "COM15", "COM16", "COM17", "COM18", "COM19", "COM20",
                                     "COM21", "COM22", "COM23", "COM24", "COM25", "COM26", "COM27", "COM28", "COM29", "COM30",
                                     "COM31", "COM32", "COM33", "COM34", "COM35", "COM36", "COM37", "COM38", "COM39", "COM40");
        combo_bitrate.getItems().setAll(9600, 14400, 19200, 28800, 33600, 38400, 56000, 57600, 115200);
        
        combo_port.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                COMPORT = newValue;
            }
        });
        
        combo_bitrate.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                BITRATE = newValue;
            }
        });
    }

//    String port;
    @FXML
    private void handleAutoAction() {
//        SmsFunc sms = new SmsFunc();
//        
////        new Thread(new Runnable() {
////            @Overridenew
////            public void run() {
//                   port = sms.autoDetectPort();
//                   
////            }
////        }).start();
//         
//        System.out.println("This is the PORT" + port);

    }
    
    @FXML
    private void handleSaveAction() {
        
        String Modem = txt_modem.getText();
        String Pin = txt_pin.getText();
        String Smsc = txt_smsc.getText();
        String SmsNo = txt_smsno.getText();
        String Time = txt_time.getText();
        String ip = txt_ip.getText();
        String user = txt_user.getText();
        String pass = txt_pass.getText();
        
        if (COMPORT == null || BITRATE == 0 || Smsc.equals("") || SmsNo.equals("") || Time.equals("")) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Required Fields are Empty",
                    ButtonType.OK);
            alert.showAndWait();
            
            if (alert.getResult() == ButtonType.OK) {
                return;
            }
            
        }
        
        if (!SmsNo.matches("[0-9]+") || !Time.matches("[0-9]+")) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Numerical Fields contain AlphaNumeric Characters",
                    ButtonType.OK);
            alert.showAndWait();
            
            if (alert.getResult() == ButtonType.OK) {
                return;
            }
        }
        
        FileHelper fh = new FileHelper();
        fh.SaveSettings(COMPORT, BITRATE, Modem, Pin, Smsc, SmsNo, Time, ip, user, pass);
        fh.write_boolean_trans(check_trans.isSelected());
        fh.write_boolean_rec(check_rec.isSelected());
        fh.write_boolean_auto(check_auto.isSelected(),check_allRep.isSelected());
        
        Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION, "Settings Saved. Restart The Apllication for the Changes to take effect.",
                ButtonType.OK);
        alert2.showAndWait();
        
        if (alert2.getResult() == ButtonType.OK) {
            Stage stage = (Stage) btn_save.getScene().getWindow();
            stage.close();
            System.exit(0);
        }
    }
    
}
