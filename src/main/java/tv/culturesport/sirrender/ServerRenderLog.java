package tv.culturesport.sirrender;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Model class for Server Render Log data
 *
 * @author Dale Kubler
 */



public class ServerRenderLog {
    private final StringProperty logIpAddress;
    private final StringProperty logServerName;
    private final StringProperty logStatMsg;
    private final StringProperty logDate;
    private final StringProperty logTime;
    private final IntegerProperty logSeqNum;

    
	/**
     * Default constructor.
     */
    public ServerRenderLog() {
        this(null, null, null, null, null, 0);
    }

    /**
     * Constructor with some initial data.
     * 
     * @param serverName
     * @param serverIpAddress
     */
    public ServerRenderLog(String logIpAddress, String logServerName, String logStatMsg, String logDate, String logTime, int logSeqNum) {
        this.logIpAddress = new SimpleStringProperty(logIpAddress);
        this.logServerName = new SimpleStringProperty(logServerName);
        this.logStatMsg = new SimpleStringProperty(logStatMsg);
        this.logDate = new SimpleStringProperty(logDate);
        this.logTime = new SimpleStringProperty(logTime);
        this.logSeqNum = new SimpleIntegerProperty(logSeqNum);
    }

	public final String getLogIpAddress() {
		return logIpAddress.get();
	}

	public void setLogIpAddress(String logIpAddress) {
		this.logIpAddress.set(logIpAddress);
	}

	public StringProperty logIpAddressProperty() {
		return logIpAddress;
	}

	public final String getLogServerName() {
		return logServerName.get();
	}

	public void setLogServerName(String logServerName) {
		this.logServerName.set(logServerName);
	}

	public StringProperty logServerNameProperty() {
		return logServerName;
	}

	public final String getLogStatMsg() {
		return logStatMsg.get();
	}

	public void setLogStatMsg(String logStatMsg) {
		this.logStatMsg.set(logStatMsg);
	}

	public StringProperty logStatMsgProperty() {
		return logStatMsg;
	}

	public final String getLogDate() {
		return logDate.get();
	}

	public void setLogDate(String logDate) {
		this.logDate.set(logDate);
	}

	public StringProperty logDateProperty() {
		return logDate;
	}

	public final String getLogTime() {
		return logTime.get();
	}

	public void setLogTime(String logTime) {
		this.logTime.set(logTime);
	}

	public StringProperty logTimeProperty() {
		return logTime;
	}

	public final int getLogSeqNum() {
		return logSeqNum.get();
	}

	public void setLogSeqNum(Integer logSeqNum) {
		this.logSeqNum.set(logSeqNum);
	}

	public IntegerProperty logSeqNumProperty() {
		return logSeqNum;
	}

	
}