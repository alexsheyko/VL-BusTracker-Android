package ru.vlbustracker.helpers;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Шейко on 07.10.14.
 */

public class BusItem implements ClusterItem {
    private final LatLng mPosition;

    public BusItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}