![Java CI](https://github.com/BioDWH2/BioDWH2-OrientDB-Server/workflows/Java%20CI/badge.svg?branch=develop) ![Release](https://img.shields.io/github/v/release/BioDWH2/BioDWH2-OrientDB-Server) ![Downloads](https://img.shields.io/github/downloads/BioDWH2/BioDWH2-OrientDB-Server/total) ![License](https://img.shields.io/github/license/BioDWH2/BioDWH2-OrientDB-Server)

# BioDWH2-OrientDB-Server
**BioDWH2** is an easy-to-use, automated, graph-based data warehouse and mapping tool for bioinformatics and medical informatics. The main repository can be found [here](https://github.com/BioDWH2/BioDWH2).

This repository contains the **BioDWH2-OrientDB-Server** utility which can be used to create and explore a OrientDB graph database from any BioDWH2 workspace. There is no need for any OrientDB installation. All necessary components are bundled with this tool.

## Download
The latest release version of **BioDWH2-OrientDB-Server** can be downloaded [here](https://github.com/BioDWH2/BioDWH2-OrientDB-Server/releases/latest).

## Usage
BioDWH2-OrientDB-Server requires the Java Runtime Environment version 8. The JRE 8 is available [here](https://www.oracle.com/java/technologies/javase-jre8-downloads.html).

Creating a database from any workspace is done using the following command. Every time the workspace is updated or changed, the create command has to be executed again.
~~~BASH
> java -jar BioDWH2-OrientDB-Server.jar --create /path/to/workspace
~~~

Once the database has been created, the database and OrientDB Studio can be started as follows:
~~~BASH
> java -jar BioDWH2-OrientDB-Server.jar --start /path/to/workspace
~~~

By default, two users are created for OrientDB Studio:
  * username: `biodwh2`, password: `biodwh2`
  * username: `root`, password: `root`

## Help
~~~
Usage: BioDWH2-OrientDB-Server.jar [-h] [-c <workspacePath>] [-cs <workspacePath>]
                                   [-s <workspacePath>] [-p <port>] [-sp <studioPort>]
  -h, --help                            print this message
  -s, --start <workspacePath>           Start an OrientDB server for the workspace
  -c, --create <workspacePath>          Create a OrientDB database from the workspace graph
  -cs, --create-start <workspacePath>   Create and start a OrientDB database from the workspace graph
  -p, --port <port>                     Specifies the OrientDB server port(-range) (default 2424-2430)
  -sp, --studio-port <studioPort>       Specifies the OrientDB Studio port(-range) (default 2480-2490)
~~~