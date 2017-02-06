package me.mathusan.parkthevalley;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dan on 05/02/2017.
 */

public class MapHelper {
    public static String getStaticMapURL(LatLng src, int width, int height){
        return String.format("https://maps.googleapis.com/maps/api/staticmap" +
                        "?markers=color:red%%7C%f,%f" +
                        "&key=%s" +
                        "&size=%dx%d",
                src.latitude,
                src.longitude,
                "AIzaSyCINA8ocu7R-4mwBJm1Fyb77taUGSLWUEQ",
                width,
                height);
    }
}
