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

    public String insertRecord(String phone, String Area, String party, String type, String msg, String time) throws Exception {

        if (checkIfRecordExists(phone, msg, time)) {
            return null;
        }

        String insert = "insert into ozekimessagein (ID,RECEIVER,MSG,ARCOD,PACOD,MSGTYPE,FLAG,RECEIVEDTIME) "
                + " values((select NVL(max(ID),0)+1 from ozekimessagein),?,?,?,?,?,'S'"
                + ",TO_DATE('" + time.split("\\.")[0] + " "
                + time.split("\\.")[1] + "','DD-MON-YYYY HH24:MI:SS'))";

        String fetch = "{? = call AUTO_REPLY(?,?,?)}";

        SmsFuncAt smsFunc = new SmsFuncAt();
        TrayHelper helper = new TrayHelper();

        try {
            Connection conn = connectDB();
            PreparedStatement statement = conn.prepareStatement(insert);
            statement.setString(1, phone);
            statement.setString(2, msg);
            statement.setString(3, Area);
            statement.setString(4, party);
            statement.setString(5, type);
            statement.executeUpdate();
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

    public void insertElse(String phone, String msg, String time) throws Exception {
        try {

            if (checkIfRecordExists(phone, msg, time)) {
                return;
            }

            String insert = "insert into ozekimessagein (ID,RECEIVER,MSG,RECEIVEDTIME,FLAG) "
                    + " values((select NVL(max(ID),0)+1 from ozekimessagein),?,?,TO_DATE('" + time.split("\\.")[0] + " "
                    + time.split("\\.")[1] + "','DD-MON-YYYY HH24:MI:SS'),'G')";
            //System.out.println(time);
//            System.out.println(insert);

            Connection conn = connectDB();
            PreparedStatement statement = conn.prepareStatement(insert);
            statement.setString(1, phone);
            statement.setString(2, msg);
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void addQueue(String phone, String msg) throws Exception {

        //System.out.println(phone + "    " + msg);
        TrayHelper helper = new TrayHelper();
        if (SmsFuncAt.sendSMS(phone, msg)) {
            helper.displayNotification("Notification", "Message Sent");
        } else {
            helper.displayNotification("Error", "Message Unable to Send");
        }
    }

    public boolean checkIfRecordExists(String phone, String msg, String time) {
        String query = "SELECT ID FROM OZEKIMESSAGEIN"
                + " WHERE RECEIVER = ? "
                + " AND MSG = ? "
                + " AND TRUNC(RECEIVEDTIME) = ? ";

        try {
            Connection con = connectDB();
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, phone);
            statement.setString(2, msg);
            statement.setString(3, time.split("\\.")[0]);
            ResultSet set = statement.executeQuery();
            if (!set.isBeforeFirst()) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(orcCon.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Returning False 2");
        return false;
    }

}
