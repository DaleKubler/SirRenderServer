package tv.culturesport.sirrender;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for a Server
 *
 * @author Dale Kubler
 */

public class RenderFileStatus {
    private final StringProperty fileName;
    private final StringProperty renderStatus;
    private final StringProperty serverName;
    private final StringProperty serverIpAddress;
    private final StringProperty currentFile;
    private final IntegerProperty frameStart;
    private final IntegerProperty frameEnd;
    private final IntegerProperty frameCount;
    private final IntegerProperty currentFrame;
    private final IntegerProperty renderFrameCount;
    private final IntegerProperty renderDefaultHangTime;
    private final IntegerProperty masterSlavePriority;
    private final StringProperty renderOutDir;


	/**
     * Default constructor.
     */
    public RenderFileStatus() {
    	this(null, null, null, null, null, 0, 0, 0, 0, 0, 0, 0, null);
    }

    /**
     * Constructor with some initial data.
     * 
     * @param serverName
     * @param serverIpAddress
     */
    public RenderFileStatus(String fileName, String renderStatus, String serverName, String serverIpAddress, String currentFile,
            int frameStart, int frameEnd, int frameCount, int currentFrame, int renderFrameCount, 
            int renderDefaultHangTime, int masterSlavePriority, String renderOutDir) 
    {
        this.fileName = new SimpleStringProperty(fileName);
        this.renderStatus = new SimpleStringProperty(renderStatus);
        this.serverName = new SimpleStringProperty(serverName);
        this.serverIpAddress = new SimpleStringProperty(serverIpAddress);
        this.currentFile = new SimpleStringProperty(currentFile);
        this.frameStart = new SimpleIntegerProperty(frameStart);
        this.frameEnd = new SimpleIntegerProperty (frameEnd);
        this.frameCount = new SimpleIntegerProperty (frameCount);
        this.currentFrame = new SimpleIntegerProperty (currentFrame);
        this.renderFrameCount = new SimpleIntegerProperty (renderFrameCount);
        this.renderDefaultHangTime = new SimpleIntegerProperty (renderDefaultHangTime);
        this.masterSlavePriority = new SimpleIntegerProperty (masterSlavePriority);
        this.renderOutDir = new SimpleStringProperty (renderOutDir);

    }

	public final StringProperty fileNameProperty() {
		return this.fileName;
	}
	
	public final String getFileName() {
		return this.fileNameProperty().get();
	}
	
	public final void setFileName(final String fileName) {
		this.fileNameProperty().set(fileName);
	}
	
	public final StringProperty renderStatusProperty() {
		return this.renderStatus;
	}
	
	public final String getRenderStatus() {
		return this.renderStatusProperty().get();
	}
	
	public final void setRenderStatus(final String renderStatus) {
		this.renderStatusProperty().set(renderStatus);
	}

	public final StringProperty currentFileProperty() {
		return this.currentFile;
	}
	

	public final String getCurrentFile() {
		return this.currentFileProperty().get();
	}
	

	public final void setCurrentFile(final String currentFile) {
		this.currentFileProperty().set(currentFile);
	}
	

	public final IntegerProperty frameStartProperty() {
		return this.frameStart;
	}
	

	public final int getFrameStart() {
		return this.frameStartProperty().get();
	}
	

	public final void setFrameStart(final int frameStart) {
		this.frameStartProperty().set(frameStart);
	}
	

	public final IntegerProperty frameEndProperty() {
		return this.frameEnd;
	}
	

	public final int getFrameEnd() {
		return this.frameEndProperty().get();
	}
	

	public final void setFrameEnd(final int frameEnd) {
		this.frameEndProperty().set(frameEnd);
	}
	

	public final IntegerProperty frameCountProperty() {
		return this.frameCount;
	}
	

	public final int getFrameCount() {
		return this.frameCountProperty().get();
	}
	

	public final void setFrameCount(final int frameCount) {
		this.frameCountProperty().set(frameCount);
	}
	

	public final IntegerProperty currentFrameProperty() {
		return this.currentFrame;
	}
	

	public final int getCurrentFrame() {
		return this.currentFrameProperty().get();
	}
	

	public final void setCurrentFrame(final int currentFrame) {
		this.currentFrameProperty().set(currentFrame);
	}
	

	public final IntegerProperty renderFrameCountProperty() {
		return this.renderFrameCount;
	}
	

	public final int getRenderFrameCount() {
		return this.renderFrameCountProperty().get();
	}
	

	public final void setRenderFrameCount(final int renderFrameCount) {
		this.renderFrameCountProperty().set(renderFrameCount);
	}
	

	public final IntegerProperty renderDefaultHangTimeProperty() {
		return this.renderDefaultHangTime;
	}
	

	public final int getRenderDefaultHangTime() {
		return this.renderDefaultHangTimeProperty().get();
	}
	

	public final void setRenderDefaultHangTime(final int renderDefaultHangTime) {
		this.renderDefaultHangTimeProperty().set(renderDefaultHangTime);
	}
	

	public final IntegerProperty masterSlavePriorityProperty() {
		return this.masterSlavePriority;
	}
	

	public final int getMasterSlavePriority() {
		return this.masterSlavePriorityProperty().get();
	}
	

	public final void setMasterSlavePriority(final int masterSlavePriority) {
		this.masterSlavePriorityProperty().set(masterSlavePriority);
	}
	

	public final StringProperty renderOutDirProperty() {
		return this.renderOutDir;
	}
	

	public final String getRenderOutDir() {
		return this.renderOutDirProperty().get();
	}
	

	public final void setRenderOutDir(final String renderOutDir) {
		this.renderOutDirProperty().set(renderOutDir);
	}

	public final StringProperty serverNameProperty() {
		return this.serverName;
	}
	

	public final String getServerName() {
		return this.serverNameProperty().get();
	}
	

	public final void setServerName(final String serverName) {
		this.serverNameProperty().set(serverName);
	}
	

	public final StringProperty serverIpAddressProperty() {
		return this.serverIpAddress;
	}
	

	public final String getServerIpAddress() {
		return this.serverIpAddressProperty().get();
	}
	

	public final void setServerIpAddress(final String serverIpAddress) {
		this.serverIpAddressProperty().set(serverIpAddress);
	}
	
	
}