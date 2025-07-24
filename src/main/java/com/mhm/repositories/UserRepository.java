package com.mhm.repositories;

import com.mhm.entities.UserEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {

    // Save a User
    public Uni<UserEntity> createUser(UserEntity user) {
        return persist(user);
    }

    // Find a User by ID
    public Uni<UserEntity> findUserById(Long id) {
        return findById(id);
    }

    // Find all Users
    public Uni<List<UserEntity>> findAllUsers() {
        return findAll().list();
    }

    // Find a User by name
    public Uni<UserEntity> findUserByName(String name) {
        return find("name", name).firstResult();
    }

    // Delete a User by ID
    public Uni<Boolean> deleteUserById(Long id) {
        return deleteById(id);
    }


}
