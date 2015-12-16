
package TCPIP;

import BLIS.sampledata;
import configuration.xmlparser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import log.DisplayMessageType;
import system.SampleDataJSON;
import system.utilities;
//Correction: Mindray BS 120
/**
 * @author Chimwemwe Kachaje <chimwemwe.kachaje@baobabhealth.org>
 */
public class MindrayBC120 extends Thread {

    private static List<String> testIDs = new ArrayList<>();
    String read;
    boolean first = true;
    Socket connSock = null;
    ServerSocket welcomeSocket=null;
    Iterator list = null;
    static Queue<String> OutQueue = new LinkedList<>();
    static final char CARRIAGE_RETURN = 13;

    boolean stopped = false;
    //Queue<String> InQueue=new LinkedList<>();

    public enum MSGTYPE {
        PID(0),
        OBR(1),
        OBX(2),
        UNKNOWN(-1);


        private MSGTYPE(int value) {
            this.Value = value;
        }

        private int Value;

    }


    public void Stop() {
        try {

            stopped = true;
            if (null != connSock) {

                connSock.close();

                connSock = null;

                System.out.println(connSock);

            }

            log.AddToDisplay.Display("Mindray BC 120 handler stopped", DisplayMessageType.TITLE);
        } catch (IOException ex) {
            Logger.getLogger(MindrayBC5800.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {
        log.AddToDisplay.Display("Mindray BC 120 handler started...", DisplayMessageType.TITLE);
        log.AddToDisplay.Display("Starting Server socket on port " + tcpsettings.PORT, DisplayMessageType.INFORMATION);

        try {
            welcomeSocket = new ServerSocket(tcpsettings.PORT);

            log.AddToDisplay.Display("Waiting for Equipment connection...", DisplayMessageType.INFORMATION);
            log.AddToDisplay.Display("Listening on port " + tcpsettings.PORT + "...", DisplayMessageType.INFORMATION);
            connSock = welcomeSocket.accept();
            log.AddToDisplay.Display("Mindray BC 120 is now Connected...", DisplayMessageType.INFORMATION);
            first = false;
            ClientThread client = new ClientThread(connSock, "Mindray BC 120");
            client.start();
            String message;
            setTestIDs();
        } catch (IOException e) {
            if (first) {
                log.AddToDisplay.Display("could not listen on port :" + tcpsettings.PORT + " " + e.getMessage(), DisplayMessageType.ERROR);
                // log.logger.Logger(e.getMessage());
            } else {
                log.AddToDisplay.Display("Mindray BC 120 client is now disconnected!", DisplayMessageType.WARNING);
                log.logger.Logger(e.getMessage());
            }


        }

    }

    public static void handleMessage(String message) {
        try {

            System.out.println("Handling results");

            String[] msgParts = message.split("\r");

            String accessionNumber = "";

            for (int i = 0; i < msgParts.length; i++) {

                MSGTYPE type = getMessageType(msgParts[i]);

                // System.out.println(testIDs);

                // System.out.println(msgParts[i]);

                if (type == MSGTYPE.PID) {

                    System.out.println("Got PID");

                } else if (type == MSGTYPE.OBR) {

                    // System.out.println("Got OBR");

                    String[] obrParts = msgParts[i].trim().split("\\|");

                    accessionNumber = obrParts[2];

                    System.out.println("accessionNumber: " + accessionNumber);

                } else if (type == MSGTYPE.OBX) {

                    System.out.println("Got OBX");

                    String[] obxParts = msgParts[i].trim().split("\\|");

                    String[] obx3Parts = obxParts[3].trim().split("\\^");

                    // String testType = obx3Parts[1];

                    String testType = obxParts[4].trim();

                    System.out.println(testType);

                    int mID = getMeasureID(testType);

                    System.out.println(mID);

                    if (mID > 0) {

                        float value = 0;

                        try {

                            value = Float.parseFloat(obxParts[5]);

                        } catch (NumberFormatException e) {
                            try {
                                value = 0;
                            } catch (NumberFormatException ex) {
                            }

                        }

                        System.out.println(accessionNumber + " : " + testType + " (" + mID + "): " + value);

                        boolean flag = false;

                        if(SaveResults(accessionNumber, mID,value)) {
                            flag = true;
                        }

                        if(flag) {

                            log.AddToDisplay.Display("\nResults with accessionNumber: "+accessionNumber + " and mID: " +
                                    mID + " sent to BLIS sucessfully",DisplayMessageType.INFORMATION);

                        } else {

                            log.AddToDisplay.Display("\nTest with accessionNumber: "+accessionNumber + " and mID: " +
                                    mID +" not Found on BLIS",DisplayMessageType.WARNING);

                        }
                    }

                }

            }

            System.out.println("End results handling");

        } catch (Exception ex) {
            log.AddToDisplay.Display("Processing Error Occured!", DisplayMessageType.ERROR);
            log.AddToDisplay.Display("Data format of Details received from Analyzer UNKNOWN", DisplayMessageType.ERROR);
        }

    }

    private static MSGTYPE getMessageType(String msg) {

        MSGTYPE type = null;

        if (msg.trim().startsWith("PID|")) {
            type = MSGTYPE.PID;
        } else if (msg.trim().startsWith("OBR|")) {
            type = MSGTYPE.OBR;
        } else if (msg.trim().startsWith("OBX|")) {
            type = MSGTYPE.OBX;
        } else {
            type = MSGTYPE.UNKNOWN;
        }

        return type;

    }

    private void setTestIDs() {
        String equipmentname = getSpecimenFilter(8);
        String blismeasureid = getSpecimenFilter(4);

        String[] equipmentnames = equipmentname.split(",");
        String[] blismeasureids = blismeasureid.split(",");
        for (int i = 0; i < equipmentnames.length; i++) {
            testIDs.add(equipmentnames[i] + ";" + blismeasureids[i]);
        }

    }

    private static String getSpecimenFilter(int whichdata) {
        String data = "";
        xmlparser p = new xmlparser("configs/mindray/mindraybc120.xml");
        try {
            data = p.getMicros60Filter(whichdata);
        } catch (Exception ex) {
            Logger.getLogger(MindrayBC5800.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    private static int getMeasureID(String equipmentID) {
        int measureid = 0;

        for (int i = 0; i < testIDs.size(); i++) {

            if (testIDs.get(i).split(";")[0].equalsIgnoreCase(equipmentID)) {
                measureid = Integer.parseInt(testIDs.get(i).split(";")[1]);
                break;
            }
        }

        return measureid;
    }

    private static boolean SaveResults(String barcode, int MeasureID, float value) {


        boolean flag = false;
        if ("1".equals(BLIS.blis.saveResults(barcode, MeasureID, value, 0))) {
            flag = true;
        }

        return flag;

    }

}