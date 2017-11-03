package javacode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.smslib.AGateway;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.AGateway.Protocols;
import org.smslib.GatewayException;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOrphanedMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Library;
import org.smslib.Message.MessageTypes;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.crypto.AESKey;
import org.smslib.modem.SerialModemGateway;

public class ReadSms {

    public void doIt() throws Exception {
        BasicConfigurator.configure();
        // Define a list which will hold the read messages. 
        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        for (Logger logger : loggers) {
            logger.setLevel(Level.OFF);
        }
        List<InboundMessage> msgList;
        // Create the notification callback method for inbound & status report 
        // messages. 
        InboundNotification inboundNotification = new InboundNotification();
        // Create the notification callback method for inbound voice calls. 
        CallNotification callNotification = new CallNotification();
        //Create the notification callback method for gateway statuses. 
        GatewayStatusNotification statusNotification = new GatewayStatusNotification();
        OrphanedMessageNotification orphanedMessageNotification = new OrphanedMessageNotification();
        try {
            System.out.println("Example: Read messages from a serial gsm modem.");
            System.out.println(Library.getLibraryDescription());
            System.out.println("Version: " + Library.getLibraryVersion());
            // Create the Gateway representing the serial GSM modem. 
            SerialModemGateway gateway = new SerialModemGateway("modem.com1", "COM8", 115200, "QualcCOmm", "");
            // Set the modem protocol to PDU (alternative is TEXT). PDU is the default, anyway... 
            gateway.setProtocol(Protocols.PDU);
            // Do we want the Gateway to be used for Inbound messages? 
            gateway.setInbound(true);
            // Do we want the Gateway to be used for Outbound messages? 
            gateway.setOutbound(true);
            // Let SMSLib know which is the SIM PIN. 
            gateway.setSimPin("");
            // Set up the notification methods. 
            Service.getInstance().setInboundMessageNotification(inboundNotification);
            Service.getInstance().setCallNotification(callNotification);
            Service.getInstance().setGatewayStatusNotification(statusNotification);
            Service.getInstance().setOrphanedMessageNotification(orphanedMessageNotification);
            // Add the Gateway to the Service object. 
            Service.getInstance().addGateway(gateway);
            // Similarly, you may define as many Gateway objects, representing 
            // various GSM modems, add them in the Service object and control all of them. 
            // Start! (i.e. connect to all defined Gateways) 
            Service.getInstance().startService();
            // Printout some general information about the modem. 
            System.out.println();
            System.out.println("Modem Information:");
            System.out.println("  Manufacturer: " + gateway.getManufacturer());
            System.out.println("  Model: " + gateway.getModel());
            System.out.println("  Serial No: " + gateway.getSerialNo());
            System.out.println("  SIM IMSI: " + gateway.getImsi());
            System.out.println("  Signal Level: " + gateway.getSignalLevel() + " dBm");
            System.out.println("  Battery Level: " + gateway.getBatteryLevel() + "%");
            System.out.println();
            // In case you work with encrypted messages, its a good time to declare your keys. 
            // Create a new AES Key with a known key value.  
            // Register it in KeyManager in order to keep it active. SMSLib will then automatically 
            // encrypt / decrypt all messages send to / received from this number. 
            Service.getInstance().getKeyManager().registerKey("+923362500671", new AESKey(new SecretKeySpec("0011223344556677".getBytes(), "AES")));
            // Read Messages. The reading is done via the Service object and 
            // affects all Gateway objects defined. This can also be more directed to a specific 
            // Gateway - look the JavaDocs for information on the Service method calls. 
            System.out.println("");
            while (true) {
                msgList = new ArrayList<InboundMessage>();
                Service.getInstance().readMessages(msgList, MessageClasses.UNREAD);
                for (InboundMessage msg : msgList) {
                    System.out.println(msg);
                    String smsText = msg.getText();
                    if(smsText.contains(".")){
                        String[] arr   = smsText.split("\\.");
                        System.out.println(arr[0]);
                    } else {
                        System.out.println("Corrupted String " + smsText);
                    }
                    
                }
                msgList.clear();
                System.out.println("Sleeping");
                Thread.sleep(1 * 10000);
                System.out.println("Waking up");
                
            }
            // Sleep now. Emulate real world situation and give a chance to the notifications 
            // methods to be called in the event of message or voice call reception. 
//            System.out.println("Now Sleeping - Hit <enter> to stop service.");
//            System.in.read();
//            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Service.getInstance().stopService();
        }
    }

    public class InboundNotification implements IInboundMessageNotification {

        public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
            if (msgType == MessageTypes.INBOUND) {
                System.out.println(">>> New Inbound message detected from Gateway: " + gateway.getGatewayId());
                System.out.println(msg);
                try {
                    gateway.deleteMessage(msg);
                } catch (TimeoutException ex) {
                    java.util.logging.Logger.getLogger(ReadSms.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (GatewayException ex) {
                    java.util.logging.Logger.getLogger(ReadSms.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(ReadSms.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(ReadSms.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                System.out.println("Deleting");
            } else if (msgType == MessageTypes.STATUSREPORT) {
                System.out.println(">>> New Inbound Status Report message detected from Gateway: " + gateway.getGatewayId());
            }
            System.out.println(msg);
        }
    }

    public class CallNotification implements ICallNotification {

        public void process(AGateway gateway, String callerId) {
            System.out.println(">>> New call detected from Gateway: " + gateway.getGatewayId() + " : " + callerId);
        }
    }

    public class GatewayStatusNotification implements IGatewayStatusNotification {

        public void process(AGateway gateway, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
            System.out.println(">>> Gateway Status change for " + gateway.getGatewayId() + ", OLD: " + oldStatus + " -> NEW: " + newStatus);
        }
    }

    public class OrphanedMessageNotification implements IOrphanedMessageNotification {

        public boolean process(AGateway gateway, InboundMessage msg) {
            System.out.println(">>> Orphaned message part detected from " + gateway.getGatewayId());
            System.out.println(msg);
            // Since we are just testing, return FALSE and keep the orphaned message part. 
            return false;
        }
    }

    public static void main(String args[]) {
        ReadSms app = new ReadSms();
        try {
            app.doIt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
