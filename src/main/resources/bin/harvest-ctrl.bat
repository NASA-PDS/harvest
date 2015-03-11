:: Copyright 2014, by the California Institute of Technology.
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

:: Check if the JAVA_HOME environment variable is set.
if not defined JAVA_HOME (
echo The JAVA_HOME environment variable is not set.
goto END
)

:: Setup environment variables.
set SCRIPT_DIR=%~dps0
set PARENT_DIR=%SCRIPT_DIR%..
set LIB_DIR=%PARENT_DIR%\lib

:: Execute the application.
"%JAVA_HOME%"\bin\java -Xms256m -Xmx1024m -Djava.ext.dirs="%LIB_DIR%" gov.nasa.jpl.oodt.cas.crawl.daemon.CrawlDaemonController %*

:END