package tv.culturesport.sirrender;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class MasterSlaveTimerThread extends Thread {
    public MasterSlaveTimerThread() {
        super("MasterSlaveTimerThread");
    }

    public void run() {
    	Boolean test = true;
    	InetAddress IP;
    	String myIpAddress = "";
    	String newMasterServerIpAddress = "";
    	
    	while (test) {
	    	// Get start time
	    	Long startTime = Utils.getCurrentTime().getTime();
	    	
			try {
		    	//IP = InetAddress.getLocalHost();
		    	//myIpAddress = IP.getHostAddress();
            	myIpAddress = Utils.getLowIpAddress();
            	MasterMain.log.debug("mypAddress="+myIpAddress);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// If this is the MasterServer or if running in H2 server mode, open a database connection and set the closeDB indicator
			// to false to allow the database connection to be reused without having to open and close with each occurrence.
			if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) || GlobalClass.isH2ServerMode()) {
				if (GlobalClass.getConection() == null) {
					GlobalClass.setConection(H2.Connector());
					GlobalClass.setCloseDB(false);
				} else {
					try {
						if (GlobalClass.getConection().isClosed()) {
							//MasterMain.log.debug("conection is closed - getting new connection in MasterSlaveTimerThread");
							GlobalClass.setConection(H2.Connector());
							GlobalClass.setCloseDB(false);
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			// If the MasterServer IP address could be pinged, get the status of the Suspend and Kill switches for the system tray.
			// The newMasterServerIpAddress equals "" when GlobalClass.isMasterServer() equals false.
			if (!newMasterServerIpAddress.equals("0.0.0.0")) {
				// Update the SirRender system tray suspend and kill switch status
		        // Determine initial status of Suspend Switch and Kill Switch
				String suspendKill = null;
		        try {
		        	if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) || GlobalClass.isH2ServerMode()) {
		        		suspendKill = H2.getServerSuspendSwitchStatus(myIpAddress) +
		        				"|" + H2.getServerKillSwitchStatus(myIpAddress) +
		        				"|" + H2.getServerBackgroundSwitchStatus(myIpAddress);
		        	} else {
		        		suspendKill = H2.netGetServerSuspendKillSwitchStatus(myIpAddress);
		        	}
					//suspendKill = H2.netGetServerSuspendKillSwitchStatus(myIpAddress);
				} catch (NumberFormatException | SQLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
		        
		        //MasterMain.log.debug("suspendKill="+suspendKill);
		        if (suspendKill.length() >= 1) {
			        if (suspendKill.substring(0,1).equals("1")) {
			        	MasterMain.cb1.setState(true);
			        } else {
			        	MasterMain.cb1.setState(false);
			        }
		        } else {
		        	MasterMain.cb1.setState(true);
		        }
		        
		        if (suspendKill.length() >= 3) {
			        if (suspendKill.substring(2,3).equals("1")) {
			        	MasterMain.cb2.setState(true);
			        } else {
			        	MasterMain.cb2.setState(false);
			        }
		        } else {
		        	MasterMain.cb2.setState(false);
		        }
		        
		        if (suspendKill.length() >= 5) {
			        if (suspendKill.substring(4,5).equals("1")) {
			        	MasterMain.cb3.setState(true);
			        } else {
			        	MasterMain.cb3.setState(false);
			        }
		        } else {
		        	MasterMain.cb3.setState(false);
		        }
			}

			// Determine if U:\SirRender\databases\SirRenderSocketDebug.txt exists
			// Determine if V:\SirRender\databases\SirRenderSocketDebug.txt exists
			// If it exists, read the contents
			GlobalClass.setSocketDebug(Utils.readDebugFile(ApplicationConstants.SIRRENDER_SOCKET_DEBUG));

			// Determine if U:\SirRender\databases\SirRenderServerProtocolDebug.txt exists
			// Determine if V:\SirRender\databases\SirRenderServerProtocolDebug.txt exists
			// If it exists, read the contents
			GlobalClass.setServerProtocolDebug(Utils.readDebugFile(ApplicationConstants.SIRRENDER_SERVER_PROTOCOL_DEBUG));

	    	// Get end time
	    	Long endTime = Utils.getCurrentTime().getTime();
	    	
	    	// Sleep time equals 60 less (endTime - startTime)/1000 seconds
	    	Long sleepTime = 60L - (endTime - startTime)/1000L;
	    	
			try {
				TimeUnit.SECONDS.sleep(sleepTime.intValue());
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    }
}