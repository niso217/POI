package com.benezra.nir.poi.Interface;

/**
 * Created by nir on 09/10/2017.
 */

public interface Constants {

    public static final String EVENT_ID = "event_id";
    public static final String EVENT_OWNER = "event_owner";
    public static final String EVENT_DETAILS = "event_details";
    public static final String EVENT_INTEREST = "event_interest";
    public static final String EVENT_LATITUDE = "event_latitude";
    public static final String EVENT_LONGITUDE = "event_longitude";
    public static final String EVENT_START = "event_start";
    public static final String EVENT_IMAGE = "event_image";
    public static final String EVENT_TITLE = "event_title";
    public static final String EVENT_ADDRESS = "event_address";

    public static final String DETAILS = "details";
    public static final String END = "end";
    public static final String IMAGE = "image";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String START = "start";
    public static final String INTEREST = "interest";
    public static final String ADDRESS = "address";
    public static final String PARTICIPATES = "participates";
    public static final String OWNER = "owner";
    public static final String ID = "id";
    public static final String URI = "uri";

    public static final String ACTION = "action";
    public static final int ACTION_REMOVE = 1;
    public static final int ACTION_FINISH = 0;

    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String OPTIONS = "options";


    //permissions type
    public static final String USER_LOCATION = "user_location";
    public static final String LOCATION = "location";
    public static final String CAMERA = "camera";

    //transportation type
    public static final String DRIVING = "driving";
    public static final String WALKING = "walking";
    public static final String CYCLING = "cycling";

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";

}