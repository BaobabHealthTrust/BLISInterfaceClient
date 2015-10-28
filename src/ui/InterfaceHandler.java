package ui;

import MSACCESS.MSACCESSABXPentra60CPlus;
import RS232.*;
import RS232.BT3000PlusChameleon;
import TCPIP.*;
import TEXT.BDFACSCalibur;
import log.logger;
import system.settings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author : Jeremy Espino MD
 * Created  10/27/15 8:42 PM
 */


public class InterfaceHandler {

    private String sourceType;
    private String blisUrl;
    private String blisUsername;
    private String blisPassword;
    private String equipment;
    private String sourceConfiguration;

    private MindrayBS200E obj = null;
    private MICROS60 abx = null;
    private MSACCESSABXPentra60CPlus msaccess_abx = null;
    private TCPIP.BT3000PlusChameleon btobj = null;
    private RS232.BT3000PlusChameleon btRSobj = null;
    private SYSMEXXS500i sysobj = null;
    private BDFACSCalibur bdobj = null;
    private SelectraJunior selobj = null;
    private ABXPentra80 pentra80Obj = null;
    private CobasAmpliPrep cobasObj = null;
    private MindrayBC3600 minbc3600obj = null;
    private MindrayBC3000 minbc3000obj = null;
    private GeneXpert expobj = null;
    private SYSMEXXT2000i sys2000iObj = null;
    private FlexorE flexObj = null;

    public void getConfigurations() {
        String value = configuration.configuration.GetParameterValue(configuration.configuration.FEED_SOURCE);
        sourceType = value;

        // set data source configurations
        switch (value) {
            case "RS232":
                sourceConfiguration = configuration.configuration.GetParameterValue(configuration.configuration.RS232_CONFIGURATIONS);
                break;
            case "TCP/IP":
                sourceConfiguration = configuration.configuration.GetParameterValue(configuration.configuration.TCP_IP_CONFIGURATIONS);
                break;
            case "MSACCESS":
                sourceConfiguration = configuration.configuration.GetParameterValue(configuration.configuration.MSACCESS_CONFIGURATIONS);
                break;
            case "TEXT":
                sourceConfiguration = configuration.configuration.GetParameterValue(configuration.configuration.TEXT);
                break;

        }

        setParams(sourceConfiguration.split(","));

        // set BLIS configuration
        String blisParams = configuration.configuration.GetParameterValue(configuration.configuration.BLIS_CONFIGURATIONS);
        setParams(blisParams.split(","));

        // set equipment configuration
        equipment = configuration.configuration.GetParameterValue(configuration.configuration.EQUIPMENT);

        String misc = configuration.configuration.GetParameterValue(configuration.configuration.MISCELLANEOUS);
        setParams(misc.split(","));


    }

    private void setParams(String[] values) {
        for (int i = 0; i < values.length; i++) {
            String[] data = values[i].split("=");
            data[0] = data[0].trim();
            data[1] = data[1].trim();
            switch (data[0]) {
                case "COMPORT":
                    RS232Settings.COMPORT = data[1];
                    break;
                case "BAUD_RATE":
                    RS232Settings.BAUD = Integer.parseInt(data[1]);
                    break;
                case "PARITY":
                    if (data[1].equalsIgnoreCase("none"))
                        RS232Settings.PARITY = 0;
                    else if (data[1].equalsIgnoreCase("odd"))
                        RS232Settings.PARITY = 1;
                    else
                        RS232Settings.PARITY = 2;
                    break;
                case "STOP_BITS":
                    RS232Settings.STOPBIT = Integer.parseInt(data[1]);
                    break;
                case "DATA_BITS":
                    RS232Settings.DATABIT_LENGTH = Integer.parseInt(data[1]);
                    break;
                case "APPEND_NEWLINE":
                    RS232Settings.APPEND_NEWLINE = data[1].equalsIgnoreCase("yes");
                    break;
                case "APPEND_CARRIAGE_RETURN":
                    RS232Settings.APPEND_CARRIAGE_RETURN = data[1].equalsIgnoreCase("yes");
                    break;
                case "PORT":
                    tcpsettings.PORT = Integer.parseInt(data[1]);
                    break;
                case "EQUIPMENT_IP":
                    tcpsettings.EQUIPMENT_IP = data[1];
                    break;
                case "BLIS_URL":
                    blisUrl = data[1];
                    settings.BLIS_URL = data[1];
                    break;
                case "BLIS_USERNAME":
                    blisUsername = data[1];
                    try {
                        settings.BLIS_USERNAME = URLEncoder.encode(data[1], "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case "BLIS_PASSWORD":
                    blisPassword = data[1];
                    try {
                        settings.BLIS_PASSWORD = URLEncoder.encode(data[1], "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case "ENABLE_LOG":
                    settings.ENABLE_LOG = data[1].equalsIgnoreCase("yes");
                    break;
                case "MSACCESS":
                    MSACCESS.Settings.DATASOURCE = data[1];
                    break;
                case "DAYS":
                    MSACCESS.Settings.DAYS = Integer.parseInt(data[1]);
                    break;
                case "DATASOURCE":
                    MSACCESS.Settings.DATASOURCE = data[1];
                    break;
                case "WRITE_TO_FILE":
                    settings.WRITE_TO_FILE = data[1].equalsIgnoreCase("yes");
                    break;
                case "POOL_DAY":
                    settings.POOL_DAY = Integer.parseInt(data[1]);
                    break;
                case "POOL_INTERVAL":
                    settings.POOL_INTERVAL = Integer.parseInt(data[1]);
                    break;
                case "ENABLE_AUTO_POOL":
                    settings.ENABLE_AUTO_POOL = data[1].equalsIgnoreCase("yes");
                    break;
                case "MODE":
                    settings.SERVER_MODE = data[1].equalsIgnoreCase("server");
                    tcpsettings.SERVER_MODE = data[1].equalsIgnoreCase("server");
                    break;
                case "CLIENT_RECONNECT":
                    tcpsettings.CLIENT_RECONNECT = data[1].equalsIgnoreCase("yes");
                    break;
                case "BASE_DIRECTORY":
                    TEXT.settings.BASE_DIRECTORY = data[1];
                    break;
                case "USE_SUB_DIRECTORIES":
                    TEXT.settings.USE_SUB_DIRECTORIES = data[1].equalsIgnoreCase("yes");
                case "SUB_DIRECTORY_FORMAT":
                    TEXT.settings.SUB_DIRECTORY_FORMAT = data[1];
                    break;
                case "FILE_NAME_FORMAT":
                    TEXT.settings.FILE_NAME_FORMAT = data[1];
                    break;
                case "FILE_EXTENSION":
                    TEXT.settings.FILE_EXTENSION = data[1];
                    break;
                case "FILE_SEPERATOR":
                    TEXT.settings.FILE_SEPERATOR = data[1];
                    TEXT.settings.setChar(data[1]);
                    break;
                case "AUTO_SPECIMEN_ID":
                    settings.AUTO_SPECIMEN_ID = data[1].equalsIgnoreCase("yes");
                    break;


            }
        }

    }

    public void startAppropriateHandler() {
        switch (sourceType) {
                    case "RS232":
                        switch (equipment.toUpperCase()) {
                            case "ABX MICROS 60":
                                abx = new MICROS60();
                                abx.start();
                                break;
                            case "SELECTRA JUNIOR":
                                selobj = new SelectraJunior();
                                selobj.start();
                                break;
                            case "ABX PENTRA 80":
                                pentra80Obj = new ABXPentra80();
                                pentra80Obj.start();
                                break;
                            case "MINDRAY BC 3600":
                                minbc3600obj = new MindrayBC3600();
                                minbc3600obj.start();
                                break;
                            case "MINDRAY BC 3000":
                                minbc3000obj = new MindrayBC3000();
                                minbc3000obj.start();
                                break;
                            case "BT3000 PLUS-CHAMELEON":
                                btRSobj = new RS232.BT3000PlusChameleon();
                                btRSobj.start();
                                break;
                            case "FLEXOR E":
                                flexObj = new FlexorE();
                                flexObj.start();
                                break;
                        }
                        break;
                    case "TCP/IP":
                        switch (equipment.toUpperCase()) {
                            case "MINDRAY BS-120":
                            case "MINDRAY BS-130":
                            case "MINDRAY BS-180":
                            case "MINDRAY BS-190":
                            case "MINDRAY BS-200":
                            case "MINDRAY BS-220":
                            case "MINDRAY BS-200E":
                            case "MINDRAY BS-220E":
                            case "MINDRAY BS-330":
                            case "MINDRAY BS-350":
                            case "MINDRAY BS-330E":
                            case "MINDRAY BS-350E":
                                obj = new MindrayBS200E(equipment);
                                obj.start();
                                break;
                            case "BT3000 PLUS-CHAMELEON":
                                btobj = new TCPIP.BT3000PlusChameleon();
                                btobj.start();
                                break;
                            case "SYSMEX XS-500I":
                                sysobj = new SYSMEXXS500i();
                                sysobj.start();
                                break;
                            case "COBAS AMPLIPREP":
                                cobasObj = new CobasAmpliPrep();
                                cobasObj.start();
                                break;
                            case "GENEXPERT":
                                expobj = new GeneXpert();
                                expobj.start();
                                break;
                            case "SYSMEX XT-2000I":
                                sys2000iObj = new SYSMEXXT2000i();
                                sys2000iObj.start();
                                break;
                        }
                        break;
                    case "MSACCESS":
                        switch (equipment.toUpperCase()) {
                            case "ABX PENTRA 60C+":
                                msaccess_abx = new MSACCESSABXPentra60CPlus();
                                msaccess_abx.start();
                                break;
                        }
                        break;
                    case "TEXT":
                        switch (equipment.toUpperCase()) {
                            case "BD FACSCALIBUR":
                                bdobj = new BDFACSCalibur();
                                bdobj.start();
                                break;
                        }
                        break;
                }

    }

    public String getEquipment() {
        return equipment;
    }

    public String getSourceConfiguration() {
        return sourceConfiguration;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getBlisUsername() {
        return blisUsername;
    }

    public String getBlisUrl() {
        return blisUrl;
    }

    public String getBlisPassword() {
        return blisPassword;
    }
}
