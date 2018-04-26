//package javacode;
//
//import datatypes.msgout;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.Scanner;
//import javafx.application.Platform;
//import javafx.scene.control.Label;
//import javax.comm.UnsupportedCommOperationException;
//import javax.crypto.spec.SecretKeySpec;
//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.smslib.AGateway;
//import org.smslib.GatewayException;
//import org.smslib.ICallNotification;
//import org.smslib.IGatewayStatusNotification;
//import org.smslib.IInboundMessageNotification;
//import org.smslib.IOrphanedMessageNotification;
//import org.smslib.IOutboundMessageNotification;
//import org.smslib.InboundMessage;
//import org.smslib.Library;
//import org.smslib.Message;
//import org.smslib.OutboundMessage;
//import org.smslib.SMSLibException;
//import org.smslib.Service;
//import org.smslib.TimeoutException;
//import org.smslib.crypto.AESKey;
//import org.smslib.modem.SerialModemGateway;
//
//public class SmsFunc {
//
//    private static String id = "";
//    private static String port = ""; //Modem Port.
//    private static int bitRate = 0; //this is also optional. leave as it is.
//    private static String modemName = ""; //this is optional.
//    private static String modemPin = ""; //Pin code if any have assigned to the modem.
//    private static String SMSC = ""; //Message Center Number ex. Mobitel
//    private static String model = "";
//    private static String number = "";
//    private static String interval = "";
//    
//    private static String[] auto_reply = null;
//
//    public SmsFunc() {
//        FileHelper fh = new FileHelper();
//        String[] settings = fh.getSettings();
//        id = "modem.com1";
//        port = settings[0];
//        bitRate = Integer.parseInt(settings[1]);
//        modemName = settings[2];
//        modemPin = settings[3];
//        SMSC = settings[4];
//        number = settings[5];
//        interval = settings[6];
//        auto_reply = fh.check_box_auto();
//    }
//
//    public void action(Label Version, Label Manufacturer, Label Model, Label SerialNo, Label IMSI, Label Signal, Label Battery) {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                orcCon o = new orcCon();
//                List<msgout> msgs = o.msgOut();
//                continuedSms(Version, Manufacturer, Model, SerialNo, IMSI, Signal, Battery);
//            }
//        });
//
//        thread.start();
//    }
//
//    TrayHelper helper = new TrayHelper();
//    orcCon oCon = new orcCon();
//    orcCon orc;
//
//    public void continuedSms(Label Version, Label Manufacturer, Label Model, Label SerialNo, Label IMSI, Label Signal, Label Battery) {
//
//        try {
//
//            BasicConfigurator.configure();
//            List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
//            loggers.add(LogManager.getRootLogger());
//            for (Logger logger : loggers) {
//                logger.setLevel(Level.OFF);
//            }
//            List<InboundMessage> msgList;
//
//            InboundNotification inboundNotification = new InboundNotification();
//            CallNotification callNotification = new CallNotification();
//            GatewayStatusNotification statusNotification = new GatewayStatusNotification();
//            OrphanedMessageNotification orphanedMessageNotification = new OrphanedMessageNotification();
//            OutboundNotification outboundNotification = new OutboundNotification();
//
//            System.out.println(Library.getLibraryDescription());
//            String version = "Version: " + Library.getLibraryVersion();
//            System.out.println("Connecting to Port: " + port);
//            System.out.println("loading...");
////            SerialModemGateway gateway = new SerialModemGateway(id, port,
////                    bitRate, modemName, model);
//            SerialModemGateway gateway = new SerialModemGateway(id, port,
//                    bitRate, modemName, model);
//
//            gateway.setInbound(true);
//            gateway.setOutbound(true);
//
//            gateway.setSimPin(modemPin);
//            //gateway.setSmscNumber(SMSC);
//            gateway.setSmscNumber(SMSC);
//
//            gateway.setProtocol(AGateway.Protocols.PDU);
//            gateway.getATHandler().setStorageLocations("SMME");
//
//            Service.getInstance().setOutboundMessageNotification(outboundNotification);
//            Service.getInstance().setInboundMessageNotification(inboundNotification);
//            Service.getInstance().setCallNotification(callNotification);
//            Service.getInstance().setGatewayStatusNotification(statusNotification);
//            Service.getInstance().setOrphanedMessageNotification(orphanedMessageNotification);
//            Service.getInstance().addGateway(gateway);
//            Service.getInstance().startService();
//
//            String man = "Manufacturer: " + gateway.getManufacturer();
//            String mod = "Model: " + gateway.getModel();
//            String serial = "Serial No: " + gateway.getSerialNo();
//            String imsi = "SIM IMSI: " + gateway.getImsi();
//            String signal = "Signal Level: " + gateway.getSignalLevel() + " dBm";
//            String battery = "Battery Level: " + gateway.getBatteryLevel() + "%";
//            System.out.println();
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    Version.setText(version);
//                    Manufacturer.setText(man);
//                    Model.setText(mod);
//                    SerialNo.setText(serial);
//                    IMSI.setText(imsi);
//                    Signal.setText(signal);
//                    Battery.setText(battery);
//                }
//            });
//            System.out.println("Loaded.");
//            int counter = 0;
//            FileHelper helper2 = new FileHelper();
//            while (true) {
//                try {
//                    orc = new orcCon();
//                    List<msgout> msgst = orc.msgOut();
//
//                    if (String.valueOf(helper2.check_box_trans()).equals("true")) {
//                        for (int i = 0; i < msgst.size(); i++) {
//                            if (counter == 50000) {
//                                try {
//                                    Thread.sleep(Long.getLong(interval) * 60000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                counter = 0;
//                            }
//                            msgout msg1 = msgst.get(i);
//                            String phone = msg1.getReciever();
//                            String msgText   = msg1.getMsg();
//                            orc.addQueue(phone, msgText);
////                            OutboundMessage msg = new OutboundMessage(phone, msgText);
////                            Service.getInstance().sendMessage(msg);
////                            System.out.println(msg);
//                            System.out.println("ID: " + msg1.getId());
//                            orc.Updateflag(msg1.getId());
//                            TrayHelper helper = new TrayHelper();
//                            helper.displayNotification("Message Sent", "Message Sent to \n ID: " + msg1.getId() + "\n TO: " + msg1.getReciever());
//                            counter++;
//                        }
//                    }
//
//                    if (String.valueOf(helper2.check_box_rec()).equals("true")) {
//                        Service.getInstance().getKeyManager().registerKey(SMSC, new AESKey(new SecretKeySpec("0011223344556677".getBytes(), "AES")));
//                        msgList = new ArrayList<InboundMessage>();
//                        try {
//                            Service.getInstance().readMessages(msgList, InboundMessage.MessageClasses.ALL);
//
////                            for (InboundMessage msg : msgList) {
////                                try {
////                                    insertFunc(msg);
////                                } catch (Exception e) {
////                                    e.printStackTrace();
////                                    continue;
////                                } finally {
////                                    gateway.deleteMessage(msg);
////                                    System.out.println("Deleted");
////                                }
////
////                            }
//                            msgList.clear();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                Thread.sleep(1 * 1000);
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            helper.displayNotification("Exception", e.getMessage());
//        }
//
//    }
//
//    public void insertFunc(InboundMessage msg) {
//        String textMessage = msg.getText();
//        String originator = msg.getOriginator();
//        if (originator.length() > 8) {
//            System.out.println();
//            String timeStamp = new SimpleDateFormat("dd-MMM-yyyy.HH:mm:ss").format(new Date());
//            System.out.println(timeStamp);
//            try {
//                System.out.println("Message Received: " + textMessage + "\nFrom: " + originator);
//
//                if (textMessage.substring(0, 6).equals("Order.") || textMessage.substring(0, 6).equals("order.")) {
//                    String[] arr = textMessage.split("\\.");
//                    System.out.println("ARCOD: " + arr[1] + " ,PACOD: " + arr[2]
//                            + " ,TYPE: " + arr[3] + " ,MSG: " + arr[4]);
//                    String msgText = oCon.insertRecord(originator, arr[1], arr[2], arr[3], arr[4], timeStamp);
//                    if (msgText == null) {
//                        return;
//                    }
//                    if (auto_reply[0].equals("true")) {
//                        System.out.println(msgText);                     
//                        String originatED = "0" + originator.substring(2);
//                        SendMsg(Service.getInstance(), originatED, msgText);
//                        //orc.addQueue(originatED, msgText);
//                    }                    
//                    System.out.println("Order Booked for " + originator);
//                    helper.displayNotification("Order Booked", "Inserted Order for " + originator);
//                } else {
//                    orc.insertElse(originator, textMessage, timeStamp);
//                    if(auto_reply[1].equals("true")) {
//                        String originatED = "0" + originator.substring(2);
//                        System.out.println("Verification Message Sent to: " + originatED);
//                        SendMsg(Service.getInstance(), originatED, "Message recieved successfully!");
//                    }
//                    System.out.println("Saved");
//                }
//            } catch (NullPointerException e) {
//                e.getLocalizedMessage();
//                try {
//                    orc.insertElse(originator, textMessage, timeStamp);
//                    if(auto_reply[1].equals("true")) {
//                        String originatED = "0" + originator.substring(2);
//                        System.out.println("Verification Message Sent to: " + originatED);
//                        SendMsg(Service.getInstance(), originatED, "Message recieved successfully!");
//                    }
//                } catch (Exception ex) {
//                    java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                }
//                System.out.println("Saved");
//            } catch (Exception e) {
//                e.getLocalizedMessage();
//                try {
//                    orc.insertElse(originator, textMessage, timeStamp);
//                    if(auto_reply[1].equals("true")) {
//                        String originatED = "0" + originator.substring(2);
//                        System.out.println("Verification Message Sent to: " + originatED);
//                        SendMsg(Service.getInstance(), originatED, "Message recieved successfully!");
//                    }
//                } catch (Exception ex) {
//                    java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                }
//                
//                System.out.println("Saved");
//            } finally {
//                try {
//                    Service.getInstance().deleteMessage(msg);
//                    System.out.println("Message Deleted");
//                } catch (TimeoutException ex) {
//                    java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                } catch (GatewayException ex) {
//                    java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                } catch (IOException ex) {
//                    java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                } catch (InterruptedException ex) {
//                    java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                }
//            }
//
//        }
//    }
//
//    public class InboundNotification implements IInboundMessageNotification {
//
//        public void process(AGateway gateway, Message.MessageTypes msgType, InboundMessage msg) {
//            if (msgType == Message.MessageTypes.INBOUND) {
////                System.out.println("Inbound Message");
//                System.out.println(msg);
//                try {
//                    insertFunc(msg);
//                } catch (Exception e) {
//                    e.printStackTrace();                  
//                } finally {
//                    try {
//                        gateway.deleteMessage(msg);
//                        System.out.println("Deleted");
//                    } catch (TimeoutException ex) {
//                        java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                    } catch (GatewayException ex) {
//                        java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                    } catch (IOException ex) {
//                        java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                    } catch (InterruptedException ex) {
//                        java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                    }                  
//                }
//               
//            } else if (msgType == Message.MessageTypes.STATUSREPORT) {
//                System.out.println(">>> New Inbound Status Report message detected from Gateway: " + gateway.getGatewayId());
//            }
//            //System.out.println(msg);
//        }
//    }
//
//    public class CallNotification implements ICallNotification {
//
//        public void process(AGateway gateway, String callerId) {
//            System.out.println(">>> New call detected from Gateway: " + gateway.getGatewayId() + " : " + callerId);
//        }
//    }
//
//    public class GatewayStatusNotification implements IGatewayStatusNotification {
//
//        public void process(AGateway gateway, AGateway.GatewayStatuses oldStatus, AGateway.GatewayStatuses newStatus) {
//            System.out.println(">>> Gateway Status change for " + gateway.getGatewayId() + ", OLD: " + oldStatus + " -> NEW: " + newStatus);
//        }
//    }
//
//    public class OrphanedMessageNotification implements IOrphanedMessageNotification {
//
//        public boolean process(AGateway gateway, InboundMessage msg) {
//            System.out.println(">>> Orphaned message part detected from " + gateway.getGatewayId());
//            System.out.println(msg);
//             System.out.println(msg);
//                try {
//                    insertFunc(msg);
//                } catch (Exception e) {
//                    e.printStackTrace();                  
//                } finally {
//                    try {
//                        gateway.deleteMessage(msg);
//                        System.out.println("Deleted");
//                    } catch (TimeoutException ex) {
//                        java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                    } catch (GatewayException ex) {
//                        java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                    } catch (IOException ex) {
//                        java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                    } catch (InterruptedException ex) {
//                        java.util.logging.Logger.getLogger(SmsFunc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//                    }                  
//                }
//            // Since we are just testing, return FALSE and keep the orphaned message part. 
//            return false;
//        }
//    }
//
//    public void SendMsg(Service service, String Phone, String message) throws Exception {
//
//        OutboundMessage msg = new OutboundMessage(Phone, message);
//        service.getInstance().sendMessage(msg);
//
//    }
//
//    public class OutboundNotification implements IOutboundMessageNotification {
//
//        public void process(AGateway gateway, OutboundMessage msg) {
//            System.out.println("Outbound handler called from Gateway: " + gateway.getGatewayId());
//            System.out.println(msg);
//        }
//    }
//
////    public String autoDetectPort() {
////
//////        System.out.println("Example: Send message from a serial gsm modem.");
//////        System.out.println(Library.getLibraryDescription());
//////        System.out.println("Version: " + Library.getLibraryVersion());
////        List<String> COMPORTS = Arrays.asList("COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10");
////
////        String returnPort = "";
////
////        for (int i = 0; i < COMPORTS.size(); i++) {
////            try {
////                BasicConfigurator.configure();
////                OutboundNotification outboundNotification = new OutboundNotification();
////                SerialModemGateway gateway = new SerialModemGateway("modem.com1", COMPORTS.get(i),
////                        115200, "", "");
////
////                gateway.setInbound(true);
////                gateway.setOutbound(true);
////                gateway.setSimPin("");
////
////                gateway.setSmscNumber("+923362500671");
////                Service.getInstance().setOutboundMessageNotification(outboundNotification);
////                Service.getInstance().addGateway(gateway);
////                Service.getInstance().startService();
////
////                OutboundMessage msg = new OutboundMessage("+923342132778", "Port Check");
////                Service.getInstance().sendMessage(msg);
////                System.out.println(COMPORTS.get(i));
////                returnPort = COMPORTS.get(i);
////
////                Service.getInstance().stopService();
////                break;
////            } catch (Exception e) {
////                try {
////                    e.printStackTrace();
////                    Service.getInstance().stopService();
////                    continue;
////                } catch (Exception ee) {
////                    ee.printStackTrace();
////                }
////            }
////        }
////        return returnPort;
////    }
//
//}
