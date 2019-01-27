package tv.culturesport.sirrender;

/**
 * Model class for a Server
 *
 * @author Dale Kubler
 */

public class Server {
    private String serverName;
    private String serverIpAddress;
    private String serverAvailable;
    private String serverKillSwitch;
    private String serverSuspend;
    private String serverBackground;
    private String serverType;
    private int serverMasterPriority  = 0;
    private int serverDefaultHangTime = 1800;
    

    /**
     * Default constructor.
     */
    public Server() {
        this(null, null, null, null, 99, 1800);
    }

    /**
     * Constructor with some initial data.
     *
     * @param serverName
     * @param serverIpAddress
     * @param serverAvailable
     * @param serverType
     * @param serverMasterSlavePriority
     * @param serverDeafaltHangTime
     */
    public Server(String serverName, String serverIpAddress, String serverAvailable, String serverType, int serverMasterSlavePriority, int serverDeafaltHangTime) {
        this.serverName =serverName;
        this.serverIpAddress = serverIpAddress;
        this.serverAvailable = serverAvailable;
        this.serverType = serverType;
        this.serverMasterPriority = serverMasterSlavePriority;
        this.serverDefaultHangTime = serverDeafaltHangTime;
    }

    public Server(String serverName, String serverIpAddress, String serverAvailable, String serverType, String serverKillSwitch, String serverSuspend, String serverBackground) {
        this.serverName = serverName;
        this.serverIpAddress = serverIpAddress;
        this.serverAvailable = serverAvailable;
        this.serverType = serverType;
        this.setServerKillSwitch(serverKillSwitch);
        this.setServerSuspend(serverSuspend);
        this.setServerBackground(serverBackground);
    }

	public final String getServerName() {
		return this.serverName;
	}

	public final void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public final String getServerIpAddress() {
		return this.serverIpAddress;
	}

	public final void setServerIpAddress(String serverIpAddress) {
		this.serverIpAddress = serverIpAddress;
	}

	public final String getServerAvailable() {
		return this.serverAvailable;
	}

	public final void setServerAvailable(String serverAvailable) {
		this.serverAvailable = serverAvailable;
	}

	public final String getServerType() {
		return this.serverType;
	}

	public final void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public int getServerMasterPriority() {
		return serverMasterPriority;
	}

	public void setServerMasterPriority(int serverMasterPriority) {
		this.serverMasterPriority = serverMasterPriority;
	}

	public int getServerDefaultHangTime() {
		return serverDefaultHangTime;
	}

	public void setServerDefaultHangTime(int serverDefaultHangTime) {
		this.serverDefaultHangTime = serverDefaultHangTime;
	}

	public String getServerKillSwitch() {
		return serverKillSwitch;
	}

	public void setServerKillSwitch(String serverKillSwitch) {
		this.serverKillSwitch = serverKillSwitch;
	}

	public String getServerSuspend() {
		return serverSuspend;
	}

	public void setServerSuspend(String serverSuspend) {
		this.serverSuspend = serverSuspend;
	}

	public String getServerBackground() {
		return serverBackground;
	}

	public void setServerBackground(String serverBackground) {
		this.serverBackground = serverBackground;
	}


}