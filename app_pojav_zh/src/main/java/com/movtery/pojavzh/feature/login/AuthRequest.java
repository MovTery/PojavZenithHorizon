package com.movtery.pojavzh.feature.login;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("agent")
    private Agent agent;
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
    @SerializedName("clientToken")
    private String clientToken;
    @SerializedName("requestUser")
    private Boolean requestUser;

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public Boolean getRequestUser() {
        return requestUser;
    }

    public void setRequestUser(Boolean requestUser) {
        this.requestUser = requestUser;
    }

    public static class Agent {
        @SerializedName("name")
        private String name;
        @SerializedName("version")
        private Double version;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getVersion() {
            return version;
        }

        public void setVersion(Double version) {
            this.version = version;
        }
    }
}