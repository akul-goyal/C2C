package c2c.com.cartalk;

/**
 *  ServerConnection handles all outgoing HTTP requests to server
 *
 */

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class ServerConnection {


    //---Variables----------------------------------------------------------------------------------

    private final String serverPath = "http://18be6be5.ngrok.io" + "/";
    private String serverResponse = "";


    //---SEND INFO----------------------------------------------------------------------------------
    /**
     * Functions for sending info to server
     * // converts HashMaps to json objects
     */

    // generic function for sending info to server
    // Function is called by:
    // - sendProfileToServer
    // - sendLocationToServer
    // - sendMessageToServer
    String sendInfoToServer(final JSONObject info, final String finalServerPath) {
        // spawn a new thread for request to run on
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                serverResponse = makePostCallToServer(info, finalServerPath);
            }
        });

        // start thread
        thread.start();

        // wait for thread to finish before moving on
        try {
            thread.join();
        } catch (Exception e) {
            System.out.println("Thread issues in SendInfoToServer()");
        }

        return serverResponse;

    }

    // calls sendInfoToServer with url /profile
    void sendProfileToServer(final JSONObject info) {
        sendInfoToServer(info, serverPath + "update_profile");
    }

    // calls sendInfoToServer with url /location
    void sendLocationToServer(final JSONObject info) {
        sendInfoToServer(info, serverPath + "update_location");
    }

    // sends info to server
    void sendMessageToServer(final JSONObject info) {
        sendInfoToServer(info, serverPath + "send_message");
    }

    String sendMessageRequestToServer(final JSONObject info) {
        return sendInfoToServer(info, serverPath + "get_message");
    }



    //---HTTP calls---------------------------------------------------------------------------------

    // makes a Post Call
    String makePostCallToServer(JSONObject info, String finalServerPath) {
        String response = "";

        System.out.println("PATH: " + finalServerPath);
        // connect
        try {
            // connect to url
            URL url = new URL(finalServerPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            //urlConnection.setRequestProperty("test", "test2");

            urlConnection.getOutputStream().write(info.toString().getBytes());
            urlConnection.getOutputStream().flush();
            urlConnection.getOutputStream().close();


            try {

                // get response
                InputStream inputStream = urlConnection.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                StringBuilder stringBuilder = new StringBuilder();
                while(scanner.hasNext()){
                    //Log.i("inside", "inside");
                    stringBuilder.append(scanner.nextLine());
                }
                response = stringBuilder.toString();

                //echoTextView.setText(document);
                System.out.println(response);

            } finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException in pingInternet()");
        } catch (IOException e) {
            System.out.println("IOException in pingInternet()");
            System.out.println(e);
        }

        return response;
    }

}
