package com.globalbooks.catalog.config;

import java.util.HashMap;
import java.util.Map;

public class SecurityConfig {

    // Security realm configuration
    public static final String SECURITY_REALM = "CatalogService";

    // Token expiration time (in milliseconds)
    public static final long TOKEN_EXPIRATION_TIME = 3600000; // 1 hour

    // Nonce cache size
    public static final int NONCE_CACHE_SIZE = 1000;

    // Password encoding type
    public static final String PASSWORD_TYPE_TEXT = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";
    public static final String PASSWORD_TYPE_DIGEST = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest";

    // User roles
    public enum UserRole {
        ADMIN("admin"),
        PARTNER("partner"),
        CLIENT("client"),
        GUEST("guest");

        private final String role;

        UserRole(String role) {
            this.role = role;
        }

        public String getRole() {
            return role;
        }
    }

    // User credentials and roles (In production, this should be in database)
    private static final Map<String, UserInfo> USERS = new HashMap<>();

    static {
        USERS.put("admin", new UserInfo("admin", "admin123", UserRole.ADMIN));
        USERS.put("client1", new UserInfo("client1", "pass123", UserRole.CLIENT));
        USERS.put("partner", new UserInfo("partner", "partner456", UserRole.PARTNER));
        USERS.put("guest", new UserInfo("guest", "guest123", UserRole.GUEST));
    }

    public static UserInfo getUser(String username) {
        return USERS.get(username);
    }

    public static boolean isValidUser(String username) {
        return USERS.containsKey(username);
    }

    public static class UserInfo {
        private final String username;
        private final String password;
        private final UserRole role;

        public UserInfo(String username, String password, UserRole role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public UserRole getRole() {
            return role;
        }
    }
}