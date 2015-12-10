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
package RS232;


import configuration.xmlparser;
import log.DisplayMessageType;
import log.logger;
import system.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Stephen Adjei-Kyei <stephen.adjei.kyei@gmail.com>
 * @author Jeremy Espino <espinoj@gmail.com>
 */
public class ErbaLytePlus extends Thread {


    private static final String EQUIPMENT_TO_BLIS_ID_MAP = "configs/Erba/erbalyteplus.xml";

    private static Map<Integer, Integer> testIDs = new HashMap<Integer, Integer>();
    static final char STX = 0x02;
    //static final char END_BLOCK = (char) 3;
    static final char CARRIAGE_RETURN = 13;
    static final char EOF = 0x1A;
    private static StringBuilder dataReceived = new StringBuilder();

    private static void logDisplay(String s, int displayMessageType) {
        if (settings.CLI_MODE) {
            logger.info(s);
        } else {
            log.AddToDisplay.Display(s, displayMessageType);
        }
    }

    @Override
    public void run() {
        logDisplay("Erba Lyte Plus handler started...", DisplayMessageType.TITLE);
        logDisplay("Checking available ports on this system...", DisplayMessageType.INFORMATION);

        String[] ports = Manager.getSerialPorts();

        logDisplay("Available ports:", DisplayMessageType.TITLE);

        for (int i = 0; i < ports.length; i++) {
            logDisplay(ports[i], DisplayMessageType.INFORMATION);
        }

        logDisplay("Now connecting to port " + RS232Settings.COMPORT, DisplayMessageType.TITLE);

        if (Manager.openPortforData("Erba Lyte Plus")) {
            logDisplay("Connected successfully", DisplayMessageType.INFORMATION);
            setTestIDs();
        }
    }

    public static void HandleDataInput(String data) {

        // check for end of message
        if (data.contains(String.valueOf(CARRIAGE_RETURN))) {

            // append the last of the data
            int endIndex = data.indexOf(String.valueOf(CARRIAGE_RETURN));
            dataReceived.append(data.substring(0, endIndex));

            processMessage();

            // clear the dataReceived object
            dataReceived = new StringBuilder();

            // handle data found after the EOF character by appending to dataReceived
            if (data.substring(endIndex).length() > 1) {
                if (data.startsWith(String.valueOf(CARRIAGE_RETURN)))
                    HandleDataInput(data.substring(endIndex + 2));
                else
                    HandleDataInput(data.substring(endIndex));
            }
        } else {
            dataReceived.append(data);
        }

    }

    private static void processMessage() {

        String dataReceivedString = dataReceived.toString();

        // check for empty message
        if (null == dataReceivedString || dataReceivedString.isEmpty())
            return;

        dataReceivedString = dataReceivedString.trim();

        // check to see if this is a sample for processing
        // if (dataReceivedString.trim().length < 10)
        //    return;

        /*
        [ '004',
          '',
          '000000000000001253',
          '',
          '160',
          '4.04',
          '139.9',
          '105.2',
          '0.00',
          '0.00',
          '',
          '',
          '0.0' ]
         */
        // parse the message with the test data
        String[] dataParts = dataReceivedString.split(" ");

        String specimenId = dataParts[2].replaceAll("^0+", "");

        System.out.println(specimenId);

        String[] readings = {
                dataParts[5],
                dataParts[6],
                dataParts[7]
        };

        int measureID;
        float value = 0;
        boolean flag = false;
        // for each string parse the value and send to BLIS
        for (int i = 0; i < readings.length; i++) {
            measureID = getMeasureID(i);
            if (measureID > 0) {
                try {
                    value = Float.parseFloat(readings[i].trim());
                } catch (NumberFormatException e) {
                    try {
                        value = Float.parseFloat(readings[i].trim());
                    } catch (NumberFormatException ex) {
                    }

                }
                logDisplay(String.format("Saving specimenId: %s measureID: %s value: %s", specimenId, measureID, value), DisplayMessageType.INFORMATION);
                if (saveResults(specimenId, measureID, value)) {
                    flag = true;
                }
            }

        }

        if (flag) {
            logDisplay("Test result with code: " + specimenId + " sent to BLIS sucessfully", DisplayMessageType.INFORMATION);
        } else {
            logDisplay("Test result with code: " + specimenId + " not found in BLIS", DisplayMessageType.WARNING);
        }


    }

    public void Stop() {
        if (Manager.closeOpenedPort()) {
            logDisplay("Port Closed successfully", DisplayMessageType.INFORMATION);
        }
    }

    /**
     * read mapping of equipment measurement IDs to BLIS measurement IDs
     */
    public static void setTestIDs() {

        String equipmentId = getSpecimenFilter(3);
        String blisMeasureId = getSpecimenFilter(4);

        String[] equipmentIds = equipmentId.split(",");
        String[] blisMeasureIds = blisMeasureId.split(",");

        for (int i = 0; i < equipmentIds.length; i++) {
            testIDs.put(Integer.parseInt(equipmentIds[i]), Integer.parseInt(blisMeasureIds[i]));
        }

    }

    /**
     * whichdata maps to an xml tag in <listest></listest>
     * <p>
     * 1 - <testtypeid></testtypeid>
     * 2 - <lissampleid></lissampleid>
     * 3 - <equipmenttestid></equipmenttestid>
     * 4 - <listestid></listestid>
     * 5 - <testcode></testcode>
     * 6 - <formula></formula>
     * 7 - <m1></m1>
     */
    private static String getSpecimenFilter(int whichdata) {
        String data = "";
        xmlparser p = new xmlparser(EQUIPMENT_TO_BLIS_ID_MAP);

        try {
            data = p.getMicros60Filter(whichdata);
        } catch (Exception ex) {
            Logger.getLogger(MICROS60.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }

    private static int getMeasureID(int equipmentID) {
        int measureId = 0;

        if (testIDs.containsKey(equipmentID))
            measureId = testIDs.get(equipmentID);

        return measureId;
    }

    private static boolean saveResults(String barcode, int measureId, float value) {
        boolean flag = false;

        if ("1".equals(BLIS.blis.saveResults(barcode, measureId, value, 0))) {
            flag = true;
        }

        return flag;

    }

}
