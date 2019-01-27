package tv.culturesport.sirrender;

/**
 * Model class for a Server
 *
 * @author Dale Kubler
 */

public class ServerStatus {
    private final String serverName;
    private final String serverIpAddress;
    private final String serverStatus;
    private final java.sql.Timestamp serverStatusTimestamp;
    private final String serverCurrentFile;
    private final java.sql.Timestamp serverRenderStart;
    private final java.sql.Timestamp serverRenderEnd;
    private final java.sql.Timestamp serverRenderLastUpdate;
    private final int serverStartFrame;
    private final int serverEndFrame;
    private final int serverFrameCount;
    private final int serverCurrentFrame;
    private final int serverCumulativeFrameCount;
    private final String serverOutputFile;
    private final int serverKillSwitch;
    private final int serverSuspendSwitch;
    private final int serverBackgroundRender;
    private final int serverHangTimeThreshold;


	/**
     * Default constructor.
     */
    public ServerStatus() {
        this(null, null, null, null, null, null, null, null, 0, 0, 0, 0, 0, null, 0, 0, 0, 0);
    }

    /**
     * Constructor with some initial data.
     * 
     * @param serverName
     * @param serverIpAddress
     * @param serverRenderLastUpdate 
     */
    public ServerStatus(String serverName, String serverIpAddress, String serverStatus, java.sql.Timestamp serverStatusTimestamp, String serverCurrentFile, 
    		java.sql.Timestamp serverRenderStart, java.sql.Timestamp serverRenderEnd, java.sql.Timestamp serverRenderLastUpdate, int serverStartFrame, 
    		int serverEndFrame, int serverFrameCount, int serverCurrentFrame, int serverCumulativeFrameCount, String serverOutputFile,
    		int serverKillSwitch, int serverSuspendSwitch, int serverBackgroundRender, int serverHangTimeThreshold) {
        this.serverName = serverName;
        this.serverIpAddress = serverIpAddress;
        this.serverStatus = serverStatus;
        this.serverStatusTimestamp = serverStatusTimestamp;
        this.serverCurrentFile = serverCurrentFile;
        this.serverRenderStart = serverRenderStart;
        this.serverRenderEnd = serverRenderEnd;
        this.serverRenderLastUpdate = serverRenderLastUpdate;
        this.serverStartFrame = serverStartFrame;
        this.serverEndFrame = serverEndFrame;
        this.serverFrameCount = serverFrameCount;
        this.serverCurrentFrame = serverCurrentFrame;
        this.serverCumulativeFrameCount = serverCumulativeFrameCount;
        this.serverOutputFile = serverOutputFile;
        this.serverKillSwitch = serverKillSwitch;
        this.serverSuspendSwitch = serverSuspendSwitch;
        this.serverBackgroundRender = serverBackgroundRender;
        this.serverHangTimeThreshold = serverHangTimeThreshold;
    }

	public final String getServerName() {
		return serverName;
	}

	public final String getServerIpAddress() {
		return serverIpAddress;
	}

	public final String getServerStatus() {
		return serverStatus;
	}

	public final java.sql.Timestamp getServerStatusTimestamp() {
		return serverStatusTimestamp;
	}

	public final String getServerCurrentFile() {
		return serverCurrentFile;
	}

	public final java.sql.Timestamp getServerRenderStart() {
		return serverRenderStart;
	}

	public final java.sql.Timestamp getServerRenderEnd() {
		return serverRenderEnd;
	}

	public final java.sql.Timestamp getServerRenderLastUpdate() {
		return serverRenderLastUpdate;
	}

	public final int getServerStartFrame() {
		return serverStartFrame;
	}

	public final int getServerEndFrame() {
		return serverEndFrame;
	}

	public final int getServerFrameCount() {
		return serverFrameCount;
	}

	public final int getServerCurrentFrame() {
		return serverCurrentFrame;
	}

	public final int getServerCumulativeFrameCount() {
		return serverCumulativeFrameCount;
	}

	public final String getServerOutputFile() {
		return serverOutputFile;
	}

	public final int getServerKillSwitch() {
		return serverKillSwitch;
	}

	public final int getServerSuspendSwitch() {
		return serverSuspendSwitch;
	}

	public final int getServerBackgroundRender() {
		return serverBackgroundRender;
	}

	public final int getServerHangTimeThreshold() {
		return serverHangTimeThreshold;
	}


	
}