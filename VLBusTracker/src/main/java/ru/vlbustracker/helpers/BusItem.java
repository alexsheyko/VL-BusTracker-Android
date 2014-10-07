package ru.vlbustracker.helpers;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import ru.vlbustracker.models.Bus;

/**
 * Created by Шейко on 07.10.14.
 */

public class BusItem implements ClusterItem {
    //private final LatLng mPosition;
    public final Bus mBus;

    public BusItem(double lat, double lng, Bus bus) {
        //mPosition = new LatLng(lat, lng);
        mBus = bus;
    }

    @Override
    public LatLng getPosition() {
        return mBus.getLocation();
        //return mPosition;
    }

}

