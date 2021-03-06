package mflix.api.daos;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import mflix.api.models.Session;
import mflix.api.models.User;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.MessageFormat;
import java.util.Map;

import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class UserDao extends AbstractMFlixDao {

  private final MongoCollection<User> usersCollection;

  private final MongoCollection<Session> sessionsCollection;

  private final Logger log;

  @Autowired
  public UserDao(
      MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
    super(mongoClient, databaseName);
    CodecRegistry pojoCodecRegistry =
        fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    usersCollection = db.getCollection("users", User.class).withCodecRegistry(pojoCodecRegistry);
    log = LoggerFactory.getLogger(this.getClass());

    sessionsCollection = db.getCollection("sessions", Session.class).withCodecRegistry(pojoCodecRegistry);
  }

  /**
   * Inserts the `user` object in the `users` collection.
   *
   * @param user - User object to be added
   * @return True if successful, throw IncorrectDaoOperation otherwise
   */
  public boolean addUser(User user) {
    if(getUser(user.getEmail())==null){
      throw new IncorrectDaoOperation("User already exists");
    }

    try {
      usersCollection.withWriteConcern(WriteConcern.MAJORITY).insertOne(user);
    } catch (Exception e) {
      throw new IncorrectDaoOperation(e.getMessage());
    }
    return true;

  }

  /**
   * Creates session using userId and jwt token.
   *
   * @param userId - user string identifier
   * @param jwt - jwt string token
   * @return true if successful
   */
  public boolean createUserSession(String userId, String jwt) {

    if(sessionsCollection.find(Filters.eq("jwt",jwt))!=null){
      throw new IncorrectDaoOperation("User session exists");
    }

    Bson updateFilter = new Document("user_id", userId);
    Bson setUpdate = Updates.set("jwt", jwt);
    UpdateOptions options = new UpdateOptions().upsert(true);
    sessionsCollection.updateOne(updateFilter, setUpdate, options);
    return true;
  }

  /**
   * Returns the User object matching the an email string value.
   *
   * @param email - email string to be matched.
   * @return User object or null.
   */
  public User getUser(String email) {
    return usersCollection.find(new Document("email", email)).limit(1).first();
  }

  /**
   * Given the userId, returns a Session object.
   *
   * @param userId - user string identifier.
   * @return Session object or null.
   */
  public Session getUserSession(String userId) {
    return sessionsCollection.find(new Document("user_id", userId)).limit(1).first();
  }

  public boolean deleteUserSessions(String userId) {
    Document sessionDeleteFilter = new Document("user_id", userId);
    DeleteResult res = sessionsCollection.deleteOne(sessionDeleteFilter);
    if (res.getDeletedCount() < 1) {
      log.warn("User `{}` could not be found in sessions collection.", userId);
    }

    return res.wasAcknowledged();
  }

  /**
   * Removes the user document that match the provided email.
   *
   * @param email - of the user to be deleted.
   * @return true if user successfully removed
   */
  public boolean deleteUser(String email) {
    // remove user sessions
    if (deleteUserSessions(email)) {
      Document userDeleteFilter = new Document("email", email);

      DeleteResult res = null;
      try {
        res = usersCollection.deleteOne(userDeleteFilter);
      } catch (Exception e) {
        return false;
      }

      if (res.getDeletedCount() < 0) {
        log.warn("User with `email` {} not found. Potential concurrent operation?!");
      }

      return res.wasAcknowledged();
    }
    return false;
  }

  /**
   * Updates the preferences of an user identified by `email` parameter.
   *
   * @param email - user to be updated email
   * @param userPreferences - set of preferences that should be stored and replace the existing
   *     ones. Cannot be set to null value
   * @return User object that just been updated.
   */
  public boolean updateUserPreferences(String email, Map<String, ?> userPreferences) {
    // make sure to check if userPreferences are not null. If null, return false immediately.
    if(userPreferences == null){
      throw new IncorrectDaoOperation("userPreferences cannot be set to null");
    }
    // create query filter and update object.
    Bson updateFilter = new Document("email", email);
    Bson updateObject = Updates.set("preferences", userPreferences);
    // update one document matching email.
    UpdateResult res = null;
    try {
      res = usersCollection.updateOne(updateFilter, updateObject);
    } catch (Exception e) {
      throw new IncorrectDaoOperation(e.getMessage());
    }
    if(res.getModifiedCount() < 1){
      log.warn("User `{}` was not updated. Trying to re-write the same `preferences` field: `{}`",
              email, userPreferences);
    }
    return true;

    //TODO > Ticket: Handling Errors - make this method more robust by
    // handling potential exceptions when updating an entry.
  }
}
