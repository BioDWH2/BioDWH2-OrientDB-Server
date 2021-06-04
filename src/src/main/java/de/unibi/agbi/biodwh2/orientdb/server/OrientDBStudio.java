package de.unibi.agbi.biodwh2.orientdb.server;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.unibi.agbi.biodwh2.orientdb.server.model.MavenMetadata;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OrientDBStudio {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrientDBStudio.class);
    private static final String STUDIO_METADATA_URL = "https://repo1.maven.org/maven2/com/orientechnologies/orientdb-studio/maven-metadata.xml";
    private static final String STUDIO_ARCHIVE_FILE_NAME = "orientdb-studio.zip";

    private final String pluginsPath;

    public OrientDBStudio(final String workspacePath) {
        pluginsPath = Paths.get(workspacePath, "orientdb", "plugins").toString();
    }

    public boolean downloadOrientDBStudio() {
        try {
            Files.createDirectories(Paths.get(pluginsPath));
            final File studioArchiveFile = Paths.get(pluginsPath, STUDIO_ARCHIVE_FILE_NAME).toFile();
            if (!studioArchiveFile.exists()) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("Downloading OrientDB Studio...");
                final String version = getOrientDBStudioVersion();
                if (version != null) {
                    final String url =
                            "https://repo1.maven.org/maven2/com/orientechnologies/orientdb-studio/" + version +
                            "/orientdb-studio-" + version + ".zip";
                    FileUtils.copyURLToFile(new URL(url), studioArchiveFile);
                    return true;
                }
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Failed to retrieve OrientDB Studio.", e);
        }
        return false;
    }

    private String getOrientDBStudioVersion() {
        final XmlMapper mapper = new XmlMapper();
        try {
            final URL metadataUrl = new URL(STUDIO_METADATA_URL);
            final MavenMetadata metadata = mapper.readValue(metadataUrl, MavenMetadata.class);
            if (metadata != null && metadata.versioning != null && metadata.versioning.latest != null)
                return metadata.versioning.latest;
        } catch (IOException | ClassCastException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Failed to retrieve OrientDB Studio download url.", e);
        }
        return null;
    }
}
