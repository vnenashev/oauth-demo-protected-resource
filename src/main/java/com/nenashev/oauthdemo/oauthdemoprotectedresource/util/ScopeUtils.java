package com.nenashev.oauthdemo.oauthdemoprotectedresource.util;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.util.StringUtils;

public class ScopeUtils {

    private ScopeUtils() {
    }

    public static boolean hasAccess(final String scope, final String access) {
        return Optional.ofNullable(scope)
            .filter(StringUtils::hasText)
            .map(s -> s.split(" "))
            .flatMap(s -> Stream.of(s).filter(Predicate.isEqual(access)).findAny())
            .orElse(null) != null;
    }
}
