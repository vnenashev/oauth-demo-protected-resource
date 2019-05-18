package com.nenashev.oauthdemo.oauthdemoprotectedresource.db;

import com.nenashev.oauthdemo.oauthdemoprotectedresource.model.AccessTokenInfo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccessTokenRepository extends MongoRepository<AccessTokenInfo, String> {

    Optional<AccessTokenInfo> findByAccessToken(String accessToken);
}
