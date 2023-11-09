package com.mrincredible.hunterx;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Random;

public class LogActivity extends AppCompatActivity {
    private final ConnectionDetails connectionDetails = new ConnectionDetails();
    private final ConnectionSettings connectionSettings = new ConnectionSettings();
    private final boolean begin = false;
    private final boolean couldnt_connect = false;
    Proxy proxxy;
    Intent intent;
    StringBuilder response = new StringBuilder();
    StringBuilder responseToWrite = new StringBuilder();
    String mproxy; //for manual proxy only
    int mport; //for manual port only
    boolean useProxy;
    boolean useManualProxy;
    String tmpfile = connectionDetails.getTmpfile();
    String tempResult = connectionDetails.getTempResult();
    private TextView tvlogResult;
    private HttpURLConnection httpURLConnection = null;
    private URL url;
    private int connectionType;
    private String filename;
    private boolean startconnection;
    private boolean isRunning = false;
    private boolean createconnection = false;
    private Handler handler;
    private String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        tvlogResult = LogActivity.this.findViewById(R.id.tvlogResult);

        intent = getIntent();
        connectionType = intent.getIntExtra("connectiontype", 3);
        host = intent.getStringExtra("Host");
        startconnection = intent.getBooleanExtra("StartConnection", false);
        useProxy = intent.getBooleanExtra("Useproxy", false);
        useManualProxy = intent.getBooleanExtra("Usemanualproxy", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        response = new StringBuilder();
        response.append(readOldResponse(tempResult));
        tvlogResult.setText(response.toString());

        switch (connectionType) {
            case 0:
                if (useManualProxy) {
                    mproxy = intent.getStringExtra("Proxy");
                    mport = intent.getIntExtra("Port", 80);
                }
                break;
            case 1:
                filename = intent.getStringExtra("Filename");
                break;
            default:
        }

        if (startconnection) {
            switchConnection();
        }
    }

    public void copy(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("resultCopy", tvlogResult.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    public void clear(View v) {
//        clearTempResult(tempResult);
        deleteTempFile(connectionDetails.getTempResult());
        tvlogResult.setText("");
    }

    public void stop(View v) {
        Toast.makeText(LogActivity.this, "connection stopped", Toast.LENGTH_SHORT).show();
        isRunning = false;
        startconnection = false;
        createconnection = false;
        finish();
    }

    private void switchConnection() {

        switch (connectionType) {
            case 0:
                //manual input
                createconnection = true;
                if (useProxy) {
                    createConnection(mproxy, mport);
                } else {
                    createConnection("noproxy", 80);
                }
//                okhttpconnection(mproxy, mport);
                return;
            case 1:
                //manual proxyfile
                readFile(getFilesDir(), filename);
                return;
            case 2:
                //austomode
                automode();
                return;
            default:
                Toast.makeText(this, "mode not selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void readFile(File directory, String fileName) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(directory + "/" + fileName))) {
            String line;
            String proxyANDport;
            while (((line = bufferedReader.readLine()) != null) && (startconnection)) {
                createconnection = true;
                proxyANDport = line.trim();
                if (connectionSettings.checkProxyInFile(proxyANDport)) {
                    if (proxyANDport.contains(":")) {
                        if (!proxyANDport.endsWith(":")) {
                            String[] split = proxyANDport.split(":");
                            createConnection(split[0], Integer.parseInt(split[1]));
                        }
                    }
                }
            }
        } catch (IOException e) {
            Toast.makeText(LogActivity.this, "Unable To Read File Content", Toast.LENGTH_SHORT).show();
        }
    }

    private String proxyGenerator() {
        Random random = new Random();

        String proxxxy = random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255);

        return proxxxy;
    }

    private void automode() {
//        createFile(tmpfile);
        // Initialize the handler on the main (UI) thread
        handler = new Handler(Looper.getMainLooper());

        // Start the asynchronous while loop
        isRunning = true;
        startAsyncWhileLoop();
    }

    private void createConnection(String tproxy, int tport) {
        Thread thread = new Thread(() -> {
            response.append("Host: " + host + "\n");
            try {
                url = new URL("http://" + host);
                if (useProxy) {
                    proxxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(tproxy, tport));
                    httpURLConnection = (HttpURLConnection) url.openConnection(proxxy);
                    response.append("Proxy: " + tproxy + "\n" + "Port: " + tport);
                } else {
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                }
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setInstanceFollowRedirects(true);
                httpURLConnection.addRequestProperty("Accept-encoding", "gzip");
                httpURLConnection.addRequestProperty("Connection", "close");
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.connect();

                response.append("Using proxy: " + httpURLConnection.usingProxy() + "\n");

                if ((httpURLConnection.getResponseCode() == 101) || (httpURLConnection.getResponseCode() == 200)) {
                    responseToWrite.append("Proxy: " + tproxy + ":" + tport + " connected to " + host + " successfully");
                    httpURLConnection.getHeaderFields().forEach((key, value) -> {
                        if (key == null) {
                            key = "Response";
                        }
                        response.append(key + ":" + value + "\n");
                        responseToWrite.append(key + ":" + value + "\n");
                    });
                    //TODO ADD THE OK RESULT TO A FILE AND SHOW USERS THE LOCATION
                    //TODO FIX DARK THEME
                    //TODO ADD CONNECTION WITH SNI HOST
                    //TODO ADD CONNECTION WITH AUTO GENERATED SNI HOSTS
                } else {
                    httpURLConnection.getHeaderFields().forEach((key, value) -> {
                        if (key == null) {
                            key = "Response";
                        }
                        response.append(key + ":" + value + "\n");
                    });
//                    response.append(httpURLConnection.getErrorStream().toString());

                    response.append("\n\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                response.append(e + "\n\n");
            } finally {
                httpURLConnection.disconnect();
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        writeToFileInternally(tempResult, response.toString());
        tvlogResult.setText(response.toString());
    }

    private void startAsyncWhileLoop() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                response.append("Host: " + host + "\n");
                while (isRunning) {
//                    writeToFile(tmpfile, proxyGenerator());

                    String tempProxy = proxyGenerator();
                    writeToFileInternally(tmpfile, tempProxy);

                    try {
                        url = new URL("http://" + host);
                        proxxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(tempProxy, 80));
                        httpURLConnection = (HttpURLConnection) url.openConnection(proxxy);
                        httpURLConnection.setRequestMethod("GET");
                        httpURLConnection.setInstanceFollowRedirects(true);
                        httpURLConnection.addRequestProperty("Accept-encoding", "gzip");
                        httpURLConnection.addRequestProperty("Connection", "close");
                        httpURLConnection.setConnectTimeout(5000);
                        httpURLConnection.setReadTimeout(5000);
                        httpURLConnection.connect();

                        if ((httpURLConnection.getResponseCode() == 101) || (httpURLConnection.getResponseCode() == 200)) {
                            responseToWrite.append("Proxy: " + tempProxy + ":" + 80 + " connected to " + host + " successfully");
                            httpURLConnection.getHeaderFields().forEach((key, value) -> {
                                if (key == null) {
                                    key = "Response";
                                }
                                response.append(key + ":" + value + "\n");
                                responseToWrite.append(key + ":" + value + "\n");
                            });

                            response.append("\n----------------END--------------\n\n");
                            responseToWrite.append("\n----------------END--------------\n\n");

                            //TODO ADD THE OK RESULT TO A FILE AND SHOW USERS THE LOCATION
                        } else {
                            httpURLConnection.getHeaderFields().forEach((key, value) -> {
                                if (key == null) {
                                    key = "Response";
                                }
                                response.append(key + ":" + value + "\n");
                                responseToWrite.append(key + ":" + value + "\n");
                            });

                            response.append("\n----------------END--------------\n\n");
                        }

//                System.out.println(response.toString());
                    } catch (MalformedURLException e) {
                        response.append("No Internet connection");
                    } catch (IOException e) {
                        e.printStackTrace();
                        response.append(e + "\n----------------END--------------\n\n");
                    } finally {
                        httpURLConnection.disconnect();
                    }


                    // Update UI on the main thread using the handler
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Update UI components or perform other UI-related actions
//                            readFile(connectionDetails.getTempDir() + "/" + connectionDetails.getTmpfile());
                            writeToFileInternally(tempResult, response.toString());
                            tvlogResult.setText(response.toString());
                        }
                    });

                    // Delay the loop for a specific interval (e.g., 1 second)
                    try {
                        Thread.sleep(1000); // Adjust the delay interval as needed
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void writeToFileInternally(String fileName, String text) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput(fileName, MODE_APPEND);
            fileOutputStream.write((text + "\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public String readOldResponse(String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String text;
            while ((text = bufferedReader.readLine()) != null) {
                stringBuilder.append(text + "\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void deleteTempFile(String filename) {
        File file = new File(getFilesDir() + "/" + filename);
        if (file.exists()) {
            file.delete();
            Toast.makeText(LogActivity.this, "Log Cleared", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        // Stop the while loop when the activity is destroyed
        isRunning = false;
        startconnection = false;
        createconnection = false;
        super.onDestroy();
    }
}