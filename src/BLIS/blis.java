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
package BLIS;

import hl7.Mindray.Message;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URLConnection;

import system.settings;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Stephen Adjei-Kyei <stephen.adjei.kyei@gmail.com>
 *         <p/>
 *         This file is responsible for sending and retrieving data from BLIS through BLIS HTTP API
 */
public class blis {


    private static String getFormatedDate(String strDate) {
        String date = "";
        date = strDate.substring(0, 4) + "-";
        date = date + strDate.substring(4, 6) + "-";
        date = date + strDate.substring(6, 8);

        //date=sdfDate.format(strDate);
        return date.toString();
    }

    public static String getTestData(String specimenTypeFilter, String specimenTestFilter, String aux) {
        return getTestData(specimenTypeFilter, specimenTestFilter, aux, MSACCESS.Settings.DAYS);
    }

    public static String getTestData(String specimenTypeFilter, String specimenTestFilter, String aux, int DAYS) {
        String data = "-1";
        try {
            String url = settings.BLIS_URL;
            url = url + "api/get_test_types.php?";          // username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
            url = url + "specimenfilter=" + specimenTypeFilter;
            url = url + "&testfilter=" + specimenTestFilter;
            url = url + "&day=" + DAYS;
            url = url + "&auxid=" + URLEncoder.encode(aux, "UTF-8");

            URL burl = new URL(url);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(burl.openStream()))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);

                }
                data = response.toString();

            } catch (Exception e) {
                log.logger.Logger(e.getMessage());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
            log.logger.PrintStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data.trim();
    }

    public static String getSampleData(String sampleID, String dateFrom, String dateTo, String specimenTypeFilter, String specimenTestFilter) {
        String data = "-1";

        try {
            String url = settings.BLIS_URL;
            url = url + "api/get_specimen.php?";            // username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
            url = url + "specimen_id=" + URLEncoder.encode(sampleID, "UTF-8") + "&specimenfilter=" + specimenTypeFilter;
            url = url + "&testfilter=" + specimenTestFilter;
            if (sampleID.isEmpty()) {
                url = url + "&datefrom=" + getFormatedDate(dateFrom);
                url = url + "&dateto=" + getFormatedDate(dateTo);
            }


            URL burl = new URL(url);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(burl.openStream()))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);

                }
                data = response.toString();

            } catch (Exception e) {
                log.logger.Logger(e.getMessage());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data.trim();

    }

    public static String saveResults(String specimenID, int measureID, String result) {
        String data = "-1";
        try {
            String url = settings.BLIS_URL;
            url = url + "api/update_result.php?";           // username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
            url = url + "specimen_id=" + URLEncoder.encode(specimenID, "UTF-8");
            url = url + "&measure_id=" + measureID;
            url = url + "&result=" + URLEncoder.encode(result, "UTF-8");
            url = url + "&dec=0";

            String authString = settings.BLIS_USERNAME + ":" + settings.BLIS_PASSWORD;
            // System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            // System.out.println("Base64 encoded auth string: " + authStringEnc);

            URL burl = new URL(url);

            URLConnection urlConnection = burl.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            InputStream is = urlConnection.getInputStream();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(is /*burl.openStream()*/))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);

                }
                data = response.toString();

            } catch (Exception e) {
                log.logger.Logger(e.getMessage());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
            log.logger.PrintStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data.trim();
    }

    public static String saveResults(String specimenID, int measureID, Float result, int dec) {

        String data = "-1";
        try {
            String url = settings.BLIS_URL;
            url = url + "api/update_result.php?";       // username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
            url = url + "specimen_id=" + URLEncoder.encode(specimenID, "UTF-8");
            url = url + "&measure_id=" + measureID;
            url = url + "&result=" + result;
            url = url + "&dec=" + dec;


            String authString = settings.BLIS_USERNAME + ":" + settings.BLIS_PASSWORD;
            // System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            // System.out.println("Base64 encoded auth string: " + authStringEnc);

            URL burl = new URL(url);

            URLConnection urlConnection = burl.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            InputStream is = urlConnection.getInputStream();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(is /*burl.openStream()*/))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);

                }
                data = response.toString();

            } catch (Exception e) {
                log.logger.Logger(e.getMessage());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
            log.logger.PrintStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data.trim();
    }

    public static String saveResults(String specimenID, int measureID, String result, int dec) {

        String data = "-1";
        try {
            String url = settings.BLIS_URL;
            url = url + "api/update_result.php?";           // username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
            url = url + "specimen_id=" + URLEncoder.encode(specimenID, "UTF-8");
            url = url + "&measure_id=" + measureID;
            url = url + "&result=" + result;
            url = url + "&dec=" + dec;


            String authString = settings.BLIS_USERNAME + ":" + settings.BLIS_PASSWORD;
            // System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            // System.out.println("Base64 encoded auth string: " + authStringEnc);

            URL burl = new URL(url);

            URLConnection urlConnection = burl.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            InputStream is = urlConnection.getInputStream();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(is /*burl.openStream()*/))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);

                }
                data = response.toString();

            } catch (Exception e) {
                log.logger.Logger(e.getMessage());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
            log.logger.PrintStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data.trim();
    }

    public static String saveResults(Message resultmsg) {

        String specimenID = resultmsg.Segments.get(2).Fields.get(1).realValue;
        String measureID = resultmsg.Segments.get(3).Fields.get(2).realValue;
        String result = resultmsg.Segments.get(3).Fields.get(4).realValue;


        String data = "-1";
        try {
            String url = settings.BLIS_URL;
            url = url + "api/update_result.php?";       // username="+settings.BLIS_USERNAME + "&password="+settings.BLIS_PASSWORD;
            url = url + "specimen_id=" + specimenID;
            url = url + "&measure_id=" + measureID;
            url = url + "&result=" + result;


            String authString = settings.BLIS_USERNAME + ":" + settings.BLIS_PASSWORD;
            // System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            // System.out.println("Base64 encoded auth string: " + authStringEnc);

            URL burl = new URL(url);

            URLConnection urlConnection = burl.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            InputStream is = urlConnection.getInputStream();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(is /*burl.openStream()*/))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);

                }
                data = response.toString();

            } catch (Exception e) {
                log.logger.Logger(e.getMessage());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
            log.logger.Logger(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(blis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data.trim();

    }


}
