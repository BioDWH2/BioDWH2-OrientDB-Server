package de.unibi.agbi.biodwh2.orientdb.server.model;

import picocli.CommandLine;

@CommandLine.Command(name = "BioDWH2-OrientDB-Server.jar")
public class CmdArgs {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "print this message")
    public boolean help;
    @CommandLine.Option(names = {
            "-s", "--start"
    }, arity = "1", paramLabel = "<workspacePath>", description = "Start an OrientDB server for the workspace")
    public String start;
    @CommandLine.Option(names = {
            "-c", "--create"
    }, arity = "1", paramLabel = "<workspacePath>", description = "Create a OrientDB database from the workspace graph")
    public String create;
    @CommandLine.Option(names = {
            "-cs", "--create-start"
    }, arity = "1", paramLabel = "<workspacePath>", description = "Create and start a OrientDB database from the workspace graph")
    public String createStart;
}
