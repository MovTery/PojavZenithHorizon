package com.movtery.pojavzh.feature.login;

import com.google.gson.annotations.SerializedName;

//https://github.com/Vera-Firefly/Pojav-Glow-Worm/commit/933dcd1d275616d21fb2bccacbfbfc174b785333
public class Refresh {
    @SerializedName("selectedProfile")
    private SelectedProfile selectedProfile;
    @SerializedName("accessToken")
    private String accessToken;
    @SerializedName("clientToken")
    private String clientToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public SelectedProfile getSelectedProfile() {
        return selectedProfile;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public void setSelectedProfile(SelectedProfile selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public static class SelectedProfile {
        @SerializedName("name")
        private String name;
        @SerializedName("id")
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}