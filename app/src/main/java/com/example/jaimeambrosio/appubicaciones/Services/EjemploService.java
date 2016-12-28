package com.example.jaimeambrosio.appubicaciones.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.example.jaimeambrosio.appubicaciones.PrincipalActivity;
import com.example.jaimeambrosio.appubicaciones.R;

import java.util.Date;

/**
 * Creado por jaimeambrosio fecha: 26/12/2016.
 */

public class EjemploService extends Service {
    MyTask myTask;
    Integer numero;
    NotificationCompat.Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Servicio creado!", Toast.LENGTH_SHORT).show();
        myTask = new MyTask();
        numero = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myTask.execute();
        Toast.makeText(this, "starcommand!", Toast.LENGTH_SHORT).show();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Servicio destruído!", Toast.LENGTH_SHORT).show();
        myTask.cancel(true);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MyTask extends AsyncTask<String, Integer, String> {
        boolean cent;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            cent = true;
            lauchNotification();
        }

        @Override
        protected String doInBackground(String... params) {
            while (cent) {
                try {
                    ++numero;
                    publishProgress(numero);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //  Toast.makeText(getApplicationContext(), values[0].toString(), Toast.LENGTH_SHORT).show();
            builder.setContentText(values[0].toString());
            startForeground(1, builder.build());

            Intent localIntent = new Intent(Constants.PROGRESO)
                    .putExtra("valor", values[0]);

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
            if (values[0] >= 100)
                stopSelf();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            cent = false;
        }
    }

    private void lauchNotification() {
        Intent notificationIntent = new Intent(this, PrincipalActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.empresa)
                .setContentTitle("Servicio en segundo plano")
                .setContentText("Procesando...").setContentIntent(pendingIntent);
        startForeground(1, builder.build());
    }
}
