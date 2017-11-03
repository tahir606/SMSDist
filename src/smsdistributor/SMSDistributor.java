package smsdistributor;

import datatypes.msgout;
import java.util.List;
import java.util.Scanner;
import javacode.SmsFunc;
import javacode.TrayHelper;
import javacode.orcCon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SMSDistributor extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Platform.setImplicitExit(false);
        Parent root = FXMLLoader.load(getClass().getResource("FXMLMain.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("BITS SMS Distributor");
        TrayHelper helper = new TrayHelper();
        helper.createTrayIcon(stage);
        helper.setTaskbarIcon(stage);
        stage.show();
    }

    public static void main(String[] args) {

        launch(args);

    }

}
