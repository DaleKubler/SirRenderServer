package tv.culturesport.sirrender;

/*
 * Copyright (c) 2016, Dale Kubler. All rights reserved.
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;


public class Utils {

	
	public static void log(String logLevel, String message) {
		//Logger log = LogManager.getLogger(Utils.class.getName());	//Logger.getLogger(className);
		//MasterMain.log.debug(getCurrentTimeForLog() + ": " + message);
		MasterMain.log.debug(message);
		//log.debug(message);
	}
	
	public static String getLowIpAddress() throws SocketException {
		String SirRenderIpAddress = System.getenv(ApplicationConstants.ENV_SIRRENDER_IP_ADDRESS);
		if (SirRenderIpAddress == null) {
			SirRenderIpAddress = "LOW";
		}
		//MasterMain.log.debug("SirRenderIpAddress="+SirRenderIpAddress);
		
		List<String> addrList = new ArrayList<String>();
		Enumeration<NetworkInterface> enumNI = NetworkInterface.getNetworkInterfaces();
		while (enumNI.hasMoreElements()) {
			NetworkInterface ifc = enumNI.nextElement();
			if (ifc.isUp()) {
				Enumeration<InetAddress> enumAdds = ifc.getInetAddresses();
				while (enumAdds.hasMoreElements()) {
					InetAddress addr = enumAdds.nextElement();
					if (addr.getHostAddress().startsWith("10.0.") && SirRenderIpAddress.equalsIgnoreCase("LOW")) {
						addrList.add(addr.getHostAddress());
					} else if (addr.getHostAddress().startsWith("192.168.") && SirRenderIpAddress.equalsIgnoreCase("HIGH")) {
						addrList.add(addr.getHostAddress());
					}
					//MasterMain.log.debug("IpAddress: " + addr.getHostAddress());
				}
			}
		}
		if (addrList.isEmpty()) {
			if (SirRenderIpAddress.equalsIgnoreCase("LOW")) {
				SirRenderIpAddress = "HIGH";
			} else {
				SirRenderIpAddress = "LOW";
			}
			enumNI = NetworkInterface.getNetworkInterfaces();
			while (enumNI.hasMoreElements()) {
				NetworkInterface ifc = enumNI.nextElement();
				if (ifc.isUp()) {
					Enumeration<InetAddress> enumAdds = ifc.getInetAddresses();
					while (enumAdds.hasMoreElements()) {
						InetAddress addr = enumAdds.nextElement();
						if (addr.getHostAddress().startsWith("10.0.") && SirRenderIpAddress.equalsIgnoreCase("LOW")) {
							addrList.add(addr.getHostAddress());
						} else if (addr.getHostAddress().startsWith("192.168.") && SirRenderIpAddress.equalsIgnoreCase("HIGH")) {
							addrList.add(addr.getHostAddress());
						}
						//MasterMain.log.debug("IpAddress: " + addr.getHostAddress());
					}
				}
			}
			Collections.sort(addrList);
			return addrList.get(0);
		} else {
			Collections.sort(addrList);
			return addrList.get(0);
		}
	}
	
    public static String pollServerStatus (String serverName, String serverIpAddress, Boolean useLocalIpAddress)  throws IOException, ConnectException, SQLException {

    	String status = "Off Line"; 
    	//MasterMain.log.debug("before calling isReachable="+serverIpAddress);
    	if (InetAddress.getByName(serverIpAddress).isReachable(2000)) {
    	//if (hostAvailabilityCheck(serverIpAddress)) {
        	//MasterMain.log.debug("inside if(isReachable)="+serverIpAddress);
    		if (clientToServerPollServerStatus(serverName, serverIpAddress)) {
		       	//MasterMain.log.debug("pollServerStatus="+"Available!");
		    	status = "Available";
		    	H2.setServerStatus(serverName, serverIpAddress, status);
		    	MasterMain.log.debug("Server " + serverName + " (" + serverIpAddress + ") is " + status);
		       	//H2.netSetServerStatus(serverName, serverIpAddress, "Available", useLocalIpAddress);
		    } else {
		       	//MasterMain.log.debug("pollServerStatus="+"Off Line!");
		    	H2.setServerStatus(serverName, serverIpAddress, status);
		    	MasterMain.log.debug("Server " + serverName + " (" + serverIpAddress + ") is " + status);
		    	//H2.netSetServerStatus(serverName, serverIpAddress, "Off Line", useLocalIpAddress);
		    }
	    } else {
        	//MasterMain.log.debug("outside if(isReachable)else="+serverIpAddress);
	       	//MasterMain.log.debug("pollServerStatus="+"Off Line!");
	    	H2.setServerStatus(serverName, serverIpAddress, status);
	    	MasterMain.log.debug("Server " + serverName + " (" + serverIpAddress + ") is " + status);
	    	//H2.netSetServerStatus(serverName, serverIpAddress, "Off Line", useLocalIpAddress);
	    }
    	return status;
    }
    
    public static boolean hostAvailabilityCheck(String host) {
    	boolean available = true;
    	try {
    		(new Socket(host, GlobalClass.getPortNum())).close();
    	} catch (UnknownHostException e) {
    		// unknown host
    		available = false;
    	} catch (IOException e) {
    		// io exception, service probably not running 
    		available = false;
    	}
     	return available;
    }    

    public static boolean clientToServerPollServerStatus(String hostName, String hostIpAddress) throws IOException, ConnectException {

    	String serverCmd = ApplicationConstants.SERVER_STATUS;
    	String fromServer = null;
    	byte[] inputData = new byte[1024];
    	boolean status = false;

    	if (!hostIpAddress.isEmpty()) {
    		serverCmd = serverCmd + hostIpAddress;
    	}

    	/*
    	if (!GlobalClass.getServerMasterIpAddress().isEmpty()) {
    		serverCmd = serverCmd + GlobalClass.getServerMasterIpAddress();
    	}
    	*/
    	
    	//MasterMain.log.debug("Starting clientToServerPollServerStatus for hostIpAddress "+hostIpAddress);
    	//MasterMain.log.debug("hostName="+hostName);
    	//MasterMain.log.debug("hostIpAddress="+hostIpAddress);
    	//MasterMain.log.debug("serverCmd="+serverCmd);
    	//MasterMain.log.debug("Attempting to connect to " + hostName + " " + hostIpAddress + ":" + GlobalClass.getPortNumberStr());

        //MasterMain.log.debug( "prior to the try/catch statement " + serverCmd + " to " + hostIpAddress);
    	Socket mcScoket = null;
    	try {
            //MasterMain.log.debug( "immdeiately before get socket " + serverCmd + " to " + hostIpAddress);
    		mcScoket = new Socket();
    		mcScoket.setTcpNoDelay(true);
    		// Timeout = 5,000 msec - Tests the ethernet connection and if ther server is listening on the port
    		mcScoket.connect(new InetSocketAddress(hostIpAddress, GlobalClass.getPortNum()),5*1000);
            //MasterMain.log.debug( "after socket connect timeout 1");
    		// Timeout = 15,000 msec - Tests the remote servers ability to provide a working socket
            mcScoket.setSoTimeout(15*1000);
            //MasterMain.log.debug( "after socket connect timeout 2");
	        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
            //MasterMain.log.debug( "after PrintWriter " + serverCmd + " to " + hostIpAddress);
            //MasterMain.log.debug( "sending " + serverCmd + " to " + hostIpAddress);
	    	out.println(serverCmd);
	    	out.flush();
            //MasterMain.log.debug( "after out " + serverCmd + " to " + hostIpAddress);
	        BufferedReader in = new BufferedReader(
	                new InputStreamReader(mcScoket.getInputStream()));
            //MasterMain.log.debug( "after in from buffered reader "+ hostIpAddress);
	        if ((fromServer = in.readLine()) != null) {
            //if (readInputStreamWithTimeout(mcScoket.getInputStream(), inputData, 2000) > 0) {
	            //MasterMain.log.debug(ApplicationConstants.SERVER + inputData.toString());
	            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
	            if (fromServer.startsWith(ApplicationConstants.SERVER_STATUS_OK)) {
	            //if (inputData.toString().startsWith(ApplicationConstants.SERVER_STATUS_OK)) {
	            	//MasterMain.log.debug(hostName + " is active");
	            	status = true;
				}
	        }
    	} catch (SocketTimeoutException e) {
    		//MasterMain.log.debug("Server TIMEOUT EXCEPTION " + hostIpAddress + ":" + GlobalClass.getPortNumberStr() + " is offline");
    		//return false;
    	} catch (ConnectException e) {
    		//MasterMain.log.debug("Server ConnectException EXCEPTION " + hostIpAddress + ":" + GlobalClass.getPortNumberStr() + " is offline");
    		//return false;
  	        //throw e;
    	} catch (IOException e) {
            try {
                if (e instanceof SocketTimeoutException) {
                	//throw new SocketTimeoutException();
            		//MasterMain.log.debug("Server SocketTimeoutException EXCEPTION " + hostIpAddress + ":" + GlobalClass.getPortNumberStr() + " is offline");
            		//return false;
                }
            } catch (Exception ee) {
            	// TODO
            }
        } finally {
        	mcScoket.close();
        }
        //MasterMain.log.debug( "immediately before return false " + serverCmd + " to " + hostIpAddress);
    	return status;
    }

	public static void clientToServerRenderFile(String hostName, String hostIpAddress, String fileName, String overrideOutputDir, String overrideRenderDevice) throws IOException {

    	String serverCmd = ApplicationConstants.RENDER_FILE+fileName+"|"+overrideOutputDir+"|"+overrideRenderDevice;
    	String fromServer = null;

    	if (GlobalClass.isServerProtocolDebug()) {
    		MasterMain.log.debug("Sending render request for " + fileName + " to " + hostIpAddress + ":" + GlobalClass.getPortNumberStr() + " (Overide Output Dir=" + overrideOutputDir +") [RenderDevice=" + overrideRenderDevice + "]");
    	}

    	try {
	        Socket mcScoket = new Socket(hostIpAddress, GlobalClass.getPortNum());
    		mcScoket.setTcpNoDelay(true);
	        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
	    	out.println(serverCmd);
	    	out.flush();
	        BufferedReader in = new BufferedReader(
	                new InputStreamReader(mcScoket.getInputStream()));
	        if ((fromServer = in.readLine()) != null) {
	            MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
	            if (fromServer.startsWith(ApplicationConstants.RENDER_PROCESS_STARTED_FOR)) {
	    	    	if (GlobalClass.isServerProtocolDebug()) {
	    	    		MasterMain.log.debug(ApplicationConstants.NEW_LINE + ApplicationConstants.RENDER_PROCESS_STARTED_ON_SERVER + hostName);
	    	    	}
				}
	        }
	        mcScoket.close();
    	} catch (SocketTimeoutException ste) {
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("Server TIMEOUT EXCEPTION" + hostIpAddress + ":" + GlobalClass.getPortNumberStr() + " is offline");
	    	}
    	} catch (ConnectException e) {
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("Server " + hostName + " is active");
	    	}
    	      //throw e;
    	} catch (IOException e) {
    	      e.printStackTrace();
    	}
    }
        
    /*
    public static int readInputStreamWithTimeout(InputStream is, byte[] b, int timeoutMillis) throws IOException  {
    	int bufferOffset = 0;
    	long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
    	while (System.currentTimeMillis() < maxTimeMillis && bufferOffset < b.length) {
    		int readLength = java.lang.Math.min(is.available(),b.length-bufferOffset);
    		// can alternatively use bufferedReader, guarded by isReady():
    		int readResult = is.read(b, bufferOffset, readLength);
    		if (readResult == -1) 
    			break;
    		bufferOffset += readResult;
    	}
    	return bufferOffset;
	}
    */
    
    public static String readMasterServerFile(String fileName) {
		try {
			FileReader reader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(reader);

			String line = bufferedReader.readLine();

			reader.close();
    		return line;
		} catch (FileNotFoundException e) {
			;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static void writeMasterServerFile(String fileName, String serverName, String ipAddress) {
		String output = serverName + "|" + ipAddress;
		try {
			FileWriter writer = new FileWriter(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(writer);

			bufferedWriter.write(output);
			bufferedWriter.close();			//FileWriter writer = new FileWriter(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteMasterServerFile(String fileName) {
		boolean success = (new File(fileName)).delete();
	}
	
    public static boolean readDebugFile(String fileName) {
		try {
			FileReader reader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(reader);

			String line = bufferedReader.readLine();

			reader.close();
			if (line.equalsIgnoreCase("TRUE")) {
				return(true);
			}
		} catch (FileNotFoundException e) {
			;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return(false);
	}
	
    public static boolean DetermineIfFileExists(String filename) {
    	 
   	    //create file object
   	    File file = new File(filename);
    	   
   	    /*
   	     * To determine whether specified file or directory exists, use
   	     * boolean exists() method of Java File class.
   	     *
   	     * This method returns true if a particular file or directory exists
   	     * at specified path in the filesystem, false otherwise.
   	     */
    	   
   	    boolean blnExists = file.exists();
    	   
   	    //MasterMain.log.debug("Does file " + file.getPath() + " exist ?: " + blnExists);
   	    return blnExists;
    }
    
    public static boolean DeleteFileOrDirectory(String filename) {
    	 
    	//create file object
   	    File file = new File(filename);
    	   
   	    /*
   	     * To delete a file or directory from filesystem, use
   	     * boolean delete() method of File class.
   	     *
   	     * This method returns true if file or directory successfully deleted. If
   	     * the file is a directory, it must be empty.
   	     */
    	   
   	    boolean blnDeleted = file.delete();
    	   
   	    //MasterMain.log.debug("Was file deleted ? : " + blnDeleted);
    	   
   	    /*
   	     * Please note that delete method returns false if the file did not exists or
   	     * the directory was not empty.
   	     */
   	    return blnDeleted;
    }
    
    public static long fileSize(String filename) {
    	long len = -1;
    	
        try {
            File file = new File(filename);
            if (!file.exists() || !file.isFile()) {
            	//MasterMain.log.debug("File does not exist");
            } else {
            	len = file.length();
            	//MasterMain.log.debug("File length = " + len);
            }
        } catch (Exception e) {
        }
        return len;
    }
    
	public static java.sql.Timestamp getCurrentTimeStamp() {
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime()/1000);
	}	
	
	public static java.sql.Timestamp getCurrentTime() {
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());
	}	
	
	public static String getCurrentTimeStampForLog() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate;
	}	
	
	public static String getCurrentTimeForLog() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate;
	}	
	
}