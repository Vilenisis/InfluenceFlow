package com.influenceflow.service;

import com.influenceflow.dao.CreatorDao;
import com.influenceflow.dao.TgUserDao;
import com.influenceflow.model.Creator;
import com.influenceflow.model.TgUser;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class CreatorService {
    private final TgUserDao tgUserDao;
    private final CreatorDao creatorDao;

    public CreatorService(TgUserDao tgUserDao, CreatorDao creatorDao) {
        this.tgUserDao = tgUserDao;
        this.creatorDao = creatorDao;
    }

    public Creator registerCreator(long telegramId, String username, String fullName, String email, String niche, String handle) {
        String normalizedUsername = normalizeUsername(username);
        TgUser user = tgUserDao.findByTelegramId(telegramId)
                .map(existing -> synchronizeUsername(existing, normalizedUsername))
                .orElseGet(() -> createTgUser(telegramId, normalizedUsername));

        Creator creator = creatorDao.findByTgUserId(user.getId())
                .map(existing -> updateCreator(existing, fullName, email, niche, handle))
                .orElseGet(() -> createCreator(user.getId(), fullName, email, niche, handle));
        return creator;
    }

    public Optional<Creator> findByTelegramId(long telegramId) {
        return tgUserDao.findByTelegramId(telegramId)
                .flatMap(user -> creatorDao.findByTgUserId(user.getId()));
    }

    public boolean isAdmin(long telegramId) {
        return tgUserDao.isAdminByTelegramId(telegramId);
    }

    private TgUser synchronizeUsername(TgUser user, String username) {
        if (!Objects.equals(user.getUsername(), username)) {
            tgUserDao.updateUsername(user.getId(), username);
            user.setUsername(username);
        }
        return user;
    }

    private TgUser createTgUser(long telegramId, String username) {
        TgUser newUser = new TgUser();
        newUser.setTelegramId(telegramId);
        newUser.setUsername(username);
        newUser.setAdmin(false);
        newUser.setCreatedAt(LocalDateTime.now());
        return tgUserDao.save(newUser);
    }

    private Creator createCreator(long tgUserId, String fullName, String email, String niche, String handle) {
        Creator creator = new Creator();
        creator.setTgUserId(tgUserId);
        creator.setFullName(fullName);
        creator.setEmail(email);
        creator.setNiche(niche);
        creator.setPlatformHandle(handle);
        return creatorDao.save(creator);
    }

    private Creator updateCreator(Creator creator, String fullName, String email, String niche, String handle) {
        creator.setFullName(fullName);
        creator.setEmail(email);
        creator.setNiche(niche);
        creator.setPlatformHandle(handle);
        return creatorDao.update(creator);
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        String trimmed = username.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
