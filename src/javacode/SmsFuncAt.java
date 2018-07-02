package javacode;

import datatypes.Sms;
import datatypes.msgout;
import gnu.io.*;
import java.util.Enumeration;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Label;
import org.ajwcc.pduUtils.gsm3040.Pdu;
import org.ajwcc.pduUtils.gsm3040.PduParser;

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

    private static int mode;    //0 == PDU Mode && 1 == Text Mode

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
        mode = FileHelper.readMode();
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
            String res = sendCmd("AT+CMGF=" + mode + "\r");
            System.out.println(res);
            if (res.equals("")) {
                isConnected = false;
                System.out.println("Port not Connected");
                return;
            }
            //Setting SMSC No for Modem           
            System.out.println(sendCmd("AT+CSCA=\"" + SMSC + "\",145\r"));

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

    private void deleteSms(int index) throws Exception {
        //Delete SMS from memory
        sendCmd("AT+CMGD=" + index + "\r");
        System.out.println("Message Deleted");
    }

    private void insertIntoDB(Sms sms, int index) throws Exception {
        //Insert into database
        insertSmsToDB(sms);
        deleteSms(index);
        index++;
    }

    public static boolean sendSMS(String phone, String msg) {
        switch (mode) {
            case 0: {   //PDU
                return sendSMSPDU(phone, msg);
            }
            case 1: {   //Text
                return sendSMSText(phone, msg);
            }
        }
        return false;
    }

    public static boolean sendSMSText(String phone, String msg) {
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

    public static boolean sendSMSPDU(String phone, String msg) {
        try {
            if (phone.startsWith("0")) {
                phone = "+92" + phone.substring(1);
            }
            System.out.println("Phone: " + phone);
            PDUConverter.PDUEncoded encoded = PDUConverter.encode("+31624000000", phone, msg);

            System.out.println(encoded.getPduCommand());
            outputStream.write(("AT+CMGS=" + encoded.getPduLength() + "\r").getBytes()); //Enter MSG to send in >
            outputStream.write(encoded.getPduEncoded().getBytes());
            ctrlZ();
            System.out.println(readResponse());
//            System.out.println("               " + phone + "               " + "Message Sent");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean isFirst = true;
    static boolean isThere = true;

    public void readSmsToIndex() {
        try {
            int index;
            while (true) {
                index = FileHelper.readSmsIndex();

                Sms sms = readSmsFromIndex(index);
                if (sms == null) {
                    break;
                }
                //Display Notification
                if (sms.getSenderPhone() == null || sms.getBody() == null) {
                    break;
                }
                System.out.println("Message Received \nFrom: " + sms.getSenderPhone() + "\nBody: " + sms.getBody());

                insertIntoDB(sms, index);

                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Sms readSmsFromIndex(int index) {
        try {
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
                    return null;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                isThere = false;
                FileHelper.writeSmsIndex(0);
                return null;
            }
            if (result.contains("ERROR")) {
                isThere = false;
                FileHelper.writeSmsIndex(0);
                return null;
            }
            Sms sms = extractSMSObject(index, result);
            if (sms == null) {
                isThere = false;
                FileHelper.writeSmsIndex(0);
                return null;
            }

//            FileHelper.writeSmsIndex(index);
//            deleteSms(index);
//            deleteSms(index);
//            FileHelper.writeSmsIndex(0);
            sms.setTimeStamp(getCurrentTimeStamp());
            if (sms.getBody().length() > 152) {
                combineSMS(sms, index);
            }
            isThere = true;
            isFirst = false;

            return sms;
        } catch (IOException ex) {
            Logger.getLogger(SmsFuncAt.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SmsFuncAt.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private Sms combineSMS(Sms sms, int prevIndex) throws Exception {
        System.out.println("Combining SMS");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(SmsFuncAt.class.getName()).log(Level.SEVERE, null, ex);
        }

        int index = prevIndex + 1;

        Sms nextSms = readSmsFromIndex(index);
        if (nextSms == null) {
            return sms;
        }
        nextSms.setTimeStamp(getCurrentTimeStamp());
        if (sms.getSenderPhone().equals(nextSms.getSenderPhone())) {
            String time = sms.getTimeStamp().split("\\s+")[1];
            int hour = Integer.parseInt(time.split(":")[0]),
                    min = Integer.parseInt(time.split(":")[1]);

            String timeNew = nextSms.getTimeStamp().split("\\s+")[1];
            int hourNew = Integer.parseInt(time.split(":")[0]),
                    minNew = Integer.parseInt(time.split(":")[1]);
            if (hour == hourNew && minNew <= min + 5) {
                sms.setBody(sms.getBody().concat(nextSms.getBody()));
            }
        } else {
            insertIntoDB(sms, prevIndex);
            insertIntoDB(nextSms, index);
        }

        return sms;
    }

    private Sms extractSMSObject(int index, String result) {
        Sms sms = null;
        switch (mode) {
            case 0: {   //PDU
                sms = extractSMSObjectPDU(index, result);
//                if (sms.getSenderPhone() == null) {
//                    
//                }
                break;
            }
            case 1: {   //Text
                sms = extractSMSObjectText(index, result);
                break;
            }
        }
        return sms;
    }

    private Sms extractSMSObjectText(int index, String result) {
        System.out.println(result);
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

    private Sms extractSMSObjectPDU(int index, String result) {
        Sms sms = null;
        try {
            System.out.println(result);
            sms = new Sms();
            sms.setIndex(String.valueOf(index));
            String arr[] = result.split("\n");
            String details;
            int pos = 0;
            for (String st : arr) {
                pos++;
                if (st.contains("+CMGR:")) {
                    break;
                }
            }
            convertPDUtoText(arr[pos], sms);
        } catch (Exception e) {
            System.out.println(e);
        }
        return sms;
    }

    public void convertPDUtoText(String resultMessage, Sms sms) {
        try {
            Pdu pdu = new PduParser().parsePdu(resultMessage);
            sms.setSenderPhone("+" + pdu.getAddress());
            sms.setBody(pdu.getDecodedText());
        } catch (Exception e) {
            return;
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
                        sendSMSPDU(originatED, "Message recieved successfully!");
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

    public static String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
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
