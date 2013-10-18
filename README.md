JSNMPWalker
===========
Version 2.0

Description
---------------
JSNMPWalker is a Swing application to run multiple SNMP sessions simultaneously. 
The results might be saved into a file. 

The uniquness of the application is that it shows both requests and responses, and shows errors in the walks, hence being useful for debugging purposes of networking software. 

Requirements
------------
Operating Systems: Windows, Linux, OS X (Mac)

The application requires Java 7 installed. Go to http://www.java.com/en/download to download and install Java 7. 

Running
-----
Go to the downloaded directory. Type in command line:

> java -jar JSNMPWalker.jar

Compiling the source
--------------------

Libraries:

1.SNMP4j

SNMP4j is a library to perform SNMP commands written in Java, provided with the project. 
Alternatively, download the snmp4j library from http://www.snmp4j.org/html/download.html, "SNMP4J sources and library 2.2.1" or whatever the lastest library available. 

Include the library snmp4j/dist/snmp4j-2.2.1.jar into your Java Build Path. 

For Eclipse, click Project -> Properties -> Java Build Path -> Libraries and add the .jar file. 

SNMP4j is provided under an Apache 2.0 license. 

2.Mibble

Mibble is a MIB browser library written in Java, provided with the project. 
Alternatively, it can be downloaded from http://www.mibble.org/download/ (stable versions) or https://github.com/cederberg/mibble (source code). 

Inlude the jar files from the folder lib/mibble-2.9.3/lib into your Java Build Path.

For Eclipse, click Project -> Properties -> Java Build Path -> Libraries and add the .jar files.  

Original Mibble was provided under a GPLv2 license which is allowed to be distributed under a later version of GPL. 

3.CIDRUtils

CIDRUtils is a Java library that enables you to get an IP range from CIDR specification, written in Java. It supports both IPv4 and IPv6.

Make sure to include the CIDR project folder into the build path. 

For Eclipse, open Project->Properties->Java Build Path->Source, click on Add Folder and navigate to lib/CIDRUtils.

The CIDRUtils project is distributed under a MIT license. 

Building:

To be able to build the project with ant, it provides build.xml script. The ant tool should be installed. 

In order to build the project, go to the project directory and type:
> ant

The default command uses the default application name JSNMPWalker and a default version which is today's date. 
To assign a different version, use the following command:
> ant -Dbuild.version=[version_number]

Building in Eclipse with ant:

Go to Project->Properties->Builders and add a new ant builder. Check the box next to it.
Go to Rub->Run configurations->Java Application->SNMPWalker->Class Path->User Entities and add JSNMPWalker.jar
to be able to run it in Eclipse

Compatibility
-------------
snmp4j-2.2.1

mibble-2.9.3

License
-------
GNU GPL, Version 3 license. Copyright 2012-2013 Z. Moskun zlatco@gmail.com, T. Moskun tanysmoskun@gmail.com.
