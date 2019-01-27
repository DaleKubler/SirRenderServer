package tv.culturesport.sirrender;

/*
 * Copyright (c) 2016, Dale Kubler. All rights reserved.
 *
 */

import java.net.*;
import java.sql.SQLException;
import java.io.*;
import tv.culturesport.sirrender.ServerProtocol;

public class MultiServerThread extends Thread {
    private Socket socket = null;

    public MultiServerThread(Socket socket) {
        super("MultiServerThread");
        this.socket = socket;
    }

    public void run() {

        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
        	if (GlobalClass.isSocketDebug() == true) {
        		MasterMain.log.debug("Socket open request on port "+GlobalClass.getPortNumberStr());
        	}
            String inputLine, outputLine;
			DebugInformation.printSocketInformation(socket);
            ///ClientServerProtocol csp = new ClientServerProtocol();
            ServerProtocol sp = new ServerProtocol();
            ///outputLine = csp.processInput(null);
            ///out.println(outputLine);
	    	///out.flush();
			//MasterMain.log.debug("outputLine1="+outputLine);
            while ((inputLine = in.readLine()) != null) {
    			//MasterMain.log.debug("inputLine1="+inputLine);
				//MasterMain.log.debug("Before csp.processInput(inputLine in MultiServerThread");
 ///               outputLine = csp.processInput(inputLine);
            	outputLine = ServerProtocol.processInput(inputLine);
				//MasterMain.log.debug("After csp.processInput(inputLine in MultiServerThread");
				//MasterMain.log.debug("outputLine2="+outputLine);
                out.println(outputLine);
    	    	out.flush();
                if (outputLine.equals(ApplicationConstants.BYE))
                    break;
            }
            in.close();
            out.close();
            socket.close();
		} catch (SocketException e) {
			MasterMain.log.debug("Socket closed - SocketException");
			DebugInformation.printSocketInformation(socket);
            e.printStackTrace();
        } catch (IOException e) {
			MasterMain.log.debug("Socket closed - IOException");
			DebugInformation.printSocketInformation(socket);
            e.printStackTrace();
        } catch (SQLException e) {
			MasterMain.log.debug("Socket closed - SQLException");
			DebugInformation.printSocketInformation(socket);
			e.printStackTrace();
		} catch (InterruptedException e) {
			MasterMain.log.debug("Socket closed - InterruptedException");
			DebugInformation.printSocketInformation(socket);
			e.printStackTrace();
		}
    }
}