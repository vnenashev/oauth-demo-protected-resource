package com.nenashev.oauthdemo.oauthdemoprotectedresource.filter;

import com.nenashev.oauthdemo.oauthdemoprotectedresource.db.AccessTokenRepository;
import com.nenashev.oauthdemo.oauthdemoprotectedresource.model.AccessTokenInfo;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class AccessTokenFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(AccessTokenFilter.class);

    private final AccessTokenRepository accessTokenRepository;

    public AccessTokenFilter(final AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        if (Objects.equals("/", request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }
        logger.info("Request is {}, getting access token", request.getServletPath());

        final Optional<String> authHeader = Optional.ofNullable(request.getHeader("Authorization"))
            .filter(StringUtils::hasText);
        final Optional<String> accessTokenParam = Optional.ofNullable(request.getParameter("access_token"))
            .filter(StringUtils::hasText);

        final String inToken;
        if (authHeader.isPresent() && authHeader.get().toLowerCase().startsWith("bearer ")) {
            inToken = authHeader.get().substring("bearer ".length());
        } else if (accessTokenParam.isPresent() && StringUtils.hasText(accessTokenParam.get())) {
            inToken = accessTokenParam.get();
        } else {
            logger.error("No access token in Authorization header or request parameter");
            response.sendError(401);
            return;
        }

        final Optional<AccessTokenInfo> foundAccessToken = accessTokenRepository.findByAccessToken(inToken);

        if (foundAccessToken.isPresent()) {
            logger.info("Found matching access token: {}", foundAccessToken);
            final Instant now = Instant.now();
            if (foundAccessToken.get().getExpireDate().isBefore(now)) {
                logger.error("Access token is expired");
                response.sendError(401);
                return;
            }
        } else {
            logger.error("No matching token was found");
            response.sendError(401);
            return;
        }

        request.setAttribute("access_token", foundAccessToken.get());

        filterChain.doFilter(request, response);
    }
}
