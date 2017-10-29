package com.benezra.nir.poi.Objects;

/**
 * Created by nirb on 29/10/2017.
 */

public class UploadInfo {

    public UploadInfo() {

    }

    public String url;



    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UploadInfo(String name, String url) {
        this.url = url;
    }
}