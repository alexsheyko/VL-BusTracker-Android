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
import ru.vlbustracker.models.Stop;

/**
 * Created by Шейко on 07.10.14.
 */

public class BusItem implements ClusterItem {
    private LatLng mPosition;
    public final Bus Bus;
    public final Stop Stop;
    public Integer type;

    public BusItem(Bus mBus, Stop mStop) {
        //mPosition = new LatLng(lat, lng);
        //type = 0;
        if (mBus!=null){
            Bus = mBus;
            type = 1;
            Stop = null;
        }else {
            if (mStop != null) {
                Bus = null;
                type = 2;
                Stop = mStop;
            }else{
                Bus = null;
                type = 0;
                Stop = null;
            }
        }
        mPosition = null;

    }

    @Override
    public LatLng getPosition() {
        return Bus.getLocation();
        //return mPosition;
    }


    public void setPosition(LatLng mPos) {
        mPosition = mPos;
    }

}

