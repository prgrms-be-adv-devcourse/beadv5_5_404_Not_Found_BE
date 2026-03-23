package com.notfound.member.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;

public final class PublicPaths {

    private static final String[] PUBLIC_PREFIXES = {"/auth/", "/product/", "/internal/"};
    private static final String[] PUBLIC_EXACT = {"/auth", "/product", "/internal"};

    private PublicPaths() {
    }

    public static boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        for (String exact : PUBLIC_EXACT) {
            if (path.equals(exact)) return true;
        }
        return false;
    }
}
