/*
 *  C4G BLIS Equipment Interface Client
 *
 *  Project funded by PEPFAR
 *
 *  Philip Boakye      - Team Lead
 *  Patricia Enninful  - Technical Officer
 *  Stephen Adjei-Kyei - Software Developer
 *
 */
package TCPIP;

import BLIS.sampledata;
import configuration.xmlparser;

import java.io.DataOutputStream;
import java.io.IOException;
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

/**
 * @author Stephen Adjei-Kyei <stephen.adjei.kyei@gmail.com>
 */
public class AquiosCL extends Thread {

    private static List<String> testIDs = new ArrayList<>();
    String read;
    boolean first = true;
    DataOutputStream outToEquipment = null;
    // ServerSocket welcomeSocket=null;
    Socket connSock = null;
    Iterator list = null;
    static Queue<String> OutQueue = new LinkedList<>();
    static final char CARRIAGE_RETURN = 13;

    boolean stopped = false;
    //Queue<String> InQueue=new LinkedList<>();


    public enum MSGTYPE {
        QUERY(0),
        RESULTS(1),
        ACK_RECEIVED(3),
        UNKNOWN(-1);


        private MSGTYPE(int value) {
            this.Value = value;
        }

        private int Value;

    }


    public void Stop() {
        try {

            stopped = true;
            if (null != connSock)
                connSock.close();

            // welcomeSocket.close();
//            connSock.close();
            log.AddToDisplay.Display("Aquios CL handler stopped", DisplayMessageType.TITLE);
        } catch (IOException ex) {
            Logger.getLogger(BT3000PlusChameleon.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {
        log.AddToDisplay.Display("Aquios CL handler started...", DisplayMessageType.TITLE);
        log.AddToDisplay.Display("Starting client socket on port " + tcpsettings.PORT, DisplayMessageType.INFORMATION);

        try {
            // welcomeSocket = new ServerSocket(tcpsettings.PORT);
            // log.AddToDisplay.Display("Waiting for Equipment connection...", DisplayMessageType.INFORMATION);
            // log.AddToDisplay.Display("Listening on port "+ tcpsettings.PORT+"...",DisplayMessageType.INFORMATION);
            // connSock = welcomeSocket.accept();
            connSock = new Socket(tcpsettings.EQUIPMENT_IP, tcpsettings.PORT);

            log.AddToDisplay.Display("Waiting for Equipment connection...", DisplayMessageType.INFORMATION);

            log.AddToDisplay.Display("Aquios CL is now Connected...", DisplayMessageType.INFORMATION);
            first = false;
            ClientThread client = new ClientThread(connSock, "AQUIOS CL");
            client.start();
            String message;
            outToEquipment = new DataOutputStream(connSock.getOutputStream());
            setTestIDs();
            while (!stopped) {
                synchronized (OutQueue) {
                    while (!OutQueue.isEmpty()) {
                        //System.out.println("Message found in sending queue");
                        log.AddToDisplay.Display("Message found in sending queue", DisplayMessageType.TITLE);
                        //log.logger.Logger("Message found in sending queue");
                        message = (String) OutQueue.poll();
                        outToEquipment.writeBytes(message);
                        //System.out.println(message+ "sent sucessfully");
                        log.AddToDisplay.Display(message + "sent successfully", DisplayMessageType.INFORMATION);
                        //log.logger.Logger(message+ "sent sucessfully");
                    }
                }


            }


        } catch (IOException e) {
            if (first) {
                log.AddToDisplay.Display("could not listen on port :" + tcpsettings.PORT + " " + e.getMessage(), DisplayMessageType.ERROR);
                // log.logger.Logger(e.getMessage());
            } else {
                log.AddToDisplay.Display("Aquios CL client is now disconnected!", DisplayMessageType.WARNING);
                log.logger.Logger(e.getMessage());
            }


        }

    }

    private static void getBLISTests(String aux_id, boolean flag, String[] query) {
        try {
            String data = BLIS.blis.getTestData(getSpecimenFilter(2), getSpecimenFilter(1), aux_id);
            List<sampledata> SampleList = SampleDataJSON.getSampleObject(data);
            SampleList = SampleDataJSON.normaliseResults(SampleList);
            if (SampleList.size() > 0) {
                for (int i = 0; i < SampleList.size(); i++) {

                    log.AddToDisplay.Display("Sending test with Code: " + SampleList.get(i).aux_id + " to Aquios CL", DisplayMessageType.INFORMATION);
                    AddtoQueue(SampleList.get(i), query, false);
                }

            } else {
                AddtoQueue(null, query, true);
                if (flag)
                    log.AddToDisplay.Display("Sample with barcode: " + aux_id + " does not exist in BLIS", DisplayMessageType.INFORMATION);
            }
        } catch (Exception ex) {
            log.logger.PrintStackTrace(ex);
        }
    }

    private static void AddtoQueue(sampledata get, String[] query, boolean empty) {
        StringBuffer strData = new StringBuffer();
        strData.append("H|\\^&|||BLIS^^^^^INTF. CLIENT||||||||E1394-97");
        strData.append(CARRIAGE_RETURN);
        //strData.append(query[1]);
        if (!empty) {
            strData.append("P|1|||");
            strData.append(get.surr_id);
            strData.append("|^");
            strData.append(get.name.trim().replaceFirst(" ", "^"));
            strData.append("||");
            strData.append(utilities.getHL7DateOnly(get.dob, get.partial_dob));
            strData.append("|");
            strData.append(get.sex);
            strData.append("|||||^");
            strData.append(get.doctor);
            strData.append("||||||||||||");
            strData.append("^^^OPD");
            strData.append(CARRIAGE_RETURN);
            strData.append("O|1|");
            strData.append(get.aux_id);
            strData.append("|");
            strData.append(query[1].split("\\|")[2].split("\\^")[0]);
            strData.append("^");
            strData.append(query[1].split("\\|")[2].split("\\^")[1]);
            // strData.append("^^");
            strData.append("|");
            strData.append("^^^^WBC\\^^^^RBC\\^^^^HGB\\^^^^HCT\\^^^^MCV\\^^^^MCH\\^^^^MCHC\\^^^^PLT\\^^^^NEUT%\\^^^^LYMPH%\\^^^^MONO%\\^^^^EO%\\^^^^BASO%\\^^^^NEUT#\\^^^^LYMPH#\\^^^^MONO#\\^^^^EO#\\^^^^BASO#\\^^^^RDW-SD\\^^^^RDW-CV\\^^^^PDW\\^^^^MPV\\^^^^P-LCR\\^^^^PCT");
            strData.append("||");
            strData.append(utilities.getHL7Date(get.date_recvd, get.date_collected, "YYYYMMDDHHMMSS"));
            strData.append("|");
            strData.append(utilities.getHL7Date(get.date_collected, get.date_recvd, "YYYYMMDDHHMMSS"));
            strData.append("||||||||");
            strData.append(get.doctor);
            strData.append(CARRIAGE_RETURN);
        } else {
            strData.append("P|1");
            strData.append(CARRIAGE_RETURN);
            strData.append("O|1");
            strData.append(CARRIAGE_RETURN);
        }
        //strData.append("|");
        // strData.append("6^2^15^1");

        strData.append("L|1|N");
        strData.append(CARRIAGE_RETURN);

        synchronized (OutQueue) {
            OutQueue.add(strData.toString());
            log.logger.Logger("New message added to sending queue\n" + strData.toString());
        }
    }

    public static void handleMessage(String message) {
        try {
            MSGTYPE type = getMessageType(message);
            String[] msgParts = message.split("\r");

            System.out.println(type);

            if (type == MSGTYPE.QUERY) {
                //todo handle query for samples
                String[] sidparts = msgParts[1].split("\\|");
                String SampleID = sidparts[2].split("\\^")[0];
                //SampleID = utilities.getSystemDate("YYYY") + SampleID;
                if (!SampleID.isEmpty()) {
                    getBLISTests(SampleID, false, msgParts);
                } else {
                    AddtoQueue(null, null, true);
                }
            } else if (type == MSGTYPE.RESULTS) {

                String patientid = "";
                String SampleID = "";

                for (int j = 0; j < msgParts.length; j++) {

                    //todo handle results
                    String pidParts[] = msgParts[j].split("\\|");

                    boolean flag = false;
                    int mID = 0;
                    float value = 0;

                    if (pidParts[0].trim().equalsIgnoreCase("P")) {

                        patientid = pidParts[3];

                    }

                    if (pidParts[0].trim().equalsIgnoreCase("O")) {

                        SampleID = pidParts[2];

                    }

                    if (pidParts[0].trim().equalsIgnoreCase("R")) {

                        System.out.println(pidParts[2].split("\\^")[3]);

                        mID = getMeasureID(pidParts[2].split("\\^")[3]);

                        System.out.println(mID);

                        if (mID > 0) {
                            try {
                                value = Float.parseFloat(pidParts[3].split("\\^")[0]);
                            } catch (NumberFormatException e) {
                                try {
                                    value = 0;
                                } catch (NumberFormatException ex) {
                                }

                            }
                            if (SaveResults(SampleID, mID, value)) {
                                flag = true;
                            }
                        }

                        if (flag) {
                            log.AddToDisplay.Display("\nResults with Code: " + SampleID + " sent to BLIS sucessfully", DisplayMessageType.INFORMATION);
                        } else {
                            log.AddToDisplay.Display("\nTest with Code: " + SampleID + " not Found on BLIS", DisplayMessageType.WARNING);
                        }

                    }

                }

            }
        } catch (Exception ex) {
            log.AddToDisplay.Display("Processing Error Occured!", DisplayMessageType.ERROR);
            log.AddToDisplay.Display("Data format of Details received from Analyzer UNKNOWN", DisplayMessageType.ERROR);
        }

    }

    private static MSGTYPE getMessageType(String msg) {
        MSGTYPE type = null;
        String[] parts = msg.split("\r");
        if (parts.length > 1) {
            if (parts[2].trim().replaceAll("^\\d+", "").startsWith("Q|")) {
                type = MSGTYPE.QUERY;
            } else if (parts[2].trim().replaceAll("^\\d+", "").startsWith("P|")) {
                type = MSGTYPE.RESULTS;
            } else {
                type = MSGTYPE.UNKNOWN;
            }
        }

        return type;

    }

    private void setTestIDs() {
        String equipmentid = getSpecimenFilter(8);
        String blismeasureid = getSpecimenFilter(4);

        String[] equipmentids = equipmentid.split(",");
        String[] blismeasureids = blismeasureid.split(",");
        for (int i = 0; i < equipmentids.length; i++) {
            testIDs.add(equipmentids[i] + ";" + blismeasureids[i]);
        }

    }

    private static String getSpecimenFilter(int whichdata) {
        String data = "";
        xmlparser p = new xmlparser("configs/aquios/aquios_cl.xml");
        try {
            data = p.getMicros60Filter(whichdata);
        } catch (Exception ex) {
            Logger.getLogger(SYSMEXXS500i.class.getName()).log(Level.SEVERE, null, ex);
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
