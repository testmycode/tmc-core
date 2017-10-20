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

    public String getName() {
        return name;
    }

    public String getInformation() {
        return information;
    }

    public String getSlug() {
        return slug;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public boolean isPinned() {
        return pinned;
    }
}
