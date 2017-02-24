package fi.helsinki.cs.tmc.core.domain;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OauthCredentials implements Serializable {

    @SerializedName("application_id")
    private String oauthApplicationId;

    @SerializedName("secret")
    private String oauthSecret;

    public OauthCredentials() {

    }

    public void setOauthApplicationId(String oauthApplicationId) {
        this.oauthApplicationId = oauthApplicationId;
    }

    public String getOauthApplicationId() {
        return oauthApplicationId;
    }

    public void setOauthSecret(String oauthSecret) {
        this.oauthSecret = oauthSecret;
    }

    public String getOauthSecret() {
        return oauthSecret;
    }
}
