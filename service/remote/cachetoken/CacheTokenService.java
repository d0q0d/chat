package org.tpl.chat.service.remote.cachetoken;

import org.tpl.chat.dal.model.CacheToken;

import java.util.Optional;

public interface CacheTokenService {

    CacheToken save(CacheToken cacheToken);

    Optional<CacheToken> getById(String id);

}
