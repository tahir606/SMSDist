package smsdistributor;

import datatypes.msgout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.List;
import java.util.ResourceBundle;
import javacode.TrayHelper;
import javacode.orcCon;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class OutFXMLController implements Initializable {

    @FXML
    private TableView<msgData> table_msgs;

    @FXML
    private TableColumn col_id;
    @FXML
    private TableColumn col_sender;
    @FXML
    private TableColumn col_receiver;
    @FXML
    private TableColumn col_msg;
    @FXML
    private TableColumn col_operator;
    @FXML
    private TableColumn col_flag;
    @FXML
    private TableColumn col_time;
    @FXML
    private TableColumn col_date;   
    
    @FXML 
    private void handleRefreshAction(){
         Platform.runLater(new Runnable() {
            @Override
            public void run() {
                buildData();
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {       
        
        
        col_id.setCellValueFactory(new PropertyValueFactory<msgData, String>("ID"));
        col_sender.setCellValueFactory(new PropertyValueFactory<msgData, String>("SENDER"));
        col_receiver.setCellValueFactory(new PropertyValueFactory<msgData, String>("RECEIVER"));
        col_msg.setCellValueFactory(new PropertyValueFactory<msgData, String>("MESSAGE"));
        col_operator.setCellValueFactory(new PropertyValueFactory<msgData, String>("OPERATOR"));
        col_flag.setCellValueFactory(new PropertyValueFactory<msgData, String>("FLAG"));

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                buildData();
            }
        });

        Platform.setImplicitExit(true);

    }
  

    private ObservableList<msgData> data;

    public void buildData() {

        data = FXCollections.observableArrayList();

        try {
            orcCon helper = new orcCon();
            List<msgout> ms = helper.msgOut();

            for (int i = 0; i < ms.size(); i++) {
                msgData usa = new msgData();
                msgout a = ms.get(i);
                System.out.println(a.getId());
                usa.ID.set(a.getId());
                System.out.println(a.getSender());
                usa.SENDER.set(a.getSender());
                System.out.println(a.getReciever());
                usa.RECEIVER.set(a.getReciever());
                System.out.println(a.getMsg());
                usa.MESSAGE.set(a.getMsg());
                System.out.println(a.getOperator());
                usa.OPERATOR.set(a.getOperator());
                System.out.println(a.getFlag());
                usa.FLAG.set(a.getFlag());
                data.add(usa);
            }

            table_msgs.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class msgData {

        public SimpleStringProperty ID = new SimpleStringProperty();
        public SimpleStringProperty SENDER = new SimpleStringProperty();
        public SimpleStringProperty RECEIVER = new SimpleStringProperty();
        public SimpleStringProperty MESSAGE = new SimpleStringProperty();
        public SimpleStringProperty OPERATOR = new SimpleStringProperty();
        public SimpleStringProperty FLAG = new SimpleStringProperty();

        public String getID() {
            return ID.get();
        }

        public void setID(SimpleStringProperty ID) {
            this.ID = ID;
        }

        public String getSENDER() {
            return SENDER.get();
        }

        public void setSENDER(SimpleStringProperty SENDER) {
            this.SENDER = SENDER;
        }

        public String getRECEIVER() {
            return RECEIVER.get();
        }

        public void setRECEIVER(SimpleStringProperty RECEIVER) {
            this.RECEIVER = RECEIVER;
        }

        public String getMESSAGE() {
            return MESSAGE.get();
        }

        public void setMESSAGE(SimpleStringProperty MESSAGE) {
            this.MESSAGE = MESSAGE;
        }

        public String getOPERATOR() {
            return OPERATOR.get();
        }

        public void setOPERATOR(SimpleStringProperty OPERATOR) {
            this.OPERATOR = OPERATOR;
        }

        public String getFLAG() {
            return FLAG.get();
        }

        public void setFLAG(SimpleStringProperty FLAG) {
            this.FLAG = FLAG;
        }
    }

}
