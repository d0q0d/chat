package org.tpl.chat.service.remote.cachetoken;

import lombok.RequiredArgsConstructor;
import org.tpl.chat.dal.model.CacheToken;
import org.tpl.chat.dal.repository.CacheTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheTokenServiceImpl implements CacheTokenService {

    private final CacheTokenRepository repository;

    @Override
    public CacheToken save(CacheToken cacheToken) {
        return repository.save(cacheToken);
    }

    @Override
    public Optional<CacheToken> getById(String id) {
        return repository.findById(id);
    }

}
