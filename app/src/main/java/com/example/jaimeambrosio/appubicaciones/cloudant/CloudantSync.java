package com.example.jaimeambrosio.appubicaciones.cloudant;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DatastoreNotCreatedException;
import com.cloudant.sync.datastore.DocumentBodyFactory;
import com.cloudant.sync.datastore.DocumentException;
import com.cloudant.sync.datastore.DocumentRevision;
import com.cloudant.sync.event.Subscribe;
import com.cloudant.sync.notifications.ReplicationCompleted;
import com.cloudant.sync.notifications.ReplicationErrored;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;
import com.example.jaimeambrosio.appubicaciones.R;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Creado por jaimeambrosio fecha: 27/12/2016.
 */

public class CloudantSync {

    private static String DATASTORE_MANGER_DIR = "DATA_LOCAL";
    private static String TASKS_DATASTORE_NAME = "DB_UBICACIONES_LOCAL";
    private Datastore datastore;
    private Replicator pushReplicator;
    private Replicator pullReplicator;

    public CloudantSync(Context context) {
        File path = context.getDir(DATASTORE_MANGER_DIR, Context.MODE_PRIVATE);
        DatastoreManager manager = DatastoreManager.getInstance(path.getAbsolutePath());
        try {
            this.datastore = manager.openDatastore(TASKS_DATASTORE_NAME);
        } catch (DatastoreNotCreatedException e) {
            e.printStackTrace();
        }

        String username = context.getResources().getString(R.string.cloudant_username);
        String dbName = context.getResources().getString(R.string.cloudant_dbname);
        String apiKey = context.getResources().getString(R.string.cloudant_api_key);
        String apiSecret = context.getResources().getString(R.string.cloudant_api_password);
        String host = username + ".cloudant.com";

        URI uri = null;
        try {
            uri = new URI("https", apiKey + ":" + apiSecret, host, 443, "/" + dbName, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        pullReplicator = ReplicatorBuilder.pull().to(datastore).from(uri).build();
        pushReplicator = ReplicatorBuilder.push().from(datastore).to(uri).build();

        pullReplicator.getEventBus().register(this);
        pushReplicator.getEventBus().register(this);
    }

    public DocumentRevision crearSeguimiento(String nombre) {
        DocumentRevision rev = new DocumentRevision();
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombre);
        map.put("fecha", new Date());
        map.put("seg", new ArrayList<Double>());
        rev.setBody(DocumentBodyFactory.create(map));
        try {
            rev = datastore.createDocumentFromRevision(rev);
        } catch (DocumentException e) {
        }
        return rev;
    }

    public void startPushReplication() {
        if (this.pushReplicator != null) {
            this.pushReplicator.start();
        }
    }

    public void startPullReplication() {
        if (this.pullReplicator != null) {
            this.pullReplicator.start();
        }
    }

    public DocumentRevision updateDocumentFromRevision(DocumentRevision revision) {
        try {
            return datastore.updateDocumentFromRevision(revision);
        } catch (DocumentException e) {
            return null;
        }
    }

    @Subscribe
    public void complete(ReplicationCompleted rc) {
        Log.d("compelete", rc.toString());
    }

    @Subscribe
    public void error(ReplicationErrored re) {
        Log.d("error", re.toString());
    }
}
