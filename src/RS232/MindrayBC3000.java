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
import jssc.*;

/**
 * @author Stephen Adjei-Kyei <stephen.adjei.kyei@gmail.com>
 * @author Jeremy Espino <espinoj@gmail.com>
 */
public class MindrayBC3000 extends Thread {


    private static final String EQUIPMENT_TO_BLIS_ID_MAP = "configs/mindray/mindraybc3000.xml";

    private static Map<Integer, Integer> testIDs = new HashMap<Integer, Integer>();
    static final char STX = 0x02;
    //static final char END_BLOCK = (char) 3;
    //static final char CARRIAGE_RETURN = 13;
    static final char EOF = 0x1A;
    private static StringBuilder dataReceived = new StringBuilder();

    private static String testMessage = "AAAI10P190000000000020102420151009006100170004004028206665233509403230869028012729101490971571440455000000000000000000100100530782550261710122360000000000000000000000000000000000000000000000000000000003005011020035064096128153186218240255254255251248239226216205198182170160150143137128118114106100095087082077073069067063058056051052050049049049051048050049052050049049050050051056054056060059062065066066066069068071070071073073075077078076080080079078079080079082081084089090090092092094092089084084084084082084087087088088088089090089087087090091094088090087089090090091091091092094094094092095094099098099102102099097098099101100099099102099100096094095096096096098097103101101101102103104104101106103102101100096094095090091088085084084084080080080078080077076075071065062060057057058057056059057058058053049046044041038036034033031031029028026025023025025023022020020019018015013013013012011011010010009009010010010010010000000000000000000000000000000000000000000000000000000000000000000000000000001002003004004005005006005005004004003003003003002002002002002002002003003004004006008011015019026033043055065079092110126144158177195210223234241247255255255250248240232223212199187175163153142132121111104097091085079076071070067064061061060060058056055055054052051049050049047045043042040039037035035034032030028025023022019018017015015014013012011010009008007006006006006005005004004003003002002002002002002002002003003003003003003003003002002002002002002002002002001001001001001001001001001001001002001001001001001001001001001001001001001001001001001001001001001001001001001001001001001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001002003004005006007009011014016019023027033039044050056062068075080085092099106113119126134142149156161166174182189197202208215223226229231233236239240241243245246248247247247247247248248249250252253255254254253253251249247246245244242240237235230226222218214211208205202199196193191189186184182181178176173171168165162160157154151148144141138135131128125122119117114112111111110110110111111112110109107106103101098096094093092091089088087087085084083083083083083083082082080078076075072069066064061059056054051049047045043042042042043044045046048050050051051051050050049049048047046045044043041039037036035034032031031031031032032032033034034034033033032032032032031031031031030029027025023022020019018018018018018018018018018018018";

    private static void logDisplay(String s, int displayMessageType) {
        if (settings.CLI_MODE) {
            logger.info(s);
        } else {
            log.AddToDisplay.Display(s, displayMessageType);
        }
    }

    @Override
    public void run() {
        logDisplay("MINDRAY BC 3000 handler started...", DisplayMessageType.TITLE);
        logDisplay("Checking available ports on this system...", DisplayMessageType.INFORMATION);

        String[] ports = Manager.getSerialPorts();

        logDisplay("Available ports:", DisplayMessageType.TITLE);

        for (int i = 0; i < ports.length; i++) {
            logDisplay(ports[i], DisplayMessageType.INFORMATION);
        }

        logDisplay("Now connecting to port " + RS232Settings.COMPORT, DisplayMessageType.TITLE);

        if (Manager.openPortforData("MINDRAY BC 3000")) {
            logDisplay("Connected successfully", DisplayMessageType.INFORMATION);
            setTestIDs();
        }
    }

    public static void HandleDataInput(String data) {

        // initialize dataReceived object
        if (data.charAt(0) == STX) {
            dataReceived = new StringBuilder();
        }

        // check for end of message
        if (data.contains(String.valueOf(EOF))) {

            // append the last of the data
            int endIndex = data.indexOf(String.valueOf(EOF));
            dataReceived.append(data.substring(0, endIndex));

            processMessage();

            // clear the dataReceived object
            dataReceived = new StringBuilder();

            // handle data found after the EOF character by appending to dataReceived
            if (data.substring(endIndex).length() > 1) {
                if (data.startsWith(String.valueOf(EOF)))
                    HandleDataInput(data.substring(endIndex + 2));
                else
                    HandleDataInput(data.substring(endIndex));
            }
        } else {
            dataReceived.append(data);
        }

    }

    private static String[] normalizeData(String data) {
        // Data is a string of numbers that are fixed width and decimal places implied
        // WBC[109/L] ###.# Lymph#[109/L] ###.# Mid#[109/L] ###.# Gran#[109/L] ###.# Lymph%[%] ##.# Mid%[%] ##.# Gran%[%]
        // ##.# RBC[1012/L] ##.# HGB[g/L] ### MCHC[g/L] #### MCV[fL] ###.# MCH [pg] ###.# RDW-CV[%] ##.# HCT[%] ##.#
        // PLT[109/L] #### MPV[fL] ##.# PDW ##.# PCT[%] .###  RDW-SD[fL] ###.#
        String[] dataFormat = {"###.#", "###.#", "###.#", "###.#", "##.#", "##.#", "##.#", "#.##", "##.#", "###.#",
                "###.#", "###.#", "##.#", "##.#", "####", "##.#", "##.#", ".###", "###.#"};

        String[] norm = new String[dataFormat.length];
        int currentDataIndexLocation = 0;
        for (int i = 0; i < dataFormat.length; i++) {
            int lengthOfFormat = numHashes(dataFormat[i]);
            String rawValue = data.substring(currentDataIndexLocation, currentDataIndexLocation + lengthOfFormat);
            //System.out.println(rawValue);
            norm[i] = customFormat(rawValue, dataFormat[i]);
            //System.out.println(norm[i]);
            currentDataIndexLocation = currentDataIndexLocation + lengthOfFormat;
        }

        return norm;

    }

    private static int numHashes(String s) {
        int counter = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '#') {
                counter++;
            }
        }
        return counter;
    }

    public static void main(String[] args) {
        setTestIDs();
        dataReceived.append(testMessage);
        processMessage();
    }

    private static String customFormat(String value, String pattern) {
        String formatted = "";
        int ind = 0;
        try {
            formatted = String.valueOf(Integer.parseInt(value));

            if(pattern == ".###") {

                formatted = "0." + formatted;

                formatted = String.valueOf(Float.parseFloat(formatted));

            } else {

                for (int i = pattern.length() - 1, in = 0; i >= 0; i--, in++) {
                    if (pattern.charAt(i) == '.') {
                        ind = in;
                        break;
                    }
                }

                if (ind > 0) {
                    for (int i = value.length() - 1, in = 0; i >= 0; i--, in++) {

                        System.out.println(pattern + " -> " + ind + "; " + value.length() + " : " + in);

                        if (in == ind) {
                            formatted = value.substring(0, i + 1) + "." + value.substring(i + 1);
                            formatted = String.valueOf(Float.parseFloat(formatted));
                            break;
                        }
                    }
                }

            }
        } catch (NumberFormatException ex) {
            formatted = "0";
        }


        return formatted;
    }

    private static void processMessage() {

        String dataReceivedString = dataReceived.toString();

        // check for empty message
        if (null == dataReceivedString || dataReceivedString.isEmpty())
            return;

        dataReceivedString = dataReceivedString.trim();

        // check to see if this is a sample for processing
        if (!"A".equals(dataReceivedString.substring(0, 1)))
            return;


        String specimenId = dataReceivedString.substring(11, 21);

        String testDateTime = dataReceivedString.substring(22, 34);

        // parse the message with the test data
        String[] dataParts = normalizeData(dataReceivedString.substring(34));

        int measureID;
        float value = 0;
        boolean flag = false;
        // for each string parse the value and send to BLIS
        for (int i = 0; i < dataParts.length; i++) {
            measureID = getMeasureID(i);
            if (measureID > 0) {
                try {
                    value = Float.parseFloat(dataParts[i].trim());
                } catch (NumberFormatException e) {
                    try {
                        value = Float.parseFloat(dataParts[i].trim());
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
