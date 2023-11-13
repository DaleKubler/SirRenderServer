package tv.culturesport.sirrender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import tv.culturesport.sirrender.ApplicationConstants;
import tv.culturesport.sirrender.GlobalClass;
import tv.culturesport.sirrender.Render;
import tv.culturesport.sirrender.Utils;


public class MasterSlavePollerThread extends Thread {

    public MasterSlavePollerThread() {
        super("MasterSlavePollerThread");
    }

    public void run() {

	    /**
	     * The data as an observable list of Servers.
	     */
	    List<Server> servers = new ArrayList<Server>();
	
	    String myIpAddress = "";
/*
		try {
			myIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
*/
	    
	    while (true) {
			try {
				myIpAddress = Utils.getLowIpAddress();
			} catch (SocketException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}

	    	// Get start time
	    	Long startTime = Utils.getCurrentTime().getTime();
	    	
	        // Determine if the poller should be running on this server
	        if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) || GlobalClass.isH2ServerMode()) {
	        	if (GlobalClass.isMasterServer()) {
		        	MasterMain.log.debug("Polling servers");
		        	
			        // Get server list from database
			        try {
						servers = H2.getServerList(false, true);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
	
			        /*
					for (int i = 0; i < servers.size(); i++) {
				    	MasterMain.log.debug("poll server list hostName="+servers.get(i).getServerName());
				    	MasterMain.log.debug("poll server list hostIpAddress="+servers.get(i).getServerIpAddress());
					}
					*/
	
					for (int i = 0; i < servers.size(); i++) {
						try {
					    	//MasterMain.log.debug("in polling loop hostName="+servers.get(i).getServerName());
					    	//MasterMain.log.debug("in polling loop hostIpAddress="+servers.get(i).getServerIpAddress());
							try {
								String status = Utils.pollServerStatus(servers.get(i).getServerName(), servers.get(i).getServerIpAddress(), false);
								//MasterMain.log.debug("pollServerStatus i="+i+"-"+servers.get(i).getServerName()+" ("+servers.get(i).getServerIpAddress()+") status="+status);
					    	} catch (SocketTimeoutException ste) {
					    	} catch (ConnectException e) {
					    	}
							//MasterMain.log.debug("before H2.validateRenderDbStatus i="+i);
							//MasterMain.log.debug("servers.get(i).getServerIpAddress()="+servers.get(i).getServerIpAddress());
							H2.validateRenderDbStatus(servers.get(i).getServerIpAddress());
							//MasterMain.log.debug("after H2.validateRenderDbStatus i="+i);
							//MasterMain.log.debug(""+"validateRenderDbStatus="+i);
						} catch (SQLException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	        	} else {
		        	MasterMain.log.debug("Updating server status");
		        	
		        	InetAddress IP = null;
					try {
						IP = InetAddress.getLocalHost();
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
		        	String hostName = IP.getHostName();

		        	try {
						H2.purgeStaleServers(hostName, myIpAddress);
					} catch (SQLException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
		        	
		        	String hostIpAddress = null;
					try {
						hostIpAddress = Utils.getLowIpAddress();
					} catch (SocketException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					// Poll self and update status
					try {
						String status = Utils.pollServerStatus(hostName, hostIpAddress, false);
						H2.validateRenderDbStatus(hostIpAddress);
					} catch (IOException | SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// Determine if any of the other servers status should be changed to "Off Line" based upon the StatusTime
			        // Get server list from database
			        try {
						servers = H2.getServerList(false, true);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
	
			        /*
					for (int i = 0; i < servers.size(); i++) {
				    	MasterMain.log.debug("poll server list hostName="+servers.get(i).getServerName());
				    	MasterMain.log.debug("poll server list hostIpAddress="+servers.get(i).getServerIpAddress());
					}
					*/
	
			        // Loop through the list of servers
					for (int i = 0; i < servers.size(); i++) {
						try {
							H2.checkServerStatusTimestamp(servers.get(i).getServerName(), servers.get(i).getServerIpAddress());
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	        	}
	        	
				try {
					//MasterMain.log.debug("Sleeping 2 seconds");
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }

	        // Determine if the local IP Address has changed.  If so, update the MasterIpAddress
	    	if (!myIpAddress.equals(GlobalClass.getServerMasterIpAddress())) {
				MasterMain.log.debug("Changing MasterServerIpAddress from "+GlobalClass.getServerMasterIpAddress()+" to "+myIpAddress);
	    		GlobalClass.setServerMasterIpAddress(myIpAddress);
	    	}
	    
	        // Determine if the scheduler should be running on this server
	        if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) || !GlobalClass.isMasterServer()) {
				MasterMain.log.debug("Running scheduler");
				try {
					if (GlobalClass.isMasterServer()) {
						serverScheduleMain(0);
					} else {
						serverScheduleSingle(0);
					}
				} catch (InterruptedException | SQLException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        } else {
	        	MasterMain.log.debug("Skipping the running of the schedule");	        	
	        }
	        
	        // Checkpoint the database as this is the Master Server
	        if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) || GlobalClass.isH2ServerMode()) {
		        try {
					H2.checkpointDatabase();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
	        
	    	// Get end time
	    	Long endTime = Utils.getCurrentTime().getTime();
	    	
	    	// Sleep time equals 60 less (endTime - startTime)/1000 seconds
	    	Long sleepTime = 60L - (endTime - startTime)/1000L;
	    	
			try {
				GlobalClass.setAboutExitCount(0);
				TimeUnit.SECONDS.sleep(sleepTime.intValue());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
        
    /**
     * Constructor
     * @return
     * @throws InterruptedException
     * @throws SQLException 
     * @throws IOException 
     */
	public static void serverScheduleMain(int timer) throws InterruptedException, SQLException, IOException {

    	String serverCmd = null;
    	String fromServer = null;
        String ipAddress = null;
        String fileName = null;
        String overrideOutputDir = null;
        String overrideRenderDevice = null;
    	boolean loop = true;

		List<Render> renders = new ArrayList<Render>();
		List<Render> serverRenders = new ArrayList<Render>();

        while (loop) {
        	// Get list of IP addresses that are available for a new render file
        	// List<Render> renders = H2.getScheduleTasks(true, "");
        	serverCmd = ApplicationConstants.RENDER_GET_SCHEDULE_IP_AVAILABLE_LIST;
        	fromServer = null;
	        ipAddress = null;
	        fileName = null;

        	//MasterMain.log.debug("Sending request for available and idle servers to " + hostIpAddress + ":" + GlobalClass.getPortNumberStr());

        	try {
    	        Socket mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
        		mcScoket.setTcpNoDelay(true);
    	        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
    	    	out.println(serverCmd);
    	    	out.flush();
    	        BufferedReader in = new BufferedReader(
                new InputStreamReader(mcScoket.getInputStream()));
    	        if ((fromServer = in.readLine()) != null) {
    	            //MasterMain.log.debug("fromServer 2=" + fromServer);
    	            //MasterMain.log.debug("ApplicationConstants.SERVER + fromServer);
                	String delims = "\\|";
                	String[] tokens = fromServer.split(delims);
                	for (int i=0; i < tokens.length; i++) {
    			        ipAddress = tokens[i];
    			        renders.add(new Render(ipAddress, "", "", ""));
                	}
    	        }
    	        mcScoket.close();
        	} catch (ConnectException e) {
                  //throw e;
        	} catch (IOException e) {
        	      e.printStackTrace();
        	}

            for (Render r: renders) {
            	MasterMain.log.debug("r-"+r.getIpAddress());
            }
            
            for (Render r: renders) {
                //MasterMain.log.debug("1-" + r.getIpAddress());
           		if (!r.getIpAddress().isEmpty()) {
	            	//List<Render> serverRenders = H2.getScheduleTasks(false, r.getIpAddress());
	            	serverCmd = ApplicationConstants.RENDER_GET_SCHEDULE_IP_FILE_AVAILABLE_LIST + r.getIpAddress();
	            	fromServer = null;
	
	            	//MasterMain.log.debug("Sending request for available render files in the queue for " + r.getIpAddress() + " to " + hostIpAddress + ":" + GlobalClass.getPortNumberStr());
	
	            	try {
	        	        //Socket mcScoket = new Socket("192.168.1.21", GlobalClass.getPortNum());
	        	        Socket mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
	            		mcScoket.setTcpNoDelay(true);
	        	        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
	        	    	out.println(serverCmd);
	        	    	out.flush();
	        	        BufferedReader in = new BufferedReader(
    	                new InputStreamReader(mcScoket.getInputStream()));
    	        		if ((fromServer = in.readLine()) != null) {
            	            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
                        	String delims = "\\|";
                        	String[] tokens = fromServer.split(delims);
                        	for (int i=0; i < tokens.length/2; i++) {
            			        ipAddress = tokens[i*4];
            			        fileName = tokens[i*4 + 1];
            			        overrideOutputDir = tokens[i*4 + 2];
            			        overrideRenderDevice = tokens[i*4 + 3];
            			        serverRenders.add(new Render(ipAddress, fileName, overrideOutputDir, overrideRenderDevice));
                        	}
    	        		}
    	    	        mcScoket.close();
	            	} catch (ConnectException e) {
	            	      throw e;
	            	} catch (IOException e) {
	            	      e.printStackTrace();
	            	}
	            }
            }

            /*
        	for (Render s: serverRenders) {
            	MasterMain.log.debug("s-"+s.getIpAddress()+" ["+s.getFileName()+"]");
        	}
        	*/
            
           	for (Render s: serverRenders) {
           		if (!s.getIpAddress().isEmpty()) {
                	Utils.clientToServerRenderFile(s.getIpAddress(), s.getIpAddress(), s.getFileName(), s.getOverrideOutputDir(), s.getOverrideRenderDevice());
           		}
        	}

           	if (timer > 0) {
            	TimeUnit.SECONDS.sleep(timer);
            } else {
            	loop = false;
            }
        }
    }

    /**
     * Constructor
     * @return
     * @throws InterruptedException
     * @throws SQLException 
     * @throws IOException 
     */
	public static void serverScheduleSingle(int timer) throws InterruptedException, SQLException, IOException {

    	String hostIpAddress = Utils.getLowIpAddress();
    	boolean loop = true;

		List<Render> renders = new ArrayList<Render>();
		List<Render> serverRenders = new ArrayList<Render>();

        while (loop) {
        	// Get list of IP addresses that are available for a new render file
        	renders = H2.getScheduleTasks(true, "");

        	// Get a file for this server's IP addresses that is available for a new render
            for (Render r: renders) {
           		if (!r.getIpAddress().isEmpty()) {
           			if (r.getIpAddress().equals(hostIpAddress)) {
           				serverRenders = H2.getScheduleTasks(false, r.getIpAddress());
           			}
	            }
            }

            /*
        	for (Render s: serverRenders) {
            	MasterMain.log.debug("s-"+s.getIpAddress()+" ["+s.getFileName()+"]");
        	}
        	*/
            
           	for (Render s: serverRenders) {
           		if (!s.getIpAddress().isEmpty()) {
           			serverRenderFile(s.getIpAddress(), s.getIpAddress(), s.getFileName(), s.getOverrideOutputDir(), s.getOverrideRenderDevice());
           		}
        	}

           	if (timer > 0) {
            	TimeUnit.SECONDS.sleep(timer);
            } else {
            	loop = false;
            }
        }
    }

	public static void serverRenderFile(String hostName, String hostIpAddress, String fileName, String overrideOutputDir, String overrideRenderDevice) throws IOException {
		String tmpFileName = H2.spawnRender(fileName, false, hostIpAddress, overrideOutputDir, overrideRenderDevice);
		String tmpFileNameVbs = tmpFileName.replace(".bat", ".vbs");
		
		// Currently the if and the else statements are identical
		if (MasterMain.cb3.getState()) {
			Process p = Runtime.getRuntime().exec( "wscript " + tmpFileNameVbs);
			//p.isAlive();
		} else {
			Process p = Runtime.getRuntime().exec( "wscript " + tmpFileNameVbs);
			// String[] command = {"cmd.exe", "/C", "Start", tmpFileName};
			// Process p =  Runtime.getRuntime().exec(command);
			//p.isAlive();
		}
		
		MasterMain.log.debug(ApplicationConstants.NEW_LINE + ApplicationConstants.RENDER_PROCESS_STARTED_ON_SERVER + hostName +
				" for " + fileName + " (" + tmpFileName + ")");
	}
	
}