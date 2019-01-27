package tv.culturesport.sirrender;

import java.sql.Connection;
import java.sql.SQLException;

public class GlobalClass {
	
	private static String serverMasterIpAddress = null;
	private static String serverMasterName = null;
	private static String portNumberStr = null;
	private static java.sql.Timestamp lastMasterServerPollTime;
	private static java.sql.Timestamp lastPollTime;
	private static Connection conection;
	private static int aboutExitCount = 0;
	private static int portNum = 0;
	private static boolean closeDB = false;
	private static boolean socketDebug = false;
	private static boolean serverProtocolDebug = false;
	private static boolean h2ServerMode = true;

	public static String getServerMasterIpAddress() {
		return serverMasterIpAddress;
	}
	
	public static void setServerMasterIpAddress(String serverMasterIpAddress) {
		GlobalClass.serverMasterIpAddress = serverMasterIpAddress;
	}
	
	public static String getServerMasterName() {
		return serverMasterName;
	}

	public static void setServerMasterName(String serverMasterName) {
		GlobalClass.serverMasterName = serverMasterName;
	}

	public static java.sql.Timestamp getLastPollTime() {
		return lastPollTime;
	}
	
	public static void setLastPollTime(java.sql.Timestamp lastPollTime) {
		GlobalClass.lastPollTime = lastPollTime;
	}

	public static Connection getConection() {
		if (conection == null) {
			MasterMain.log.debug("conection is null - getting new connection - GlobalClass.getCloseDB=true");
			setConection(H2.Connector());
//			setCloseDB(true);
		} else
			try {
				if (conection.isClosed()) {
					MasterMain.log.debug("conection is closed - getting new connection - GlobalClass.getCloseDB="+closeDB);
					setConection(H2.Connector());
					//setCloseDB(true);
				} else {
					MasterMain.log.debug("conection exists - returning existing conection - GlobalClass.getCloseDB="+closeDB);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return conection;
	}

	public static void setConection(Connection conection) {
		GlobalClass.conection = conection;
	}

	public static int getAboutExitCount() {
		return aboutExitCount;
	}

	public static void setAboutExitCount(int aboutExitCount) {
		GlobalClass.aboutExitCount = aboutExitCount;
	}

	public static String getPortNumberStr() {
		return portNumberStr;
	}

	public static void setPortNumberStr(String portNumberStr) {
		GlobalClass.portNumberStr = portNumberStr;
	}

	public static int getPortNum() {
		return portNum;
	}

	public static void setPortNum(int portNum) {
		GlobalClass.portNum = portNum;
	}

	public static boolean getCloseDB() {
		return closeDB;
	}

	public static void setCloseDB(boolean closeDB) {
		GlobalClass.closeDB = closeDB;
	}

	public static java.sql.Timestamp getLastMasterServerPollTime() {
		return lastMasterServerPollTime;
	}

	public static void setLastMasterServerPollTime(java.sql.Timestamp lastMasterServerPollTime) {
		GlobalClass.lastMasterServerPollTime = lastMasterServerPollTime;
	}

	public static boolean isSocketDebug() {
		return socketDebug;
	}

	public static void setSocketDebug(boolean socketDebug) {
		GlobalClass.socketDebug = socketDebug;
	}

	public static boolean isH2ServerMode() {
		return h2ServerMode;
	}

	public static void setH2ServerMode(boolean h2ServerMode) {
		GlobalClass.h2ServerMode = h2ServerMode;
	}

	public static boolean isServerProtocolDebug() {
		return serverProtocolDebug;
	}

	public static void setServerProtocolDebug(boolean serverProtocolDebug) {
		GlobalClass.serverProtocolDebug = serverProtocolDebug;
	}


}
