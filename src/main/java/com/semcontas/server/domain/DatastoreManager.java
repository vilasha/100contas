package com.semcontas.server.domain;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class DatastoreManager {
    private static DatastoreService service;

    public static DatastoreService getDatastoreService() {
        if (service == null)
            service = DatastoreServiceFactory.getDatastoreService();
        return service;
    }
}
