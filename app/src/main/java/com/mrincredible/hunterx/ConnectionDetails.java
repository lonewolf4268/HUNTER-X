package com.mrincredible.hunterx;

public class ConnectionDetails {
    private String host;
    private String proxy;
    private int port;
    private String okResults = "200 OK results";
    private String tempResult = "tempResult.txt";
    private String tempProxyFilename = "tempProxyFile.txt";
    private String detailsBefore = "detailsBefore.txt";
    private boolean startConnection;
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getTempProxyFilename() {
        return tempProxyFilename;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTempResult() {
        return tempResult;
    }

    public String getDetailsBefore() {
        return detailsBefore;
    }

    public String getOkResults() {
        return okResults;
    }

    public void splitProxyPort(String pp) {
        String trim = pp.trim();
        if (trim.contains(":")) {
            if (!trim.endsWith(":")) {
                String[] split = trim.split(":");
                setProxy(split[0]);
                setPort(Integer.parseInt(split[1]));
            }
        } else {
            setProxy(trim);
            setPort(80);
        }
    }

    /**
    * 0 = no proxyfile
    * 1 = proxyfile
    * 2 = automode
    * */

    public boolean isStartConnection() {
        return startConnection;
    }

    public void setStartConnection(boolean startConnection) {
        this.startConnection = startConnection;
    }
}
