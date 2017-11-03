package javacode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class FileHelper {

    private static final String FADD = "C:\\Users\\" + System.getProperty("user.name") + "\\SMS\\";

    public FileHelper() {

    }

    public void SaveSettings(String port, int bit, String modem, String pin, String smsc, String num,
            String interval, String ip, String user, String pass) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(
                    new File(FADD + "Settings.txt"));

            writer.write(port + "," + bit + "," + modem + "," + pin + "," + smsc + "," + num + "," + interval
                    + "," + ip + "," + user + "," + pass);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            writer.close();;
        }
    }

    public String[] getSettings() {
        String text = "";
        InputStreamReader isReader = null;
        try {
            isReader = new InputStreamReader(
                    new FileInputStream(
                            new File(FADD + "Settings.txt")));
            BufferedReader br = new BufferedReader(isReader);

            text = br.readLine();
            String[] t = text.split(",");
            return t;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                isReader.close();
            } catch (IOException ex) {

            }
        }
        return null;
    }

    public String check_box_trans() {
        String text;
        InputStreamReader isReader = null;
        try {
            isReader = new InputStreamReader(
                    new FileInputStream(
                            new File(FADD + "checkboxTrans.txt")));
            BufferedReader br = new BufferedReader(isReader);

            text = br.readLine();

            return text;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                isReader.close();
            } catch (IOException ex) {

            }
        }
        System.out.println("Here");
        return "";
    }

    public String check_box_rec() {
        String text;
        InputStreamReader isReader = null;
        try {
            isReader = new InputStreamReader(
                    new FileInputStream(
                            new File(FADD + "checkboxRec.txt")));
            BufferedReader br = new BufferedReader(isReader);

            text = br.readLine();

            return text;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                isReader.close();
            } catch (IOException ex) {

            }
        }
        System.out.println("here");
        return "";
    }

    public void write_boolean_trans(boolean choice) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(
                    new File(FADD + "checkboxTrans.txt"));

            writer.write(String.valueOf(choice));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            writer.close();
        }
    }

    public void write_boolean_rec(boolean choice) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(
                    new File(FADD + "checkboxRec.txt"));

            writer.write(String.valueOf(choice));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            writer.close();
        }
    }
    
       public String check_box_auto() {
        String text;
        InputStreamReader isReader = null;
        try {
            isReader = new InputStreamReader(
                    new FileInputStream(
                            new File(FADD + "checkboxAuto.txt")));
            BufferedReader br = new BufferedReader(isReader);

            text = br.readLine();

            return text;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                isReader.close();
            } catch (IOException ex) {

            }
        }
        return "";
    }

    public void write_boolean_auto(boolean choice) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(
                    new File(FADD + "checkboxAuto.txt"));

            writer.write(String.valueOf(choice));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            writer.close();
        }
    }
}
