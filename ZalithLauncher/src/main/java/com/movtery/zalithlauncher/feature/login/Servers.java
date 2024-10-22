package com.movtery.zalithlauncher.feature.login;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Servers {

    @SerializedName("server")
    private List<Server> server;
    @SerializedName("info")
    private String info;

    public List<Server> getServer() {
        return server;
    }

    public void setServer(List<Server> server) {
        this.server = server;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public static class Server {
        @SerializedName("baseUrl")
        private String baseUrl;
        @SerializedName("serverName")
        private String serverName;
        @SerializedName("register")
        private String register;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getRegister() {
            return register;
        }

        public void setRegister(String register) {
            this.register = register;
        }
    }
}