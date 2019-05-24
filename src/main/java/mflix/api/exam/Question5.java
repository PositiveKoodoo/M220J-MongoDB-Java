package mflix.api.exam;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class Question5 {

    public static void main(String[] args) {

        MongoClient mongoClient = MongoClients.create("mongodb+srv://m220student:m220password@mflix-g7fg7.mongodb.net");
        MongoDatabase db = mongoClient.getDatabase("mflix");
        MongoCollection employeesCollection =
                db.getCollection("employees");

        Document doc1 = new Document("_id", 11)
                .append("name", "Edgar Martinez")
                .append("salary", "8.5M");
        Document doc2 = new Document("_id", 3)
                .append("name", "Alex Rodriguez")
                .append("salary", "18.3M");
        Document doc3 = new Document("_id", 24)
                .append("name", "Ken Griffey Jr.")
                .append("salary", "12.4M");
        Document doc4 = new Document("_id", 11)
                .append("name", "David Bell")
                .append("salary", "2.5M");
        Document doc5 = new Document("_id", 19)
                .append("name", "Jay Buhner")
                .append("salary", "5.1M");

        List<WriteModel> requests = Arrays.asList(
                new InsertOneModel<>(doc1),
                new InsertOneModel<>(doc2),
                new InsertOneModel<>(doc3),
                new InsertOneModel<>(doc4),
                new InsertOneModel<>(doc5));
        try {
            employeesCollection.bulkWrite(requests);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.toString());
        }

    }
}
