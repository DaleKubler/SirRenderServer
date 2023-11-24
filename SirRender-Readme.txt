SirRender 

A network rendering farm application written by Joe Kubler (with a little help from his Dad)

Files:
------
SirRender.jar (The main client/server application)
runSirRenderClient.bat (Executes the SirRender client)
runSirRenderServer.bat (Executes the SirRender server)




Default File Locations and Environment Variable Overrides):
-----------------------------------------------------------
U:\\SirRender\\temp\\ (SIRRENDER_TMP_PATH) - Location where all temporary files are written to
                                           - SirRender temporary files are automatically deleted when the server closes)

U:\\SirtRender\\tmpBlenderIn\\ (SIRRENDER_BLENDER_INPUT_PATH) - Location of *.blend source files
                                                              - All servers should use the same network location

U:\\SirRender\\tmpBlenderOut\\ (SIRRENDER_BLENDER_OUTPUT_PATH) - Location of rendered *.png files
                                                               - All servers should use the same network location



Installation:
-------------
1. Create an installation directory (anywhere on the computer) and copy all three files to that location.

2. Edit the runSirRenderClient.bat file and change the IP address on the second line to the IP address 
   of the server you want this client to communicate with.  This will eventually become a drop-down 
   selection in the next version and will no longer be required.

3. If you wantg to run against multiple server, create multiple copies of the runSirRenderClient.bat
   file with an IP address for each server on te farm.  Rename the file appropriately so you will know
   which server the client will be talking to (e.g. runSirRenderClient-JOE.bat, runSirRenderClientGREG.bat,
   runSirRenderClientJOHN-MICHAEL.bat, etc.)  THIS IS TEMPORARY

4. Set the ennvironment variables on each server as desired.  This is only requirted if you want 
   to override the defaults.  I can change the defaults locations as desired once you determine
   the common network mapping location you will be using for all the servers.




Execution:
----------
1.  Start the server on all computers on the network that will be participating in the render farm (Ctrl-C to exit)

2.  Start the client application for the server you want to communicate with.  Again, this is temporary until
    the user interface GUI is developed.

3.  Press enter on the command line for a lite of avauilable commands (help, render, quit)

4.  On the client screen, type render and ENTER

5.  Enter the filename when prompted. This is the file name WITHOUT the ".blend" extension.  No directory paths allowed.

6.  Watch the server open a window as it renders the file.

7.  Only one file at a time currently.

8.  I will start to add statistics, monitoring or where the swervers are in the render process, etc. after the GUI is coimpleted.

9.  You may start or stop (quit) a client at anytime.  
    Once a server starts rendering a file, the client is no longer required.

10. The server will continue to run until you type CTRL-C in the server window to close the server.
    Once a server starts rendering a file, the server is no longer required however, I would leave
    it running so it will be availabler to accept the next client request.  Also, if you close
    the seerver while it is rendering a file, the render window will not automatically close and
    will have to be closed manually.

==================================================================================================

You also need to install the h2-setup-2017-03-10.exe file as it is the the new version of the manual database management tool similar to what we used in FireFox with Sqlite.  Once installed, you will need this for a connection string:

Saved Settings:  (you will select this)
Generic H2 Embedded

Settings Name: (you will type this and use it in future selects AFTER you save all the settings with the SAVE button)
SirRender H2 Database  

Driver Class:  (automatically populated based on the first selection)
org.h2.Driver

JDBC URL:
jdbc:h2:file:V:/SirRender2/databases/SirRender2Db;

User Name: (leave blank)

Password:  (leave blank)