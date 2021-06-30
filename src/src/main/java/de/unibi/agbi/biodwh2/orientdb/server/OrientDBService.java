package de.unibi.agbi.biodwh2.orientdb.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.*;
import de.unibi.agbi.biodwh2.core.lang.Type;
import de.unibi.agbi.biodwh2.core.model.graph.Edge;
import de.unibi.agbi.biodwh2.core.model.graph.Graph;
import de.unibi.agbi.biodwh2.core.model.graph.IndexDescription;
import de.unibi.agbi.biodwh2.core.model.graph.Node;
import de.unibi.agbi.biodwh2.orientdb.server.model.SecurityConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * https://orientdb.com/docs/last/internals/Embedded-Server.html
 */
public class OrientDBService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrientDBService.class);

    private final String workspacePath;
    private final Path orientdbPath;
    private final Path databasePath;
    private final Path wwwPath;
    private final Path configPath;
    private final Path securityFilePath;
    private OServer server;

    public OrientDBService(final String workspacePath) {
        this.workspacePath = workspacePath;
        orientdbPath = Paths.get(workspacePath, "orientdb");
        databasePath = Paths.get(workspacePath, "orientdb", "orientdb");
        wwwPath = Paths.get(workspacePath, "orientdb", "www");
        configPath = Paths.get(workspacePath, "orientdb", "config");
        securityFilePath = Paths.get(workspacePath, "orientdb", "config", "security.json");
    }

    public void startOrientDBService() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Starting OrientDB DBMS on localhost:2424...");
        try {
            System.setProperty("ORIENTDB_HOME", orientdbPath.toString());
            System.setProperty("ORIENTDB_ROOT_PASSWORD", "root");
            Files.createDirectories(databasePath);
            Files.createDirectories(configPath);
            writeSecurityConfigFile();
            server = OServerMain.create();
            server.startup(getServerConfig());
            server.activate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeSecurityConfigFile() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(securityFilePath.toFile(), getSecurityConfig());
    }

    private SecurityConfig getSecurityConfig() {
        final SecurityConfig securityConfig = new SecurityConfig();
        securityConfig.enabled = true;
        securityConfig.authentication = new SecurityConfig.Authentication();
        securityConfig.authentication.allowDefault = true;
        securityConfig.authentication.authenticators = new ArrayList<>();
        securityConfig.authentication.authenticators.add(getPasswordAuthenticator());
        securityConfig.authentication.authenticators.add(getServerConfigAuthenticator());
        return securityConfig;
    }

    private SecurityConfig.Authenticator getPasswordAuthenticator() {
        final SecurityConfig.Authenticator authenticator = new SecurityConfig.Authenticator();
        authenticator.enabled = true;
        authenticator.name = "Password";
        authenticator.className = "com.orientechnologies.orient.server.security.authenticator.ODefaultPasswordAuthenticator";
        authenticator.users = new ArrayList<>();
        final SecurityConfig.User guestUser = new SecurityConfig.User();
        guestUser.username = "guest";
        guestUser.resources = "connect,server.listDatabases,server.dblist";
        authenticator.users.add(guestUser);
        final SecurityConfig.User rootUser = new SecurityConfig.User();
        rootUser.username = "root";
        rootUser.resources = "*";
        authenticator.users.add(rootUser);
        final SecurityConfig.User biodwh2User = new SecurityConfig.User();
        biodwh2User.username = "biodwh2";
        biodwh2User.resources = "*";
        authenticator.users.add(biodwh2User);
        return authenticator;
    }

    private SecurityConfig.Authenticator getServerConfigAuthenticator() {
        final SecurityConfig.Authenticator authenticator = new SecurityConfig.Authenticator();
        authenticator.enabled = true;
        authenticator.name = "ServerConfig";
        authenticator.className = "com.orientechnologies.orient.server.security.authenticator.OServerConfigAuthenticator";
        return authenticator;
    }

    private OServerConfiguration getServerConfig() {
        final OServerConfiguration config = new OServerConfiguration();
        config.network = getNetwork();
        config.users = getUsers();
        config.properties = getServerProperties();
        return config;
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
        final OServerCommandConfiguration httpCommand = new OServerCommandConfiguration();
        httpCommand.implementation = "com.orientechnologies.orient.server.network.protocol.http.command.get.OServerCommandGetStaticContent";
        httpCommand.pattern = "GET|www GET|studio/ GET| GET|*.htm GET|*.html GET|*.xml GET|*.jpeg GET|*.jpg GET|*.png GET|*.gif GET|*.js GET|*.css GET|*.swf GET|*.ico GET|*.txt GET|*.otf GET|*.pjs GET|*.svg";
        httpCommand.parameters = new OServerEntryConfiguration[]{
                new OServerEntryConfiguration("http.cache:*.htm *.html",
                                              "Cache-Control: no-cache, no-store, max-age=0, must-revalidate\r\nPragma: no-cache"),
                new OServerEntryConfiguration("http.cache:default", "Cache-Control: max-age=120")
        };
        httpListener.commands = new OServerCommandConfiguration[]{httpCommand};
        final OServerParameterConfiguration charsetParameter = new OServerParameterConfiguration();
        charsetParameter.name = "network.http.charset";
        charsetParameter.value = "utf-8";
        httpListener.parameters = new OServerParameterConfiguration[]{charsetParameter};
        network.listeners = new ArrayList<>(Arrays.asList(binaryListener, httpListener));
        return network;
    }

    private OServerUserConfiguration[] getUsers() {
        final List<OServerUserConfiguration> result = new ArrayList<>();
        result.add(new OServerUserConfiguration("root", "root", "*"));
        result.add(new OServerUserConfiguration("biodwh2", "biodwh2", "*"));
        result.add(new OServerUserConfiguration("guest", "guest", "connect,server.listDatabases,server.dblist"));
        return result.toArray(new OServerUserConfiguration[0]);
    }

    private OServerEntryConfiguration[] getServerProperties() {
        final List<OServerEntryConfiguration> result = new ArrayList<>();
        result.add(new OServerEntryConfiguration("server.cache.staticResources", "false"));
        result.add(new OServerEntryConfiguration("log.console.level", "warning"));
        result.add(new OServerEntryConfiguration("log.file.level", "fine"));
        result.add(new OServerEntryConfiguration("plugin.dynamic", "true"));
        result.add(new OServerEntryConfiguration("server.database.path", databasePath.toString()));
        result.add(new OServerEntryConfiguration("orientdb.www.path", wwwPath.toString()));
        return result.toArray(new OServerEntryConfiguration[0]);
    }

    public void stopOrientDBService() {
        if (server != null)
            server.shutdown();
    }

    public void deleteOldDatabase() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Removing old database...");

        try {
            FileUtils.deleteDirectory(databasePath.toFile());
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Failed to remove old database '" + databasePath + "'", e);
        }
    }

    public void createDatabase() {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Creating OrientDB database...");
        server.createDatabase("BioDWH2", ODatabaseType.PLOCAL, OrientDBConfig.defaultConfig());
        try (ODatabaseDocumentInternal db = server.openDatabase("BioDWH2"); Graph graph = new Graph(
                Paths.get(workspacePath, "sources/mapped.db"), true)) {
            Files.createDirectories(databasePath);
            final HashMap<Long, ORID> nodeIdOrientDBIdMap = new HashMap<>();
            createNodes(db, graph, nodeIdOrientDBIdMap);
            createEdges(db, graph, nodeIdOrientDBIdMap);
            createIndices(db, graph);
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Failed to create OrientDB database '" + databasePath + "'", e);
        }
    }

    private void createNodes(final ODatabaseDocumentInternal db, final Graph graph,
                             final HashMap<Long, ORID> nodeIdOrientDBIdMap) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Creating node definitions...");
        for (final String label : graph.getNodeLabels()) {
            final OClass definition = db.createVertexClass(label);
            final Map<String, Type> propertyKeyTypes = graph.getPropertyKeyTypesForNodeLabel(label);
            for (final String key : propertyKeyTypes.keySet())
                if (!Node.IGNORED_FIELDS.contains(key))
                    definition.createProperty(key, OType.getTypeByClass(propertyKeyTypes.get(key).getType()));
        }
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Creating nodes...");
        for (final Node node : graph.getNodes()) {
            final String label = node.getLabel();
            OVertex orientNode = db.newVertex(label);
            for (final String propertyKey : node.keySet())
                setPropertySafe(node, orientNode, propertyKey);
            final ORID id = orientNode.save().getIdentity();
            nodeIdOrientDBIdMap.put(node.getId(), id);
        }
    }

    private void setPropertySafe(final Node node, final OVertex orientNode, final String propertyKey) {
        try {
            if (!Node.IGNORED_FIELDS.contains(propertyKey)) {
                Object value = node.getProperty(propertyKey);
                if (value instanceof Collection)
                    value = convertCollectionToArray((Collection<?>) value);
                if (value != null)
                    orientNode.setProperty(propertyKey, value);
            }
        } catch (IllegalArgumentException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn(
                        "Illegal property '" + propertyKey + " -> " + node.getProperty(propertyKey) + "' for node '" +
                        node.getId() + "[:" + node.getLabel() + "]'");
        }
    }

    @SuppressWarnings({"SuspiciousToArrayCall"})
    private Object convertCollectionToArray(final Collection<?> collection) {
        Class<?> type = null;
        for (Object t : collection) {
            if (t != null) {
                type = t.getClass();
                break;
            }
        }
        if (type != null) {
            if (type.equals(String.class))
                return collection.stream().map(type::cast).toArray(String[]::new);
            if (type.equals(Boolean.class))
                return collection.stream().map(type::cast).toArray(Boolean[]::new);
            if (type.equals(Integer.class))
                return collection.stream().map(type::cast).toArray(Integer[]::new);
            if (type.equals(Float.class))
                return collection.stream().map(type::cast).toArray(Float[]::new);
            if (type.equals(Long.class))
                return collection.stream().map(type::cast).toArray(Long[]::new);
            if (type.equals(Double.class))
                return collection.stream().map(type::cast).toArray(Double[]::new);
            if (type.equals(Byte.class))
                return collection.stream().map(type::cast).toArray(Byte[]::new);
            if (type.equals(Short.class))
                return collection.stream().map(type::cast).toArray(Short[]::new);
        }
        return collection.stream().map(Object::toString).toArray(String[]::new);
    }

    private void createEdges(final ODatabaseDocumentInternal db, final Graph graph,
                             final HashMap<Long, ORID> nodeIdOrientDBIdMap) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Creating edge definitions...");
        for (final String label : graph.getEdgeLabels()) {
            final OClass definition = db.createEdgeClass(label);
            final Map<String, Type> propertyKeyTypes = graph.getPropertyKeyTypesForEdgeLabel(label);
            for (final String key : propertyKeyTypes.keySet())
                if (!Edge.IGNORED_FIELDS.contains(key))
                    definition.createProperty(key, OType.getTypeByClass(propertyKeyTypes.get(key).getType()));
        }
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Creating edges...");
        for (final Edge edge : graph.getEdges()) {
            final OVertex fromNode = db.getRecord(nodeIdOrientDBIdMap.get(edge.getFromId()));
            final OVertex toNode = db.getRecord(nodeIdOrientDBIdMap.get(edge.getToId()));
            final OEdge orientEdge = db.newEdge(fromNode, toNode, edge.getLabel());
            for (final String propertyKey : edge.keySet())
                if (!Edge.IGNORED_FIELDS.contains(propertyKey)) {
                    Object value = edge.getProperty(propertyKey);
                    if (value instanceof Collection)
                        value = convertCollectionToArray((Collection<?>) value);
                    if (value != null)
                        orientEdge.setProperty(propertyKey, value);
                }
        }
    }

    private void createIndices(final ODatabaseSession db, final Graph graph) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Creating indices...");
        final IndexDescription[] indices = graph.indexDescriptions();
        for (final IndexDescription index : indices) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Creating " + index.getType() + " index on '" + index.getProperty() + "' field for " +
                            index.getTarget() + " label '" + index.getLabel() + "'...");
            final OClass.INDEX_TYPE type = index.getType() == IndexDescription.Type.UNIQUE ? OClass.INDEX_TYPE.UNIQUE :
                                           OClass.INDEX_TYPE.NOTUNIQUE;
            final ODocument metadata = new ODocument().field("ignoreNullValues", true);
            final OProperty propertyDefinition = db.getClass(index.getLabel()).getProperty(index.getProperty());
            if (propertyDefinition != null)
                propertyDefinition.createIndex(type, metadata);
            else if (LOGGER.isErrorEnabled())
                LOGGER.error("Failed to create index on undefined property '" + index.getProperty() + "'");
        }
    }
}
