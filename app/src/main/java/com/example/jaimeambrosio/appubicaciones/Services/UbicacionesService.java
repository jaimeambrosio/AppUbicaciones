package com.example.jaimeambrosio.appubicaciones.Services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.cloudant.sync.datastore.DocumentBodyFactory;
import com.cloudant.sync.datastore.DocumentRevision;
import com.example.jaimeambrosio.appubicaciones.PrincipalActivity;
import com.example.jaimeambrosio.appubicaciones.R;
import com.example.jaimeambrosio.appubicaciones.SeguimientoActivity;
import com.example.jaimeambrosio.appubicaciones.cloudant.CloudantSync;

import java.util.ArrayList;
import java.util.Map;

/**
 * Creado por jaimeambrosio fecha: 27/12/2016.
 */

public class UbicacionesService extends Service {

    ArrayList<Double> values;
    CloudantSync cloudant;
    LocationManager locationManager;
    MyLocationListener listener;
    DocumentRevision revision;
    NotificationCompat.Builder builder;


    @Override
    public void onCreate() {
        super.onCreate();
        cloudant = new CloudantSync(getApplicationContext());
        values = new ArrayList<>();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String nombre = intent.getStringExtra("nombre");
        revision = cloudant.crearSeguimiento(nombre);
        int minTime = 1000;
        float minDistance = 20;
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "sin permisos", Toast.LENGTH_SHORT).show();
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);
            lauchNotification();
        }
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else
            locationManager.removeUpdates(listener);
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location l) {
            values.add(l.getLatitude());
            values.add(l.getLongitude());
            values.add((double) l.getSpeed());

            Map<String, Object> map = revision.getBody().asMap();
            map.put("seg", values);
            revision.setBody(DocumentBodyFactory.create(map));
            revision = cloudant.updateDocumentFromRevision(revision);

            String valor = l.getLongitude() + ";" + l.getLatitude() + ":" + l.getSpeed() + " m/s";
            builder.setContentText(valor);
            startForeground(1, builder.build());

            Intent localIntent = new Intent(Constants.UBI)
                    .putExtra("valor", valor);

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private void lauchNotification() {
        Intent notificationIntent = new Intent(this, SeguimientoActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.empresa)
                .setContentTitle("Te estamos siguiendo")
                .setContentText("Buscando...").setContentIntent(pendingIntent);
        startForeground(1, builder.build());

    }
}
