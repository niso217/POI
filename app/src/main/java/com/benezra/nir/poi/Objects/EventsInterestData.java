package com.benezra.nir.poi.Objects;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Created by nirb on 07/11/2017.
 */

@IgnoreExtraProperties
public class EventsInterestData {

    private String interest;
    private String image;
    private String title;
    private String details;
    private String categories;


    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public EventsInterestData() {
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
