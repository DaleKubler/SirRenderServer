package tv.culturesport.sirrender;

import java.net.Socket;

public class DebugInformation {

	/**
	 * Prints debug output (to stdout) for the given Java Socket.
	 */
	public static void printSocketInformation(Socket socket)
	{
		if (GlobalClass.isSocketDebug() == true) {
		  try
		  {
		    MasterMain.log.info("Port:                 " + socket.getPort());
		    MasterMain.log.debug("Canonical Host Name:  " + socket.getInetAddress().getCanonicalHostName());
		    MasterMain.log.info("Host Address:         " + socket.getInetAddress().getHostAddress());
		    MasterMain.log.debug("Local Address:        " + socket.getLocalAddress());
		    MasterMain.log.debug("Local Port:           " + socket.getLocalPort());
		    MasterMain.log.info("Local Socket Address: " + socket.getLocalSocketAddress());
		    MasterMain.log.debug("Receive Buffer Size:  " + socket.getReceiveBufferSize());
		    MasterMain.log.debug("Send Buffer Size:     " + socket.getSendBufferSize());
		    MasterMain.log.debug("Keep-Alive:           " + socket.getKeepAlive());
		    MasterMain.log.debug("SO Timeout:           " + socket.getSoTimeout());
		  } catch (Exception e) {
		    e.printStackTrace();
		  }
		}
	}
	
}
