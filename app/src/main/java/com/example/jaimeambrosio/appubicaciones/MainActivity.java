package com.example.jaimeambrosio.appubicaciones;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final static int GETLOCATION_PERMISO = 1;
    String proveedor;

    TextView txtLatitudGPS;
    TextView txtLongitudGPS;
    TextView txtLatitudNET;
    TextView txtLongitudNET;
    TextView txtVelocidadGPS;
    TextView txtVelocidadNET;
    EditText etLatitud;
    EditText etLongitud;


    LocationManager locationManager;
    MyLocationListener myLocationListener;
    Integer cantidad = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        //  BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_US_SOUTH); // Asegúrese de apuntar a su región
        // En este código de ejemplo, Analytics está configurado para registrar sucesos de ciclo de vida.
        //  Analytics.init(getApplication(), "AppUbicaciones", "82b1df4a-7135-443c-b465-bdd319388131", true, Analytics.DeviceEvent.ALL);
        //      Analytics.enable();
        myLocationListener = null;
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

        txtLatitudGPS = (TextView) findViewById(R.id.txtLatitudGPS);
        txtLongitudGPS = (TextView) findViewById(R.id.txtLongitudGPS);
        txtLatitudNET = (TextView) findViewById(R.id.txtLatitudNET);
        txtLongitudNET = (TextView) findViewById(R.id.txtLongitudNET);
        txtVelocidadGPS = (TextView) findViewById(R.id.txtVelocidadGPS);
        txtVelocidadNET = (TextView) findViewById(R.id.txtVelocidadNET);
        etLatitud = (EditText) findViewById(R.id.etLatitud);
        etLongitud = (EditText) findViewById(R.id.etLongitud);
        Button btnObtenerGPS = (Button) findViewById(R.id.btnObtenerGPS);
        btnObtenerGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation(LocationManager.GPS_PROVIDER);
            }
        });
        Button btnObtenerNET = (Button) findViewById(R.id.btnObtenerNET);
        btnObtenerNET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation(LocationManager.NETWORK_PROVIDER);
            }
        });

        Button btnVerGPS = (Button) findViewById(R.id.btnVerGPS);
        btnVerGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creates an Intent that will load a map of San Francisco
                String s = txtLatitudGPS.getText().toString();
                showGoogleMaps(txtLatitudGPS.getText().toString(), txtLongitudGPS.getText().toString());
            }
        });
        Button btnVerNET = (Button) findViewById(R.id.btnVerNET);
        btnVerNET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creates an Intent that will load a map of San Francisco
                showGoogleMaps(txtLatitudNET.getText().toString(), txtLongitudNET.getText().toString());
            }
        });
        Button btnmostrarmapa = (Button) findViewById(R.id.btnmostrarmapa);
        btnmostrarmapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGoogleMaps(etLatitud.getText().toString(), etLongitud.getText().toString());
            }
        });
        Button btnIrServicio = (Button) findViewById(R.id.btnIrServicio);
        btnIrServicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, PrincipalActivity.class);
                startActivity(i);
            }
        });
        Button btnSeguidor = (Button) findViewById(R.id.btnSeguidor);
        btnSeguidor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SeguimientoActivity.class);
                startActivity(i);
            }
        });
    }

    private void getLocation(String proveedor) {
        this.proveedor = proveedor;
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, GETLOCATION_PERMISO);

        } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GETLOCATION_PERMISO);
        } else {
            if (locationManager.isProviderEnabled(proveedor)) {
                if (myLocationListener == null) {
                    myLocationListener = new MyLocationListener();
                    locationManager.requestLocationUpdates(proveedor, 1000, 0.3f, myLocationListener);
                } else {
                    locationManager.removeUpdates(myLocationListener);
                    myLocationListener = null;
                }
            } else {
                Toast.makeText(this, "Proveedor desactivado", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.btnServicios) {
            Intent i = new Intent(MainActivity.this, PrincipalActivity.class);
            startActivity(i);

        }if (id == R.id.btnSeg) {
            Intent i = new Intent(MainActivity.this, SeguimientoActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case GETLOCATION_PERMISO: {
                    getLocation(proveedor);
                    break;
                }
                default: {
                    break;
                }
            }

        } else {
            Toast.makeText(this, "Sin permisos", Toast.LENGTH_LONG).show();
        }

    }

    private void showGoogleMaps(String latitude, String longitud) {
        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitud + "?z=21&q=" + latitude + "," + longitud + "(Marcador)");
        //   Uri gmmIntentUri = Uri.parse("geo:" + latit + "," + longitud + "?z=15&q=Marcador");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (proveedor.equals(LocationManager.GPS_PROVIDER)) {
                if (location != null) {
                    txtLatitudGPS.setText(String.valueOf(location.getLatitude()));
                    txtLongitudGPS.setText(String.valueOf(location.getLongitude()));

                    float v = location.getSpeed();
                    float v2 = v * 3.6f;
                    txtVelocidadGPS.setText(location.hasSpeed() + " -> " + v + " m/s -- " + v2 + " km/h");
                } else {
                    txtLatitudGPS.setText("");
                    txtLongitudGPS.setText("");
                    txtVelocidadGPS.setText("");
                }
            } else {
                if (location != null) {
                    txtLatitudNET.setText(String.valueOf(location.getLatitude()));
                    txtLongitudNET.setText(String.valueOf(location.getLongitude()));
                    float v = location.getSpeed();
                    float v2 = v * 3.6f;
                    txtVelocidadNET.setText(location.hasSpeed() + " -> " + v + " m/s -- " + v2 + " km/h");
                } else {
                    txtLatitudNET.setText("");
                    txtLongitudNET.setText("");
                    txtVelocidadGPS.setText("");
                }
            }


        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    @Override
    protected void onDestroy() {

        //  Analytics.send(new ResponseListener() {
        //       @Override
        //       public void onSuccess(Response response) {
        //       }

        //        @Override
        //        public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
        //       }
        //    });
        super.onDestroy();
    }
}
