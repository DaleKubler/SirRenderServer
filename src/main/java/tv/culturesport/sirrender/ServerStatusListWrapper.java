package tv.culturesport.sirrender;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Helper class to wrap a list of serverStatuses. This is used for saving the list of serverStatuses to XML.
 * 
 * @author Dale Kubler
 */
@XmlRootElement(name = "serverStatuses")
public class ServerStatusListWrapper {

    private List<ServerStatus> serverStatuses;

    @XmlElement(name = "server")
    public List<ServerStatus> getServerStatuses() {
        return serverStatuses;
    }

    public void setServerStatuses(List<ServerStatus> serverStatuses) {
        this.serverStatuses = serverStatuses;
    }
}