package com.example.jaimeambrosio.appubicaciones;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jaimeambrosio.appubicaciones.Services.Constants;
import com.example.jaimeambrosio.appubicaciones.Services.EjemploService;
import com.example.jaimeambrosio.appubicaciones.Services.UbicacionesService;
import com.example.jaimeambrosio.appubicaciones.cloudant.CloudantSync;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Map;

public class SeguimientoActivity extends AppCompatActivity implements OnMapReadyCallback {


    CloudantSync cloudant;
    private GoogleMap map;
    LocationManager locationManager;
    final static int PERMISO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
    }


    private void init() {
        // etNombre = (EditText) findViewById(R.id.etNombre);
        map = null;
        cloudant = new CloudantSync(getApplicationContext());
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        Button btnIniciarSeg = (Button) findViewById(R.id.btnIniciarSeg);
        btnIniciarSeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SeguimientoActivity.this);
                LayoutInflater inflater = SeguimientoActivity.this.getLayoutInflater();
                View v = inflater.inflate(R.layout.modal_seguimiento, null);
                final EditText etNombre = (EditText) v.findViewById(R.id.etNombre);
                builder.setView(v)
                        .setTitle("Información requerida")
                        .setPositiveButton("CREAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(SeguimientoActivity.this, UbicacionesService.class);
                                i.putExtra("nombre", etNombre.getText().toString().trim());
                                startService(i);
                            }
                        })
                        .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                Dialog dialog = builder.create();
                dialog.show();


            }
        });
        Button btnDetenerSeg = (Button) findViewById(R.id.btnDetenerSeg);
        btnDetenerSeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(SeguimientoActivity.this, UbicacionesService.class));
            }
        });
        Button btnPush = (Button) findViewById(R.id.btnPush);
        btnPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloudant.startPushReplication();
            }
        });
        Button btnPull = (Button) findViewById(R.id.btnPull);
        btnPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloudant.startPullReplication();
            }
        });


        IntentFilter filter = new IntentFilter(Constants.UBI);
        SeguimientoReceiver receiver =
                new SeguimientoReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver,
                filter);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else
            map.setMyLocationEnabled(true);
        //  CameraUpdate ZoomCam = CameraUpdateFactory.zoomTo(14);
        //map.animateCamera(ZoomCam);
        /*
        UiSettings sg = map.getUiSettings();
        sg.setCompassEnabled(true);
        PolylineOptions sudamericaRect = new PolylineOptions()
                .add(new LatLng(12.897489, -82.441406)) // P1
                .add(new LatLng(12.897489, -32.167969)) // P2
                .add(new LatLng(-55.37911, -32.167969)) // P3
                .add(new LatLng(-55.37911, -82.441406)) // P4
                .add(new LatLng(12.897489, -82.441406)) // P1
                .color(Color.parseColor("#f44336"));    // Rojo 500
        Polyline polyline = map.addPolyline(sudamericaRect);
        LatLng lng = new LatLng(-20.96144, -61.347656);
        map.addMarker(new MarkerOptions()
                .position(lng)
                .title("Cali la Sucursal del cielo"));

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(lng,3));
        */

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> list = locationManager.getAllProviders();
        for (String p : list) {
            Location l = locationManager.getLastKnownLocation(p);
            if (l != null) {
                LatLng lng = new LatLng(l.getLatitude(), l.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(lng)      // Sets the center of the map to Mountain View
                        .zoom(16)                   // Sets the zoom
                        //     .bearing(0)                // Sets the orientation of the camera to east
                        //   .tilt(80)                 // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                break;
            }
        }
    }

    private class SeguimientoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.UBI)) {
                String i = intent.getStringExtra("valor");
            }
        }
    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            LatLng lng = new LatLng(location.getLatitude(), location.getLongitude());
            // map.animateCamera(CameraUpdateFactory.newLatLngZoom(lng, 10));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(lng)      // Sets the center of the map to Mountain View
                    .zoom(16)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(80)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            } else
                locationManager.removeUpdates(MyLocationListener.this);
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

}
