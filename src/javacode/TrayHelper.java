
package javacode;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.ImageIcon;

public class TrayHelper {
   
    
    public static TrayIcon trayIcon;
    
    public TrayHelper(){
        
    }
    
    java.awt.Image image = null;
    public static SystemTray tray;
    
    public void createTrayIcon(Stage stage){
        
        
        image = new ImageIcon(getClass().getResource("Sms-icon-tray.png")).getImage();
                
        if (SystemTray.isSupported()){
            
            tray = SystemTray.getSystemTray();

        }
        
       stage.getIcons().add(new Image(getClass().getResourceAsStream("Sms-icon.png")));
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                hide(stage);
            }
        });
        
        final ActionListener closeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.exit();
                System.exit(0);
                tray.remove(trayIcon);
            }
        };
        
        ActionListener showListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        stage.show();
                    }
                });
            }
        };
        
        PopupMenu popup =  new PopupMenu();
        
        MenuItem showItem = new MenuItem("Show");
        showItem.addActionListener(showListener);
        popup.add(showItem);
        
        MenuItem closeItem = new MenuItem("Close");
        closeItem.addActionListener(closeListener);
        popup.add(closeItem);
        
        trayIcon = new TrayIcon(image,"Ticketer",popup);
        trayIcon.setImageAutoSize(true);
        
        trayIcon.addActionListener(showListener);
        
        try{
            tray.add(trayIcon);
        } catch(AWTException e){
            e.printStackTrace();
        }
           
    }
    
    private void hide(final Stage stage){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(SystemTray.isSupported()){
                    stage.hide();
                    
                } else{
                    System.exit(0);
                }
            }
        });
    }
    
    public void removeTray(){
        tray.remove(trayIcon);
    }
    
    public void displayNotification(String caption , String msg){
        trayIcon.displayMessage(caption, msg, TrayIcon.MessageType.INFO);
    }
    
    public void setTaskbarIcon(Stage stage){
        stage.getIcons().add(new Image(getClass().getResourceAsStream("Sms-icon.png")));
    }
}
