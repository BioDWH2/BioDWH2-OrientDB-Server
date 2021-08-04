package de.unibi.agbi.biodwh2.orientdb.server.model;

import picocli.CommandLine;

@CommandLine.Command(name = "BioDWH2-OrientDB-Server.jar", sortOptions = false, separator = " ", footer = "Visit https://biodwh2.github.io for more documentation.")
public class CmdArgs {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "print this message", order = 1)
    public boolean help;
    @CommandLine.Option(names = {
            "-s", "--start"
    }, arity = "1", paramLabel = "<workspacePath>", description = "Start an OrientDB server for the workspace", order = 2)
    public String start;
    @CommandLine.Option(names = {
            "-c", "--create"
    }, arity = "1", paramLabel = "<workspacePath>", description = "Create a OrientDB database from the workspace graph", order = 3)
    public String create;
    @CommandLine.Option(names = {
            "-cs", "--create-start"
    }, arity = "1", paramLabel = "<workspacePath>", description = "Create and start a OrientDB database from the workspace graph", order = 4)
    public String createStart;
    @CommandLine.Option(names = {
            "-p", "--port"
    }, defaultValue = "2424-2430", paramLabel = "<port>", description = "Specifies the OrientDB server port(-range) (default 2424-2430)", order = 5)
    public String port;
    @CommandLine.Option(names = {
            "-sp", "--studio-port"
    }, defaultValue = "2480-2490", paramLabel = "<studioPort>", description = "Specifies the OrientDB Studio port(-range) (default 2480-2490)", order = 6)
    public String studioPort;
}
