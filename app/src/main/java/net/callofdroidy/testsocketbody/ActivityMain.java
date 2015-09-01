package net.callofdroidy.testsocketbody;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityMain extends AppCompatActivity {

    Pattern pattern_Location = Pattern.compile("Location:\\s.+Content-Length");
    Pattern pattern_sso_oppai_rid = Pattern.compile("sso_oppai_rid=[A-Za-z0-9]+;");
    Pattern pattern_PHPSESSID = Pattern.compile("PHPSESSID=[A-Za-z0-9]+;");
    Pattern pattern_sso_server_id = Pattern.compile("sso_server_id=[A-Za-z0-9-]+;");
    Pattern pattern_sso_server_id2 = Pattern.compile("sso_server_id2=[A-Za-z0-9-]+;");
    Pattern pattern_login = Pattern.compile("name=\"[A-Za-z0-9-%_]+");  //extract group index 2, 3, 5
    Pattern pattern_sso_server_ns = Pattern.compile("sso_server_ns=[A-Za-z0-9-%]+;");
    Pattern pattern_sso_server_er = Pattern.compile("sso_server_er=[A-Za-z0-9-%]+;");
    Pattern pattern_sso_server_ern = Pattern.compile("sso_server_ern=[A-Za-z0-9-%]+;");

    private String nextPath = "/sso_client/";
    private String sso_oppai_rid = "";
    private String PHPSESSID = "";
    private String sso_server_id = "";
    private String sso_server_id2 = "";
    private String sso_server_ns = "";
    private String sso_server_er = "";
    private String sso_server_ern = "";
    private String username = "";
    private String password = "";
    private String signin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //parameters list: (method, urlHost, port, path, extraHeaderData, bodyData, round)
        new ATSendSocketHTTPRequest().execute("GET", "104.236.52.185", "80", nextPath, null, null, "1");
    }

    //this method will be wrapped in the AsyncTask
    private String sendSocketRequest(String method, String urlHost, int port, String path, String headerExtraData, String bodyData){
        Socket socket = null;
        PrintWriter writer = null;
        BufferedReader reader = null;
        StringBuilder respLine = new StringBuilder("");
        try{
            socket = new Socket(urlHost, port);
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.println(method + " " + path + " HTTP/1.1");  //Method + Path
            writer.println("Host: " + urlHost);   //Host base address
            writer.println("Accept: */*");
            writer.println("User-Agent: curl/7.37.0");
            if(headerExtraData != null){ //check if has extra header data
                Log.e("extra header:", headerExtraData);
                writer.println(headerExtraData);
            }
            writer.println("\r"); // Important, else the server will expect that there's more into the request.
            if(bodyData != null){//check if has extra body data
                Log.e("extra payload data:", bodyData);
                String encodedBodyData = URLEncoder.encode(bodyData, "UTF-8");
                writer.println(encodedBodyData);
            }
            writer.flush();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
            for (String line; (line = reader.readLine()) != null;) {
                /*
                if (line.isEmpty())
                    break; // Stop when headers are completed. We're not interested in all the HTML.
                */
                respLine.append(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
            if (writer != null) { writer.close(); }
            if (socket != null) try { socket.close(); } catch (IOException logOrIgnore) {}
            return respLine.toString();
        }
    }

    private void matchAPattern(String raw, Pattern target, String var){
        Matcher matcher = target.matcher(raw);
        int count = 0;
        while (matcher.find()){
            switch (var){
                case "nextPath":
                    nextPath = matcher.group(0).substring(31, matcher.group(0).length() - 14);
                    Log.e("nextPath", nextPath);
                    break;
                case "sso_oppai_rid":
                    sso_oppai_rid = matcher.group(0);
                    Log.e("sso_oppai_rid:", sso_oppai_rid);
                    break;
                case "PHPSESSID":
                    PHPSESSID = matcher.group(0);
                    Log.e("PHPSESSID:", matcher.group(0));
                    break;
                case "sso_server_id":
                    sso_server_id = matcher.group(0);
                    Log.e("sso_server_id:", sso_server_id);
                    break;
                case "sso_server_id2":
                    sso_server_id2 = matcher.group(0);
                    Log.e("sso_server_id2:", sso_server_id2);
                    break;
                case "sso_server_ns":
                    sso_server_ns = matcher.group(0);
                    Log.e("sso_server_ns:", sso_server_ns);
                    break;
                case "sso_server_er":
                    sso_server_er = matcher.group(0);
                    Log.e("sso_server_er:", sso_server_er);
                    break;
                case "sso_server_ern":
                    sso_server_ern = matcher.group(0);
                    Log.e("sso_server_ern:", sso_server_ern);
                    break;
                case "login":
                    count++;
                    switch (count){
                        case 2:
                            username = matcher.group().substring(6, matcher.group().length());
                            Log.e("username:", username);
                            break;
                        case 3:
                            password = matcher.group().substring(6, matcher.group().length());
                            Log.e("password", password);
                            break;
                        case 5:
                            signin = matcher.group().substring(6, matcher.group().length());
                            Log.e("sign in", signin);
                            break;
                    }
            }
        }
        matcher.reset();
    }

    private void extractRespData(int round, String respRaw){
        switch (round){
            case 1:
                matchAPattern(respRaw, pattern_Location, "nextPath");
                matchAPattern(respRaw, pattern_sso_oppai_rid, "sso_oppai_rid");
                matchAPattern(respRaw, pattern_PHPSESSID, "PHPSESSID");
                new ATSendSocketHTTPRequest().execute("GET", "104.236.52.185", "80", nextPath, null, null, "2");
                break;
            case 2:
                matchAPattern(respRaw, pattern_Location, "nextPath");
                matchAPattern(respRaw, pattern_sso_server_id, "sso_server_id");
                new ATSendSocketHTTPRequest().execute("GET", "104.236.52.185", "80", nextPath,
                        "Cookie: " + sso_server_id + " " + PHPSESSID.substring(0, PHPSESSID.length() - 1), null, "3");
                break;
            case 3:
                matchAPattern(respRaw, pattern_login, "login");
                nextPath = "/sso/server/index.php?sso_provider=sso_login";
                Log.e("nextPath", nextPath);
                String dataExtraHeader = "Cookie: " + sso_server_id + " " + PHPSESSID.substring(0, PHPSESSID.length() - 1) + "\n" +
                        "Content-Type: multipart/form-data; boundary=----WebKitFormBoundarydDgacBdUjUzQsKuW";
                String dataLogin = username + "=krishna&" + password + "=krishna1&" + signin + "=Sign in";
                new ATSendSocketHTTPRequest().execute("POST", "104.236.52.185", "80", nextPath, dataExtraHeader, dataLogin, "4");
                break;
            case 4:
                //
                break;
            case 5:
                //
                break;
        }
    }

    class ATSendSocketHTTPRequest extends AsyncTask<String, String, String> {
        private int round;
        @Override
        protected String doInBackground(String...params){
            round = Integer.valueOf(params[6]);
            return sendSocketRequest(params[0], params[1], Integer.valueOf(params[2]), params[3], params[4], params[5]);
        }
        protected void onPostExecute(String respRaw) {
            Log.e("round :", "" + round);
            Log.e("raw resp:", respRaw);
            extractRespData(round, respRaw);
        }
    }
}