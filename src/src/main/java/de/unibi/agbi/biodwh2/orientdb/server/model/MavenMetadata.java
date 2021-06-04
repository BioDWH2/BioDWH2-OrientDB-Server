package de.unibi.agbi.biodwh2.orientdb.server.model;

import java.util.List;

public class MavenMetadata {
    public String groupId;
    public String artifactId;
    public Versioning versioning;

    public static class Versioning {
        public String latest;
        public String release;
        public String lastUpdated;
        public List<String> versions;
    }
}
