package javacode;

import datatypes.Sms;
import datatypes.msgout;
import gnu.io.*;
import java.util.Enumeration;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.InboundMessage;
import org.smslib.Library;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.crypto.AESKey;
import org.smslib.modem.SerialModemGateway;

public class SmsFuncAt {

    private static Enumeration portList;
    private static CommPortIdentifier portId;
    private static SerialPort serialPort;
    private static boolean outputBufferEmptyFlag = false;
    private static String comPort;

    private static boolean isConnected = false;

    private static String id = "";
    private static String port = ""; //Modem Port.
    private static int bitRate = 0; //this is also optional. leave as it is.
    private static String modemName = ""; //this is optional.
    private static String modemPin = ""; //Pin code if any have assigned to the modem.
    private static String SMSC = ""; //Message Center Number ex. Mobitel
    private static String model = "";
    private static String number = "";
    private static String interval = "";

    private static String[] auto_reply = null;

    private static OutputStream outputStream;   //Send AT Commands through this stream
    private static InputStream inputStream;     //Receive responses through this stream

    public SmsFuncAt() {
        FileHelper fh = new FileHelper();
        String[] settings = fh.getSettings();
        id = "modem.com1";
        comPort = settings[0];
        bitRate = Integer.parseInt(settings[1]);
        modemName = settings[2];
        modemPin = settings[3];
        SMSC = settings[4];
        number = settings[5];
        interval = settings[6];
        auto_reply = fh.check_box_auto();

        if (!isConnected) {
            connectModem();
        }

    }

    private void connectModem() {
        boolean portFound = false;
        portList = CommPortIdentifier.getPortIdentifiers(); //Getting list of ports
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(comPort)) {             //If port matches entered port
                    System.out.println("Got Hold on " + comPort);
                    portFound = true;
                    try {
                        serialPort = (SerialPort) portId.open(comPort, 2000);
                        outputStream = serialPort.getOutputStream();
                        inputStream = serialPort.getInputStream();
                        serialPort.setSerialPortParams(115200,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);
                        serialPort.notifyOnOutputEmpty(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!portFound) {
            System.out.println("port " + comPort + " not found.");
            isConnected = false;
            return;
        }
        //Intialize Modem if port is found
        try {
            //Setting Mode to Text (0 is for PDU)
            if (sendCmd("AT+CMGF=0\r").equals("")) {
                isConnected = false;
                System.out.println("Port not Connected");
                return;
            }
            //Setting SMSC No for Modem           
            System.out.println(sendCmd("AT+CSCA=\"+923189244444\",145\r"));

            //Setting preferred storage as SIM
            System.out.println(sendCmd("AT+CPMS=\"MT\",\"MT\",\"MT\"\r"));

            //For Receicing messages
            System.out.println(sendCmd("AT+CNMI=0,0,0,0\r"));

            isConnected = true;
            System.out.println("Port Connected Successfully\n\r");
        } catch (Exception e) {
            isConnected = false;
            e.printStackTrace();
        }
    }

    public static boolean sendSMS(String phone, String msg) {
        try {
            String sendCmd = "AT+CMGS=\"" + phone + "\"\r";
            sendCmd(sendCmd);
            outputStream.write(msg.getBytes()); //Enter MSG to send in >
            ctrlZ();
            System.out.println(readResponse());
//            System.out.println("               " + phone + "               " + "Message Sent");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void readUnReadSMS() {
        try {
//            String sendCmd = "AT+CMGL=\"ALL\"\r";
            String sendCmd = "AT+CMGR=15\r";
            outputStream.write(sendCmd.getBytes());
            String result = readResponse();
            System.out.println("Result: " + result);

            //Extract the sms object
            String arr[] = result.split("\\+CMGR:");
            for (String msg : arr) {    //Iterating each msg
                String it[] = msg.split("\n");
                String details;
                StringBuilder body = new StringBuilder();
                Sms sms = new Sms();
                try {
                    details = it[0];
                    String det[] = details.trim().replaceAll("\"", "").split(",");
                    sms.setIndex(det[0]);
                    sms.setSenderPhone(det[2]);
                    sms.setTimeStamp(det[4] + " " + det[5]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
                for (int i = 1; i < it.length; i++) {   //Iterating Body
                    if (!it[i].trim().equals("")) {
                        body.append(it[i] + "\n");
                    }
                }
                sms.setBody(body.toString());

                //Save the ms object in database
                insertSmsToDB(sms);

                //Delete SMS from memory
                sendCmd("AT+CMGD=" + sms.getIndex() + "\r");
                System.out.println("Message Deleted");

                Thread.sleep(2000); //Take a break after each mesage
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean isFirst = true;
    static boolean isThere = true;

    public void readSmsToIndex() {
        try {

            int index;
            while (true) {
                index = FileHelper.readSmsIndex();
                String sendCmd = "AT+CMGR=" + index + "\r";
                outputStream.write(sendCmd.getBytes());
                String result = readResponse();
                String lines[] = result.trim().split("\n");

                if (isThere == false) {     //if on the first check there are no sms, empty storage
                    if (isFirst == true) {
                        emptySMStorage();
                        isFirst = false;
                    }
                }

                try {
                    if (lines.length < 4 && lines[2].equals("OK")) {    //Empty
                        isThere = false;
                        FileHelper.writeSmsIndex(0);
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    isThere = false;
                    FileHelper.writeSmsIndex(0);
                    break;
                }
                if (result.contains("ERROR")) {
                    isThere = false;
                    FileHelper.writeSmsIndex(0);
                    break;
                }
                Sms sms = extractSMSObject(index, result);
                if (sms == null) {
                    isThere = false;
                    FileHelper.writeSmsIndex(0);
                    break;
                }
                isThere = true;
                isFirst = false;

                //Display Notification
                System.out.println("Message Received \nFrom: " + sms.getSenderPhone() + "\nBody: " + sms.getBody());

                //Insert into database
                insertSmsToDB(sms);

                //Delete SMS from memory
                sendCmd("AT+CMGD=" + index + "\r");
                System.out.println("Message Deleted");

                index++;
                FileHelper.writeSmsIndex(index);

                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Sms extractSMSObject(int index, String result) {
//        System.out.println(result);
        Sms sms = new Sms();
        sms.setIndex(String.valueOf(index));
        String arr[] = result.split("\n");
        String details;
        int pos = -1;
        for (String st : arr) {
            pos++;
            if (st.contains("+CMGR:")) {
                break;
            }
        }
        try {
            details = arr[pos];
            String det[] = details.trim().replaceAll("\"", "").split(",");
            sms.setSenderPhone(det[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e);
            System.out.println(result);
            return null;
        }
        StringBuilder body = new StringBuilder();
        for (int i = pos + 1; i < arr.length; i++) {   //Iterating Body
            if (!arr[i].trim().equals("")) {
                body.append(arr[i] + "\n");
            }
        }
        sms.setBody(body.toString());
        return sms;
    }

    public void readParticularSMS(String no) {
        try {
            Thread.sleep(500);
            String sendCmd = "AT+CMGR=" + no + "\r";
            System.out.println(sendCmd);
            outputStream.write(sendCmd.getBytes());
            Thread.sleep(500);
            System.out.println(readResponse());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void emptySMStorage() {
        try {
            Thread.sleep(500);
            String sendCmd = "AT+CMGD=1,4\r";
            System.out.println(sendCmd(sendCmd));
            System.out.println("Storage Emptied");
            FileHelper.writeSmsIndex(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static BufferedReader reader;
    static StringBuilder builder;

    private static String readResponse() {
        reader = new BufferedReader(new InputStreamReader(inputStream));
        builder = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
//                    System.out.println("Line: " + line);
                builder.append(line + "\n");

            }
        } catch (IOException ex) {
//            System.out.println("x--x");
        }
        return builder.toString();
    }

    static BufferedReader sReader;

    public static void listenToModem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    sReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    try {
                        while ((line = sReader.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (IOException ex) {

                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void action(Label Version, Label Manufacturer, Label Model, Label SerialNo, Label IMSI, Label Signal, Label Battery) {
//        listenToModem();
        FileHelper fHelper = new FileHelper();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SmsFuncAt sms = new SmsFuncAt();
                while (isConnected) {
                    sms.sendPeriodSms();
//                    sms.readUnReadSMS();   
                    if (fHelper.check_box_rec().equals("true")) {
                        readSmsToIndex();
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    static TrayHelper helper = new TrayHelper();
    static orcCon orc;

    public void sendPeriodSms() {
        try {
            int counter = 0;
            FileHelper helper2 = new FileHelper();
            try {
                if (String.valueOf(helper2.check_box_trans()).equals("true")) {
                    orc = new orcCon();
                    List<msgout> msgst = orc.msgOut();
                    for (int i = 0; i < msgst.size(); i++) {
                        msgout msg1 = msgst.get(i);
                        String phone = msg1.getReciever();
                        String msgText = msg1.getMsg();
                        sendSMS(phone, msgText);
                        orc.Updateflag(msg1.getId());
                        TrayHelper helper = new TrayHelper();
                        helper.displayNotification("Message Sent", "Message Sent to \n ID: " + msg1.getId() + "\n TO: " + msg1.getReciever());
                        counter++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1 * 1000);

        } catch (Exception e) {
            e.printStackTrace();
            helper.displayNotification("Exception", e.getMessage());
        }
    }

    public static void insertSmsToDB(Sms sms) {
        String textMessage = sms.getBody();
        String originator = sms.getSenderPhone();
        orc = new orcCon();
        if (originator.length() > 8) {
            System.out.println();
            String timeStamp = new SimpleDateFormat("dd-MMM-yyyy.HH:mm:ss").format(new Date());
            try {
//                System.out.println("Message Received: " + textMessage + " From: " + originator);
                if (textMessage.substring(0, 6).equals("Order.") || textMessage.substring(0, 6).equals("order.")) {
                    String[] arr = textMessage.split("\\.");
                    System.out.println("ARCOD: " + arr[1] + " ,PACOD: " + arr[2]
                            + " ,TYPE: " + arr[3] + " ,MSG: " + arr[4]);
                    String msgText = orc.insertRecord(originator, arr[1], arr[2], arr[3], arr[4], timeStamp);
                    if (msgText == null) {
                        return;
                    }
                    if (auto_reply[0].equals("true")) {
//                        String originatED = "0" + originator.substring(3);
                        String originatED = originator;
                        sendSMS(originatED, msgText);
                        System.out.println("Verification Message Sent");
                    }
                    helper.displayNotification("Order Booked", "Inserted Order for " + originator);
                } else {
                    orc.insertElse(originator, textMessage, timeStamp);
                    if (auto_reply[1].equals("true")) {
                        String originatED = originator;
                        sendSMS(originatED, "Message recieved successfully!");
                        System.out.println("Verification Message Sent");
                    }
                }
            } catch (NullPointerException e) {
                e.getLocalizedMessage();
                try {
                    orc.insertElse(originator, textMessage, timeStamp);
                    if (auto_reply[1].equals("true")) {
                        String originatED = originator;
                        sendSMS(originatED, "Message recieved successfully!");
                        System.out.println("Verification Message Sent");
                    }
                } catch (Exception ex) {
                    ex.getLocalizedMessage();
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
                try {
                    orc.insertElse(originator, textMessage, timeStamp);
                    if (auto_reply[1].equals("true")) {
                        String originatED = originator;
                        sendSMS(originatED, "Message recieved successfully!");
                        System.out.println("Verification Message Sent");
                    }
                } catch (Exception ex) {
                    ex.getLocalizedMessage();
                }
            }
        }
    }

    public static String sendCmd(String command) throws Exception {
        outputStream.write(command.getBytes());
        Thread.sleep(500);
        String result = readResponse();
        int times = 2;
        if (result.contains("ERROR")) {
            System.out.println(result);
            for (int i = 0; i < times; i++) {               
                outputStream.write(command.getBytes());
                result = readResponse();
                System.out.println(result);
            }           
        }
        return result;
    }

    public static void ctrlZ() {
        try {
            Thread.sleep(500);
            outputStream.write(26);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
