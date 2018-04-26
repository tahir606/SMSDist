/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smsdistributor;

import datatypes.msgout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javacode.FileHelper;
import javacode.SmsFuncAt;
import javacode.TrayHelper;
import javacode.orcCon;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FXMLMainController implements Initializable {

    @FXML
    private Label label_version;
    @FXML
    private Label label_manufacturer;
    @FXML
    private Label label_model;
    @FXML
    private Label label_serial;
    @FXML
    private Label label_signal;
    @FXML
    private Label label_imsi;
    @FXML
    private Label label_battery;

    @FXML
    private TextArea txt_console;

    @FXML
    private Button btn_settings;


    @FXML
    private void handleExitAction(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleOutAction(ActionEvent event) {
        TrayHelper tray = new TrayHelper();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("OutFXML.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage2 = new Stage();
            stage2.initModality(Modality.APPLICATION_MODAL);
            stage2.initStyle(StageStyle.UTILITY);
            stage2.setTitle("Outgoing SMS Details");
            stage2.setScene(new Scene(root1));
            tray.setTaskbarIcon(stage2);
            //TrayHelper.tray.remove(TrayHelper.trayIcon);
            Platform.setImplicitExit(true);
            stage2.show();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @FXML
    private void handleInAction(ActionEvent event) {
        TrayHelper tray = new TrayHelper();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("InFXML.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage2 = new Stage();
            stage2.initModality(Modality.APPLICATION_MODAL);
            stage2.initStyle(StageStyle.UTILITY);
            stage2.setTitle("Incoming SMS Details");
            stage2.setScene(new Scene(root1));
            tray.setTaskbarIcon(stage2);
            //TrayHelper.tray.remove(TrayHelper.trayIcon);
            Platform.setImplicitExit(true);
            stage2.show();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @FXML
    private void handleSettingsAction(javafx.event.ActionEvent event) {
        TrayHelper tray = new TrayHelper();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMSettings.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage2 = new Stage();
            stage2.initModality(Modality.APPLICATION_MODAL);
            stage2.initStyle(StageStyle.UTILITY);
            stage2.setTitle("SMS Settings");
            stage2.setScene(new Scene(root1));
            tray.setTaskbarIcon(stage2);
            //TrayHelper.tray.remove(TrayHelper.trayIcon);
            Platform.setImplicitExit(true);
            stage2.show();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private PrintStream ps;
    private FileHelper helper;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btn_settings.setStyle("-fx-background-image: url('smsdistributor/Settings-icon.png');");
        ps = new PrintStream(new Console(txt_console));
        System.setOut(ps);
        System.setErr(ps);
        SmsFuncAt fx = new SmsFuncAt();
        fx.action(label_version, label_manufacturer, label_model, label_serial, label_imsi, label_signal, label_battery);
    }

    public void handleSMSAction() {
        TrayHelper tray = new TrayHelper();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SMSFXML.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage2 = new Stage();
            stage2.initModality(Modality.APPLICATION_MODAL);
            stage2.initStyle(StageStyle.UTILITY);
            stage2.setTitle("Send SMS");
            stage2.setScene(new Scene(root1));
            tray.setTaskbarIcon(stage2);
            //TrayHelper.tray.remove(TrayHelper.trayIcon);
            Platform.setImplicitExit(true);
            stage2.show();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public class Console extends OutputStream {

        private TextArea console;

        public Console(TextArea console) {
            this.console = console;
        }

        public void appendText(String valueOf) {
            Platform.runLater(() -> console.appendText(valueOf));
        }

        public void write(int b) throws IOException {
            appendText(String.valueOf((char) b));
        }
    }

}
