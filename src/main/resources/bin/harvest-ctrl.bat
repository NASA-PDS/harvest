:: Copyright 2011, by the California Institute of Technology.
:: ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
:: Any commercial use must be negotiated with the Office of Technology Transfer
:: at the California Institute of Technology.
::
:: This software is subject to U. S. export control laws and regulations
:: (22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software
:: is subject to U.S. export control laws and regulations, the recipient has
:: the responsibility to obtain export licenses or other export authority as
:: may be required before exporting such information to foreign countries or
:: providing access to foreign nationals.
::
:: $Id$

:: Batch file that allows easy execution of the Crawler Daemon Controller
:: without the need to set the CLASSPATH or having to type in that long java
:: command (java gov.nasa.jpl.oodt.cas.crawl.daemon.CrawlDaemonController ...)

@echo off

:: Set the JAVA_HOME environment variable here in the script if it will
:: not be defined in the environment.
if not defined JAVA_HOME (
  set JAVA_HOME=\path\to\java\home
)

:: Setup environment variables.
set SCRIPT_DIR=%~dps0
set PARENT_DIR=%SCRIPT_DIR%..
set LIB_DIR=%PARENT_DIR%\lib

:: Execute the application.
"%JAVA_HOME%"\bin\java -Djava.ext.dirs="%LIB_DIR%" gov.nasa.jpl.oodt.cas.crawl.daemon.CrawlDaemonController %*
