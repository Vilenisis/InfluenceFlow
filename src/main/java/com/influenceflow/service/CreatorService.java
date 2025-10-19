package com.influenceflow.service;

import com.influenceflow.dao.CreatorDao;
import com.influenceflow.dao.TgUserDao;
import com.influenceflow.model.Creator;
import com.influenceflow.model.TgUser;

import java.time.LocalDateTime;
import java.util.Optional;

public class CreatorService {
    private final TgUserDao tgUserDao;
    private final CreatorDao creatorDao;

    public CreatorService(TgUserDao tgUserDao, CreatorDao creatorDao) {
        this.tgUserDao = tgUserDao;
        this.creatorDao = creatorDao;
    }

    public Creator registerCreator(String username, String fullName, String email, String niche, String handle) {
        Optional<TgUser> existingUser = tgUserDao.findByUsername(username);
        TgUser user = existingUser.orElseGet(() -> {
            TgUser newUser = new TgUser();
            newUser.setUsername(username);
            newUser.setAdmin(false);
            newUser.setCreatedAt(LocalDateTime.now());
            return tgUserDao.save(newUser);
        });

        Optional<Creator> existingCreator = creatorDao.findByTgUserId(user.getId());
        if (existingCreator.isPresent()) {
            return existingCreator.get();
        }
        Creator creator = new Creator();
        creator.setTgUserId(user.getId());
        creator.setFullName(fullName);
        creator.setEmail(email);
        creator.setNiche(niche);
        creator.setPlatformHandle(handle);
        return creatorDao.save(creator);
    }

    public Optional<Creator> findByUsername(String username) {
        return tgUserDao.findByUsername(username)
                .flatMap(user -> creatorDao.findByTgUserId(user.getId()));
    }
}
