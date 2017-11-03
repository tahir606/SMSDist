package javacode;

import datatypes.msgout;
import java.awt.TrayIcon;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import org.smslib.GatewayException;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.TimeoutException;

public class orcCon {

    public static String ipAddress = "";
    public static String user = "";
    public static String pass = "";

    public orcCon() {

    }

    public Connection connectDB() {

        Connection con;

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Driver Not Found");

        }

        FileHelper helper = new FileHelper();
        String[] data = helper.getSettings();

        ipAddress = "jdbc:oracle:thin:@" + data[7] + ":orcl";
        user = data[8];
        pass = data[9];
//        System.out.println(ipAddress);
//        System.out.println(user);
//        System.out.println(pass);
        try {
            con = DriverManager.getConnection(ipAddress, user, pass);
            //System.out.println("Successfully Connected");

            return con;

        } catch (SQLException ex) {
            System.out.println("Not Connected");
            ex.printStackTrace();
            return null;
        }
    }

    public List<msgout> msgOut() {

        String query = "select id,sender,receiver,msg,operator,flag from ozekimessageout where flag = 'S' order by id";

        try {
            Connection conn = connectDB();
            Statement statement = conn.createStatement();
            ResultSet set = statement.executeQuery(query);
            List<msgout> listM = new ArrayList<>();
            while (set.next()) {
                String id, sender, reciever, msg, operator, flag;
                id = set.getString("id");
                sender = set.getString("sender");
                reciever = set.getString("receiver");
                msg = set.getString("msg");
                operator = set.getString("operator");
                flag = set.getString("flag");
                msgout msgO = new msgout(id, sender, reciever, msg, operator, flag);
                listM.add(msgO);
            }
            set.close();
            statement.close();
            conn.close();
            return listM;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void Updateflag(String id) {
        String query = "update ozekimessageout set Flag = 'T' where id = " + id;
        try {
            Connection conn = connectDB();
            PreparedStatement statement = conn.prepareStatement(query);
            //statement.setString(1, id);
            statement.executeQuery(query);
            System.out.println("Updated where id == " + id);
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    public String insertRecord(String phone, String Area, String party, String type, String msg, String time) {

        String insert = "insert into ozekimessagein (ID,RECEIVER,MSG,ARCOD,PACOD,MSGTYPE,FLAG,RECEIVEDTIME) "
                + " values((select NVL(max(ID),0)+1 from ozekimessagein),'" + phone + "','" + msg + "','" + Area
                + "','" + party + "','" + type + "','S',TO_DATE('" + time.split("\\.")[0] + " "
                    + time.split("\\.")[1] + "','DD-MON-YYYY HH24:MI:SS'))";      
        
        String fetch = "{? = call AUTO_REPLY(?,?,?)}";

        SmsFunc smsFunc = new SmsFunc();
        TrayHelper helper = new TrayHelper();

        try {
            Connection conn = connectDB();
            PreparedStatement statement = conn.prepareStatement(insert);
            statement.executeQuery(insert);
            statement.close();

            CallableStatement statement1 = conn.prepareCall(fetch);
            statement1.registerOutParameter(1, Types.VARCHAR);
            statement1.setInt(2, Integer.parseInt(Area));
            statement1.setInt(3, Integer.parseInt(party));
            statement1.setInt(4, 1);
            statement1.executeQuery();
            String msgText = statement1.getString(1);
            statement1.close();
            conn.close();
            return msgText;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void insertElse(String phone, String msg, String time) {
        try {
//            String insert = "insert into ozekimessagein (ID,RECEIVER,MSG,RECEIVEDTIME,FLAG) "
//                    + " values((select NVL(max(ID),0)+1 from ozekimessagein),'" + phone + "','" + msg + "','" + time.split("\\.")[0] + "','G')";
            String insert = "insert into ozekimessagein (ID,RECEIVER,MSG,RECEIVEDTIME,FLAG) "
                    + " values((select NVL(max(ID),0)+1 from ozekimessagein),'" + phone + "','" + msg + "',TO_DATE('" + time.split("\\.")[0] + " "
                    + time.split("\\.")[1] + "','DD-MON-YYYY HH24:MI:SS'),'G')";

            //System.out.println(time);
            //System.out.println(insert);

            Connection conn = connectDB();
            PreparedStatement statement = conn.prepareStatement(insert);
            statement.executeQuery(insert);
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void addQueue(String phone, String msg) throws Exception {

        //System.out.println(phone + "    " + msg);
        OutboundMessage Omsg = new OutboundMessage(phone, msg);
        Service.getInstance().sendMessage(Omsg);
        TrayHelper helper = new TrayHelper();
        helper.displayNotification("Notification", "Message Sent");

    }

}
