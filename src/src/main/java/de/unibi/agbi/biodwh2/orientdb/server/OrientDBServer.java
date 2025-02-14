package de.unibi.agbi.biodwh2.orientdb.server;

import de.unibi.agbi.biodwh2.core.net.BioDWH2Updater;
import de.unibi.agbi.biodwh2.orientdb.server.model.CmdArgs;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OrientDBServer {
    private static final Logger LOGGER = LogManager.getLogger(OrientDBServer.class);

    private OrientDBServer() {
    }

    public static void main(final String... args) {
        final CmdArgs commandLine = parseCommandLine(args);
        new OrientDBServer().run(commandLine);
    }

    private static CmdArgs parseCommandLine(final String... args) {
        final CmdArgs result = new CmdArgs();
        final CommandLine cmd = new CommandLine(result);
        cmd.parseArgs(args);
        return result;
    }

    private void run(final CmdArgs commandLine) {
        BioDWH2Updater.checkForUpdate("BioDWH2-OrientDB-Server",
                "https://api.github.com/repos/BioDWH2/BioDWH2-OrientDB-Server/releases");
        if (commandLine.createStart != null)
            createAndStartWorkspaceServer(commandLine);
        else if (commandLine.start != null)
            startWorkspaceServer(commandLine);
        else if (commandLine.create != null)
            createWorkspaceDatabase(commandLine);
        else
            printHelp(commandLine);
    }

    private void createAndStartWorkspaceServer(final CmdArgs commandLine) {
        final String workspacePath = commandLine.createStart;
        if (!verifyWorkspaceExists(workspacePath)) {
            printHelp(commandLine);
            return;
        }
        final OrientDBService service = new OrientDBService(workspacePath);
        service.deleteOldDatabase();
        final OrientDBStudio studio = new OrientDBStudio(workspacePath);
        studio.downloadOrientDBStudio();
        service.startOrientDBService(commandLine.port, commandLine.studioPort);
        service.createDatabase();
        storeWorkspaceHash(workspacePath);
        service.openBrowser();
    }

    private boolean verifyWorkspaceExists(final String workspacePath) {
        if (StringUtils.isEmpty(workspacePath) || !Paths.get(workspacePath).toFile().exists()) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Workspace path '{}' was not found", workspacePath);
            return false;
        }
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Using workspace directory '{}'", workspacePath);
        return true;
    }

    private void printHelp(final CmdArgs commandLine) {
        CommandLine.usage(commandLine, System.out);
    }

    private void storeWorkspaceHash(final String workspacePath) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Updating workspace OrientDB cache checksum...");
        final Path hashFilePath = Paths.get(workspacePath, "orientdb/checksum.txt");
        try {
            final String hash = HashUtils.getFastPseudoHashFromFile(
                    Paths.get(workspacePath, "sources/mapped.db").toString());
            final FileWriter writer = new FileWriter(hashFilePath.toFile());
            writer.write(hash);
            writer.close();
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Failed to store hash of workspace mapped graph", e);
        }
    }

    private void startWorkspaceServer(final CmdArgs commandLine) {
        final String workspacePath = commandLine.start;
        if (!verifyWorkspaceExists(workspacePath)) {
            printHelp(commandLine);
            return;
        }
        if (!checkOrientDBDatabaseMatchesWorkspace(workspacePath) && LOGGER.isInfoEnabled())
            LOGGER.warn("The OrientDB database is out-of-date and should be recreated with the --create command");
        final OrientDBService service = new OrientDBService(workspacePath);
        final OrientDBStudio studio = new OrientDBStudio(workspacePath);
        studio.downloadOrientDBStudio();
        service.startOrientDBService(commandLine.port, commandLine.studioPort);
        service.openBrowser();
    }

    private boolean checkOrientDBDatabaseMatchesWorkspace(final String workspacePath) {
        try {
            final String hash = HashUtils.getFastPseudoHashFromFile(
                    Paths.get(workspacePath, "sources/mapped.db").toString());
            final Path hashFilePath = Paths.get(workspacePath, "orientdb/checksum.txt");
            if (Files.exists(hashFilePath)) {
                final String storedHash = new String(Files.readAllBytes(hashFilePath)).trim();
                return hash.equals(storedHash);
            }
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Failed to check hash of workspace mapped graph", e);
        }
        return false;
    }

    private void createWorkspaceDatabase(final CmdArgs commandLine) {
        final String workspacePath = commandLine.create;
        if (!verifyWorkspaceExists(workspacePath)) {
            printHelp(commandLine);
            return;
        }
        final OrientDBService service = new OrientDBService(workspacePath);
        service.deleteOldDatabase();
        service.startOrientDBService(commandLine.port, commandLine.studioPort);
        service.createDatabase();
        storeWorkspaceHash(workspacePath);
        service.stopOrientDBService();
    }
}
