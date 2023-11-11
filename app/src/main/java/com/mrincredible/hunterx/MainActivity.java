package com.mrincredible.hunterx;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    CircularRevealCardView cardView;
    MaterialButtonToggleGroup modeselection;
    MaterialButton btmanual, btauto, btlog;
    Chip cproxyfilemode, cusemanualproxy;
    TextInputLayout tilhost, tilproxy, tilproxyfile;
    TextInputEditText tiehost, tieproxy, tieproxyfile;
    boolean proxyfilemode = false;
    boolean useProxy = true;
    String mode = "manual";
    String defaulthost = "fr1.test3.net"; //104.26.15.41:80  43.154.19.161
    ConnectionSettings connectionSettings = new ConnectionSettings();
    ConnectionDetails connectionDetails = new ConnectionDetails();
    String extension;
    StringBuilder detailsBefore;
    ActivityResultLauncher<String> launchFileManager = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap;

        if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            mimeTypeMap = MimeTypeMap.getSingleton();
            extension = mimeTypeMap.getExtensionFromMimeType(getContentResolver().getType(uri));
        } else {
            extension = "null";
        }

        if (extension.equals("txt")) {
            deleteTempFile(connectionDetails.getTempProxyFilename());
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder text = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line).append("\n");
                }
                br.close();
                if (writeToFileInternally(connectionDetails.getTempProxyFilename(), text.toString())) {
                    Toast.makeText(this, "tempProxyFile created successfully", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to create tempProxyFile", Toast.LENGTH_SHORT).show();
            }
            tieproxyfile.setText(connectionDetails.getTempProxyFilename());
        } else {
            Toast.makeText(this, "File must be in txt format", Toast.LENGTH_SHORT).show();
            tilproxyfile.setError(null);
            tilproxyfile.setHelperText("File must be in txt format");
        }
    });
    private int connectionType;
    private int savedConnectionType, savedPort;
    private String savedHost, savedProxy, savedFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modeselection = findViewById(R.id.mbtgSelectionGroup);
        cardView = findViewById(R.id.cvTitleBar);
        btauto = findViewById(R.id.btAuto);
        btmanual = findViewById(R.id.btManual);
        btlog = findViewById(R.id.btLog);
        cproxyfilemode = findViewById(R.id.cproxyfilemode);
        cusemanualproxy = findViewById(R.id.cusemanualproxy);
        tilhost = findViewById(R.id.tilhost);
        tilproxy = findViewById(R.id.tilproxy);
        tilproxyfile = findViewById(R.id.tilproxyfile);
        tiehost = findViewById(R.id.tiehost);
        tieproxy = findViewById(R.id.tieproxy);
        tieproxyfile = findViewById(R.id.tieproxyfile);

        permissionHandler();
    }

    @Override
    protected void onPause() {
        deleteTempFile(connectionDetails.getDetailsBefore());
        detailsBefore = new StringBuilder();
        detailsBefore.append("connectiontype-" + connectionType);
        detailsBefore.append("\nHost-" + tiehost.getText().toString());
        detailsBefore.append("\nUseproxy-" + useProxy);
        detailsBefore.append("\nProxy-" + tieproxy.getText().toString());
        detailsBefore.append("\nFilename-" + connectionDetails.getTempProxyFilename());
        detailsBefore.append("\nUsemanualproxy-" + cusemanualproxy.isChecked());
        detailsBefore.append("\nUseproxyfile-" + cproxyfilemode.isChecked());
        writeToFileInternally(connectionDetails.getDetailsBefore(), detailsBefore.toString());
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readDetailsBefore(connectionDetails.getDetailsBefore());
        handler();
    }

    public void readDetailsBefore(String filename) {
        FileInputStream fileInputStream = null;
        if (pathFileExist(filename)) {
            try {
                fileInputStream = openFileInput(filename);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String text;
                while ((text = bufferedReader.readLine()) != null) {
                    doAsSaid(text.trim());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void doAsSaid(String wish){
        if (!wish.isEmpty()){
            String split[] = wish.split("-");
            String wishKey = split[0];
            String wishValue = split[1];
            switch (wishKey){
                case "connectiontype":
                    if (wishValue.equals(0) || wishValue.equals(1)) {
                        modeselection.check(R.id.btManual);
                    } else if (wishValue.equals(2)) {
                        modeselection.check(R.id.btAuto);
                    }
                    break;
                case "Host":
                    tiehost.setText(wishValue);
                    break;
                case "Useproxy":
                    useProxy = wishValue.matches("true");
                    break;
                case "Proxy":
                    tieproxy.setText(wishValue);
                    break;
                case "Filename":
                    tieproxyfile.setText(wishValue);
                    break;
                case "Usemanualproxy":
                    cusemanualproxy.setChecked(wishValue.matches("true"));
                    break;
                case "Useproxyfile":
                    cproxyfilemode.setChecked(wishValue.matches("true"));
                    break;
                default:
                    break;
            }
        }

    }

    public void permissionHandler() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public boolean permissionGranted() {
        return ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) || (Build.VERSION.SDK_INT >= 31));
    }

    public int getSavedConnectionType() {
        return savedConnectionType;
    }

    public void setSavedConnectionType(int savedConnectionType) {
        this.savedConnectionType = savedConnectionType;
    }

    public int getSavedPort() {
        return savedPort;
    }

    public void setSavedPort(int savedPort) {
        this.savedPort = savedPort;
    }

    public String getSavedHost() {
        return savedHost;
    }

    public void setSavedHost(String savedHost) {
        this.savedHost = savedHost;
    }

    public String getSavedProxy() {
        return savedProxy;
    }

    public void setSavedProxy(String savedProxy) {
        this.savedProxy = savedProxy;
    }

    public String getSavedFilename() {
        return savedFilename;
    }

    public void setSavedFilename(String savedFilename) {
        this.savedFilename = savedFilename;
    }


    private void handler() {

        cproxyfilemode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (cusemanualproxy.isChecked()) {
                        tilproxy.setVisibility(View.GONE);
                        cusemanualproxy.setChecked(false);
                    }
                    tilproxyfile.setVisibility(View.VISIBLE);
                } else {
                    tilproxyfile.setVisibility(View.GONE);
                    tilproxy.setVisibility(View.GONE);
                }
                useProxy = isChecked;
                proxyfilemode = isChecked;
                tieproxyfile.setEnabled(false);
            }
        });

        cusemanualproxy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (cproxyfilemode.isChecked()) {
                        tilproxyfile.setVisibility(View.GONE);
                        proxyfilemode = false;
                        cproxyfilemode.setChecked(false);
                    }
                    tilproxy.setVisibility(View.VISIBLE);
                } else {
                    tilproxy.setVisibility(View.GONE);
                }
                useProxy = isChecked;
            }
        });

        tiehost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkHost(s.toString());
            }
        });

        tieproxy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkProxy(s.toString());
            }
        });

        tieproxyfile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkProxyFileName(s.toString());
            }
        });

        tilproxyfile.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchFileManager.launch("text/plain");
            }
        });
    }

    public boolean readyToConnect() {
        if (mode.equals("manual")) {
            if (proxyfilemode) {
                checkDetails(1);
            } else {
                checkDetails(0);
            }
        } else if (mode.equals("auto")) {
            connectionDetails.setHost(Objects.requireNonNull(tiehost.getText()).toString());
            connectionType = 2;
            connectionDetails.setStartConnection(true);
        } else {
            connectionDetails.setStartConnection(false);
        }
        return connectionDetails.isStartConnection();
    }

    public void manual(View v) {
        manualmode();
    }

    public void manualmode() {
        tiehost.setText(null);
        mode = "manual";
        cproxyfilemode.setVisibility(View.VISIBLE);
        cusemanualproxy.setVisibility(View.VISIBLE);
        tiehost.setEnabled(true);

        if (cproxyfilemode.isChecked()) {
            tilproxy.setVisibility(View.GONE);
            tilproxyfile.setVisibility(View.VISIBLE);
            cusemanualproxy.setChecked(false);
            proxyfilemode = true;
            useProxy = true;
        } else if (cusemanualproxy.isChecked()) {
            tilproxy.setVisibility(View.VISIBLE);
            tilproxyfile.setVisibility(View.GONE);
            cproxyfilemode.setChecked(false);
            proxyfilemode = false;
            useProxy = true;
        } else {
            useProxy = false;
        }
    }

    public void auto(View v) {
        automode();
    }

    public void automode() {
        mode = "auto";
        useProxy = true;
        tiehost.setText(defaulthost);
        tiehost.setEnabled(false);
        cproxyfilemode.setVisibility(View.GONE);
        cusemanualproxy.setVisibility(View.GONE);
        tilproxy.setVisibility(View.GONE);
        tilproxyfile.setVisibility(View.GONE);
    }

    public void start(View v) {

        if (!connectionSettings.getReason().equals("null") && !connectionSettings.getReason().equals("Valid")) {
            Toast.makeText(this, connectionSettings.getReason(), Toast.LENGTH_SHORT).show();
        }

        if (permissionGranted()) {
            if (mode.equals("manual")) {
                if (!Objects.requireNonNull(tiehost.getText()).toString().trim().isEmpty()) {
                    if (proxyfilemode) {
                        if (!Objects.requireNonNull(tieproxyfile.getText()).toString().trim().isEmpty()) {
                            if (readyToConnect()) {
                                openLog();
                            }
                        } else {
                            Toast.makeText(this, "proxy filename is empty", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (useProxy) {
                            if (!Objects.requireNonNull(tieproxy.getText()).toString().trim().isEmpty()) {
                                if (readyToConnect()) {
                                    openLog();
                                }
                            } else {
                                Toast.makeText(this, "proxy is empty", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            openLog();
                        }
                    }
                } else {
                    Toast.makeText(this, "host is empty", Toast.LENGTH_SHORT).show();
                }
            } else if (mode.equals("auto")) {
                if (readyToConnect()) {
                    openLog();
                }
            } else {
                Toast.makeText(this, "mode not selected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "please give storage permission", Toast.LENGTH_SHORT).show();
            permissionHandler();
        }
    }

    public void log(View v) {
        openLogFrombt();
    }

    /**
     * 0 = no proxyfile
     * 1 = proxyfile
     */
    public void checkDetails(int m) {
        switch (m) {
            case 0:
                if (checkHost(Objects.requireNonNull(tiehost.getText()).toString()) && checkProxy(Objects.requireNonNull(tieproxy.getText()).toString())) {
                    connectionDetails.setHost(tiehost.getText().toString().trim());
                    connectionDetails.splitProxyPort(tieproxy.getText().toString().trim());
                    connectionType = 0;
                    connectionDetails.setStartConnection(true);
                } else {
                    connectionDetails.setStartConnection(false);
                }
                break;
            case 1:
                if (checkHost(Objects.requireNonNull(tiehost.getText()).toString()) && checkProxyFileName(Objects.requireNonNull(tieproxyfile.getText()).toString())) {
                    //get text in the file in log activity
                    connectionDetails.setHost(tiehost.getText().toString().trim());
                    connectionType = 1;
                    connectionDetails.setStartConnection(true);
                } else {
                    connectionDetails.setStartConnection(false);
                }
                break;
            default:
                connectionDetails.setStartConnection(false);
        }
    }

    public boolean checkHost(String text) {
        if (!(text == null)) {
            String h = text.toLowerCase();
            if (h.length() <= 20) {
                if (connectionSettings.matchHostRegex(h)) {
                    tilhost.setError(null);
                    tilhost.setHelperText(connectionSettings.getReason());
                    return true;
                } else {
                    tilhost.setError(null);
                    tilhost.setHelperText(connectionSettings.getReason());
                }
            } else {
                tilhost.setHelperText(null);
                tilhost.setError("Hostname too long");
            }
        } else {
            tilhost.setHelperText(null);
            tilhost.setError("Hostname cannot be empty");
        }
        return false;
    }

    public boolean checkProxy(String text) {
        if (!(text == null)) {
            boolean canconnect;
            text = text.trim();
            if (!(text.length() > 20)) {
                if (!text.matches(connectionSettings.getHttpRegex())) {
                    if (!text.matches(connectionSettings.getHostRegex())) {
                        if (text.matches(connectionSettings.getProxyPortRegex())) {
                            canconnect = connectionSettings.checkProxyPort(text);
                            tilproxy.setError(null);
                            tilproxy.setHelperText(connectionSettings.getReason());
                            return canconnect;
                        } else {
                            if (text.contains(":")) {
                                if (!text.endsWith(":")) {
                                    if (connectionSettings.Length(text, 2) > 5) {
                                        tilproxy.setHelperText(null);
                                        tilproxy.setError("Port cannot exceed 65535");
                                    }
                                }
                            } else {
                                tilproxy.setHelperText(null);
                                tilproxy.setError("proxy:port eg. 104.26.15.41:80");
                            }
                        }
                    } else {
                        tilproxy.setHelperText("");
                        tilproxy.setError("invalid proxyhost");
                    }
                } else {
                    tilproxy.setError(null);
                    tilproxy.setHelperText("remove http:// or https:// from proxyhost");
                }
            } else {
                tilproxy.setHelperText(null);
                tilproxy.setError("field cannot exceed 20 characters");
            }
        } else {
            tilproxy.setHelperText(null);
            tilproxy.setError("Proxy cannot be empty");
        }
        return false;
    }

    public boolean checkProxyFileName(String filename) {
        if (!(filename == null)) {
            if (pathFileExist(filename)) {
                if (filename.endsWith(".txt")) {
                    tilproxyfile.setError(null);
                    tilproxyfile.setHelperText("File Found");
                    return true;
                } else {
                    tilproxyfile.setError(null);
                    tilproxyfile.setHelperText("File must be in .txt format eg. file.txt");
                }
            } else {
                tilproxyfile.setError(null);
                tilproxyfile.setHelperText("File Not Found, File must be in " + getExternalFilesDir(null).toString() + "/");
            }
        } else {
            tilproxyfile.setError(null);
            tilproxyfile.setHelperText("Filename cannot be empty");
        }
        return false;
    }

    private boolean pathFileExist(String string) {
        //Returns true if the file to read file from already exists
        File file = new File(getFilesDir(), string);
        return file.exists();
    }

    private boolean checkForIllegalChars(String s) {
        char[] illegalchars = {92, 47, 58, 63, 42, 34, 60, 62, 124};
        for (char kh : illegalchars) {
            if (s.indexOf(kh) >= 0) {
                return true;
            }
        }
        return false;
    }

//    private boolean makeDir(boolean b, File f) {
//        if (b) {
//            return f.mkdir();
//        } else {
//            return f.mkdirs();
//        }
//    }

    public void deleteTempFile(String filename) {
        File file = new File(getFilesDir() + "/" + filename);
        if (file.exists()) {
            file.delete();
        }
    }

    public boolean writeToFileInternally(String fileName, String text) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput(fileName, MODE_APPEND);
            fileOutputStream.write((text + "\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    private void openLog() {
        setSavedConnectionType(connectionType);
        setSavedHost(connectionDetails.getHost());
        Intent intent = new Intent(getApplicationContext(), LogActivity.class);
        intent.putExtra("connectiontype", connectionType);
        intent.putExtra("Host", connectionDetails.getHost());
        intent.putExtra("StartConnection", true);
        intent.putExtra("Useproxy", useProxy);
        switch (connectionType) {
            case 0:
                if (cusemanualproxy.isChecked()) {
                    setSavedProxy(connectionDetails.getProxy());
                    setSavedPort(connectionDetails.getPort());
                    intent.putExtra("Usemanualproxy", true);
                    intent.putExtra("Proxy", connectionDetails.getProxy());
                    intent.putExtra("Port", connectionDetails.getPort());
                } else {
                    intent.putExtra("Host", tiehost.getText().toString());
                }
                break;
            case 1:
                intent.putExtra("Filename", connectionDetails.getTempProxyFilename());
                break;
            default:
        }
        startActivity(intent);
    }

    private void openLogFrombt() {
        startActivity(new Intent(getApplicationContext(), LogActivity.class));
    }

    public void about(View view) {
        startActivity(new Intent(getApplicationContext(), About.class));
    }
}