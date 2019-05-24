package mflix.api.exam;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.SslSettings;

public class Question3 {

    public static void main(String[] args) {
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString("mongodb+srv://m220student:m220password@mflix-g7fg7.mongodb.net")).build();
        MongoClient mongoClient = MongoClients.create(settings);

// TODO do a read on the cluster to ensure you are connected

        SslSettings sslSettings = settings.getSslSettings();
        ReadPreference readPreference = settings.getReadPreference();
        ReadConcern readConcern = settings.getReadConcern();
        WriteConcern writeConcern = settings.getWriteConcern();

        System.out.println("SSLSettings: isInvalidHostNameAllowed: "+ sslSettings.isInvalidHostNameAllowed());
        System.out.println("SSLSettings: isEnabled: "+ sslSettings.isEnabled());
        System.out.println("ReadConcern: "+readConcern.asDocument().toString());
        System.out.println("WriteConcern: "+writeConcern.asDocument().toString());
        System.out.println("ReadPreference: "+readPreference.toString());
    }
}
