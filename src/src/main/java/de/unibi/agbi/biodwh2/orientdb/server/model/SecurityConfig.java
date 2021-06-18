package de.unibi.agbi.biodwh2.orientdb.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SecurityConfig {
    public boolean enabled;
    public Authentication authentication;

    public static class Authentication {
        public boolean allowDefault;
        public List<Authenticator> authenticators;
    }

    public static class Authenticator {
        public String name;
        @JsonProperty("class")
        public String className;
        public boolean enabled;
        public List<User> users;
    }

    public static class User {
        public String username;
        public String resources;
    }
}
