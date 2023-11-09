package com.mrincredible.hunterx;

public class ConnectionSettings {
    private String host;
    private String proxy;
    private int port;
    private final String hostRegex = "^[a-zA-Z0-9]+([\\-\\.]{1}[a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}$";
    private final String httpRegex = "^(http?://)";
    private final String proxyPortRegex = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\:[0-9]{1,5}";
    private String reason = "null";
    private ConnectionDetails connectionDetails = new ConnectionDetails();

    public String getHostRegex() {
        return hostRegex;
    }

    public String getHttpRegex() {
        return httpRegex;
    }

    public String getProxyPortRegex() {
        return proxyPortRegex;
    }

    public boolean matchHostRegex(String tieHost) {
        if (tieHost.length() > 3) {
            if (!tieHost.matches(httpRegex)) {
                if (tieHost.matches(hostRegex)) {
                    reason = "Valid";
                    return true;
                } else {
                    reason = "Invalid Host";
                }
            } else {
                reason = "Remove http:// or https://";
            }
        } else {
            reason = "Invalid Host";
        }
        return false;
    }

    public boolean checkProxyPort(String proxyandport) {
        if (proxyandport.contains(":")) {
            int proxylength = Length(proxyandport, 1);
            int portlength = Length(proxyandport, 2);

            //"min proxy = 0.0.0.0 and max proxy = 125.125.125.125"
            if (!(proxylength > 15)) {
                if (!(proxylength < 7)) {
                    if (!(portlength > 5)) {
                        if (!(portlength < 0)) {
                            if (isVailidNum(proxyandport)) {
                                reason = "Valid";
                                return true;
                            } else {
                                reason = "Invalid Proxy";
                            }
                        } else {
                            reason = "Min port = 0";
                        }
                    } else {
                        reason = "Max port = 65535";
                    }
                } else {
                    reason = "Min proxy = 0.0.0.0";
                }
            } else {
                reason = "Max proxy = 255.255.255.255";
            }
        }

        return false;
    }

    private boolean isVailidNum(String a) {
        if (a.contains(":")){
            String[] b = a.trim().split("\\.");
            String[] c = b[3].split(":");

            if ((Integer.parseInt(b[0]) >= 0) && (Integer.parseInt(b[0]) <= 255)) {
                if ((Integer.parseInt(b[1]) >= 0) && (Integer.parseInt(b[1]) <= 255)) {
                    if ((Integer.parseInt(b[2]) >= 0) && (Integer.parseInt(b[2]) <= 255)) {
                        if ((Integer.parseInt(c[0]) >= 0) && (Integer.parseInt(c[0]) <= 255)) {
                            if ((Integer.parseInt(c[1]) >= 0) && (Integer.parseInt(c[1]) <= 65535)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }else {
            String[] b = a.trim().split("\\.");
            if ((Integer.parseInt(b[0]) >= 0) && (Integer.parseInt(b[0]) <= 255)) {
                if ((Integer.parseInt(b[1]) >= 0) && (Integer.parseInt(b[1]) <= 255)) {
                    if ((Integer.parseInt(b[2]) >= 0) && (Integer.parseInt(b[2]) <= 255)) {
                        if ((Integer.parseInt(b[3]) >= 0) && (Integer.parseInt(b[3]) <= 255)) {
                            return true;

                        }
                    }
                }
            }
        }

        return false;
    }

    public int Length(String a, int proxy_or_port) {
        if (a.contains(":")) {
            String[] b = a.trim().split(":");
            String c = b[0];
            String d = b[1];
            switch (proxy_or_port) {
                case 1:
                    return c.length();
                case 2:
                    return d.length();
                default:
                    return 0;
            }
        }else {
            switch (proxy_or_port) {
                case 1:
                    return a.length();
                default:
                    return 0;
            }
        }
    }

    public String getReason() {
        return reason;
    }

    public boolean checkProxyInFile (String text){
        if (!text.equals(null)) {
            boolean canconnect;
            text = text.trim();
            if (!(text.length() > 15)) {
                if (!text.matches(getHttpRegex())) {
                    if (!text.matches(getHostRegex())) {
                        if (text.matches(getProxyPortRegex())) {
                            canconnect = checkProxyPort(text);
                            return canconnect;
                        }
                    }
                }
            }
        }
        return false;
    }

}
