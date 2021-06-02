package de.unibi.agbi.biodwh2.orientdb.server;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * https://orientdb.com/docs/last/internals/Embedded-Server.html
 */
public class OrientDBService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrientDBService.class);

    private final String workspacePath;
    private final String orientDBPath;
    private final String databasePath;

    public OrientDBService(final String workspacePath) {
        this.workspacePath = workspacePath;
        orientDBPath = Paths.get(workspacePath, "orientdb").toString();
        databasePath = Paths.get(orientDBPath, "orientdb").toString();
    }

    public void startOrientDBService() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Starting OrientDB DBMS on bolt://localhost:...");
        try {
            final OServer server = OServerMain.create();
            final OServerConfiguration cfg = new OServerConfiguration();
            cfg.network = getNetwork();
            cfg.users = new OServerUserConfiguration[]{getRootUser()};
            cfg.properties = new OServerEntryConfiguration[]{
                    getServerProperty("server.cache.staticResources", "false"),
                    getServerProperty("log.console.level", "info"),
                    getServerProperty("log.file.level", "fine"),
                    getServerProperty("plugin.dynamic", "false")
            };
            // TODO
            server.startup(cfg);
            server.activate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OServerNetworkConfiguration getNetwork() {
        final OServerNetworkConfiguration network = new OServerNetworkConfiguration();
        final OServerNetworkProtocolConfiguration binaryProtocol = new OServerNetworkProtocolConfiguration();
        binaryProtocol.name = "binary";
        binaryProtocol.implementation = "com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary";
        final OServerNetworkProtocolConfiguration httpProtocol = new OServerNetworkProtocolConfiguration();
        httpProtocol.name = "http";
        httpProtocol.implementation = "com.orientechnologies.orient.server.network.protocol.http.ONetworkProtocolHttpDb";
        network.protocols = new ArrayList<>(Arrays.asList(binaryProtocol, httpProtocol));
        final OServerNetworkListenerConfiguration binaryListener = new OServerNetworkListenerConfiguration();
        binaryListener.ipAddress = "0.0.0.0";
        binaryListener.portRange = "2424-2430";
        binaryListener.protocol = "binary";
        final OServerNetworkListenerConfiguration httpListener = new OServerNetworkListenerConfiguration();
        httpListener.ipAddress = "0.0.0.0";
        httpListener.portRange = "2480-2490";
        httpListener.protocol = "http";
        network.listeners = new ArrayList<>(Arrays.asList(binaryListener, httpListener));
        return network;
    }

    private OServerUserConfiguration getRootUser() {
        final OServerUserConfiguration user = new OServerUserConfiguration();
        user.name = "root";
        user.password = "";
        return user;
    }

    private OServerEntryConfiguration getServerProperty(final String key, final String value) {
        final OServerEntryConfiguration property = new OServerEntryConfiguration();
        property.name = key;
        property.value = value;
        return property;
    }

    public void deleteOldDatabase() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Removing old database...");
        // TODO
    }

    public void createDatabase() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Creating Neo4j database...");
        // TODO
    }
}
