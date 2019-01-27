package tv.culturesport.sirrender;

/*
 * Copyright (c) 2016, Dale Kubler. All rights reserved.
 *
 */

import java.net.*;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.io.*;
import tv.culturesport.sirrender.MultiServerThread;
import tv.culturesport.sirrender.GlobalClass;
import tv.culturesport.sirrender.H2;
import tv.culturesport.sirrender.MasterSlaveTimerThread;

public class ServerApp {
    public static void serverMain() throws IOException, SQLException {

        boolean listening = true;
        
    	// Create the MasterSlaveTimer Thread
        MasterMain.log.debug("Creating MasterSlaveTimer Thread");
        new MasterSlaveTimerThread().start();

		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        // Create the MasterSlavePoller Thread
        MasterMain.log.debug("Creating MasterSlavePoller Thread");
        new MasterSlavePollerThread().start();

		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        // Start listening on the server port
        try (ServerSocket serverSocket = new ServerSocket(GlobalClass.getPortNum())) {
			MasterMain.log.debug(ApplicationConstants.SIRRENDER_SERVER_TITLE + ApplicationConstants.SIRRENDER_SERVER_ANNOUNCE);
            while (listening) {
//            	if (GlobalClass.isSocketDebug() == true) {
//            		MasterMain.log.debug(ApplicationConstants.SIRRENDER_SERVER_NEW_THREAD);
//            	}
	            new MultiServerThread(serverSocket.accept()).start();
	        }
 	    } catch (IOException e) {
 	    	MasterMain.log.debug(ApplicationConstants.COULD_NOT_LISTEN_ON_PORT + GlobalClass.getPortNumberStr());
            System.exit(-1);
        }
    }
}