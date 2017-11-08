package fi.helsinki.cs.tmc.core.domain;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Organization implements Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("information")
    private String information;

    @SerializedName("slug")
    private String slug;

    @SerializedName("logo_path")
    private String logoPath;

    @SerializedName("pinned")
    private boolean pinned;

    public Organization() {

    }

    public Organization(String name, String information, String slug, String logoPath, boolean pinned) {
        this.name = name;
        this.information = information;
        this.slug = slug;
        this.logoPath = logoPath;
        this.pinned = pinned;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getInformation() {
        return information;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isPinned() {
        return pinned;
    }
}
