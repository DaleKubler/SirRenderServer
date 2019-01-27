package tv.culturesport.sirrender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.h2.jdbc.JdbcSQLException;
import org.h2.message.DbException;

public class H2 {

	public static Connection Connector() {
		Connection conn = null;
		boolean connectionIsNull = true;
		
		while (connectionIsNull) {
			try {
	        	//MasterMain.log.debug("Opening database connection");
				Class.forName("org.h2.Driver");
				//conn = DriverManager.getConnection("jdbc:h2:file:U:/SirRender/databases/SirRenderDb;AUTO_SERVER=TRUE;TRACE_MAX_FILE_SIZE=2");
				//conn = DriverManager.getConnection("jdbc:h2:file:U:/SirRender/databases/SirRenderDb;AUTO_SERVER=TRUE");
				conn = DriverManager.getConnection("jdbc:h2:file:V:/SirRender/databases/SirRenderDb;AUTO_SERVER=TRUE");
	        	//MasterMain.log.debug("Obtained a database conection");
				connectionIsNull = false;
			} catch (JdbcSQLException jde) {
				// Catches the "Database may be already in use: null. Possible solutions: close all other connection(s); use the server mode [90020-194]" errors 
				try {
					TimeUnit.SECONDS.sleep(15);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				MasterMain.log.debug(jde);
				// TODO: handle exception
			} catch (DbException dbe) {
				// Stops the "The connection was not closed by the application and is garbage collected [90018-194]" errors in the H2 trace.db file
			} catch (Exception e) {
				try {
					TimeUnit.SECONDS.sleep(15);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				MasterMain.log.debug(e);
				// TODO: handle exception
			}
		}
		return conn;
	}

	public static synchronized void setServerLogDbMessage(String serverIpAddress, String ServerStatMsg) throws SQLException {
		Connection conection = GlobalClass.getConection();
		//conection.setAutoCommit(false);
		
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement2 = null;
		String serverName = getServerLogDbServerName(serverIpAddress);
		String query = "INSERT into ServerLog(IpAddress, ServerName, StatMsg, TimeStamp, SeqNum) values(?, ?, ?, ?, select max(SeqNum) + 1 from ServerLog)";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, serverName);
			preparedStatement.setString(3, ServerStatMsg);
			preparedStatement.setTimestamp(4, Utils.getCurrentTime());
			preparedStatement.executeUpdate();
			//conection.commit();
		} catch (JdbcSQLException se) {
			try {
				query = "INSERT into ServerLog(IpAddress, ServerName, StatMsg, TimeStamp, SeqNum) values(?, ?, ?, ?, 1)";
				MasterMain.log.debug("INSERT setServerLogDbMessage Error Handler()");
				preparedStatement2 = conection.prepareStatement(query);
				preparedStatement2.setString(1, serverIpAddress);
				preparedStatement2.setString(2, serverName);
				preparedStatement2.setString(3, ServerStatMsg);
				preparedStatement2.setTimestamp(4, Utils.getCurrentTime());
				preparedStatement2.executeUpdate();
				MasterMain.log.debug("INSERT setServerLogDbMessage Error Handler() COMPLETE");
				//conection.commit();
			} catch (Exception e) {
				MasterMain.log.debug("INSERT setServerLogDbMessage Error Handler() ERROR");
				MasterMain.log.debug("ERROR = " + e);
				//conection.rollback();
				// TODO: handle exception
			} finally {
				close(null, preparedStatement2, null); 
			}
		} catch (Exception e) {
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
		}
	}

	public static synchronized String getServerLogDbMessage(String serverIpAddress) throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "SELECT StatMsg from ServerLog where IpAddress = ? order by SeqNum desc limit 1";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				String serverStatMsg = resultSet.getString("StatMsg");
				MasterMain.log.debug(serverStatMsg);
				return serverStatMsg;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return "";
	}

	public static synchronized String getServerLogDbServerName(String serverIpAddress) throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "SELECT ServerName from ServerStatus where IpAddress = ?";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				String serverName = resultSet.getString("ServerName");
				//MasterMain.log.debug(serverName);
				return serverName;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return "";
	}

	public static synchronized List<String> getServerLogDbIpAddresses() throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String serverRenderLogIpAddress = null;
		List<String> serverRenderLogIpAddresses = new ArrayList<String>();
		String query = "SELECT distinct IpAddress from ServerLog order by IpAddress";
		try {
			preparedStatement = conection.prepareStatement(query);
			
			resultSet = preparedStatement.executeQuery();

			serverRenderLogIpAddresses.add(" ");
			while (resultSet.next()) {
				serverRenderLogIpAddress = resultSet.getString("IpAddress");
				serverRenderLogIpAddresses.add(serverRenderLogIpAddress);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return serverRenderLogIpAddresses;
	}

	public static synchronized List<String> getServerLogDbServerNames() throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String serverRenderLogServerName = null;
		List<String> serverRenderLogServerNames = new ArrayList<String>();
		String query = "SELECT distinct ServerName FROM SERVERSTATUS order by ServerName";
		try {
			preparedStatement = conection.prepareStatement(query);
			
			resultSet = preparedStatement.executeQuery();

			serverRenderLogServerNames.add(" ");
			while (resultSet.next()) {
				serverRenderLogServerName = resultSet.getString("ServerName");
				serverRenderLogServerNames.add(serverRenderLogServerName);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return serverRenderLogServerNames;
	}

	public static synchronized int getServerStatusMaxRowId() throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "SELECT MAX(RowId) AS MaxRowId from ServerStatus";
		try {
			preparedStatement = conection.prepareStatement(query);
			
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				int maxRowId = resultSet.getInt("MaxRowId");
				//MasterMain.log.debug("maxRowId="+maxRowId);
				return maxRowId;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return 0;
	}

	public static synchronized List<String> getServerLogDbBlendFiles() throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<String> serverRenderLogBlendFiles = new ArrayList<String>();
		String query = "SELECT distinct REPLACE(REPLACE(REPLACE(REPLACE(StatMsg,'Completed processing ','')," +
					   "'Started processing ',''),'Cancelled processing ',''),'Processing terminated (kill switch) for ','') BlendFile " +
					   "FROM SERVERLOG WHERE LENGTH(statmsg) >= 50 order by BlendFile";
		try {
			preparedStatement = conection.prepareStatement(query);
			
			resultSet = preparedStatement.executeQuery();
			
			serverRenderLogBlendFiles.add(" ");
			while (resultSet.next()) {
				String serverRenderLogBlendFile = resultSet.getString("BlendFile");
				serverRenderLogBlendFiles.add(serverRenderLogBlendFile);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return serverRenderLogBlendFiles;
	}

	public static synchronized List<ServerRenderLog> readServerRenderLog(String serverLogStartDate, String serverLogEndDate, 
																		 String serverLogServerName, String serverLogIpAddress, 
																		 String serverLogMessage, String serverLogBlendFile,
																		 Boolean exportCsv, String csvFileName) throws SQLException {
		Connection conection = GlobalClass.getConection();
		List<ServerRenderLog> serverRenderLog = new ArrayList<ServerRenderLog>();
		PreparedStatement preparedStatement = null;
		String query = null;
		String startDate = null;
		String endDate = null;
		ResultSet resultSet = null;

		//MasterMain.log.debug("serverLogStartDate="+serverLogStartDate);
		//MasterMain.log.debug("serverLogEndDate="+serverLogEndDate);
		//MasterMain.log.debug("serverLogServerName="+serverLogServerName);
		//MasterMain.log.debug("serverLogIpAddress="+serverLogIpAddress);
		//MasterMain.log.debug("serverLogMessage="+serverLogMessage);
		//MasterMain.log.debug("serverLogBlendFile="+serverLogBlendFile);

		if (serverLogStartDate != null && serverLogStartDate.trim().length() > 0) {
			startDate = serverLogStartDate + " 00:00:00.000";
			//endDate = serverLogDate + " 23:59:59.999";
		}

		if (serverLogEndDate != null && serverLogEndDate.trim().length() > 0) {
			//startDate = serverLogDate + " 00:00:00.000";
			endDate = serverLogEndDate + " 23:59:59.999";
		}

		boolean useAnd = false;
		query = "SELECT IpAddress, ServerName, StatMsg, TimeStamp, SeqNum from ServerLog where ";
		if (startDate != null && endDate != null) {
			if (useAnd) {
				query += " AND ";
			}
			query += "TimeStamp >= ? AND TimeStamp <= ?";
			useAnd = true;
		}
		
		if (serverLogServerName != null && serverLogServerName.trim().length() > 0) {
			if (useAnd) {
				query += " AND ";
			}
			query += "ServerName = ?";
			useAnd = true;
		}
		
		if (serverLogIpAddress != null && serverLogIpAddress.trim().length() > 0) {
			if (useAnd) {
				query += " AND ";
			}
			query += "IpAddress = ?";
			useAnd = true;
		}
		
		if (serverLogMessage != null && serverLogMessage.trim().length() > 0) {
			if (useAnd) {
				query += " AND ";
			}
			query += "StatMsg LIKE ?";
			useAnd = true;
		}
		
		if (serverLogBlendFile != null && serverLogBlendFile.trim().length() > 0) {
			if (useAnd) {
				query += " AND ";
			}
			query += "StatMsg LIKE ?";
			useAnd = true;
		}
		
		query += " ORDER BY SeqNum";
		MasterMain.log.debug("query="+query);

		try {
			preparedStatement = conection.prepareStatement(query);
			int positionCounter = 1;
			
			if (startDate != null && endDate != null) {
				preparedStatement.setString(positionCounter++, startDate);
				preparedStatement.setString(positionCounter++, endDate);
			}

			if (serverLogServerName != null && serverLogServerName.trim().length() > 0) {
				preparedStatement.setString(positionCounter++, serverLogServerName);
			}
			
			if (serverLogIpAddress != null && serverLogIpAddress.trim().length() > 0) {
				preparedStatement.setString(positionCounter++, serverLogIpAddress);
			}
			
			if (serverLogMessage != null && serverLogMessage.trim().length() > 0) {
				preparedStatement.setString(positionCounter++, "%" + serverLogMessage + "%");
			}
			
			if (serverLogBlendFile != null && serverLogBlendFile.trim().length() > 0) {
				preparedStatement.setString(positionCounter++, "%" + serverLogBlendFile.replace("\\", "\\\\") + "%");
			}
			
			if (exportCsv) {
				// Add the column labels for the CSV export file
				 serverRenderLog.add(new ServerRenderLog( "IP Address", "Server Name", "Message", "Date", "Time", 0));
			}
			
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
			    String ipAddress = resultSet.getString("IpAddress");
			    String serverName = resultSet.getString("ServerName");
			    String statMsg = resultSet.getString("StatMsg");
			    String timeStamp = resultSet.getString("TimeStamp");
			    int seqNum = resultSet.getInt("SeqNum");
			    String logDate = timeStamp.substring(5, 7) + "/" + timeStamp.substring(8, 10) + "/" + timeStamp.substring(0, 4);
			    String logTime = timeStamp.substring(11, 19);
			    //MasterMain.log.debug("ipAddress="+ipAddress);
			    //MasterMain.log.debug("serverName="+serverName);
			    //MasterMain.log.debug("statMsg="+statMsg);
			    //MasterMain.log.debug("seqNum="+seqNum);
			    //MasterMain.log.debug("logDate="+logDate);
			    //MasterMain.log.debug("logTime="+logTime);
				if (exportCsv) {
					// Export in different order when output to CSV file
					serverRenderLog.add(new ServerRenderLog(ipAddress, serverName, statMsg, logDate, logTime, seqNum));
				} else {
					serverRenderLog.add(new ServerRenderLog(ipAddress, serverName, statMsg, logDate, logTime, seqNum));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection);
		}
		return serverRenderLog;
	}

	public static synchronized void setServerAvailability(String serverIpAddress, String ServerStatMsg) throws SQLException {
		Connection conection = GlobalClass.getConection();
		//conection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement2 = null;
		String serverName = getServerLogDbServerName(serverIpAddress);
		String query = "INSERT into ServerLog(IpAddress, ServerName, StatMsg, TimeStamp, SeqNum) values(?, ?, ?, ?, select max(SeqNum) + 1 from ServerLog)";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, serverName);
			preparedStatement.setString(3, ServerStatMsg);
			preparedStatement.setTimestamp(4, Utils.getCurrentTime());
			preparedStatement.executeUpdate();
			//conection.commit();
		} catch (JdbcSQLException se) {
			try {
				query = "INSERT into ServerLog(IpAddress, ServerName, StatMsg, TimeStamp, SeqNum) values(?, ?, ?, ?, 1)";
				MasterMain.log.debug("INSERT setServerAvailability Error Handler()");
				preparedStatement2 = conection.prepareStatement(query);
				preparedStatement2.setString(1, serverIpAddress);
				preparedStatement2.setString(2, serverName);
				preparedStatement2.setString(3, ServerStatMsg);
				preparedStatement2.setTimestamp(4, Utils.getCurrentTime());
				preparedStatement2.executeUpdate();
				MasterMain.log.debug("INSERT setServerAvailability Error Handler() COMPLETE");
				//conection.commit();
			} catch (Exception e) {
				MasterMain.log.debug("INSERT setServerAvailability Error Handler() ERROR");
				MasterMain.log.debug("ERROR = " + e);
				//conection.rollback();
				// TODO: handle exception
			} finally {
				close(null, preparedStatement2, null); 
			}
		} catch (Exception e) {
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
		}
	}

	public static synchronized String getServerAvailability(String serverIpAddress) throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "SELECT StatMsg from ServerLog where IpAddress = ? order by SeqNum desc limit 1";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				String serverStatMsg = resultSet.getString("StatMsg");
				MasterMain.log.debug(serverStatMsg);
				return serverStatMsg;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return "";
	}

	public static synchronized void setServerStatus(String serverName, String serverIpAddress, String serverStatus) throws SQLException {
		if (getServerStatus(serverName, serverIpAddress).equalsIgnoreCase("ERROR")) {
			if (serverName != null && serverIpAddress != null) {
				if (!serverName.equals("") && !serverIpAddress.equals("")) {
					// Get the maximum RowId value from the ServerStatus table
					int maxRowId = getServerStatusMaxRowId() + 1;
					
					Connection conection = GlobalClass.getConection();
					//conection.setAutoCommit(false);
					//MasterMain.log.debug("Starting setServerStatus()");
					PreparedStatement preparedStatement = null;
					PreparedStatement preparedStatement2 = null;
					String query = "insert into ServerStatus(RowId, ServerName, IpAddress, Status, TimeStamp, StatusTime) values(?, ?, ?, ?, ?, ?)";
					try {
						MasterMain.log.debug("INSERT setServerStatus()");
						preparedStatement = conection.prepareStatement(query);
						preparedStatement.setInt(1, maxRowId);
						preparedStatement.setString(2, serverName);
						preparedStatement.setString(3, serverIpAddress);
						preparedStatement.setString(4, serverStatus);
						preparedStatement.setTimestamp(5, Utils.getCurrentTime());
						preparedStatement.setTimestamp(6, Utils.getCurrentTime());
						preparedStatement.executeUpdate();
						MasterMain.log.debug("INSERT setServerStatus() COMPLETE");
						//conection.commit();
					} catch (JdbcSQLException se) {
						try {
							query = "insert into ServerStatus(RowId, ServerName, IpAddress, Status, TimeStamp, StatusTime) values(?, ?, ?, ?, ?, ?)";
							MasterMain.log.debug("INSERT setServerStatus Error Handler()");
							preparedStatement.setInt(1, maxRowId);
							preparedStatement2 = conection.prepareStatement(query);
							preparedStatement2.setString(2, serverName);
							preparedStatement2.setString(3, serverIpAddress);
							preparedStatement2.setString(4, serverStatus);
							preparedStatement2.setTimestamp(5, Utils.getCurrentTime());
							preparedStatement2.setTimestamp(6, Utils.getCurrentTime());
							preparedStatement2.executeUpdate();
							MasterMain.log.debug("INSERT setServerStatus Error Handler() COMPLETE");
							//conection.commit();
						} catch (Exception e) {
							MasterMain.log.debug("INSERT setServerStatus Error Handler() ERROR");
							MasterMain.log.debug("ERROR = " + e);
							//conection.rollback();
							// TODO: handle exception
						} finally {
							close(null, preparedStatement2, null); 
						}
					} catch (Exception e) {
						MasterMain.log.debug("INSERT setServerStatus() ERROR");
						MasterMain.log.debug("ERROR = " + e);
						//conection.rollback();
						// TODO: handle exception
					} finally {
						close(null, preparedStatement, conection); 
					}
				}
			}
		} else {
			//MasterMain.log.debug("UPDATE setServerStatus()");
			updateServerStatus(serverName, serverIpAddress, serverStatus);
		}
		
	}

	public static synchronized void purgeStaleServers(String serverName, String serverIpAddress) throws SQLException {
		Connection conection = GlobalClass.getConection();
		//conection.setAutoCommit(false);
		//Connection conection = GlobalClass.getPoolMgr().getConnection();
		//MasterMain.log.debug("Starting purgeStaleServers()");
		PreparedStatement preparedStatement = null;
		String query = "delete from ServerStatus where rowid in (select rowid from ServerStatus where (ServerName=? and IpAddress<>?) "
				+ "union "
				+ "select rowid from ServerStatus where (ServerName<>? and IpAddress=?))";
		try {
			MasterMain.log.debug("DELETE stale servers that share the same "
					+ "ipAddress or machine name as this server from the ServerStatus table.");
			//MasterMain.log.debug("query="+query);
			//MasterMain.log.debug("serverName="+serverName);
			//MasterMain.log.debug("serverIpAddress="+serverIpAddress);
			preparedStatement = conection.prepareStatement(query);
			//MasterMain.log.debug("after preparedStatement");
			preparedStatement.setString(1, serverName);
			//MasterMain.log.debug("after setString 1");
			preparedStatement.setString(2, serverIpAddress);
			//MasterMain.log.debug("after setString 2");
			preparedStatement.setString(3, serverName);
			//MasterMain.log.debug("after setString 3");
			preparedStatement.setString(4, serverIpAddress);
			//MasterMain.log.debug("after setString 4");
			//MasterMain.log.debug("before exucuteUpdate");
			preparedStatement.executeUpdate();
			//MasterMain.log.debug("after exucuteUpdate");
			//conection.commit();
		} catch (Exception e) {
			//conection.rollback();
			// TODO: handle exception
		} finally {
			close(null, preparedStatement, conection); 
		}
		
	}

	public static synchronized String getMasterServerIpAddress() throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "select IpAddress from ServerStatus where Status = 'Available' order by MasterSlavePriority, IpAddress limit 1";
		try {
			preparedStatement = conection.prepareStatement(query);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				String ipAddress = resultSet.getString("IpAddress");
				MasterMain.log.debug("getMasterServerIpAddress()="+ipAddress);
				return ipAddress;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return "";
	}

	public static synchronized String getMasterServerName(String ipAddress) throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "select ServerName from ServerStatus where ipAddress = ? limit 1";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, ipAddress);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				String serverName = resultSet.getString("ServerName");
				MasterMain.log.debug("getMasterServerName()="+serverName);
				return serverName;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return "";
	}

	public static synchronized String getServerStatus(String serverName, String serverIpAddress) throws SQLException {
		
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String serverStatus = "ERROR";
		String query = "select Status from ServerStatus where ServerName = ? and IpAddress = ?";
		
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverName);
			preparedStatement.setString(2, serverIpAddress);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				serverStatus = resultSet.getString("Status");
				//MasterMain.log.debug(serverStatus);
				//return serverStatus;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return serverStatus;
	}

	public static synchronized void updateServerStatus(String serverName, String serverIpAddress, String serverStatus) throws SQLException {
		//MasterMain.log.debug("Starting updateServerStatusStatus");
		//MasterMain.log.debug("serverName="+serverName+", ipAddress="+serverIpAddress+", status="+serverStatus);
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		//MasterMain.log.debug("status="+serverStatus);
		if (serverName != null && serverIpAddress != null) {
			if (!serverName.equals("") && !serverIpAddress.equals("")) {
				Connection conection = GlobalClass.getConection();
				//conection.setAutoCommit(false);
				PreparedStatement preparedStatement = null;
				//String query = "update ServerStatus set Status = ?, TimeStamp = ?, StatusTime = ? where serverName = ? and IpAddress = ?";
				String query = "update ServerStatus set Status = ?, StatusTime = ? where serverName = ? and IpAddress = ?";
				try {
					preparedStatement = conection.prepareStatement(query);
					preparedStatement.setString(1, serverStatus);
					/*
					preparedStatement.setTimestamp(2, Utils.getCurrentTime());
					preparedStatement.setTimestamp(3, Utils.getCurrentTime());
					preparedStatement.setString(4, serverName);
					preparedStatement.setString(5, serverIpAddress);
					*/
					preparedStatement.setTimestamp(2, Utils.getCurrentTime());
					preparedStatement.setString(3, serverName);
					preparedStatement.setString(4, serverIpAddress);
					preparedStatement.executeUpdate();
					//conection.commit();
				} catch (Exception e) {
					//MasterMain.log.debug("Rolling back transaction");
					//conection.rollback();
				} finally {
					close(null, preparedStatement, conection); 
					//MasterMain.log.debug("Ending updateServerStatusStatus");
				}
			}
		}
	}

	public static synchronized void updateServerStatusFileName(String serverIpAddress, String fileName) throws SQLException {
		//MasterMain.log.debug("Starting updateServerStatusFileName");
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		//MasterMain.log.debug("fileName="+fileName);
		if ((serverIpAddress != null) && !serverIpAddress.equals("")) {
			Connection conection = GlobalClass.getConection();
			//conection.setAutoCommit(false);
			PreparedStatement preparedStatement = null;
			String query = "UPDATE ServerStatus set CurrentFile = ? where IpAddress = ?";
			try {
				preparedStatement = conection.prepareStatement(query);
				preparedStatement.setString(1, fileName);
				preparedStatement.setString(2, serverIpAddress);
				preparedStatement.executeUpdate();
				//conection.commit();
			} catch (Exception e) {
				//conection.rollback();
			} finally {
				close(null, preparedStatement, conection); 
				//MasterMain.log.debug("Ending updateServerStatusFileName");
			}
		}
	}

	public static synchronized void updateServerStatusCurrentFrame(String serverIpAddress, int frameNumber) throws SQLException {
		//MasterMain.log.debug("Starting updateServerStatusCurrentFrame");
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		//MasterMain.log.debug("frameNumber="+frameNumber);
		if ((serverIpAddress != null) && !serverIpAddress.equals("")) {
			Connection conection = GlobalClass.getConection();
			//conection.setAutoCommit(false);
			PreparedStatement preparedStatement = null;
			String query = "UPDATE ServerStatus set currentFrame = ?, TimeStamp=?, renderFrameCount=renderFrameCount + 1 where IpAddress = ?";
			try {
				preparedStatement = conection.prepareStatement(query);
				preparedStatement.setInt(1, frameNumber);
				preparedStatement.setTimestamp(2, Utils.getCurrentTime());
				preparedStatement.setString(3, serverIpAddress);
				preparedStatement.executeUpdate();
				//conection.commit();
			} catch (Exception e) {
				//conection.rollback();
			} finally {
				close(null, preparedStatement, conection); 
				//MasterMain.log.debug("Ending updateServerStatusCurrentFrame");
			}
		}
	}

	public static synchronized void updateServerStatusCancel(String serverIpAddress) throws SQLException {
		//MasterMain.log.debug("Starting updateServerStatusCancel");
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		if ((serverIpAddress != null) && !serverIpAddress.equals("")) {
			Connection conection = GlobalClass.getConection();
			//conection.setAutoCommit(false);
			PreparedStatement preparedStatement = null;
			String query = "UPDATE ServerStatus SET frameStart=0, frameEnd=0, frameCount=0, currentFrame=0, currentFile='', renderEnd=null WHERE iPaddress = ?";
			try {
				preparedStatement = conection.prepareStatement(query);
				preparedStatement.setString(1, serverIpAddress);
				preparedStatement.executeUpdate();
				//conection.commit();
			} catch (Exception e) {
				//conection.rollback();
			} finally {
				close(null, preparedStatement, conection); 
				//MasterMain.log.debug("Ending updateServerStatusCancel");
			}
		}
	}

	public static synchronized void updateServerStatusComplete(String serverIpAddress) throws SQLException {
		//MasterMain.log.debug("Starting updateServerStatusComplete");
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		if ((serverIpAddress != null) && !serverIpAddress.equals("")) {
			Connection conection = GlobalClass.getConection();
			//conection.setAutoCommit(false);
			PreparedStatement preparedStatement = null;
			String query = "UPDATE ServerStatus SET frameStart=0, frameEnd=0, frameCount=0, currentFrame=0, currentFile='', renderEnd=?, renderTotalFrameCount=renderTotalFrameCount + renderFrameCount WHERE iPaddress = ?";
			try {
				preparedStatement = conection.prepareStatement(query);
				preparedStatement.setTimestamp(1, Utils.getCurrentTime());
				preparedStatement.setString(2, serverIpAddress);
				preparedStatement.executeUpdate();
				//conection.commit();
			} catch (Exception e) {
				//conection.rollback();
			} finally {
				close(null, preparedStatement, conection); 
				//MasterMain.log.debug("Ending updateServerStatusComplete");
			}
		}
	}

	public static synchronized void updateServerStatusStartStatistics(String serverIpAddress, int sfs, int sfe, int sfc, int cff, String cfn, String odir) throws SQLException {
		MasterMain.log.debug("Starting updateServerStatusStartStatistics");
		MasterMain.log.debug("sfs="+sfs);
		MasterMain.log.debug("sfe="+sfe);
		MasterMain.log.debug("sfc="+sfc);
		MasterMain.log.debug("cff="+cff);
		MasterMain.log.debug("cfn="+cfn);
		MasterMain.log.debug("odir="+odir);
		MasterMain.log.debug("ipAddress="+serverIpAddress);
		if ((serverIpAddress != null) && !serverIpAddress.equals("")) {
			Connection conection = GlobalClass.getConection();
			//conection.setAutoCommit(false);
			PreparedStatement preparedStatement = null;
			String query = "UPDATE ServerStatus SET frameStart=?, frameEnd=?, frameCount=?, currentFrame=?, CurrentFile=?, TimeStamp=?, renderStart=?, renderEnd=null, renderFrameCount=0, renderOutDir=?, killSwitch=0 WHERE iPaddress = ?";
			try {
				//MasterMain.log.debug("Starting prepared statements in updateServerStatusStartStatistics");
				preparedStatement = conection.prepareStatement(query);
				preparedStatement.setInt(1, sfs);
				preparedStatement.setInt(2, sfe);
				preparedStatement.setInt(3, sfc);
				preparedStatement.setInt(4, cff);
				preparedStatement.setString(5, cfn);
				preparedStatement.setTimestamp(6, Utils.getCurrentTime());
				preparedStatement.setTimestamp(7, Utils.getCurrentTime());
				preparedStatement.setString(8, odir);
				preparedStatement.setString(9, serverIpAddress);
				//MasterMain.log.debug("Before executeUpdate in updateServerStatusStartStatistics");
				preparedStatement.executeUpdate();
				//MasterMain.log.debug("After executeUpdate in updateServerStatusStartStatistics");
				//conection.commit();
			} catch (Exception e) {
				//conection.rollback();
			} finally {
				close(null, preparedStatement, conection); 
				//MasterMain.log.debug("Ending updateServerStatusStartStatistics");
			}
		}
}

	public static synchronized void resetServerStatusKillSwitch(String serverIpAddress) throws SQLException {
		//MasterMain.log.debug("Starting resetServerStatusKillSwitch");
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		if ((serverIpAddress != null) && !serverIpAddress.equals("")) {
			Connection conection = GlobalClass.getConection();
			//conection.setAutoCommit(false);
			PreparedStatement preparedStatement = null;
			String query = "UPDATE ServerStatus SET killSwitch=0 WHERE iPaddress = ?";
			try {
				preparedStatement = conection.prepareStatement(query);
				preparedStatement.setString(1, serverIpAddress);
				preparedStatement.executeUpdate();
				//conection.commit();
			} catch (Exception e) {
				//conection.rollback();
			} finally {
				close(null, preparedStatement, conection); 
				//MasterMain.log.debug("Ending resetServerStatusKillSwitch");
			}
		}
	}

	public static synchronized void updateServerSuspendKillSwitchStatus(String serverName, String serverIpAddress, int serverKillSwitch, int serverSuspendStatus, int backgroundRenderStatus) throws SQLException {
		if (serverName != null && serverIpAddress != null) {
			if (!serverName.equals("") && !serverIpAddress.equals("")) {
				Connection conection = GlobalClass.getConection();
				//conection.setAutoCommit(false);
				PreparedStatement preparedStatement = null;
				String query = "update ServerStatus set killSwitch = ?, suspendSwitch = ?, backgroundSwitch = ? where serverName = ? and IpAddress = ?";
				try {
					preparedStatement = conection.prepareStatement(query);
					preparedStatement.setInt(1, serverKillSwitch);
					preparedStatement.setInt(2, serverSuspendStatus);
					preparedStatement.setInt(3, backgroundRenderStatus);
					preparedStatement.setString(4, serverName);
					preparedStatement.setString(5, serverIpAddress);
					preparedStatement.executeUpdate();
					//conection.commit();
				} catch (Exception e) {
					//conection.rollback();
					// TODO: handle exception
				} finally {
					close(null, preparedStatement, conection); 
				}
			}
		}
	}

	public static synchronized String getServerSuspendSwitchStatus(String serverIpAddress) throws SQLException {
		String suspendSwitch = "";
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "select suspendSwitch from ServerStatus where IpAddress = ?";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				suspendSwitch = resultSet.getString("suspendSwitch");
				//MasterMain.log.debug("suspendSwitch="+suspendSwitch);
				//return suspendSwitch;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			//MasterMain.log.debug("suspendSwitch="+suspendSwitch);
			close(resultSet, preparedStatement, conection); 
		}
		return suspendSwitch;
	}

	public static synchronized String getServerKillSwitchStatus(String serverIpAddress) throws SQLException {
		String killSwitch = "";
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "SELECT killSwitch from ServerStatus WHERE iPaddress = ?";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				killSwitch = resultSet.getString("killSwitch");
				//MasterMain.log.debug("killSwitch="+killSwitch);
				//return killSwitch;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			//MasterMain.log.debug("killSwitch="+killSwitch);
			close(resultSet, preparedStatement, conection); 
		}
		return killSwitch;
	}
	
	public static synchronized String getServerBackgroundSwitchStatus(String serverIpAddress) throws SQLException {
		String backgroundSwitch = "";
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "SELECT backgroundSwitch from ServerStatus WHERE iPaddress = ?";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				backgroundSwitch = resultSet.getString("backgroundSwitch");
				//MasterMain.log.debug("bsckgroundSwitch="+bsckgroundSwitch);
				//return bsckgroundSwitch;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			//MasterMain.log.debug("killSwitch="+killSwitch);
			close(resultSet, preparedStatement, conection); 
		}
		return backgroundSwitch;
	}
	
	public static synchronized void checkpointDatabase() throws SQLException {
		/*
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "PRAGMA wal_checkpoint(PASSIVE)";
		//String query = "PRAGMA wal_checkpoint(FULL)";
		try {
			preparedStatement = conection.prepareStatement(query);
			resultSet = preparedStatement.executeQuery();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
			//PRAGMA wal_autocheckpoint=N;
		}
		*/
	}
	
	public static synchronized void setWalAutoCheckpointSize(int size) throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "PRAGMA wal_autocheckpoint="+size;
		try {
			preparedStatement = conection.prepareStatement(query);
			resultSet = preparedStatement.executeQuery();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
			//PRAGMA wal_autocheckpoint=N;
		}
	}
	
	public static synchronized void updateServerSuspendSwitchStatus(String serverName, String serverIpAddress, int serverSuspendStatus) throws SQLException {
		if (serverName != null && serverIpAddress != null) {
			if (!serverName.equals("") && !serverIpAddress.equals("")) {
				Connection conection = GlobalClass.getConection();
				//conection.setAutoCommit(false);
				PreparedStatement preparedStatement = null;
				String query = "update ServerStatus set suspendSwitch = ? where serverName = ? and IpAddress = ?";
				try {
					preparedStatement = conection.prepareStatement(query);
					preparedStatement.setInt(1, serverSuspendStatus);
					preparedStatement.setString(2, serverName);
					preparedStatement.setString(3, serverIpAddress);
					preparedStatement.executeUpdate();
					//conection.commit();
				} catch (Exception e) {
					//conection.rollback();
					// TODO: handle exception
				} finally {
					close(null, preparedStatement, conection); 
				}
			}
		}
	}

	public static synchronized void updateServerKillSwitchStatus(String serverName, String serverIpAddress, int serverKillSwitch) throws SQLException {
		if (serverName != null && serverIpAddress != null) {
			if (!serverName.equals("") && !serverIpAddress.equals("")) {
				Connection conection = GlobalClass.getConection();
				//conection.setAutoCommit(false);
				PreparedStatement preparedStatement = null;
				String query = "update ServerStatus set killSwitch = ? where serverName = ? and IpAddress = ?";
				try {
					preparedStatement = conection.prepareStatement(query);
					preparedStatement.setInt(1, serverKillSwitch);
					preparedStatement.setString(2, serverName);
					preparedStatement.setString(3, serverIpAddress);
					preparedStatement.executeUpdate();
					//conection.commit();
				} catch (Exception e) {
					//conection.rollback();
					// TODO: handle exception
				} finally {
					close(null, preparedStatement, conection); 
				}
			}
		}
	}

	public static synchronized void updateServerBackgroundSwitchStatus(String serverName, String serverIpAddress, int serverBackgroundSwitch) throws SQLException {
		if (serverName != null && serverIpAddress != null) {
			if (!serverName.equals("") && !serverIpAddress.equals("")) {
				Connection conection = GlobalClass.getConection();
				//conection.setAutoCommit(false);
				PreparedStatement preparedStatement = null;
				String query = "update ServerStatus set backgroundSwitch = ? where serverName = ? and IpAddress = ?";
				try {
					preparedStatement = conection.prepareStatement(query);
					preparedStatement.setInt(1, serverBackgroundSwitch);
					preparedStatement.setString(2, serverName);
					preparedStatement.setString(3, serverIpAddress);
					preparedStatement.executeUpdate();
					//conection.commit();
				} catch (Exception e) {
					//conection.rollback();
					// TODO: handle exception
				} finally {
					close(null, preparedStatement, conection); 
				}
			}
		}
	}

	public static synchronized void updateServerQueueErrorCount(String serverIpAddress, String fileName, int overrideOutputDir, String overrideRenderDevice) throws SQLException {
		//MasterMain.log.debug("Starting updateServerQueueErrorCount");
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		//MasterMain.log.debug("fileName="+fileName);
		Connection conection = GlobalClass.getConection();
		//conection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
		String query = "UPDATE ServerQueue SET ErrorCount=ErrorCount + 1 WHERE iPaddress = ? AND FileName = ? and OverrideOutputDir = ? and RenderDevice = ?";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, fileName);
			preparedStatement.setInt(3, overrideOutputDir);
			preparedStatement.setString(4, overrideRenderDevice);
			preparedStatement.executeUpdate();
			//conection.commit();
		} catch (Exception e) {
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
			//MasterMain.log.debug("Ending updateServerQueueErrorCount");
		}
	}

	public static synchronized void decreaseServerQueueErrorCount(String serverIpAddress, String fileName, int overrideOutputDir, String overrideRenderDevice) throws SQLException {
		//MasterMain.log.debug("Starting updateServerQueueErrorCount");
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		//MasterMain.log.debug("fileName="+fileName);
		//MasterMain.log.debug("overrideOutputDir="+overrideOutputDir);
		Connection conection = GlobalClass.getConection();
		//conection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
		String query = "UPDATE ServerQueue SET ErrorCount=ErrorCount - 1 WHERE iPaddress = ? AND FileName = ? and OverrideOutputDir = ? and RenderDevice = ?";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, fileName);
			preparedStatement.setInt(3, overrideOutputDir);
			preparedStatement.setString(4, overrideRenderDevice);
			preparedStatement.executeUpdate();
			//conection.commit();
		} catch (Exception e) {
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
			//MasterMain.log.debug("Ending updateServerQueueErrorCount");
		}
	}

	public static synchronized void deleteServerQueueFile(String serverIpAddress, String fileName, String overrideOutputDir, String overrideRenderDevice) throws SQLException {
		//MasterMain.log.debug("Starting deleteServerQueueFile");
		//MasterMain.log.debug("serverIpAddress="+serverIpAddress);
		//MasterMain.log.debug("fileName="+fileName);
		//MasterMain.log.debug("overrideOutputDir="+overrideOutputDir);
		//MasterMain.log.debug("overrideRenderDevice="+overrideRenderDevice);
		String query;
		Connection conection = GlobalClass.getConection();
		//conection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
		if (serverIpAddress.equals("0.0.0.0")) {
			if (overrideRenderDevice != null && !overrideRenderDevice.isEmpty()) {
				query = "DELETE FROM ServerQueue WHERE FileName = ? and OverrideOutputDir = ? and RenderDevice = ?";
			} else {
				query = "DELETE FROM ServerQueue WHERE FileName = ? and OverrideOutputDir = ?";
			}
		} else {
			if (overrideRenderDevice != null && !overrideRenderDevice.isEmpty()) {
				query = "DELETE FROM ServerQueue WHERE FileName = ? and OverrideOutputDir = ? and RenderDevice = ? and IpAddress = ?";
			} else {
				query = "DELETE FROM ServerQueue WHERE FileName = ? and OverrideOutputDir = ? and IpAddress = ?";
			}
		}
		try {
			//MasterMain.log.debug("query="+query);
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, fileName);
			//MasterMain.log.debug("XXXfileName="+fileName+"|");
			preparedStatement.setInt(2, Integer.valueOf(overrideOutputDir));
			//MasterMain.log.debug("XXXoverrideOutputDir="+overrideOutputDir+"|");
			if (overrideRenderDevice != null && !overrideRenderDevice.isEmpty()) {
				preparedStatement.setString(3, overrideRenderDevice);
				//MasterMain.log.debug("XXXoverrideRenderDevice="+overrideRenderDevice+"|");
				if (!serverIpAddress.equals("0.0.0.0")) {
					preparedStatement.setString(4, serverIpAddress);
					//MasterMain.log.debug("XXXserverIpAddress="+serverIpAddress+"|");
				}
			} else {
				if (!serverIpAddress.equals("0.0.0.0")) {
					preparedStatement.setString(3, serverIpAddress);
					//MasterMain.log.debug("YYYserverIpAddress="+serverIpAddress+"|");
				}
			}
			preparedStatement.executeUpdate();
			//conection.commit();
		} catch (Exception e) {
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
			//MasterMain.log.debug("Ending deleteServerQueueFile");
		}
	}

	public static synchronized void insertDummyServerQueueFile() throws SQLException {
		//MasterMain.log.debug("Starting insertDummyServerQueueFile");
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		//MasterMain.log.debug("fileName="+fileName);
		Connection conection = GlobalClass.getConection();
		//Connection conection = GlobalClass.getPoolMgr().getConnection();
		//conection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
		String query = "INSERT INTO ServerQueue(IpAddress, FileName, rowid) values('0.0.0.0', 'Dummy Placeholder', 1000)";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.executeUpdate();
			//conection.commit();
		} catch (Exception e) {
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
		}
	}

	public static synchronized void deleteDummyServerQueueFile() throws SQLException {
		//MasterMain.log.debug("Starting deleteDummyServerQueueFile");
		//MasterMain.log.debug("ipAddress="+serverIpAddress);
		//MasterMain.log.debug("fileName="+fileName);
		Connection conection = GlobalClass.getConection();
		//conection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
		String query = "DELETE FROM ServerQueue where IpAddress = '0.0.0.0' and FileName = 'Dummy Placeholder' and rowid = 1000";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.executeUpdate();
			//conection.commit();
		} catch (Exception e) {
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
		}
	}

	public static String spawnRender(String theInput, Boolean chkBackground, String masterServerIpAddress, String overrideOutputDir, String overrideRenderDevice)
	{
		String fileName;
		String tmpFileName = null;
		String tmpFileNameVbs = null;
		String myIpAddress = "";		

		try {
			InetAddress IP = InetAddress.getLocalHost();
			//String myIpAddress = IP.getHostAddress();
			myIpAddress = Utils.getLowIpAddress();
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String tempOutputPath = System.getenv(ApplicationConstants.ENV_SIRRENDER_TMP_PATH);
		//MasterMain.log.debug("Temporary File Environment path: "+tempOutputPath);
		if (tempOutputPath == null) {
			tempOutputPath = ApplicationConstants.DEFAULT_TMP_PATH;
		}
		MasterMain.log.debug("Temporary File Actual path: "+tempOutputPath);

		fileName = theInput;
		
		if (fileName != ""){
			//MasterMain.log.debug("6");
			// creates temporary file
			tmpFileName = ApplicationConstants.DEFAULT_TMP_PATH + ApplicationConstants.TMP_FILE_PREFIX + myIpAddress + ApplicationConstants.TMP_FILE_EXTENSION;
			try{
				//Delete if tempFile exists
				File fileTemp = new File(tmpFileName);
				if (fileTemp.exists()){
					fileTemp.delete();
				}   
			}catch(Exception e){
				// if any error occurs
				e.printStackTrace();
			}
				
			// MasterMain.cb3 is the tray icon status of the Render in Background switch
			// Currently the if and the else statements are identical
			if (MasterMain.cb3.getState()) {
				// creates temporary file
				tmpFileNameVbs = ApplicationConstants.DEFAULT_TMP_PATH + ApplicationConstants.TMP_FILE_PREFIX + myIpAddress + ApplicationConstants.VBS_FILE_EXTENSION;
				try{
					//Delete if tempFile exists
					File fileTemp = new File(tmpFileNameVbs);
					if (fileTemp.exists()){
						fileTemp.delete();
					}   
				}catch(Exception e){
					// if any error occurs
					e.printStackTrace();
				}
			} else {
				// creates temporary file
				tmpFileNameVbs = ApplicationConstants.DEFAULT_TMP_PATH + ApplicationConstants.TMP_FILE_PREFIX + myIpAddress + ApplicationConstants.VBS_FILE_EXTENSION;
				try{
					//Delete if tempFile exists
					File fileTemp = new File(tmpFileNameVbs);
					if (fileTemp.exists()){
						fileTemp.delete();
					}   
				}catch(Exception e){
					// if any error occurs
					e.printStackTrace();
				}
			}
				
			try {
				FileWriter writer = new FileWriter(tmpFileName, true);
				//writer.write("java -jar " + ApplicationConstants.DEFAULT_PATH + "SirRender.jar clientserverstatus " + fileName);
				//writer.write("\r\n");   // write new line
				//writer.write("java -jar " + ApplicationConstants.DEFAULT_PATH + "SirRender.jar clientbatch \"Started processing " + fileName);
				//writer.write("\r\n");   // write new line
				writer.write("rem SirRender spawnRender");
				writer.write("\r\n");   // write new line

				writer.write("TITLE SirRender " + fileName);  // Write window title
				writer.write("\r\n");   // write new line
				
				// MasterMain.cb3 is the tray icon status of the Render in Background switch
				if (MasterMain.cb3.getState()) {
					if (GlobalClass.isH2ServerMode()) {
						//writer.write("blender --background \"" + fileName + "\" --python V:\\SirRender\\pythonScripts\\renderDbUpdate.py -- " + myIpAddress + " " + GlobalClass.getPortNumberStr() + " " + overrideOutputDir + " " + overrideRenderDevice + " Y");
						writer.write("blender --background \"" + fileName + "\" --python C:\\SirRender\\pythonScripts\\renderDbUpdate.py -- " + myIpAddress + " " + GlobalClass.getPortNumberStr() + " " + overrideOutputDir + " " + overrideRenderDevice + " Y");
					} else {
						//writer.write("blender --background \"" + fileName + "\" --python V:\\SirRender\\pythonScripts\\renderDbUpdate.py -- " + masterServerIpAddress + " " + GlobalClass.getPortNumberStr() + " " + overrideOutputDir + " " + overrideRenderDevice + " Y");
						writer.write("blender --background \"" + fileName + "\" --python C:\\SirRender\\pythonScripts\\renderDbUpdate.py -- " + masterServerIpAddress + " " + GlobalClass.getPortNumberStr() + " " + overrideOutputDir + " " + overrideRenderDevice + " Y");
					}
				} else {
					if (GlobalClass.isH2ServerMode()) {
						//writer.write("blender \"" + fileName + "\" --python V:\\SirRender\\pythonScripts\\renderDbUpdate.py -- " + myIpAddress + " " + GlobalClass.getPortNumberStr() + " " + overrideOutputDir + " " + overrideRenderDevice + " N");
						writer.write("blender \"" + fileName + "\" --python C:\\SirRender\\pythonScripts\\renderDbUpdate.py -- " + myIpAddress + " " + GlobalClass.getPortNumberStr() + " " + overrideOutputDir + " " + overrideRenderDevice + " N");
					} else {
						//writer.write("blender \"" + fileName + "\" --python V:\\SirRender\\pythonScripts\\renderDbUpdate.py -- " + masterServerIpAddress + " " + GlobalClass.getPortNumberStr() + " " + overrideOutputDir + " " + overrideRenderDevice + " N");
						writer.write("blender \"" + fileName + "\" --python C:\\SirRender\\pythonScripts\\renderDbUpdate.py -- " + masterServerIpAddress + " " + GlobalClass.getPortNumberStr() + " " + overrideOutputDir + " " + overrideRenderDevice + " N");
					}
				}
				writer.write("\r\n");   // write new line
				writer.write("exit");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// MasterMain.cb3 is the tray icon status of the Render in Background switch
			// Currently the if and the else statements are identical
			if (MasterMain.cb3.getState()) {
				try {
					FileWriter writer = new FileWriter(tmpFileNameVbs, true);
					writer.write("Set WshShell = CreateObject(\"WScript.Shell\" )");
					writer.write("\r\n");   // write new line

					writer.write("WshShell.Run chr(34) & \"" + tmpFileName + "\" & Chr(34), 0");
					writer.write("\r\n");   // write new line
					
					writer.write("Set WshShell = Nothing");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					FileWriter writer = new FileWriter(tmpFileNameVbs, true);
					writer.write("Set WshShell = CreateObject(\"WScript.Shell\" )");
					writer.write("\r\n");   // write new line

					writer.write("WshShell.Run chr(34) & \"" + tmpFileName + "\" & Chr(34), 0");
					writer.write("\r\n");   // write new line
					
					writer.write("Set WshShell = Nothing");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return tmpFileName;
	}

    public static boolean netSetServerStatus(String hostName, String hostIpAddress, String status, Boolean useLocalIpAddress) throws IOException, ConnectException {

    	String serverCmd = ApplicationConstants.RENDER_UPDATE_SERVER_STATUS_STATUS + hostName + "|" + hostIpAddress + "|" + status;
    	String fromServer = null;

    	//MasterMain.log.debug("hostName="+hostName);
    	//MasterMain.log.debug("hostIpAddress="+hostIpAddress);
    	//MasterMain.log.debug("portNumber="+GlobalClass.getPortNum());
    	//MasterMain.log.debug("serverCmd="+serverCmd);
    	//MasterMain.log.debug("Attempting to connect to " + hostName + " " + hostIpAddress + ":" + GlobalClass.getPortNum());

    	Socket mcScoket = null;
    	try {
    		//InetAddress IP = InetAddress.getLocalHost();
    		//String localIpAddress = IP.getHostAddress();
        	String localIpAddress = Utils.getLowIpAddress();

        	//String localMachineName = IP.getHostName();

    		mcScoket = new Socket();
        	if (useLocalIpAddress || GlobalClass.isH2ServerMode()) {
        		mcScoket.connect(new InetSocketAddress(localIpAddress, GlobalClass.getPortNum()), 15*1000);	// Timeout = 15,000 msec
        	} else {
        		mcScoket.connect(new InetSocketAddress(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum()), 15*1000);	// Timeout = 15,000 msec
        	}
    		mcScoket.setTcpNoDelay(true);
	        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
            MasterMain.log.debug("serverCmd="+serverCmd);
	    	out.println(serverCmd);
	    	out.flush();
	        BufferedReader in = new BufferedReader(
	                new InputStreamReader(mcScoket.getInputStream()));
	        if ((fromServer = in.readLine()) != null) {
	            MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
	            //if (fromServer.startsWith(ApplicationConstants.SERVER_STATUS_OK)) {
//		        if (fromServer.startsWith(ApplicationConstants.AWAITING_YOUR_COMMAND)) {
	                //MasterMain.log.debug("Server " + hostIpAddress + " is active");
	                return true;
//				}
	        }
    	} catch (SocketTimeoutException ste) {
    		MasterMain.log.debug("Server TIMEOUT EXCEPTION" + hostIpAddress + ":" + GlobalClass.getPortNumberStr() + " is offline");
			try {
    			MasterMain.log.debug("before H2.setServerStatus");
				H2.setServerStatus(hostName, hostIpAddress, status);
    			MasterMain.log.debug("after H2.setServerStatus");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} catch (ConnectException e) {
    		MasterMain.log.debug("Server " + hostIpAddress + ":" + GlobalClass.getPortNumberStr() + " is offline");
    		// Still need to update database with status
    		try {
    			MasterMain.log.debug("calling H2.setServerStatus");
				H2.setServerStatus(hostName, hostIpAddress, status);
    			MasterMain.log.debug("returning from H2.setServerStatus");
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		return false;
  	        //throw e;
    	} catch (IOException e) {
  	        e.printStackTrace();
        } finally {
        	mcScoket.close();
        }
    	return false;
    }

	public static synchronized List<Server> getServerList(Boolean active, Boolean ignoreSuspended) throws SQLException {

		//MasterMain.log.debug("Starting getServerList");
		List<Server> servers = new ArrayList<Server>();
		String query = null;

		Connection conection = GlobalClass.getConection();

/*		if (conection == null) {
			MasterMain.log.debug("00");
		} else {
			MasterMain.log.debug("0");
		} */
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		if (active) {
			query = "select ServerName, IpAddress, Status, killSwitch, suspendSwitch, backgroundSwitch from ServerStatus where Status = 'Available' order by ServerName";
		} else {
			if (ignoreSuspended) {
				query = "select ServerName, IpAddress, Status, killSwitch, suspendSwitch, backgroundSwitch from ServerStatus order by ServerName";
			} else {
				query = "select ServerName, IpAddress, Status, killSwitch, suspendSwitch, backgroundSwitch from ServerStatus where suspendSwitch = 0 order by ServerName";
			}
		}
		//MasterMain.log.debug("query="+query);

		try {
			//MasterMain.log.debug("1");
			preparedStatement = conection.prepareStatement(query);
			//MasterMain.log.debug("2");
			resultSet = preparedStatement.executeQuery();
			//MasterMain.log.debug("3");
			while(resultSet.next()) {
			    String serverName = resultSet.getString(1);
			    String ipAddress = resultSet.getString(2);
			    String serverStatus = resultSet.getString(3);
			    int killSwitch = resultSet.getInt(4);
			    int suspendSwitch = resultSet.getInt(5);
			    int backgroundSwitch = resultSet.getInt(6);
			    //MasterMain.log.debug("serverName="+serverName);
			    //MasterMain.log.debug("ipAddress="+ipAddress);
			    //MasterMain.log.debug("serverStatus="+serverStatus);
			    //MasterMain.log.debug("killSwitch="+killSwitch);
			    //MasterMain.log.debug("suspendSwitch="+suspendSwitch);
			    //MasterMain.log.debug("backgroundSwitch="+backgroundSwitch);
				//MasterMain.log.debug("4");
			    servers.add(new Server(serverName, ipAddress, serverStatus, null, Integer.toString(killSwitch), Integer.toString(suspendSwitch), Integer.toString(backgroundSwitch)));
				//MasterMain.log.debug("5");
			}
		} catch (Exception e) {
			MasterMain.log.debug(e);
		} finally {
			//MasterMain.log.debug("6");
			close(resultSet, preparedStatement, conection); 
			//MasterMain.log.debug("7");
			//MasterMain.log.debug("Ending getServerList");
		}
		//MasterMain.log.debug("8");
		return servers;
	}

	public static synchronized List<RenderFileStatus> getRenderedFileStatusList(Boolean distinct, String fileName) throws SQLException {

		//MasterMain.log.debug("Starting getRenderedFileStatusList");
		List<RenderFileStatus> renderedFiles = new ArrayList<RenderFileStatus>();
		String query = null;

		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		if (distinct) {
			query = "select distinct CurrentFile from ServerStatus where CurrentFile <> '' and LENGTH(TRIM(CurrentFile)) > 0 order by CurrentFile";
		} else {
			query = "select ServerName, IpAddress, CurrentFile, frameStart, frameEnd, frameCount, currentFrame, renderStart, renderEnd, renderFrameCount, renderDefaultHangTime, masterSlavePriority, renderOutDir from ServerStatus where CurrentFile = ?";
		}
		//MasterMain.log.debug("query="+query);

		try {
			preparedStatement = conection.prepareStatement(query);
			if (!distinct) {
				preparedStatement.setString(1, fileName);
			}
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (distinct) {
				    String currentFile = resultSet.getString(1);

				    MasterMain.log.debug("currentFile="+currentFile);
					
				    renderedFiles.add(new RenderFileStatus(" ", " ", " ", " ", currentFile, 0, 0, 0, 0, 0, 0, 0, " "));
				} else {
				    String serverName = resultSet.getString(1);
				    String ipAddress = resultSet.getString(2);
				    String currentFile = resultSet.getString(3);
				    int frameStart = resultSet.getInt(4);
				    int frameEnd = resultSet.getInt(5);
				    int frameCount = resultSet.getInt(6);
				    int currentFrame = resultSet.getInt(7);
				    int renderFrameCount = resultSet.getInt(10);
				    int renderDefaultHangTime = resultSet.getInt(11);
				    int masterSlavePriority = resultSet.getInt(12);
				    String renderOutDir = resultSet.getString(13);
				    
				    /*
				    MasterMain.log.debug("serverName="+serverName);
				    MasterMain.log.debug("ipAddress="+ipAddress);
	        		MasterMain.log.debug("currentFile="+currentFile);
	        		MasterMain.log.debug("frameStart="+frameStart);
	        		MasterMain.log.debug("frameEnd="+frameEnd);
	        		MasterMain.log.debug("frameCount="+frameCount);
	        		MasterMain.log.debug("currentFrame="+currentFrame);
	        		MasterMain.log.debug("renderFrameCount="+renderFrameCount);
	        		MasterMain.log.debug("renderDefaultHangTime="+renderDefaultHangTime);
	        		MasterMain.log.debug("masterSlavePriority="+masterSlavePriority);
	        		MasterMain.log.debug("renderOutDir="+renderOutDir);
					*/
				    
				    renderedFiles.add(new RenderFileStatus(" ", " ", serverName, ipAddress, currentFile, frameStart, frameEnd, frameCount, currentFrame, renderFrameCount, renderDefaultHangTime, masterSlavePriority, renderOutDir));
				}
			}
		} catch (Exception e) {
			MasterMain.log.debug(e);
		} finally {
			close(resultSet, preparedStatement, conection); 
			//MasterMain.log.debug("Ending getRenderedFileStatusList");
		}
		return renderedFiles;
	}

	public static synchronized List<RenderFileStatusCounts> getRenderedFileStatusCounts(Boolean singleFile, String fileName) throws SQLException {

		//MasterMain.log.debug("Starting getRenderedFileStatusCounts");
		List<RenderFileStatusCounts> renderFileStatusCounts = new ArrayList<RenderFileStatusCounts>();
		String query = null;

		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		if (singleFile) {
			//query = "select currentFrame, CurrentFile, frameStart, frameEnd, frameCount, renderStart, renderEnd, renderFrameCount, renderDefaultHangTime, masterSlavePriority from ServerStatus where CurrentFile = ?";
			query = "select CurrentFile, frameStart, frameEnd, frameCount, currentFrame from ServerStatus where CurrentFile = ?";
		} else {
			//query = "select max(currentFrame), CurrentFile, frameStart, frameEnd, frameCount, renderStart, renderEnd, renderFrameCount, renderDefaultHangTime, masterSlavePriority from ServerStatus where CurrentFile = ?";
			query = "select CurrentFile, frameStart, frameEnd, frameCount, max(currentFrame) from ServerStatus where CurrentFile = ? group by currentfile, frameStart, frameEnd, frameCount";
		}
		MasterMain.log.debug("query="+query);

		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, fileName);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
			    String currentFile = resultSet.getString(1);
			    int frameStart = resultSet.getInt(2);
			    int frameEnd = resultSet.getInt(3);
			    int frameCount = resultSet.getInt(4);
			    int currentFrame = resultSet.getInt(5);
			    
        		//MasterMain.log.debug("currentFile="+currentFile);
        		//MasterMain.log.debug("frameStart="+frameStart);
        		//MasterMain.log.debug("frameEnd="+frameEnd);
        		//MasterMain.log.debug("frameCount="+frameCount);
        		//MasterMain.log.debug("currentFrame="+currentFrame);

        		renderFileStatusCounts.add(new RenderFileStatusCounts(" ", " ", currentFile, frameStart, frameEnd, frameCount, currentFrame, 0, 0, 0));
			}
		} catch (Exception e) {
			MasterMain.log.debug(e);
		} finally {
			close(resultSet, preparedStatement, conection); 
			//MasterMain.log.debug("Ending getRenderedFileStatusCounts");
		}
		return renderFileStatusCounts;
	}

	public static synchronized List<Render> getScheduleTasks(Boolean distinct, String serverIpAddress) throws SQLException {

		//MasterMain.log.debug("Starting getScheduleTasks");
		List<Render> renders = new ArrayList<Render>();
		String query = null;

		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		if (distinct) {
			query = "select distinct sq.IpAddress, '' as FileName, 0 as overrideOutputDir, 'D' as RenderDevice from ServerQueue sq, ServerStatus ss " +
					"where sq.IpAddress = ss.IpAddress " +
					"and (ss.CurrentFile is null or ss.CurrentFile = '') " +
					"and ss.Status <> 'Off Line' " +
					"and suspendSwitch = 0";
		} else {
			query = "select IpAddress, FileName, OverrideOutputDir, RenderDevice, rowid from ServerQueue where IpAddress = ? and ErrorCount <= 3 Order By rowid asc limit 1";
		}
		try {
			preparedStatement = conection.prepareStatement(query);
			if (!distinct) {
				preparedStatement.setString(1, serverIpAddress);
			}

			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
			    String ipAddress = resultSet.getString(1);
			    String fileName = resultSet.getString(2);
			    int overrideOutputDir = resultSet.getInt(3);
			    String overrideRenderDevice = resultSet.getString(4);
			    renders.add(new Render(ipAddress, fileName, String.valueOf(overrideOutputDir), overrideRenderDevice));
			}
		} catch (Exception e) {
			MasterMain.log.debug("getScheduleTasks="+e);
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return renders;
	}

	public static synchronized void addFileToServerQueue(String serverIpAddress, String serverCurrentFile, int overrideOutputDir, String overrideRenderDevice) throws SQLException {

		//MasterMain.log.debug("Starting addFileToServerQueue");
		Connection conection = GlobalClass.getConection();
		//MasterMain.log.debug("Created conection in addFileToServerQueue");
		//conection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
		String query = "INSERT INTO ServerQueue (rowid, IpAddress, FileName, ErrorCount, OverrideOutputDir, RenderDevice) VALUES ((SELECT max(rowid) + 1 from ServerQueue),?, ?, 0, ?, ?)";
		//MasterMain.log.debug("query="+query);
		
		try {
			//MasterMain.log.debug("query="+query);
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, serverCurrentFile);
			preparedStatement.setInt(3, overrideOutputDir);
			preparedStatement.setString(4, overrideRenderDevice);
			//MasterMain.log.debug("Before executeUpdate in addFileToServerQueue");
			preparedStatement.executeUpdate();
			//MasterMain.log.debug("After executeUpdate in addFileToServerQueue");
			//conection.commit();
		} catch (Exception e) {
			MasterMain.log.debug("addFileToServerQueue error="+e);
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
			//MasterMain.log.debug("Exiting addFileToServerQueue");
		}
	}
	
	public static synchronized void addFileToTopOfServerQueue(String serverIpAddress, String serverCurrentFile, int overrideOutputDir, String overrideRenderDevice) throws SQLException {

		//MasterMain.log.debug("Starting addFileToTopOfServerQueue");
		Connection conection = GlobalClass.getConection();
		//MasterMain.log.debug("Created conection in addFileToTopOfServerQueue");
		//conection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
		String query = "INSERT INTO ServerQueue (rowid, IpAddress, FileName, ErrorCount, OverrideOutputDir, RenderDevice) VALUES ((SELECT min(rowid) - 1 from ServerQueue), ?, ?, 0, ?, ?)";
		//MasterMain.log.debug("query="+query);

		try {
			//MasterMain.log.debug("query="+query);
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, serverCurrentFile);
			preparedStatement.setInt(3, overrideOutputDir);
			preparedStatement.setString(4, overrideRenderDevice);
			//MasterMain.log.debug("Before executeUpdate in addFileToTopOfServerQueue");
			preparedStatement.executeUpdate();
			//MasterMain.log.debug("After executeUpdate in addFileToTopOfServerQueue");
			//conection.commit();
		} catch (Exception e) {
			MasterMain.log.debug("addFileToTopOfServerQueue error="+e);
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
			//MasterMain.log.debug("Exiting addFileToTopOfServerQueue");
		}
	}

	public static synchronized List<String> readFileServerQueue(Boolean showErrors) throws SQLException {

		//MasterMain.log.debug("Starting readFileServerQueue");
		Connection conection = GlobalClass.getConection();
		//MasterMain.log.debug("Created conection in readFileServerQueue");
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<String> fileList = new ArrayList<String>();
		String query;

		if (showErrors) {
			query = "SELECT FileName, OverrideOutputDir, RenderDevice, count(*), min(rowid) as SORT1 from ServerQueue where ErrorCount > 3 and IpAddress <> '0.0.0.0' group by FileName, OverrideOutputDir, RenderDevice order by SORT1";
		} else {
			query = "SELECT FileName, OverrideOutputDir, RenderDevice, count(*), min(rowid) as SORT1 from ServerQueue where ErrorCount <= 3 and IpAddress <> '0.0.0.0' group by FileName, OverrideOutputDir, RenderDevice order by SORT1";
		}
		
		//MasterMain.log.debug("query="+query);
		try {
			//MasterMain.log.debug("before preparedStatement=");
			preparedStatement = conection.prepareStatement(query);
			//MasterMain.log.debug("after preparedStatement=");
			resultSet = preparedStatement.executeQuery();
			//MasterMain.log.debug("after resultSet=");
			while(resultSet.next()) {
				//MasterMain.log.debug("inside while loop");
			    String fileName = resultSet.getString(1);
			    int overrideOutputDir = resultSet.getInt(2);
			    String renderDevice = resultSet.getString(3);
			    int serverCount = resultSet.getInt(4);
			    if (overrideOutputDir == 0) {
				    //MasterMain.log.debug("fileName=" + fileName);
			    	if (renderDevice.equalsIgnoreCase("D")) {
			    		fileList.add(fileName + "  <" + serverCount + ">");
			    	} else if (renderDevice.equalsIgnoreCase("C")) {
			    		fileList.add(fileName + ApplicationConstants.OVERRIDE_RENDER_DEVICE_C + "  <" + serverCount + ">");
			    	} else if (renderDevice.equalsIgnoreCase("G")) {
			    		fileList.add(fileName + ApplicationConstants.OVERRIDE_RENDER_DEVICE_G + "  <" + serverCount + ">");
			    	}
			    } else {
				    //MasterMain.log.debug("fileName=" + fileName + ApplicationConstants.OVERRIDE_OUTPUT_DIR);
			    	if (renderDevice.equalsIgnoreCase("D")) {
				    	fileList.add(fileName + ApplicationConstants.OVERRIDE_OUTPUT_DIR + "  <" + serverCount + ">");
			    	} else if (renderDevice.equalsIgnoreCase("C")) {
				    	fileList.add(fileName + ApplicationConstants.OVERRIDE_OUTPUT_DIR + ApplicationConstants.OVERRIDE_RENDER_DEVICE_C + "  <" + serverCount + ">");
			    	} else if (renderDevice.equalsIgnoreCase("G")) {
				    	fileList.add(fileName + ApplicationConstants.OVERRIDE_OUTPUT_DIR + ApplicationConstants.OVERRIDE_RENDER_DEVICE_G + "  <" + serverCount + ">");
			    	}
			    }
			}
		} catch (Exception e) {
			MasterMain.log.debug("readFileServerQueue error="+e);
		} finally {
			close(resultSet, preparedStatement, conection); 
			//MasterMain.log.debug("Exiting readFileServerQueue");
		}
		return fileList;
	}
	
	public static synchronized String validateRenderDbStatus(String serverIpAddress) throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "select TimeStamp, renderDefaultHangTime, currentFile from ServerStatus where IpAddress = ? and CurrentFile is not null and CurrentFile <> ?";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, "");

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				// Get time of last update to render file
				Timestamp serverTimeStamp = resultSet.getTimestamp("TimeStamp");
				long timeSum = serverTimeStamp.getTime()/1000L;
				//MasterMain.log.debug(serverIpAddress+": timeSum="+timeSum);

				String serverCurrentFile = resultSet.getString("currentFile");
				//MasterMain.log.debug("serverCurrentFile="+serverCurrentFile);

				// Initialize serverHangTime (Default = serverAverageTime)
				int serverHangTime = resultSet.getInt("renderDefaultHangTime");
				//MasterMain.log.debug(serverIpAddress+": serverHangTime(I)="+serverHangTime);
				
				// Get current time
				long currentTimeStamp = Utils.getCurrentTime().getTime()/1000L;
				//MasterMain.log.debug(serverIpAddress+": currentTimeStamp="+currentTimeStamp);

				if ((timeSum + serverHangTime) < currentTimeStamp) {
					// Render process appears hung or terminated
					MasterMain.log.debug(serverIpAddress+": Render process appears hung or terminated ("+serverIpAddress+")");
					
					// Update serverQueue
					// First, get the full information from the ServerQueue for the currently hung file
					// This can be obtained by getting the next item in the queue for the server in question
					// as this file will either have been rendered by other servers or is still hung.
					List<Render> serverRenders = new ArrayList<Render>();
       				serverRenders = H2.getScheduleTasks(false, serverIpAddress);
               		if (!serverRenders.get(0).getIpAddress().isEmpty()) {
               			Boolean test = validateServerQueueHungProcess(serverIpAddress, serverCurrentFile, Integer.valueOf(serverRenders.get(0).getOverrideOutputDir()), serverRenders.get(0).getOverrideRenderDevice());
               			updateServerQueueHungProcess(serverIpAddress, serverCurrentFile, test, Integer.valueOf(serverRenders.get(0).getOverrideOutputDir()), serverRenders.get(0).getOverrideRenderDevice());
               		}
					clearServerStatus(serverIpAddress);
				} else {
					MasterMain.log.debug(serverIpAddress+": No hung processes");
					// Do nothing - Timer has not expired yet
				}
			}
		} catch (Exception e) {
			MasterMain.log.debug("exception="+e.toString());
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return "";
	}

	public static synchronized Boolean validateServerQueueHungProcess(String serverIpAddress, String serverCurrentFile, int overrideOutputDir, String overrideRenderDevice) throws SQLException {

		//MasterMain.log.debug("Starting validateServerQueueHungProcess");
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "select FileName from ServerQueue where IpAddress = ? and FileName = ? and OverrideOutputDir = ? and RenderDevice = ?";
		Boolean status = false;
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, serverCurrentFile);
			preparedStatement.setInt(3, overrideOutputDir);
			preparedStatement.setString(4, overrideRenderDevice);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				// Row exists in ServerQueue - Increment the ErrorCount
				//MasterMain.log.debug("validateServerQueueHungProcess-Row exists in ServerQueue");
				status = true;
			} else {
				// Row does not exist in ServerQueue - Add the row
				//MasterMain.log.debug("validateServerQueueHungProcess-Row does NOT exist in ServerQueue");
				status = false;
			}
		} catch (Exception e) {
			MasterMain.log.debug("validateServerQueueHungProcess 5="+e);
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return status;
	}
	
	public static synchronized void updateServerQueueHungProcess(String serverIpAddress, String serverCurrentFile, Boolean update, int overrideOutputDir, String overrideRenderDevice) throws SQLException {

		//MasterMain.log.debug("Starting updateServerQueueHungProcess");
		Connection conection = GlobalClass.getConection();
		//MasterMain.log.debug("Created conection in updateServerQueueHungProcess");
		//conection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
		String query;
	
		if (update) {
			query = "UPDATE ServerQueue SET ErrorCount=ErrorCount + 1 WHERE iPaddress = ? AND FileName = ? and OverrideOutputDir = ? and RenderDevice = ?";
		} else {
			query = "INSERT INTO ServerQueue (IpAddress, FileName, OverrideOutputDir, RenderDevice, ErrorCount, RowId) VALUES (?, ?, ?, ?, 1, (SELECT max(rowid) + 1 from ServerQueue))";
		}
		
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, serverCurrentFile);
			preparedStatement.setInt(3, overrideOutputDir);
			preparedStatement.setString(4, overrideRenderDevice);
			//MasterMain.log.debug("Before executeUpdate in updateServerQueueHungProcess");
			preparedStatement.executeUpdate();
			//MasterMain.log.debug("After executeUpdate in updateServerQueueHungProcess");
			//conection.commit();
		} catch (Exception e) {
			MasterMain.log.debug("updateServerQueueHungProcess error="+e);
			//conection.rollback();
		} finally {
			close(null, preparedStatement, conection); 
			//MasterMain.log.debug("Exiting updateServerQueueHungProcess");
		}
	}

	public static synchronized void clearServerStatus(String serverIpAddress) throws SQLException {
		//MasterMain.log.debug("Starting clearServerStatus");
		if ((serverIpAddress != null) && !serverIpAddress.equals("")) {
			Connection conection = GlobalClass.getConection();
			//MasterMain.log.debug("Created conection in clearServerStatus");
			//conection.setAutoCommit(false);
			PreparedStatement preparedStatement = null;
			String query = "UPDATE ServerStatus SET frameStart=?, frameEnd=?, frameCount=?, currentFrame=?, currentFile=?, renderEnd=null, killSwitch=? WHERE iPaddress = ?";
			
			try {
				preparedStatement = conection.prepareStatement(query);
				preparedStatement.setInt(1, 0);
				preparedStatement.setInt(2, 0);
				preparedStatement.setInt(3, 0);
				preparedStatement.setInt(4, 0);
				preparedStatement.setString(5, "");
				preparedStatement.setInt(6, 0);
				preparedStatement.setString(7, serverIpAddress);
				
				//MasterMain.log.debug("Before executeUpdate in clearServerStatus");
				preparedStatement.executeUpdate();
				//MasterMain.log.debug("After executeUpdate in clearServerStatus");
				//conection.commit();
			} catch (Exception e) {
				MasterMain.log.debug("clearServerStatus error="+e);
				//conection.rollback();
			} finally {
				close(null, preparedStatement, conection); 
				//MasterMain.log.debug("Exiting clearServerStatus");
			}
		}
	}
	
	public static synchronized String getRenderedFilePath(String serverIpAddress) throws SQLException {
		
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String tmpFilePath1 = null;
		String tmpFilePath2 = null;
		String filePath = null;
		String query = "SELECT RenderOutDir FROM ServerStatus where ipaddress = ?";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				tmpFilePath1 = resultSet.getString("RenderOutDir");
				tmpFilePath2 = tmpFilePath1.replace('\\','/');
				filePath = tmpFilePath2.substring(0, tmpFilePath2.lastIndexOf('/'));
				//MasterMain.log.debug("filePath in H2 = " + filePath);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return filePath;
	}

	public static synchronized List<Render> getComputerTasks(String serverIpAddress) throws SQLException {

		//MasterMain.log.debug("Starting getComputerTasks");
		List<Render> renders = new ArrayList<Render>();
		String query = null;

		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		query = "select CurrentFile as FileName, 0 as ErrorCount, 0 as rowid, 0 as OverrideOutputDir, 'D' as RenderDevice from ServerStatus where CurrentFile <> '' and LENGTH(TRIM(CurrentFile)) > 0 and IpAddress = ? " +
				"union " +
				"select FileName, ErrorCount, rowid, OverrideOutputDir, RenderDevice from ServerQueue where IpAddress = ? " + 
				"Order By ErrorCount, rowid asc";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);
			preparedStatement.setString(2, serverIpAddress);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
			    String fileName = resultSet.getString(1);
			    int errorCount = resultSet.getInt(2);
			    int rowid = resultSet.getInt(3);
			    int overrideOutputDir = resultSet.getInt(4);
			    String overrideRenderDevice = resultSet.getString(5);
			    renders.add(new Render(serverIpAddress, fileName, String.valueOf(rowid), String.valueOf(errorCount), String.valueOf(overrideOutputDir), overrideRenderDevice));
			}
		} catch (Exception e) {
			MasterMain.log.debug("getComputerTasks="+e);
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
		return renders;
	}

	public static synchronized void checkServerStatusTimestamp(String serverName, String serverIpAddress) throws SQLException {
		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "select StatusTime from ServerStatus where Status = 'Available' and IpAddress = ?";
		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, serverIpAddress);

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				// Get time of last update to render file
				Timestamp serverTimeStamp = resultSet.getTimestamp("StatusTime");
				long timeSum = serverTimeStamp.getTime()/1000L;
				//MasterMain.log.debug(serverIpAddress+": timeSum="+timeSum);

				// Initialize serverHangTime (Default = serverAverageTime)
				int serverHangTime = 120;		// 120 seconds - 2 minutes
				//MasterMain.log.debug(serverIpAddress+": serverHangTime(I)="+serverHangTime);
				
				// Get current time
				long currentTimeStamp = Utils.getCurrentTime().getTime()/1000L;
				//MasterMain.log.debug(serverIpAddress+": currentTimeStamp="+currentTimeStamp);

				if ((timeSum + serverHangTime) < currentTimeStamp) {
					// Server appears to be "Off Line"
					MasterMain.log.debug(serverIpAddress+": Server appears to be 'Off Line' ("+serverIpAddress+")");
					
					// Update serverQueue
					updateServerStatus(serverName, serverIpAddress, "Off Line");
				}
			}
		} catch (Exception e) {
			MasterMain.log.debug("exception="+e.toString());
			// TODO: handle exception
		} finally {
			close(resultSet, preparedStatement, conection); 
		}
	}

	public static synchronized List<ServerStatus> getServerStatusList(Boolean distinct, String requestedServerName, String requestedServerIpAddress) throws SQLException {

		//MasterMain.log.debug("Starting getServerStatusList");
		List<ServerStatus> serverStatuses = new ArrayList<ServerStatus>();
		String query = null;

		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		if (distinct) {
			query = "select distinct serverName, ipAddress as serverIpAddress from ServerStatus order by serverName";
		} else {
			query = "select serverName, ipAddress as serverIpAddress, status as serverStatus, statusTime as serverStatusTimestamp, currentFile as serverCurrentFile, " +
					"renderStart as serverRenderStart, renderEnd as serverRenderEnd, timestamp as serverRenderLastUpdate, frameStart as serverStartFrame, frameEnd as serverEndFrame, " +
					"frameCount as serverFrameCount, currentFrame as serverCurrentFrame, renderTotalFrameCount as serverCumulativeFrameCount, renderOutDir as serverOutputFile, " +
					"killSwitch as serverKillSwitch, suspendSwitch as serverSuspendSwitch, backgroundSwitch as serverBackgroundRender, renderDefaultHangTime as serverHangTimeThreshold " +
					"from ServerStatus where serverName = ? and ipAddress = ?";
		}
		//MasterMain.log.debug("query="+query);

		try {
			preparedStatement = conection.prepareStatement(query);
			if (!distinct) {
				preparedStatement.setString(1, requestedServerName);
				preparedStatement.setString(2, requestedServerIpAddress);
			}
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				if (distinct) {
				    String serverName = resultSet.getString("serverName");
				    String serverIpAddress = resultSet.getString("serverIpAddress");

				    //MasterMain.log.debug("serverName="+serverName);
				    //MasterMain.log.debug("serverIpAddress="+serverIpAddress);
					
				    serverStatuses.add(new ServerStatus(serverName, serverIpAddress, null, null, null, null, null, null, 0, 0, 0, 0, 0, null, 0, 0, 0, 0));
				} else {
				    String serverName = resultSet.getString("serverName");
				    String serverIpAddress = resultSet.getString("serverIpAddress");
				    String serverStatus = resultSet.getString("serverStatus");
				    Timestamp serverStatusTimestamp = resultSet.getTimestamp("serverStatusTimestamp");
				    String currentFile = resultSet.getString("serverCurrentFile");
				    Timestamp serverRenderStart = resultSet.getTimestamp("serverRenderStart");
				    Timestamp serverRenderEnd = resultSet.getTimestamp("serverRenderEnd");
				    Timestamp serverRenderLastUpdate = resultSet.getTimestamp("serverRenderLastUpdate");
				    int frameStart = resultSet.getInt("serverStartFrame");
				    int frameEnd = resultSet.getInt("serverEndFrame");
				    int frameCount = resultSet.getInt("serverFrameCount");
				    int currentFrame = resultSet.getInt("serverCurrentFrame");
				    int renderFrameCount = resultSet.getInt("serverCumulativeFrameCount");
				    String renderOutDir = resultSet.getString("serverOutputFile");
				    int killSwitch = resultSet.getInt("serverKillSwitch");
				    int suspendSwitch = resultSet.getInt("serverSuspendSwitch");
				    int backgroundRender = resultSet.getInt("serverBackgroundRender");
				    int renderDefaultHangTime = resultSet.getInt("serverHangTimeThreshold");
				    
				    /*
				    MasterMain.log.debug("serverName="+serverName);
				    MasterMain.log.debug("serverIpAddress="+serverIpAddress);
	        		MasterMain.log.debug("serverStatus="+serverStatus);
	        		MasterMain.log.debug("serverStatusTimestamp="+serverStatusTimestamp);
	        		MasterMain.log.debug("currentFile="+currentFile);
	        		MasterMain.log.debug("serverRenderStart="+serverRenderStart);
	        		MasterMain.log.debug("serverRenderEnd="+serverRenderEnd);
	        		MasterMain.log.debug("serverRenderLastUpdate="+serverRenderLastUpdate);
	        		MasterMain.log.debug("frameStart="+frameStart);
	        		MasterMain.log.debug("frameEnd="+frameEnd);
	        		MasterMain.log.debug("frameCount="+frameCount);
	        		MasterMain.log.debug("currentFrame="+currentFrame);
	        		MasterMain.log.debug("renderFrameCount="+renderFrameCount);
	        		MasterMain.log.debug("renderOutDir="+renderOutDir);
	        		MasterMain.log.debug("killSwitch="+killSwitch);
	        		MasterMain.log.debug("suspendSwitch="+suspendSwitch);
	        		MasterMain.log.debug("backgroundRender="+backgroundRender);
	        		MasterMain.log.debug("renderDefaultHangTime="+renderDefaultHangTime);
					*/
				    
	        		serverStatuses.add(new ServerStatus(serverName, serverIpAddress, serverStatus, serverStatusTimestamp, currentFile, 
				    		serverRenderStart, serverRenderEnd, serverRenderLastUpdate, frameStart, frameEnd, frameCount, currentFrame, renderFrameCount, 
				    		renderOutDir, killSwitch, suspendSwitch, backgroundRender, renderDefaultHangTime));
				}
			}
		} catch (Exception e) {
			MasterMain.log.debug(e);
		} finally {
			close(resultSet, preparedStatement, conection); 
			//MasterMain.log.debug("Ending getServerStatusList");
		}
		return serverStatuses;
	}

	public static void deleteServerStatusData(String requestedServerName, String requestedServerIpAddress) throws SQLException {

		//MasterMain.log.debug("Starting deleteServerStatusData");
		String query = null;

		Connection conection = GlobalClass.getConection();
		PreparedStatement preparedStatement = null;
		query = "delete from ServerStatus where serverName = ? and ipAddress = ?";
		//MasterMain.log.debug("query="+query);

		try {
			preparedStatement = conection.prepareStatement(query);
			preparedStatement.setString(1, requestedServerName);
			preparedStatement.setString(2, requestedServerIpAddress);
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			MasterMain.log.debug(e);
		} finally {
			close(null, preparedStatement, conection); 
			//MasterMain.log.debug("Ending deleteServerStatusData");
		}
	}

   	public static void saveServerStatusData(String serverName, String serverIpAddress, String serverStatus, String lastStatusUpdate, String currentFile,
				String renderStartTime, String renderEndTime, String lastRenderUpdate, String startFrame, String endFrame,
				String countFrame, String currentFrameNumber, String cumulativeRenderFrameCount, String renderOutDir,
				String hangTimeThreshold, String switchSuspend, String switchKill, String backgroundSwitch) throws SQLException {

   		
		//MasterMain.log.debug("Starting saveServerStatusData");
		/*
		MasterMain.log.debug("serverName="+serverName);
	    MasterMain.log.debug("serverIpAddress="+serverIpAddress);
		MasterMain.log.debug("serverStatus="+serverStatus);
		MasterMain.log.debug("serverStatusTimestamp="+lastStatusUpdate);
		MasterMain.log.debug("currentFile="+currentFile);
		MasterMain.log.debug("serverRenderStart="+renderStartTime);
		MasterMain.log.debug("serverRenderEnd="+renderEndTime);
		MasterMain.log.debug("serverRenderLastUpdate="+lastRenderUpdate);
		MasterMain.log.debug("frameStart="+startFrame);
		MasterMain.log.debug("frameEnd="+endFrame);
		MasterMain.log.debug("frameCount="+countFrame);
		MasterMain.log.debug("currentFrame="+currentFrameNumber);
		MasterMain.log.debug("renderFrameCount="+cumulativeRenderFrameCount);
		MasterMain.log.debug("renderOutDir="+renderOutDir);
		MasterMain.log.debug("killSwitch="+switchKill);
		MasterMain.log.debug("suspendSwitch="+switchSuspend);
		MasterMain.log.debug("backgroundRender="+backgroundSwitch);
		MasterMain.log.debug("renderDefaultHangTime="+hangTimeThreshold);
		*/

	    Timestamp serverStatusTimestamp = null;
	    if (!lastStatusUpdate.equals(" ")) {
	    	serverStatusTimestamp = Timestamp.valueOf(lastStatusUpdate);
	    }
		//MasterMain.log.debug("1="+serverStatusTimestamp);

	    Timestamp serverRenderStart = null;
	    if (!renderStartTime.equals(" ")) {
	    	serverRenderStart = Timestamp.valueOf(renderStartTime);
	    }
		//MasterMain.log.debug("2="+serverRenderStart);
	    
	    Timestamp serverRenderEnd = null;
	    if (!renderEndTime.equals(" ")) {
	    	serverRenderEnd = Timestamp.valueOf(renderEndTime);
	    }
		//MasterMain.log.debug("3="+serverRenderEnd);
	    
	    Timestamp serverRenderLastUpdate = null;
	    if (!lastRenderUpdate.equals(" ")) {
	    	//MasterMain.log.debug("4a="+lastRenderUpdate+"|");
	    	serverRenderLastUpdate = Timestamp.valueOf(lastRenderUpdate);
	    } else {
	    	//MasterMain.log.debug("4b="+lastRenderUpdate+"1988-08-19 00:00:00.0"+"|");
	    	serverRenderLastUpdate = Timestamp.valueOf("1988-08-19 00:00:00.0");
	    	//Utils.getCurrentTime()
	    }
		//MasterMain.log.debug("4="+serverRenderLastUpdate);
	    
	    int frameStart = 0;
	    if (!startFrame.equals(" ")) {
	    	frameStart = Integer.parseInt(startFrame);
	    }
		//MasterMain.log.debug("5="+frameStart);
	    int frameEnd = 0;
	    if (!endFrame.equals(" ")) {
	    	frameEnd = Integer.parseInt(endFrame);
	    }
		//MasterMain.log.debug("6="+frameEnd);
	    int frameCount = 0;
	    if (!countFrame.equals(" ")) {
	    	frameCount = Integer.parseInt(countFrame);
	    }
		//MasterMain.log.debug("7="+frameCount);
	    int currentFrame = 0;
	    if (!currentFrameNumber.equals(" ")) {
	    	currentFrame = Integer.parseInt(currentFrameNumber);
	    }
		//MasterMain.log.debug("8="+currentFrame);
	    int renderFrameCount = 0;
	    if (!cumulativeRenderFrameCount.equals(" ")) {
	    	renderFrameCount = Integer.parseInt(cumulativeRenderFrameCount);
	    }
		//MasterMain.log.debug("9="+renderFrameCount);
	    int killSwitch = 0;
	    if (!switchKill.equals(" ")) {
	    	killSwitch = Integer.parseInt(switchKill);
	    }
		//MasterMain.log.debug("10="+killSwitch);
	    int suspendSwitch = 1;
	    if (!switchSuspend.equals(" ")) {
	    	suspendSwitch = Integer.parseInt(switchSuspend);
	    }
		//MasterMain.log.debug("11="+suspendSwitch);
	    int backgroundRender = 0;
	    if (!backgroundSwitch.equals(" ")) {
	    	backgroundRender = Integer.parseInt(backgroundSwitch);
	    }
		//MasterMain.log.debug("12="+backgroundRender);
	    int renderDefaultHangTime = 1800;
	    if (!hangTimeThreshold.equals(" ")) {
	    	renderDefaultHangTime = Integer.parseInt(hangTimeThreshold);
	    }
		//MasterMain.log.debug("13="+renderDefaultHangTime);
		
		/*
		MasterMain.log.debug("serverName="+serverName);
	    MasterMain.log.debug("serverIpAddress="+serverIpAddress);
		MasterMain.log.debug("serverStatus="+serverStatus);
		MasterMain.log.debug("serverStatusTimestamp="+serverStatusTimestamp);
		MasterMain.log.debug("currentFile="+currentFile);
		MasterMain.log.debug("serverRenderStart="+serverRenderStart);
		MasterMain.log.debug("serverRenderEnd="+serverRenderEnd);
		MasterMain.log.debug("serverRenderLastUpdate="+serverRenderLastUpdate);
		MasterMain.log.debug("frameStart="+frameStart);
		MasterMain.log.debug("frameEnd="+frameEnd);
		MasterMain.log.debug("frameCount="+frameCount);
		MasterMain.log.debug("currentFrame="+currentFrame);
		MasterMain.log.debug("renderFrameCount="+renderFrameCount);
		MasterMain.log.debug("renderOutDir="+renderOutDir);
		MasterMain.log.debug("killSwitch="+killSwitch);
		MasterMain.log.debug("suspendSwitch="+suspendSwitch);
		MasterMain.log.debug("backgroundRender="+backgroundRender);
		MasterMain.log.debug("renderDefaultHangTime="+renderDefaultHangTime);
		*/
		
		if ((serverName != null) && !serverName.equals("") && (serverIpAddress != null) && !serverIpAddress.equals("")) {

			// Get the maximum RowId value from the ServerStatus table
			int maxRowId = getServerStatusMaxRowId() + 1;
			
			Connection conection = GlobalClass.getConection();
			//conection.setAutoCommit(false);
			PreparedStatement preparedStatement = null;
			String query = null;
			boolean insertData = false;
			boolean updateData = false;

			if (getServerStatus(serverName, serverIpAddress).equalsIgnoreCase("ERROR"))  {
				insertData = true;
				//MasterMain.log.debug("Inserting ServerStatus data");
			} else {
				updateData = true;
				//MasterMain.log.debug("Updating ServerStatus data");
			}
			
			if (insertData) 
			{
				query = "INSERT INTO ServerStatus (ServerName, IpAddress, Status, StatusTime, CurrentFile, RenderStart, " +
						"RenderEnd, Timestamp, FrameStart, FrameEnd, FrameCount, CurrentFrame, RenderTotalFrameCount, " +
						"RenderOutDir, KillSwitch, SuspendSwitch, BackgroundSwitch, RenderDefaultHangTime, RowId) " +
						"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			} else {
				query = "UPDATE ServerStatus SET ServerName=?, IpAddress=?, Status=?, StatusTime=?, CurrentFile=?, RenderStart=?, " +
					"RenderEnd=?, Timestamp=?, FrameStart=?, FrameEnd=?, FrameCount=?, CurrentFrame=?, RenderTotalFrameCount=?, " +
					"RenderOutDir=?, KillSwitch=?, SuspendSwitch=?, BackgroundSwitch=?, RenderDefaultHangTime=? where ServerName = ? and IpAddress = ?";
			}
			//MasterMain.log.debug("query="+query);
				
			try {
				//MasterMain.log.debug("Starting prepared statements in saveServerStatusData");
				preparedStatement = conection.prepareStatement(query);
				preparedStatement.setString(1, serverName);
				preparedStatement.setString(2, serverIpAddress);
				preparedStatement.setString(3, serverStatus);
				preparedStatement.setTimestamp(4, serverStatusTimestamp);
				preparedStatement.setString(5, currentFile);
				preparedStatement.setTimestamp(6, serverRenderStart);
				preparedStatement.setTimestamp(7, serverRenderEnd);
				preparedStatement.setTimestamp(8, serverRenderLastUpdate);
				preparedStatement.setInt(9, frameStart);
				preparedStatement.setInt(10, frameEnd);
				preparedStatement.setInt(11, frameCount);
				preparedStatement.setInt(12, currentFrame);
				preparedStatement.setInt(13, renderFrameCount);
				preparedStatement.setString(14, renderOutDir);
				preparedStatement.setInt(15, killSwitch);
				preparedStatement.setInt(16, suspendSwitch);
				preparedStatement.setInt(17, backgroundRender);
				preparedStatement.setInt(18, renderDefaultHangTime);
				if (insertData)
				{
					preparedStatement.setInt(19, maxRowId);
				} else {
					preparedStatement.setString(19, serverName);
					preparedStatement.setString(20, serverIpAddress);
				}
				//MasterMain.log.debug("Before executeUpdate in saveServerStatusData");
				preparedStatement.executeUpdate();
				//MasterMain.log.debug("After executeUpdate in saveServerStatusData");
				//conection.commit();
			} catch (Exception e) {
				//conection.rollback();
			} finally {
				close(null, preparedStatement, conection); 
				//MasterMain.log.debug("Ending saveServerStatusData");
			}
		}
   	}

	public static void netUpdateServerSuspendSwitchStatus(String serverName, String serverIpAddress, int serverSuspendStatus) throws SQLException {

		Boolean pendingUpdate = true;
    	String fromServer = null;
    	String localIpAddress = null;
    	
    	try {
			localIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	String serverCmd = ApplicationConstants.RENDER_UPDATE_SUSPEND_SWITCH + serverName + "|" + serverIpAddress +"|" + Integer.toString(serverSuspendStatus);
    	
    	//MasterMain.log.debug("Sending server suspend switch update request for " + serverName + "(" + serverIpAddress + ")");

    	while (pendingUpdate) {
    		Socket mcScoket = null;
	    	try {
	    		if (GlobalClass.isH2ServerMode()) {
	    			mcScoket = new Socket(localIpAddress, GlobalClass.getPortNum());
	    		} else {
	    			mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
	    		}
	    		mcScoket.setTcpNoDelay(true);
		        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
		    	out.println(serverCmd);
    	    	out.flush();
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(mcScoket.getInputStream()));
		        if ((fromServer = in.readLine()) != null) {
		            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
		            if (fromServer.equals(ApplicationConstants.DATABASE_UPDATE_OK)) {
		            	pendingUpdate = false;
			            //MasterMain.log.debug("Update completed");
		            }
		        }
		        mcScoket.close();
	    	} catch (ConnectException e) {
	    	      //throw e;
	    	} catch (IOException e) {
	    	      e.printStackTrace();
	    	}
    	}
	}
	
	public static void netUpdateServerKillSwitchStatus(String serverName, String serverIpAddress, int serverKillSwitchStatus) throws SQLException {

		Boolean pendingUpdate = true;
    	String fromServer = null;
    	String localIpAddress = null;
    	
    	try {
			localIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	String serverCmd = ApplicationConstants.RENDER_UPDATE_KILL_SWITCH + serverName + "|" + serverIpAddress +"|" + Integer.toString(serverKillSwitchStatus);
    	
    	//MasterMain.log.debug("Sending server kill switch update request for " + serverName + "(" + serverIpAddress + ")");

    	while (pendingUpdate) {
    		Socket mcScoket = null;
	    	try {
	    		if (GlobalClass.isH2ServerMode()) {
	    			mcScoket = new Socket(localIpAddress, GlobalClass.getPortNum());
	    		} else {
	    			mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
	    		}
	    		mcScoket.setTcpNoDelay(true);
		        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
		    	out.println(serverCmd);
    	    	out.flush();
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(mcScoket.getInputStream()));
		        if ((fromServer = in.readLine()) != null) {
		            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
		            if (fromServer.equals(ApplicationConstants.DATABASE_UPDATE_OK)) {
		            	pendingUpdate = false;
			            //MasterMain.log.debug("Update completed");
		            }
		        }
		        mcScoket.close();
	    	} catch (ConnectException e) {
	    	      //throw e;
	    	} catch (IOException e) {
	    	      e.printStackTrace();
	    	}
    	}
	}
	
	public static void netUpdateServerBackgroundSwitchStatus(String serverName, String serverIpAddress, int serverBackgroundSwitchStatus) throws SQLException {

		Boolean pendingUpdate = true;
    	String fromServer = null;
    	String localIpAddress = null;
    	
    	try {
			localIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	String serverCmd = ApplicationConstants.RENDER_UPDATE_BACKGROUND_SWITCH + serverName + "|" + serverIpAddress +"|" + Integer.toString(serverBackgroundSwitchStatus);
    	
    	//MasterMain.log.debug("Sending server background switch update request for " + serverName + "(" + serverIpAddress + ")");

    	while (pendingUpdate) {
    		Socket mcScoket = null;
	    	try {
	    		if (GlobalClass.isH2ServerMode()) {
	    			mcScoket = new Socket(localIpAddress, GlobalClass.getPortNum());
	    		} else {
	    			mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
	    		}
	    		mcScoket.setTcpNoDelay(true);
		        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
		    	out.println(serverCmd);
    	    	out.flush();
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(mcScoket.getInputStream()));
		        if ((fromServer = in.readLine()) != null) {
		            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
		            if (fromServer.equals(ApplicationConstants.DATABASE_UPDATE_OK)) {
		            	pendingUpdate = false;
			            //MasterMain.log.debug("Update completed");
		            }
		        }
		        mcScoket.close();
	    	} catch (ConnectException e) {
	    	      //throw e;
	    	} catch (IOException e) {
	    	      e.printStackTrace();
	    	}
    	}
	}
	
	public static void netGetServerSuspendSwitchStatus(String serverIpAddress, int serverSuspendStatus) throws SQLException {

		Boolean pendingUpdate = true;
    	String fromServer = null;
    	String localIpAddress = null;
    	
    	try {
			localIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	String serverCmd = ApplicationConstants.RENDER_GET_SUSPEND_SWITCH + serverIpAddress +"|" + Integer.toString(serverSuspendStatus);
    	
    	//MasterMain.log.debug("Sending server get suspend switch status request for " + serverIpAddress);

    	while (pendingUpdate) {
    		Socket mcScoket = null;
	    	try {
	    		if (GlobalClass.isH2ServerMode()) {
	    			mcScoket = new Socket(localIpAddress, GlobalClass.getPortNum());
	    		} else {
	    			mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
	    		}
	    		mcScoket.setTcpNoDelay(true);
		        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
		    	out.println(serverCmd);
    	    	out.flush();
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(mcScoket.getInputStream()));
		        if ((fromServer = in.readLine()) != null) {
		            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
		            if (fromServer.equals(ApplicationConstants.DATABASE_UPDATE_OK)) {
		            	pendingUpdate = false;
		            }
		        }
		        mcScoket.close();
	    	} catch (ConnectException e) {
	    	      //throw e;
	    	} catch (IOException e) {
	    	      e.printStackTrace();
	    	}
    	}
	}
	
	public static String netGetServerSuspendKillSwitchStatus(String serverIpAddress) throws SQLException {

		Boolean pendingUpdate = true;
    	String fromServer = null;
    	String localIpAddress = null;
    	
    	try {
			localIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	String serverCmd = ApplicationConstants.RENDER_GET_SUSPEND_KILL_SWITCH + serverIpAddress;
    	
    	//MasterMain.log.debug("Sending server get suspend and kill switch status request for " + serverIpAddress);

    	while (pendingUpdate) {
    		Socket mcScoket = null;
	    	try {
	    		if (GlobalClass.isH2ServerMode()) {
	    			mcScoket = new Socket(localIpAddress, GlobalClass.getPortNum());
	    		} else {
	    			mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
	    		}
	    		mcScoket.setTcpNoDelay(true);
		        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
		    	out.println(serverCmd);
    	    	out.flush();
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(mcScoket.getInputStream()));
		        if ((fromServer = in.readLine()) != null) {
		            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
	        		fromServer = fromServer.substring(ApplicationConstants.RENDER_GET_SUSPEND_KILL_SWITCH.length());
	            	pendingUpdate = false;
		        }
		        mcScoket.close();
	    	} catch (ConnectException e) {
	    	      //throw e;
	    	} catch (IOException e) {
	    	      e.printStackTrace();
	    	}
    	}
    	return fromServer;
	}
	
	public static void netGetServerKillSwitchStatus(String serverIpAddress, int serverKillSwitchStatus) throws SQLException {

		Boolean pendingUpdate = true;
    	String fromServer = null;
    	String localIpAddress = null;
    	
    	try {
			localIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	String serverCmd = ApplicationConstants.RENDER_GET_KILL_SWITCH + serverIpAddress +"|" + Integer.toString(serverKillSwitchStatus);
    	
    	//MasterMain.log.debug("Sending server get kill switch status request for " + serverIpAddress);

    	while (pendingUpdate) {
    		Socket mcScoket = null;
	    	try {
	    		if (GlobalClass.isH2ServerMode()) {
	    			mcScoket = new Socket(localIpAddress, GlobalClass.getPortNum());
	    		} else {
	    			mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
	    		}
	    		mcScoket.setTcpNoDelay(true);
		        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
		    	out.println(serverCmd);
    	    	out.flush();
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(mcScoket.getInputStream()));
		        if ((fromServer = in.readLine()) != null) {
		            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
		            if (fromServer.equals(ApplicationConstants.DATABASE_UPDATE_OK)) {
		            	pendingUpdate = false;
		            }
		        }
		        mcScoket.close();
	    	} catch (ConnectException e) {
	    	      //throw e;
	    	} catch (IOException e) {
	    	      e.printStackTrace();
	    	}
    	}
	}
	
	public static void netPurgeStaleServers(String serverName, String serverIpAddress) throws SQLException {

		Boolean pendingUpdate = true;
    	String fromServer = null;
    	String localIpAddress = null;
    	
    	try {
			localIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	String serverCmd = ApplicationConstants.PURGE_STALE_SERVERS + serverName + "|" + serverIpAddress;
    	
    	MasterMain.log.debug("Sending purge stale servers request for " + serverName + " (" + serverIpAddress + ")");

    	while (pendingUpdate) {
    		Socket mcScoket = null;
	    	try {
	    		if (GlobalClass.isH2ServerMode()) {
	    			mcScoket = new Socket(localIpAddress, GlobalClass.getPortNum());
	    		} else {
	    			mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
	    		}
	    		mcScoket.setTcpNoDelay(true);
		        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
		    	out.println(serverCmd);
    	    	out.flush();
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(mcScoket.getInputStream()));
		        if ((fromServer = in.readLine()) != null) {
		            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
		            if (fromServer.equals(ApplicationConstants.DATABASE_UPDATE_OK)) {
		            	pendingUpdate = false;
			            //MasterMain.log.debug("Update completed");
		            }
		        }
		        mcScoket.close();
	    	} catch (ConnectException e) {
	    	      //throw e;
	    	} catch (IOException e) {
	    	      e.printStackTrace();
	    	}
    	}
	}
	
	public static synchronized void netInsertDummyServerQueueFile() throws SQLException {

		//		Boolean pendingUpdate = true;
    	String fromServer = null;
    	boolean pendingUpdate = true;
    	String localIpAddress = null;
    	
    	try {
			localIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	String serverCmd = ApplicationConstants.INSERT_DUMMY_SERVER_QUEUE_FILE;
    	
    	MasterMain.log.debug("Inserting dummy server queue file");

    	while (pendingUpdate) {
    		Socket mcScoket = null;
	    	try {
	    		if (GlobalClass.isH2ServerMode()) {
	    			mcScoket = new Socket(localIpAddress, GlobalClass.getPortNum());
	    		} else {
	    			mcScoket = new Socket(GlobalClass.getServerMasterIpAddress(), GlobalClass.getPortNum());
	    		}
	    		mcScoket.setTcpNoDelay(true);
		        PrintWriter out = new PrintWriter(mcScoket.getOutputStream(), true);
		    	out.println(serverCmd);
    	    	out.flush();
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(mcScoket.getInputStream()));
		        if ((fromServer = in.readLine()) != null) {
		            //MasterMain.log.debug(ApplicationConstants.SERVER + fromServer);
		            if (fromServer.equals(ApplicationConstants.DATABASE_UPDATE_OK)) {
		            	pendingUpdate = false;
			            //MasterMain.log.debug("Update completed");
		            }
		        }
		        mcScoket.close();
	    	} catch (ConnectException e) {
	    	      //throw e;
	    	} catch (IOException e) {
	    	      e.printStackTrace();
	    	}
    	}
	}

	public static void close(ResultSet rs, PreparedStatement ps, Connection conn)
	{
	    if (rs!=null) {
	        try {
	            rs.close();
	        } catch(SQLException e) {
	        	MasterMain.log.error("The result set cannot be closed.", e);
	        }
	    }
	    
	    if (ps != null) {
	        try {
	            ps.close();
	        } catch (SQLException e) {
	        	MasterMain.log.error("The statement cannot be closed.", e);
	        }
	    }

	    //MasterMain.log.debug("GlobalClass.getCloseDB()="+GlobalClass.getCloseDB());
	    /*
	    if (conn != null) {
		    MasterMain.log.debug("close conn != null - GlobalClass.getCloseDB()="+GlobalClass.getCloseDB());
	    } else {
		    MasterMain.log.debug("close conn == null - GlobalClass.getCloseDB()="+GlobalClass.getCloseDB());
	    }
	    */
	    if (GlobalClass.getCloseDB()) {
		    if (conn != null) {
		        try {
		        	//MasterMain.log.debug("Closing database connection");
		            conn.close();
		            GlobalClass.setConection(null);
		        } catch (SQLException e) {
		        	MasterMain.log.error("The data source connection cannot be closed.", e);
		        }
		    }
            GlobalClass.setCloseDB(false);
	    }
	}	
	
}
