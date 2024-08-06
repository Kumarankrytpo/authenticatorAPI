package com.krypto.connection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoConnection {
    MongoClient mongodb=null;
    public void MongoConnection(){
        mongodb = MongoClients.create("mongodb://localhost:27017");
    }

    public MongoClient getMongoConnection(){
        return mongodb;
    }
}
