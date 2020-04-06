package com.semcontas.server.domain;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.semcontas.server.entities.User;

import java.util.logging.Logger;

public class UserManager {

    private static final String USER_KIND = "User";
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
}
