package com.doctrine7.tgbot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    List<User> findAllByChatIdIn(List<Long> id);
}
