package com.semcontas.server.domain;

import com.google.appengine.api.datastore.*;
import com.semcontas.server.entities.Result;
import com.semcontas.server.entities.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class UserManager {

    private static final String USER_KIND = "User";
    private static final int FETCH_LIMIT = 10;
    private Logger log = Logger.getLogger(UserManager.class.getName());

    public User entityToUser(Entity entity) {
        return new User.Builder()
                .id(entity.getKey().getId())
                .username((String)entity.getProperty(User.USERNAME))
                .firstName((String)entity.getProperty(User.FIRST_NAME))
                .lastName((String)entity.getProperty(User.LAST_NAME))
                .email((String)entity.getProperty(User.EMAIL))
                .build();
    }

    public Long createUser(User user) {
        Entity userEntity = new Entity(USER_KIND);
        userEntity.setProperty(User.USERNAME, user.getUsername());
        userEntity.setProperty(User.FIRST_NAME, user.getFirstName());
        userEntity.setProperty(User.LAST_NAME, user.getLastName());
        userEntity.setProperty(User.EMAIL, user.getEmail());
        Key userKey = DatastoreManager.datastore().put(userEntity);
        return userKey.getId();
    }

    public User readUser(Long userId) {
        try {
            Entity userEntity = DatastoreManager.datastore().get(KeyFactory.createKey(USER_KIND, userId));
            return entityToUser(userEntity);
        } catch (EntityNotFoundException ex) {
            log.severe("Entity USER with id = " + userId + " not found");
            return null;
        }
    }

    public void updateUser(User user) {
        Key key = KeyFactory.createKey(USER_KIND, user.getId());
        Entity entity = new Entity(key);
        entity.setProperty(User.USERNAME, user.getUsername());
        entity.setProperty(User.FIRST_NAME, user.getFirstName());
        entity.setProperty(User.LAST_NAME, user.getLastName());
        entity.setProperty(User.EMAIL, user.getEmail());
        DatastoreManager.datastore().put(entity);
    }

    public void deleteUser(Long userId) {
        Key key = KeyFactory.createKey(USER_KIND, userId);
        DatastoreManager.datastore().delete(key);
    }

    public List<User> entitiesToUsers(Iterator<Entity> results) {
        List<User> resultUsers = new ArrayList<>();
        while (results.hasNext())
            resultUsers.add(entityToUser(results.next()));
        return resultUsers;
    }

    public Result<User> listUsers(String startCursorString) {
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(FETCH_LIMIT);
        if (startCursorString != null && !startCursorString.equals(""))
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursorString));  // where we left off
        Query query = new Query(USER_KIND)
                .addSort(User.FIRST_NAME, Query.SortDirection.ASCENDING)
                .addSort(User.LAST_NAME, Query.SortDirection.ASCENDING);
        PreparedQuery preparedQuery = DatastoreManager.datastore().prepare(query);
        QueryResultIterator<Entity> results = preparedQuery.asQueryResultIterator(fetchOptions);
        List<User> resultUsers = entitiesToUsers(results);
        Cursor cursor = results.getCursor();                    // Where to start next time
        if (cursor != null && resultUsers.size() == FETCH_LIMIT) {      // Are we paging? Save Cursor
            String cursorString = cursor.toWebSafeString();
            return new Result<>(resultUsers, cursorString);
        } else {
            return new Result<>(resultUsers);
        }
    }
}
