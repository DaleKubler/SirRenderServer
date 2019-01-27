package tv.culturesport.sirrender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.opencsv.CSVWriter;
import tv.culturesport.sirrender.Utils;
import tv.culturesport.sirrender.ApplicationConstants;

/*
 * Copyright (c) 2016, Dale Kubler. All rights reserved.
 *
 */

public class ServerProtocol {

	private static String fileName;
	private static String tmpFileName;
	private static String tmpName;
	private static String currentRenderStatusMsg = "";
	private static String hostIpAddress;

    public static String processInput(String theInput) throws SQLException, InterruptedException, IOException {

    	String theOutput = null;
        
    	//if (GlobalClass.isServerProtocolDebug()) {
    		MasterMain.log.debug("theInput2=|"+theInput+"|");
    	//}
        if (theInput.equalsIgnoreCase("QUIT")) {
            theOutput = "Bye.";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.GET_MASTER_SERVER_IP_ADDRESS)) {
			theOutput = GlobalClass.getServerMasterIpAddress();
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_FILE)) {
			try {
            	String phrase = theInput.substring(ApplicationConstants.RENDER_FILE.length());
            	String delims = "[|]+";
            	String[] tokens = phrase.split(delims);
            	fileName = tokens[0];
            	String overrideOutputDir = tokens[1];
            	String overrideRenderDevice = tokens[2];
				tmpFileName = H2.spawnRender(fileName, false, GlobalClass.getServerMasterIpAddress(), overrideOutputDir, overrideRenderDevice);
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
				
				theOutput = ApplicationConstants.RENDER_PROCESS_STARTED_FOR + fileName + " (" + tmpFileName + ")";
			} catch (IOException ex) {
			}
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_CMD_FILE)) {
			try {
				tmpFileName = theInput.substring(ApplicationConstants.RENDER_CMD_FILE.length());
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

				theOutput = ApplicationConstants.RENDER_PROCESS_STARTED_FOR + tmpFileName;
			} catch (IOException ex) {
			}
        } else if (theInput.toUpperCase().startsWith(ApplicationConstants.SERVER_STATUS)) {
        	/*
        	String tmp = theInput.substring(ApplicationConstants.SERVER_STATUS.length());
        	if (!tmp.isEmpty()) {
        		MasterMain.log.debug("SERVER_STATUS serverMasterIpAddress: " + tmp);            		
            	GlobalClass.setServerMasterIpAddress(tmp);
               	GlobalClass.setLastPollTime(Utils.getCurrentTime());
        	} else {
        		MasterMain.log.debug("SERVER_STATUS serverMasterIpAddress is empty: " + tmp);            		
        	}
        	*/
        	GlobalClass.setLastMasterServerPollTime(Utils.getCurrentTime());
			theOutput = ApplicationConstants.SERVER_STATUS_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_STATUS_MSG)) {
        	//tmpFileName = theInput.substring(ApplicationConstants.RENDER_STATUS.length());
			//theOutput = ApplicationConstants.RENDER_PROCESS_IN_PROGRESS_FOR + tmpFileName;
        	//String currentRenderStatusMsg = theInput.substring(ApplicationConstants.RENDER_STATUS_MSG.length());
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String phrase = theInput.substring(ApplicationConstants.RENDER_STATUS_MSG.length());
        	String delims = "[|]+";
        	String[] tokens = phrase.split(delims);
        	hostIpAddress = tokens[0];
        	currentRenderStatusMsg = tokens[1];
        	//MasterMain.log.debug("Status message=" + currentRenderStatusMsg);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
			H2.setServerLogDbMessage(hostIpAddress, currentRenderStatusMsg);
        	theOutput="";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_STATUS)) {
        	//tmpFileName = theInput.substring(ApplicationConstants.RENDER_STATUS.length());
			//theOutput = ApplicationConstants.RENDER_PROCESS_IN_PROGRESS_FOR + tmpFileName;
        	InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	hostIpAddress = Utils.getLowIpAddress();
        	currentRenderStatusMsg = H2.getServerLogDbMessage(hostIpAddress);
			theOutput = currentRenderStatusMsg;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.LOG_SERVERLOG_MESSAGE)) {
        	//String message = theInput.substring(ApplicationConstants.LOG_SERVERLOG_MESSAGE.length());
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	String phrase = theInput.substring(ApplicationConstants.LOG_SERVERLOG_MESSAGE.length());
        	String delims = "[|]+";
        	String[] tokens = phrase.split(delims);
        	hostIpAddress = tokens[0];
        	String message = tokens[1];
        	//MasterMain.log.debug("Message=" + Message);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("LOG_SERVERLOG_MESSAGE="+message);
	    	}
			H2.setServerLogDbMessage(hostIpAddress, message);
        	theOutput="";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_SERVER_QUEUE_ERROR_COUNT)) {
        	//String message = theInput.substring(ApplicationConstants.RENDER_UPDATE_SERVER_QUEUE_ERROR_COUNT.length());
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String phrase = theInput.substring(ApplicationConstants.RENDER_UPDATE_SERVER_QUEUE_ERROR_COUNT.length());
        	String delims = "[|]+";
        	String[] tokens = phrase.split(delims);
        	hostIpAddress = tokens[0];
        	String message = tokens[1];
        	String overrideOutputDir = tokens[2];
        	String overrideRenderDevice = tokens[3];
        	//MasterMain.log.debug("Message=" + Message);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("RENDER_UPDATE_SERVER_QUEUE_ERROR_COUNT="+message);
	    	}
			H2.updateServerQueueErrorCount(hostIpAddress, message, Integer.valueOf(overrideOutputDir), overrideRenderDevice);
        	theOutput="";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_ADD_SERVER_QUEUE_FILE)) {
        	//String message = theInput.substring(ApplicationConstants.RENDER_ADD_SERVER_QUEUE_FILE.length());
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();            	
        	String phrase = theInput.substring(ApplicationConstants.RENDER_ADD_SERVER_QUEUE_FILE.length());
        	String delims = "[|]+";
        	String[] tokens = phrase.split(delims);
        	hostIpAddress = tokens[0];
        	String fileName = tokens[1];
        	String overrideOutputDir = tokens[2];
        	String topOfFile = tokens[3];
        	String overrideRenderDevice = tokens[4];
        	//MasterMain.log.debug("Message=" + Message);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("RENDER_ADD_SERVER_QUEUE_FILE="+hostIpAddress+"-"+fileName+"-"+overrideOutputDir+"-"+topOfFile+"-"+overrideRenderDevice);
	    	}
			if (topOfFile.equals("0")) {
				H2.addFileToServerQueue(hostIpAddress, fileName, Integer.valueOf(overrideOutputDir), overrideRenderDevice);
			} else {
				H2.addFileToTopOfServerQueue(hostIpAddress, fileName, Integer.valueOf(overrideOutputDir), overrideRenderDevice);
			}
			theOutput=ApplicationConstants.DATABASE_UPDATE_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_DELETE_SERVER_QUEUE_FILE)) {
        	//String message = theInput.substring(ApplicationConstants.RENDER_DELETE_SERVER_QUEUE_FILE.length());
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();            	
        	String phrase = theInput.substring(ApplicationConstants.RENDER_DELETE_SERVER_QUEUE_FILE.length());
        	String delims = "[|]+";
        	String[] tokens = phrase.split(delims);
        	String serverIpAddress = tokens[0];
        	String fileName = tokens[1];
        	// Remove the <count> info at end of some file names
        	if (fileName.contains("<")) {
            	if (fileName.contains(">")) {
            		fileName = (fileName.substring(0, fileName.length()-5).trim());
            	}
        	}
        	String overrideOutputDir = tokens[2];
        	String overrideRenderDevice = tokens[3];
        	//MasterMain.log.debug("Message=" + Message);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("RENDER_DELETE_SERVER_QUEUE_FILE="+fileName+"-"+overrideOutputDir+"-"+overrideRenderDevice);
	    	}
			H2.deleteServerQueueFile(serverIpAddress, fileName, overrideOutputDir, overrideRenderDevice);
        	theOutput=ApplicationConstants.DATABASE_UPDATE_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_READ_SERVER_QUEUE)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	//MasterMain.log.debug("before readFileServerQueue(false)");
        	List<String> fileList = new ArrayList<String>();
        	fileList = H2.readFileServerQueue(false);
        	//MasterMain.log.debug("after readFileServerQueue(false)");
        	Boolean delimiterFlag = false;
        	tmpName = ApplicationConstants.RENDER_READ_SERVER_QUEUE;
        	//MasterMain.log.debug("before for for loop");
        	for (int i = 0; i < fileList.size(); i++) {
        		//MasterMain.log.debug(fileList.get(i));
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + fileList.get(i);
    		}        	
        	//MasterMain.log.debug("after for loop");
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.LOG_GET_SERVERLOG_SERVER_LIST)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	//MasterMain.log.debug("before getServerLogDbServerNames(false)");
        	List<String> serverNames = new ArrayList<String>();
        	serverNames = H2.getServerLogDbServerNames();
        	//MasterMain.log.debug("after getServerLogDbServerNames(false)");
        	Boolean delimiterFlag = false;
        	tmpName = ApplicationConstants.LOG_GET_SERVERLOG_SERVER_LIST;
        	//MasterMain.log.debug("before for for loop");
        	for (int i = 0; i < serverNames.size(); i++) {
        		//MasterMain.log.debug(serverNames.get(i));
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + serverNames.get(i);
    		}        	
        	//MasterMain.log.debug("after for loop");
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.LOG_GET_SERVERLOG_IP_ADDRESS_LIST)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	//MasterMain.log.debug("before getServerLogDbIpAddresses()");
        	List<String> serverIpAddresses = new ArrayList<String>();
        	serverIpAddresses = H2.getServerLogDbIpAddresses();
        	//MasterMain.log.debug("after getServerLogDbIpAddresses()");
        	Boolean delimiterFlag = false;
        	tmpName = ApplicationConstants.LOG_GET_SERVERLOG_IP_ADDRESS_LIST;
        	//MasterMain.log.debug("before for for loop");
        	for (int i = 0; i < serverIpAddresses.size(); i++) {
        		//MasterMain.log.debug(serverIpAddresses.get(i));
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + serverIpAddresses.get(i);
    		}        	
        	//MasterMain.log.debug("after for loop");
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.LOG_GET_SERVERLOG_BLEND_FILE_LIST)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	//MasterMain.log.debug("before getServerLogDbBlendFiles()");
        	List<String> blendFiles = new ArrayList<String>();
        	blendFiles = H2.getServerLogDbBlendFiles();
        	//MasterMain.log.debug("after getServerLogDbBlendFiles()");
        	Boolean delimiterFlag = false;
        	tmpName = ApplicationConstants.LOG_GET_SERVERLOG_BLEND_FILE_LIST;
        	//MasterMain.log.debug("before for for loop");
        	for (int i = 0; i < blendFiles.size(); i++) {
        		//MasterMain.log.debug(blendFiles.get(i));
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + blendFiles.get(i);
    		}        	
        	//MasterMain.log.debug("after for loop");
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.LOG_GET_SERVERLOG_LIST)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String phrase = theInput.substring(ApplicationConstants.LOG_GET_SERVERLOG_LIST.length());
    		//MasterMain.log.debug("phrase="+phrase);
        	String delims = "[|]+";
        	String[] tokens = phrase.split(delims);
        	String logStartDate = tokens[1];
        	String logEndDate = tokens[2];
        	String logServerName = tokens[3];
        	String logIpAddress = tokens[4];
        	String logMessage = tokens[5];
        	String logBlendFile = tokens[6];
    		//MasterMain.log.debug("logStartDate="+logStartDate);
    		//MasterMain.log.debug("logEndDate="+logEndDate);
    		//MasterMain.log.debug("logServerName="+logServerName);
    		//MasterMain.log.debug("logIpAddress="+logIpAddress);
    		//MasterMain.log.debug("logMessage="+logMessage);
    		//MasterMain.log.debug("logBlendFile="+logBlendFile);
        	//MasterMain.log.debug("before readServerRenderLog()");
    		List<ServerRenderLog> serverRenderLog = new ArrayList<ServerRenderLog>();
    		serverRenderLog = H2.readServerRenderLog(logStartDate, logEndDate, logServerName, logIpAddress, logMessage, logBlendFile, false, null);
        	//MasterMain.log.debug("after readServerRenderLog()");
        	Boolean delimiterFlag = false;
        	tmpName = ApplicationConstants.LOG_GET_SERVERLOG_LIST;
        	//MasterMain.log.debug("before for for loop");
        	for (int i = 0; i < serverRenderLog.size(); i++) {
        		//MasterMain.log.debug(serverRenderLog.get(i));
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + 
            			serverRenderLog.get(i).getLogIpAddress() + "|" +
            			serverRenderLog.get(i).getLogServerName() + "|" +
            			serverRenderLog.get(i).getLogStatMsg() + "|" +
            			serverRenderLog.get(i).getLogDate() + "|" +
            			serverRenderLog.get(i).getLogTime() + "|" +
            			serverRenderLog.get(i).getLogSeqNum();
    		}        	
        	//MasterMain.log.debug("after for loop");
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.CREATE_SERVERLOG_CSV_FILE)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String phrase = theInput.substring(ApplicationConstants.CREATE_SERVERLOG_CSV_FILE.length());
    		//MasterMain.log.debug("phrase="+phrase);
        	String delims = "[|]+";
        	String[] tokens = phrase.split(delims);
        	String logStartDate = tokens[1];
        	String logEndDate = tokens[2];
        	String logServerName = tokens[3];
        	String logIpAddress = tokens[4];
        	String logMessage = tokens[5];
        	String logBlendFile = tokens[6];
        	String csvFileName = tokens[7];
    		//MasterMain.log.debug("logStartDate="+logStartDate);
    		//MasterMain.log.debug("logEndDate="+logEndDate);
    		//MasterMain.log.debug("logServerName="+logServerName);
    		//MasterMain.log.debug("logIpAddress="+logIpAddress);
    		//MasterMain.log.debug("logMessage="+logMessage);
    		//MasterMain.log.debug("logBlendFile="+logBlendFile);
    		MasterMain.log.debug("csvFileName="+csvFileName);
        	//MasterMain.log.debug("before readServerRenderLog()");
    		List<ServerRenderLog> serverRenderLog = new ArrayList<ServerRenderLog>();
    		serverRenderLog = H2.readServerRenderLog(logStartDate, logEndDate, logServerName, logIpAddress, logMessage, logBlendFile, true, csvFileName);
        	//MasterMain.log.debug("after readServerRenderLog()");
    		
    		// Create CSV file
    		String csv = null;
    		if (csvFileName == null || csvFileName.equals("")) {
    			csv = ApplicationConstants.DEFAULT_SERVERLOG_CSV_FILE_NAME;
    		} else {
    			csv = csvFileName;
    		}
    		CSVWriter writer = new CSVWriter(new FileWriter(csv));

    		// Convert ArrayList<ServerRenderLog> to ArrayList<String[]>
    		List<String[]> csvServerRenderLog = new ArrayList<String[]>();

        	//MasterMain.log.debug("before for for loop");
        	for (int i = 0; i < serverRenderLog.size(); i++) {
        		csvServerRenderLog.add(new String[] {serverRenderLog.get(i).getLogDate(), serverRenderLog.get(i).getLogTime(), serverRenderLog.get(i).getLogServerName(), 
        										  serverRenderLog.get(i).getLogIpAddress(), serverRenderLog.get(i).getLogStatMsg(), 
        										  String.valueOf(serverRenderLog.get(i).getLogSeqNum())});
        	}
        	//MasterMain.log.debug("after for loop");

        	// Write the data to the csv file
    		writer.writeAll(csvServerRenderLog);

    		// Close the csv file
    		writer.close();
    		
            //MasterMain.log.debug("tmpName=" + tmpName);
        	tmpName = ApplicationConstants.CREATE_SERVERLOG_CSV_FILE;
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.SERVER_GET_SERVERS)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	//MasterMain.log.debug("before readFileServerQueue(false)");
        	List<Server> servers = new ArrayList<Server>();
        	servers = H2.getServerList(false, true);
        	//MasterMain.log.debug("after readFileServerQueue(false)");
        	Boolean delimiterFlag = false;
        	tmpName = ApplicationConstants.SERVER_GET_SERVERS;
        	//MasterMain.log.debug("before for for loop");
        	for (int i = 0; i < servers.size(); i++) {
    			//MasterMain.log.debug(servers.get(i).getServerIpAddress());
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + 
            			servers.get(i).getServerName() + "|" +
            			servers.get(i).getServerIpAddress() + "|" +
            			servers.get(i).getServerAvailable() + "|" +
            			servers.get(i).getServerKillSwitch() + "|" +
            			servers.get(i).getServerSuspend() + "|" +
            			servers.get(i).getServerBackground();
    		}        	
        	//MasterMain.log.debug("after for loop");
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.SERVER_GET_RENDERING_FILE_STATUS)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String phrase = theInput.substring(ApplicationConstants.SERVER_GET_RENDERING_FILE_STATUS.length());
        	Boolean distinctFiles = false;
        	if (phrase.equals("1")) {
        		distinctFiles = true;
        		fileName = "";
        	} else {
        		fileName = phrase;
        	}

        	List<RenderFileStatus> renderedFiles = new ArrayList<RenderFileStatus>();
        	
        	// Get files from ACTIVE Servers
        	//MasterMain.log.debug("before getRenderedFileStatusList(distinctFiles)");
        	renderedFiles = H2.getRenderedFileStatusList(distinctFiles, fileName);
        	//MasterMain.log.debug("after getRenderedFileStatusList(distinctFiles)");
        	
        	Boolean delimiterFlag = false;
        	tmpName = ApplicationConstants.SERVER_GET_RENDERING_FILE_STATUS;
        	//MasterMain.log.debug("before for for loop");
        	for (int i = 0; i < renderedFiles.size(); i++) {
        		//MasterMain.log.debug("renderedFiles.get(i).getServerIpAddress()="+renderedFiles.get(i).getServerIpAddress());
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + 
            			renderedFiles.get(i).getFileName() + "|" +
            			renderedFiles.get(i).getRenderStatus() + "|" +
            			renderedFiles.get(i).getServerName() + "|" +
            			renderedFiles.get(i).getServerIpAddress() + "|" +
            			renderedFiles.get(i).getCurrentFile() + "|" +
            			renderedFiles.get(i).getFrameStart() + "|" +
            			renderedFiles.get(i).getFrameEnd() + "|" +
            			renderedFiles.get(i).getFrameCount() + "|" +
            			renderedFiles.get(i).getCurrentFrame() + "|" +
            			renderedFiles.get(i).getRenderFrameCount() + "|" +
            			renderedFiles.get(i).getRenderDefaultHangTime() + "|" +
            			renderedFiles.get(i).getMasterSlavePriority() + "|" +
            			renderedFiles.get(i).getRenderOutDir();
    		}
        	//MasterMain.log.debug("after for loop");
        	
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.SERVER_GET_RENDERING_FILE_COUNTS)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String phrase = theInput.substring(ApplicationConstants.SERVER_GET_RENDERING_FILE_COUNTS.length());
        	Boolean distinctFiles = false;
        	if (phrase.equals("1")) {
        		distinctFiles = true;
        		fileName = "";
        	} else {
        		fileName = phrase;
        	}
        	String fileName = theInput.substring(ApplicationConstants.SERVER_GET_RENDERING_FILE_COUNTS.length());

        	List<RenderFileStatusCounts> renderFileStatusCounts = new ArrayList<RenderFileStatusCounts>();
        	
        	// Get the file statistics for fileNasme
        	//MasterMain.log.debug("before getRenderedFileStatusCounts(false, fileName)");
        	renderFileStatusCounts = H2.getRenderedFileStatusCounts(false, fileName);
        	//MasterMain.log.debug("after getRenderedFileStatusCounts(false, fileName)");
        	
        	Boolean delimiterFlag = false;
        	tmpName = ApplicationConstants.SERVER_GET_RENDERING_FILE_COUNTS;
        	//MasterMain.log.debug("before for loop");
        	for (int i = 0; i < renderFileStatusCounts.size(); i++) {
        		//MasterMain.log.debug("renderFileStatusCounts.get(i).getCurrentFrame()="+renderFileStatusCounts.get(i).getCurrentFrame());
        		//MasterMain.log.debug("renderFileStatusCounts.get(i).getFrameEnd()="+renderFileStatusCounts.get(i).getFrameEnd());
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + 
            			renderFileStatusCounts.get(i).getCurrentFile() + "|" +
            			renderFileStatusCounts.get(i).getFrameStart() + "|" +
            			renderFileStatusCounts.get(i).getFrameEnd() + "|" +
            			renderFileStatusCounts.get(i).getFrameCount() + "|" +
            			renderFileStatusCounts.get(i).getCurrentFrame();
    		}
        	//MasterMain.log.debug("after for loop");
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_START_STATS)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String phrase = theInput.substring(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_START_STATS.length());
        	String delims = "[|]+";
        	String[] tokens = phrase.split(delims);
        	
        	for (int i = 0; i < tokens.length; i++)
        		//MasterMain.log.debug(tokens[i]);

        	hostIpAddress = tokens[0];
        	int sfs = Integer.valueOf(tokens[1]);
        	int sfe  = Integer.valueOf(tokens[2]);
        	int sfc  = Integer.valueOf(tokens[3]);
        	int cff  = Integer.valueOf(tokens[4]);
        	String cfn = tokens[5];
        	String odir = tokens[6];
        	
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_START_STATS);
	    	}
			H2.updateServerStatusStartStatistics(hostIpAddress, sfs, sfe, sfc, cff, cfn, odir);
        	theOutput="";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_STATUS)) {
        	//String currentFrameNumber = theInput.substring(ApplicationConstants.RENDER_UPDATE_CURRENT_FRAME.length());
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String phrase = theInput.substring(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_STATUS.length());
         	String delims = "[|]+";
         	String[] tokens = phrase.split(delims);
         	String hostName = tokens[0];
         	hostIpAddress = tokens[1];
         	String status = tokens[2];
        	//MasterMain.log.debug("Status=" + status);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("RENDER_UPDATE_SERVER_STATUS_STATUS="+status);
	    	}
			//H2.updateServerStatusStatus(hostName, hostIpAddress, status);
			H2.setServerStatus(hostName, hostIpAddress, status);
        	theOutput="ApplicationConstants.DATABASE_UPDATE_OK";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_SUSPEND_KILL_SWITCH)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String phrase = theInput.substring(ApplicationConstants.RENDER_UPDATE_SUSPEND_KILL_SWITCH.length());
         	String delims = "[|]+";
         	String[] tokens = phrase.split(delims);
         	String hostName = tokens[0];
         	hostIpAddress = tokens[1];
         	int serverKillSwitch = Integer.valueOf(tokens[2]);
         	int serverSuspendStatus = Integer.valueOf(tokens[3]);
         	int backgroundRenderStatus = Integer.valueOf(tokens[4]);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("RENDER_UPDATE_SUSPEND_KILL_SWITCH="+tokens[0]+"-"+tokens[1]+"-"+tokens[2]+"-"+tokens[3]+"-"+tokens[4]);
	    	}
			H2.updateServerSuspendKillSwitchStatus(hostName, hostIpAddress, serverKillSwitch, serverSuspendStatus, backgroundRenderStatus);
        	theOutput=ApplicationConstants.DATABASE_UPDATE_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_SUSPEND_SWITCH)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String phrase = theInput.substring(ApplicationConstants.RENDER_UPDATE_SUSPEND_SWITCH.length());
         	String delims = "[|]+";
         	String[] tokens = phrase.split(delims);
         	String hostName = tokens[0];
         	hostIpAddress = tokens[1];
         	int serverSuspendStatus = Integer.valueOf(tokens[2]);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("RENDER_UPDATE_SUSPEND_SWITCH="+tokens[0]+"-"+tokens[1]+"-"+tokens[2]);
	    	}
			H2.updateServerSuspendSwitchStatus(hostName, hostIpAddress, serverSuspendStatus);
        	theOutput=ApplicationConstants.DATABASE_UPDATE_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_KILL_SWITCH)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String phrase = theInput.substring(ApplicationConstants.RENDER_UPDATE_KILL_SWITCH.length());
         	String delims = "[|]+";
         	String[] tokens = phrase.split(delims);
         	String hostName = tokens[0];
         	hostIpAddress = tokens[1];
         	int serverKillSwitchStatus = Integer.valueOf(tokens[2]);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("RENDER_UPDATE_KILL_SWITCH="+tokens[0]+"-"+tokens[1]+"-"+tokens[2]);
	    	}
			H2.updateServerKillSwitchStatus(hostName, hostIpAddress, serverKillSwitchStatus);
        	theOutput=ApplicationConstants.DATABASE_UPDATE_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_BACKGROUND_SWITCH)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String phrase = theInput.substring(ApplicationConstants.RENDER_UPDATE_BACKGROUND_SWITCH.length());
         	String delims = "[|]+";
         	String[] tokens = phrase.split(delims);
         	String hostName = tokens[0];
         	hostIpAddress = tokens[1];
         	int serverBackgroundSwitchStatus = Integer.valueOf(tokens[2]);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("RENDER_UPDATE_BACKGROUND_SWITCH="+tokens[0]+"-"+tokens[1]+"-"+tokens[2]);
	    	}
			H2.updateServerBackgroundSwitchStatus(hostName, hostIpAddress, serverBackgroundSwitchStatus);
        	theOutput=ApplicationConstants.DATABASE_UPDATE_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_GET_SUSPEND_KILL_SWITCH)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	hostIpAddress = theInput.substring(ApplicationConstants.RENDER_GET_SUSPEND_KILL_SWITCH.length());
        	//MasterMain.log.debug("before getServerSuspendSwitchStatus(hostIpAddress)");
        	String suspendSwitch = H2.getServerSuspendSwitchStatus(hostIpAddress);
        	//MasterMain.log.debug("after getServerSuspendSwitchStatus(hostIpAddress)");
        	//MasterMain.log.debug("before getServerKillSwitchStatus(hostIpAddress)");
        	String killSwitch = H2.getServerKillSwitchStatus(hostIpAddress);
        	//MasterMain.log.debug("after getServerKillSwitchStatus(hostIpAddress)");
        	String backgroundSwitch = H2.getServerBackgroundSwitchStatus(hostIpAddress);
        	//MasterMain.log.debug("after getServerBackgroundSwitchStatus(hostIpAddress)");
        	tmpName = ApplicationConstants.RENDER_GET_SUSPEND_KILL_SWITCH;
           	tmpName = tmpName + suspendSwitch + "|" + killSwitch + "|" + backgroundSwitch;
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.SERVER_SET_MASTER_SERVER_IP_ADDRESS)) {
        	hostIpAddress = theInput.substring(ApplicationConstants.SERVER_SET_MASTER_SERVER_IP_ADDRESS.length());
        	//String ????? = H2.??????(hostIpAddress);
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = "";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_CURRENT_FRAME)) {
        	//String currentFrameNumber = theInput.substring(ApplicationConstants.RENDER_UPDATE_CURRENT_FRAME.length());
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String phrase = theInput.substring(ApplicationConstants.RENDER_UPDATE_CURRENT_FRAME.length());
         	String delims = "[|]+";
         	String[] tokens = phrase.split(delims);
         	hostIpAddress = tokens[0];
         	String currentFrameNumber = tokens[1];
        	//MasterMain.log.debug("Frame Number=" + currentFrameNumber);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("RENDER_UPDATE_CURRENT_FRAME="+currentFrameNumber);
	    	}
			H2.updateServerStatusCurrentFrame(hostIpAddress, Integer.valueOf(currentFrameNumber));
        	theOutput="";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_CANCEL)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String phrase = theInput.substring(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_CANCEL.length());
         	String delims = "[|]+";
         	String[] tokens = phrase.split(delims);
         	hostIpAddress = tokens[0];
         	String fileName = tokens[1];
         	int overrideOutputDir = Integer.valueOf(tokens[2]);
         	String overrideRenderDevice = tokens[3];
         	
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_CANCEL);
	    	}
			H2.updateServerStatusCancel(hostIpAddress);
			H2.decreaseServerQueueErrorCount(hostIpAddress, fileName, overrideOutputDir, overrideRenderDevice);

			// Delete any remaining zero byte files - locked files will be skipped
			String filePath = H2.getRenderedFilePath(hostIpAddress);
			//MasterMain.log.debug("filePath in ServerProtocol = " + filePath);
			
			File[] files = new File(filePath).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(ApplicationConstants.PNG_FILE_EXTENSION);
				}
			});
			
			for (File file : files) {
			    if (file.isFile()) {
					//MasterMain.log.debug("fileName = " + file.getAbsolutePath());
			    	if (Utils.fileSize(file.getAbsolutePath()) == 0l) {
			    		//MasterMain.log.debug("Deleting file: " + file);
			    		Utils.DeleteFileOrDirectory(file.getAbsolutePath());
			    	}
			    }
			}
        	theOutput="";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_COMPLETE)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String hostIpAddress = theInput.substring(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_COMPLETE.length());
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug(ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_COMPLETE);
	    	}
			H2.updateServerStatusComplete(hostIpAddress);
        	theOutput="";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_RESET_SERVER_STATUS_KILL_SWITCH)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String hostIpAddress = theInput.substring(ApplicationConstants.RENDER_RESET_SERVER_STATUS_KILL_SWITCH.length());
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug(ApplicationConstants.RENDER_RESET_SERVER_STATUS_KILL_SWITCH);
	    	}
			H2.resetServerStatusKillSwitch(hostIpAddress);
        	theOutput="";
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_SERVER_STATUS_SELECT_KILL_SWITCH)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
         	String hostIpAddress = theInput.substring(ApplicationConstants.RENDER_SERVER_STATUS_SELECT_KILL_SWITCH.length());
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug(ApplicationConstants.RENDER_SERVER_STATUS_SELECT_KILL_SWITCH);
	    	}
			//H2.getServerKillSwitchStatus(hostIpAddress);
        	//theOutput="";
			theOutput=H2.getServerKillSwitchStatus(hostIpAddress);
	    	if (GlobalClass.isServerProtocolDebug()) {
	    		MasterMain.log.debug("ClientServer theOutput="+theOutput);
	    	}
        	//MasterMain.log.debug("ClientServer theOutput.length()="+theOutput.length());

        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_GET_SCHEDULE_IP_AVAILABLE_LIST)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	//MasterMain.log.debug("before getScheduleTasks(true)");
        	List<Render> renders = H2.getScheduleTasks(true, "");
        	//MasterMain.log.debug("after getScheduleTasks(true)");
        	Boolean delimiterFlag = false;
        	tmpName = "";
        	//MasterMain.log.debug("before for Render r loop");
            for (Render r: renders) {
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + r.getIpAddress();
            }
        	//MasterMain.log.debug("after for Render r loop");
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_GET_SCHEDULE_IP_FILE_AVAILABLE_LIST)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String ipAddress = theInput.substring(ApplicationConstants.RENDER_GET_SCHEDULE_IP_FILE_AVAILABLE_LIST.length());
        	List<Render> serverRenders = H2.getScheduleTasks(false, ipAddress);
        	Boolean delimiterFlag = false;
        	tmpName = "";
            for (Render r: serverRenders) {
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName + r.getIpAddress() + "|" + r.getFileName() + "|" + r.getOverrideOutputDir() + "|" + r.getOverrideRenderDevice();
            }
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_POLL_SCHEDULE_QUEUE)) {
			//MasterMain.log.debug("RENDER_POLL_SCHEDULE_QUEUE started");
        	pollServerSchedule(0);
			//MasterMain.log.debug("RENDER_POLL_SCHEDULE_QUEUE completed");
        } else if (theInput.toUpperCase().contains(ApplicationConstants.RENDER_GET_COMPUTER_IP_ALL_FILES_LIST)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String ipAddress = theInput.substring(ApplicationConstants.RENDER_GET_COMPUTER_IP_ALL_FILES_LIST.length());
        	List<Render> serverRenders = H2.getComputerTasks(ipAddress);
        	Boolean delimiterFlag = false;
        	tmpName = "";
            for (Render r: serverRenders) {
            	if (delimiterFlag) {
            		tmpName = tmpName + "|";
            	} else {
            		delimiterFlag = true;
            	}
            	tmpName = tmpName 
            			+ r.getIpAddress() + "|" 
            			+ r.getFileName() + "|" 
            			+ r.getErrorCount() + "|" 
            			+ r.getRowId() + "|" 
            			+ r.getOverrideOutputDir() + "|" 
            			+ r.getOverrideRenderDevice();
            }
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.PURGE_STALE_SERVERS)) {
        	//String message = theInput.substring(ApplicationConstants.LOG_SERVERLOG_MESSAGE.length());
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	String phrase = theInput.substring(ApplicationConstants.PURGE_STALE_SERVERS.length());
        	String delims = "[|]+";
        	String[] tokens = phrase.split(delims);
        	String hostName = tokens[0];
        	hostIpAddress = tokens[1];
        	//MasterMain.log.debug("Message=" + Message);
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
			H2.purgeStaleServers(hostName, hostIpAddress);
        	theOutput=ApplicationConstants.DATABASE_UPDATE_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.INSERT_DUMMY_SERVER_QUEUE_FILE)) {
        	//String message = theInput.substring(ApplicationConstants.LOG_SERVERLOG_MESSAGE.length());
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//MasterMain.log.debug("IP address of my system="+hostIpAddress);
        	H2.insertDummyServerQueueFile();
        	theOutput=ApplicationConstants.DATABASE_UPDATE_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.SERVER_GET_SERVER_DATA)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String phrase = theInput.substring(ApplicationConstants.SERVER_GET_SERVER_DATA.length());
        	Boolean distinctServers = false;
        	String requestedServerName = null; 
        	String requestedServerIpAddress = null;
        	if (phrase.equals("1")) {
        		distinctServers = true;
        	} else {
             	phrase = theInput.substring(ApplicationConstants.SERVER_GET_SERVER_DATA.length());
             	String delims = "[|]+";
             	String[] tokens = phrase.split(delims);
             	requestedServerName = tokens[0];
             	requestedServerIpAddress = tokens[1];
            	//MasterMain.log.debug("requestedServerName==" + requestedServerName);
            	//MasterMain.log.debug("requestedServerIpAddress="+requestedServerIpAddress);
        	}

        	List<ServerStatus> serverStatuses = new ArrayList<ServerStatus>();
        	
        	// Get server data
        	//MasterMain.log.debug("before getServerStatusList(distinctServers, requestedServerName, requestedServerIpAddress)");
        	serverStatuses = H2.getServerStatusList(distinctServers, requestedServerName, requestedServerIpAddress);
        	//MasterMain.log.debug("after distinctServers, requestedServerName, requestedServerIpAddress");

        	tmpName = ApplicationConstants.SERVER_GET_SERVER_DATA;

        	if (distinctServers) {
            	Boolean delimiterFlag = false;
            	//MasterMain.log.debug("before for for loop");
            	for (int i = 0; i < serverStatuses.size(); i++) {
            		//MasterMain.log.debug("serverStatuses.get(i).getServerName()="+serverStatuses.get(i).getServerName());
            		//MasterMain.log.debug("serverStatuses.get(i).getServerIpAddress()="+serverStatuses.get(i).getServerIpAddress());
                	if (delimiterFlag) {
                		tmpName = tmpName + "|";
                	} else {
                		delimiterFlag = true;
                	}
                	tmpName = tmpName + 
                			serverStatuses.get(i).getServerName() + "|" +
                			serverStatuses.get(i).getServerIpAddress();
        		}
            	//MasterMain.log.debug("after for loop");
        	} else {
        		//MasterMain.log.debug("serverStatuses.get(0).getServerName()="+serverStatuses.get(00).getServerName());
        		//MasterMain.log.debug("serverStatuses.get(0).getServerIpAddress()="+serverStatuses.get(0).getServerIpAddress());
            	tmpName = tmpName + 
            			serverStatuses.get(0).getServerName() + "|" +
            			serverStatuses.get(0).getServerIpAddress() + "|" +
            			serverStatuses.get(0).getServerStatus() + "|" +
            			serverStatuses.get(0).getServerStatusTimestamp() + "|" +
            			serverStatuses.get(0).getServerCurrentFile() + "|" +
            			serverStatuses.get(0).getServerRenderStart() + "|" +
            			serverStatuses.get(0).getServerRenderEnd() + "|" +
            			serverStatuses.get(0).getServerRenderLastUpdate() + "|" +
            			serverStatuses.get(0).getServerStartFrame() + "|" +
            			serverStatuses.get(0).getServerEndFrame() + "|" +
            			serverStatuses.get(0).getServerFrameCount() + "|" +
            			serverStatuses.get(0).getServerCurrentFrame() + "|" +
            			serverStatuses.get(0).getServerCumulativeFrameCount() + "|" +
            			serverStatuses.get(0).getServerOutputFile() + "|" +
            			serverStatuses.get(0).getServerKillSwitch() + "|" +
            			serverStatuses.get(0).getServerSuspendSwitch() + "|" +
            			serverStatuses.get(0).getServerBackgroundRender() + "|" +
            			serverStatuses.get(0).getServerHangTimeThreshold();
        	}
        	
            //MasterMain.log.debug("tmpName=" + tmpName);
			theOutput = tmpName;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.SERVER_DELETE_SERVER_DATA)) {
        	//InetAddress IP = InetAddress.getLocalHost();
        	//hostIpAddress = IP.getHostAddress();
        	//hostIpAddress = Utils.getLowIpAddress();
        	String deletedServerName = null; 
        	String deletedServerIpAddress = null;
         	String phrase = theInput.substring(ApplicationConstants.SERVER_DELETE_SERVER_DATA.length());
         	String delims = "[|]+";
         	String[] tokens = phrase.split(delims);
         	deletedServerName = tokens[0];
         	deletedServerIpAddress = tokens[1];
        	//MasterMain.log.debug("deletedServerName==" + deletedServerName);
        	//MasterMain.log.debug("deletedServerIpAddress="+deletedServerIpAddress);

        	// Delete server data
        	//MasterMain.log.debug("before deleteServerStatusData(deletedServerName, deletedServerIpAddress)");
        	H2.deleteServerStatusData(deletedServerName, deletedServerIpAddress);
        	//MasterMain.log.debug("after deletedServerName, deletedServerIpAddress");
			theOutput = ApplicationConstants.DATABASE_UPDATE_OK;
        } else if (theInput.toUpperCase().contains(ApplicationConstants.SERVER_SAVE_SERVER_DATA)) {
        	String phrase = theInput.substring(ApplicationConstants.SERVER_SAVE_SERVER_DATA.length());
        	//MasterMain.log.debug("phrase1="+phrase);
        	phrase = phrase.replace("||", "| |");
        	//MasterMain.log.debug("phrase2="+phrase);
        	phrase = phrase.replace("||", "| |");
        	//MasterMain.log.debug("phrase3="+phrase);
        	phrase = phrase.replace("null", " ");
        	//MasterMain.log.debug("phrase4="+phrase);
         	String delims = "[|]+";
         	String[] tokens = phrase.split(delims);
         	
         	/*
         	for (int i=0; i < 18; i++) {
         		MasterMain.log.debug("token["+i+"]="+tokens[i]+"END");
         	}
         	*/
         	
         	H2.saveServerStatusData(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5],
         							tokens[6], tokens[7], tokens[8], tokens[9], tokens[10], tokens[11],
         							tokens[12], tokens[13], tokens[14], tokens[15], tokens[16], tokens[17]);
         			
         	/*		
	       	phrase = ApplicationConstants.SERVER_SAVE_SERVER_DATA + serverName 
        			+ "|" + serverIpAddress 
        			+ "|" + status 
        			+ "|" + lastStatusUpdate
        			+ "|" + currentRenderFile
        			+ "|" + renderStartTime 
        			+ "|" + renderEndTime 
        			+ "|" + lastRenderUpdate
        			+ "|" + startFrame
        			+ "|" + endFrame 
        			+ "|" + frameCount 
        			+ "|" + currentFrame
        			+ "|" + cumulativeRenderFrameCount
        			+ "|" + renderOutputLocationAndFileName 
        			+ "|" + hangTimeThreshold 
        			+ "|" + suspendSwitch
        			+ "|" + killSwitch
        			+ "|" + backgroundSwitch;
         	*/
         	
			theOutput = ApplicationConstants.DATABASE_UPDATE_OK;
        } else {
            theOutput = ApplicationConstants.SERVER_STATUS_OK;
        }
    	if (GlobalClass.isServerProtocolDebug() && !theOutput.isEmpty()) {
    		MasterMain.log.debug("theOutput=" + theOutput);
    	}
        return theOutput;
    }	
	
	public static void pollServerSchedule(int timer) throws InterruptedException, SQLException, IOException {

		InetAddress IP;
		String hostIpAddress;
		boolean loop = true;

		IP = InetAddress.getLocalHost();
    	//hostIpAddress = IP.getHostAddress();
    	hostIpAddress = Utils.getLowIpAddress();
    	
        while (loop) {
        	//MasterMain.log.debug("loop="+loop);
        	
        	// Get list of IP addresses that are available for a new render file
        	List<Render> renders = H2.getScheduleTasks(true, "");

            for (Render r: renders) {
    	    	if (GlobalClass.isServerProtocolDebug()) {
    	    		MasterMain.log.debug("r-"+r.getIpAddress());
    	    	}
            }
            for (Render r: renders) {
                //MasterMain.log.debug("1-" + r.getIpAddress());
                
            	List<Render> serverRenders = H2.getScheduleTasks(false, r.getIpAddress());

            	for (Render s: serverRenders) {
        	    	if (GlobalClass.isServerProtocolDebug()) {
        	    		MasterMain.log.debug("s-"+s.getIpAddress()+" ["+s.getFileName()+"]"+" ("+s.getOverrideOutputDir()+") <"+s.getOverrideRenderDevice()+">");
        	    	}
            	}
               	for (Render s: serverRenders) {
                    //MasterMain.log.debug("s-"+s.getIpAddress()+" ["+s.getFileName()+"]"+" ("+s.getOverriderOutputDir()+") <"+s.getOverrideRenderDevice()+">");
                    if (s.getIpAddress().equals(hostIpAddress)) {
                    	H2.spawnRender(s.getFileName(),false, GlobalClass.getServerMasterIpAddress(), s.getOverrideOutputDir(), s.getOverrideRenderDevice());
                    } else {
                    	Utils.clientToServerRenderFile(s.getIpAddress(), s.getIpAddress(), s.getFileName(), s.getOverrideOutputDir(), s.getOverrideRenderDevice());	
                    }
            	}
            }
            /*
			for (int i = 0; i < serverData.size(); i++) {
				String status;
				try {
					status = Utils.pollServerStatus(serverData.get(i).getServerName(), serverData.get(i).getServerIpAddress(), false);
					//MasterMain.log.debug("pollServerStatus="+i+" status="+status);
					//MasterMain.log.debug("serverData.get(i).getServerIpAddress()="+serverData.get(i).getServerIpAddress());
					H2.validateRenderDbStatus(serverData.get(i).getServerIpAddress());
					//MasterMain.log.debug("validateRenderDbStatus="+i);
				} catch (ConnectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			*/
            if (timer > 0) {
            	TimeUnit.SECONDS.sleep(timer);
            } else {
            	loop = false;
            }
        }
    }
    

}
