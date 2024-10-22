package com.movtery.zalithlauncher.feature.login;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AuthResult {
    @SerializedName("accessToken")
    private String accessToken;
    @SerializedName("clientToken")
    private String clientToken;
    @SerializedName("availableProfiles")
    private List<AvailableProfiles> availableProfiles;
    @SerializedName("user")
    private User user;
    @SerializedName("selectedProfile")
    private SelectedProfile selectedProfile;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public List<AvailableProfiles> getAvailableProfiles() {
        return availableProfiles;
    }

    public void setAvailableProfiles(List<AvailableProfiles> availableProfiles) {
        this.availableProfiles = availableProfiles;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SelectedProfile getSelectedProfile() {
        return selectedProfile;
    }

    public void setSelectedProfile(SelectedProfile selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public static class User {
        @SerializedName("id")
        private String id;
        @SerializedName("properties")
        private List<?> properties;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<?> getProperties() {
            return properties;
        }

        public void setProperties(List<?> properties) {
            this.properties = properties;
        }
    }

    public static class SelectedProfile {
        @SerializedName("id")
        private String id;
        @SerializedName("name")
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class AvailableProfiles {
        @SerializedName("id")
        private String id;
        @SerializedName("name")
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}