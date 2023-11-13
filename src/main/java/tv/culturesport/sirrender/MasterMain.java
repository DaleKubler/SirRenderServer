package tv.culturesport.sirrender;

/*
 * Copyright (c) 2016, Dale Kubler. All rights reserved.
 *
 */

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import tv.culturesport.sirrender.H2;
import tv.culturesport.sirrender.Utils;

public class MasterMain extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8458732766431721056L;

	static Console console;

	/* Get actual class name to be printed on */
	static Logger log = Logger.getLogger(MasterMain.class.getName()); //LogManager.getLogger(MasterMain.class.getName());
	
	static String defaultPortNumberStr = "4444";

    static CheckboxMenuItem cb1 = new CheckboxMenuItem("Suspend Server");
	static CheckboxMenuItem cb2 = new CheckboxMenuItem("Enable Kill Switch");
	static CheckboxMenuItem cb3 = new CheckboxMenuItem("Render in Background");

    static String myIpAddress = null;
	static String myName = null;

	public static void main(String[] args) throws Throwable {

		InetAddress IP = InetAddress.getLocalHost();
		//String myIpAddress = IP.getHostAddress();
    	String myIpAddress = Utils.getLowIpAddress();
    	String myName = IP.getHostName();

    	if(args != null && args.length == 1) {
    		defaultPortNumberStr = args[0];
    	}
		MasterMain.log.debug("defaultPortNumberStr="+defaultPortNumberStr);
		GlobalClass.setServerMasterIpAddress(myIpAddress);
		GlobalClass.setPortNumberStr(defaultPortNumberStr);
		GlobalClass.setPortNum(Integer.valueOf(defaultPortNumberStr));

//        GlobalClass.setConection(H2.Connector());

    	// Register the server if this is the first time it has been seen by SirRender
    	try {
        	/* Use an appropriate Look and Feel */
    		//MasterMain.log.debug("Setting look and feel");
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                /* Turn off metal's use of bold fonts */
                UIManager.put("swing.boldMetal", Boolean.FALSE);
            } catch (UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            
            //Schedule a job for the event-dispatching thread:
            //adding TrayIcon.
    		//MasterMain.log.debug("Before createAndShowGUI");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI();
                }
            });

    		//MasterMain.log.debug("After createAndShowGUI");
            console = new Console(); // create console with not reference
    		MasterMain.log.debug("new Console()");
            
    		SwingUtilities.invokeLater(new Runnable() {
    			public void run() {
    				MainLogger.init(
    						new JTextAreaAppender(console.getTextArea()),
    						org.apache.log4j.Level.DEBUG);
    			}
    		});
            
    		if (GlobalClass.isMasterServer()) {
    			GlobalClass.setServerMasterIpAddress(getInitialMasterServerIpAddress());
    			MasterMain.log.debug("MasterServer IP Address="+GlobalClass.getServerMasterIpAddress());
    		
    			//MasterMain.log.debug("Before purge");
    			//MasterMain.log.debug("myName="+myName);
    			MasterMain.log.debug("myIpAddress="+myIpAddress);
    			//MasterMain.log.debug("portStr="+GlobalClass.getPortNumberStr());
    			//MasterMain.log.debug("port="+GlobalClass.getPortNum());
	    		if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) 
	    				|| "0.0.0.0".equals(GlobalClass.getServerMasterIpAddress())
	    				|| GlobalClass.isH2ServerMode()) 
	    		{
	    			//MasterMain.log.debug("database call");
		        	GlobalClass.setConection(H2.Connector());
	    			H2.purgeStaleServers(myName, myIpAddress);
	//    			GlobalClass.getConection().close();
	    		} else {
	    			//MasterMain.log.debug("network database call");
	    			H2.netPurgeStaleServers(myName, myIpAddress);
	    		}
	    		//MasterMain.log.debug("After purge");
    		} else {
	        	GlobalClass.setConection(H2.Connector());
				GlobalClass.setCloseDB(false);
    			H2.purgeStaleServers(myName, myIpAddress);
    		}

    		// Pause 2 seconds to allow database work to complete
    		try {
    			TimeUnit.SECONDS.sleep(2);
    		} catch (InterruptedException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
    		
        	// Verify there are at least 1000 rows in the ServerQueue table
        	// This is to allow for inserts to  the top of the queue
            //MasterMain.log.debug("Before inserting DummyServerQueueFile");
    		if (GlobalClass.isMasterServer()) {
	    		if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) 
	    				|| "0.0.0.0".equals(GlobalClass.getServerMasterIpAddress()) 
	    				|| GlobalClass.isH2ServerMode()) 
	    		{
	    			//MasterMain.log.debug("database call");
	    			H2.insertDummyServerQueueFile();
	    		} else {
	    			//MasterMain.log.debug("network database call");
	    			H2.netInsertDummyServerQueueFile();
	    		}
    		} else {
    			H2.insertDummyServerQueueFile();
    		}
    		//MasterMain.log.debug("After inserting DummyServerQueueFile");
        	//H2.deleteDummyServerQueueFile();

    		// Pause 2 seconds to allow database work to complete
    		try {
    			TimeUnit.SECONDS.sleep(2);
    		} catch (InterruptedException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
    		
	        // Determine if the database should be opened on this server and set server status to "Available"
			//MasterMain.log.debug("Before set server status");
    		if (GlobalClass.isMasterServer()) {
	    		if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) 
	    				|| "0.0.0.0".equals(GlobalClass.getServerMasterIpAddress()) 
	    				|| GlobalClass.isH2ServerMode()) 
	    		{
		    		MasterMain.log.debug("This is the master server - open database connection");
	    			//MasterMain.log.debug("database call");
	    			H2.setServerStatus(myName, myIpAddress, "Available");
	    		} else {
	    			//MasterMain.log.debug("network database call");
	    			H2.netSetServerStatus(myName, myIpAddress, "Available", false);
	    		}
    		} else {
    			H2.setServerStatus(myName, myIpAddress, "Available");
    		}
    		//MasterMain.log.debug("After set server status");
    		
    		// Pause 2 seconds to allow database work to complete
    		try {
    			TimeUnit.SECONDS.sleep(2);
    		} catch (InterruptedException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
    		
	        // If this is the MasterServer or is running in H@ server mode, checkpoint the database
    		if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) 
    				|| "0.0.0.0".equals(GlobalClass.getServerMasterIpAddress()) 
    				|| GlobalClass.isH2ServerMode()) 
    		{
	    		MasterMain.log.debug("checkpointing database");
	        	H2.checkpointDatabase();
	        }
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	/* Use an appropriate Look and Feel */
/*    	
		//MasterMain.log.debug("Setting look and feel");
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            // Turn off metal's use of bold fonts 
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
        //Schedule a job for the event-dispatching thread:
        //adding TrayIcon.
		//MasterMain.log.debug("Before createAndShowGUI");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

		//MasterMain.log.debug("After createAndShowGUI");
        console = new Console(); // create console with not reference
		MasterMain.log.debug("new Console()");
        
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainLogger.init(
						new JTextAreaAppender(console.getTextArea()),
						org.apache.log4j.Level.DEBUG);
			}
		});
*/
    	
        // Determine who the Master Server is
        //GlobalClass.setServerMasterIpAddress(H2.getMasterServerIpAddress());
        //GlobalClass.setServerMasterIpAddress(getInitialMasterServerIpAddress(GlobalClass.getPortNumberStr()));
        //GlobalClass.setLastPollTime(Utils.getCurrentTime());

		try {
			if(args != null && args.length > 1) {
				log.debug("====================================================");
				log.debug("Port number may be optionally specified.  Default port is 4444.");
				log.debug(" ");
				log.debug("Valid parameters are:");
				log.debug("   SirRender");
				log.debug("       or");
				log.debug("   SirRender <port number>");
			} else {
				tv.culturesport.sirrender.ServerApp.serverMain();
			}
		} catch (Exception ex) {
			System.exit(1);
		}

    }

    private static void createAndShowGUI() {
    	InetAddress IP = null;
		try {
			IP = InetAddress.getLocalHost();
		} catch (UnknownHostException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
    	
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            MasterMain.log.debug("SystemTray is not supported");
            return;
        }
        
        String suspendKill = "";
		try {
			myIpAddress = Utils.getLowIpAddress();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	myName = IP.getHostName();
        
        //String icoPath = "U:/SirRender/images/CS-Guy.png";
        //String icoPath = "V:/SirRender/images/CS-Guy.png";
        //String icoPath = "W:/SirRender/images/CS-Guy.png";
        //String icoPath = "X:/SirRender/images/CS-Guy.png";
        String icoPath = ApplicationConstants.DEFAULT_SIRRENDER_LAN_DRIVE+":/SirRender/images/CS-Guy.png";
        //String icoPath = "/resources/CS-Guy.jpg";

        final PopupMenu popup = new PopupMenu();

        final TrayIcon trayIcon = 
        		new TrayIcon(new ImageIcon(icoPath, "omt").getImage(), "SirRender Server");

        trayIcon.setImageAutoSize(true);// Autosize icon base on space
        
        final SystemTray tray = SystemTray.getSystemTray();
        
        // Create a popup menu components
    	// Remove requirement to display About box three times when quitting
    	// Replace with a Quit option
        // MenuItem aboutItem = new MenuItem("About");
        MenuItem aboutItem = new MenuItem("Quit");
        //CheckboxMenuItem cb1 = new CheckboxMenuItem("Suspend Server");
        //CheckboxMenuItem cb2 = new CheckboxMenuItem("Enable Kill Switch");
    	//CheckboxMenuItem cb3 = new CheckboxMenuItem("Render in Background");

        MenuItem exitItem = new MenuItem("Exit");
        
        MenuItem openItem=new MenuItem("Open");
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	console.getFrame().setVisible(true);
            	console.getFrame().setExtendedState(JFrame.NORMAL);
            	//testFrame.getContentPane().setVisible(true);
            }
        });

        //Add components to popup menu
        popup.add(openItem);
        popup.addSeparator();
        popup.add(cb1);
        popup.add(cb2);
        popup.add(cb3);
        popup.addSeparator();
        //popup.add(exitItem);
        popup.add(aboutItem);
        
        trayIcon.setPopupMenu(popup);
        trayIcon.setToolTip(ApplicationConstants.SIRRENDER_SERVER_TITLE);

        // Determine initial status of Suspend Switch and Kill Switch if MasterServer otherwise, await the polling status update thread
        try {
        	if (myIpAddress.equals(GlobalClass.getServerMasterIpAddress()) || GlobalClass.isH2ServerMode()) 
        	{
        		suspendKill = H2.getServerSuspendSwitchStatus(myIpAddress)
        				+ "|" + H2.getServerKillSwitchStatus(myIpAddress)
        				+ "|" + H2.getServerBackgroundSwitchStatus(myIpAddress);
        	}
		} catch (NumberFormatException | SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
        //MasterMain.log.debug("suspendKill="+suspendKill);
        if (suspendKill.length() >= 1) {
	        if (suspendKill.substring(0,1).equals("1")) {
	        	cb1.setState(true);
	        } else {
	        	cb1.setState(false);
	        }
        } else {
        	cb1.setState(true);
        }
        
        if (suspendKill.length() >= 3) {
	        if (suspendKill.substring(2,3).equals("1")) {
	        	cb2.setState(true);
	        } else {
	        	cb2.setState(false);
	        }
        } else {
        	cb2.setState(false);
        }

        if (suspendKill.length() >= 5) {
	        if (suspendKill.substring(4,5).equals("1")) {
	        	cb3.setState(true);
	        } else {
	        	cb3.setState(false);
	        }
        } else {
        	cb3.setState(true);
        }
        
        //MasterMain.log.debug("before adding TrayIcon");
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            MasterMain.log.debug("TrayIcon could not be added.");
            return;
        }
        
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	console.getFrame().setVisible(true);
            	console.getFrame().setExtendedState(JFrame.NORMAL);
            	//testFrame.getContentPane().setVisible(true);
            }
        });
        
        //MasterMain.log.debug("before adding action listener");
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	// Remove requirement to display About box three times when quitting
            	// Replace with a Quit option
            	/*
                JOptionPane.showMessageDialog(null,
                        ApplicationConstants.SIRRENDER_SERVER_TITLE+"\n\n");
                // Increment the about exit counter - quit when = 3
                GlobalClass.setAboutExitCount(GlobalClass.getAboutExitCount() + 1);
                if (GlobalClass.getAboutExitCount() == 3) {
                */
                // Increment the about exit counter - quit when = 1
                GlobalClass.setAboutExitCount(GlobalClass.getAboutExitCount() + 1);
                if (GlobalClass.getAboutExitCount() == 1) {
                	// Change the status to "Off Line" if running in H2 server mode
        			try {
						H2.updateServerStatus(myName, myIpAddress, "Off Line");
					} catch (SQLException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
        	        // Checkpoint the database as it is running in WAL mode
        	        try {
        				H2.checkpointDatabase();
        			} catch (SQLException e1) {
        				// TODO Auto-generated catch block
        				e1.printStackTrace();
        			}
        	        // Exit SirRender
                    tray.remove(trayIcon);
        			System.exit(1);
                }
            }
        });
        
        cb1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                
            	int cb1Id = e.getStateChange();
            	
            	if (cb1Id == ItemEvent.SELECTED){
                    //trayIcon.setImageAutoSize(true);
                    try {
						H2.netUpdateServerSuspendSwitchStatus(myName, myIpAddress, 1);
					} catch (NumberFormatException | SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                } else {
                    //trayIcon.setImageAutoSize(false);
                    try {
						H2.netUpdateServerSuspendSwitchStatus(myName, myIpAddress, 0);
					} catch (NumberFormatException | SQLException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
                }
            }
        });
        
        cb2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                
            	int cb2Id = e.getStateChange();
            	
            	if (cb2Id == ItemEvent.SELECTED){
                    //trayIcon.setImageAutoSize(true);
                    try {
						H2.netUpdateServerKillSwitchStatus(myName, myIpAddress, 1);
					} catch (NumberFormatException | SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                } else {
                    //trayIcon.setImageAutoSize(false);
                    try {
						H2.netUpdateServerKillSwitchStatus(myName, myIpAddress, 0);
					} catch (NumberFormatException | SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
            }
        });
        
        cb3.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                
            	int cb3Id = e.getStateChange();
            	
            	if (cb3Id == ItemEvent.SELECTED){
                    //trayIcon.setImageAutoSize(true);
                    try {
						H2.netUpdateServerBackgroundSwitchStatus(myName, myIpAddress, 1);
					} catch (NumberFormatException | SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                } else {
                    //trayIcon.setImageAutoSize(false);
                    try {
						H2.netUpdateServerBackgroundSwitchStatus(myName, myIpAddress, 0);
					} catch (NumberFormatException | SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
            }
        });
        
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
    }

    public CheckboxMenuItem getCb1() {
		return cb1;
	}

	public CheckboxMenuItem getCb2() {
		return cb2;
	}

	public CheckboxMenuItem getCb3() {
		return cb3;
	}

	public static String getInitialMasterServerIpAddress() throws SQLException, SocketException, UnknownHostException {
		
		String configFileInput = null;
		String newMasterServerName = null;
		String newMasterServerIpAddress = null;
				
		// Determine if ?:\SirRender\databases\masterServerIpAddress.txt exists
		// If it exists, read the contents
		configFileInput = Utils.readMasterServerFile(ApplicationConstants.MASTER_SERVER_IP_ADDRESS_TXT_FILE);

		// If returned configFileInput is null or empty string, no masterServerIpAddress has been established
		// Access the database and determine who the MasterServer is
		if (configFileInput == null || configFileInput.isEmpty()) {
			// This method opens the database, finds a masterServer, and updates the database and global variables
			newMasterServerIpAddress = pingGetSetMasterServer();
        	return newMasterServerIpAddress;
		} else {
			// Determine if you can ping the current MasterServer
	        try {
            	String delims = "[|]+";
            	String[] tokens = configFileInput.split(delims);
            	newMasterServerName = tokens[0];
            	newMasterServerIpAddress = tokens[1];

				// Test to determine if it is responding to a ping and update status accordingly
	        	MasterMain.log.debug( "Test to determine if MasterServer " + newMasterServerName + " (" + newMasterServerIpAddress + ") is responding to a ping and update status accordingly");
				//MasterMain.log.debug("testingMasterServerIpAddress="+newMasterServerIpAddress);
				//MasterMain.log.debug("testingMasterServerName="+newMasterServerName);
				
	        	int maxPingCount = 3;
	        	int pingCount = 0;
	        	boolean polled = false;

	        	while (pingCount < maxPingCount) {
					if (Utils.clientToServerPollServerStatus(newMasterServerName, newMasterServerIpAddress)) {
						// Refresh GlobalClass variables
			        	MasterMain.log.debug("Refresh GlobalClass variables");
			        	//MasterMain.log.debug("MasterSlaveTimerThread: "+"Available!");
						GlobalClass.setServerMasterIpAddress(newMasterServerIpAddress);
						GlobalClass.setServerMasterName(newMasterServerName);
						GlobalClass.setLastPollTime(Utils.getCurrentTime());
//						// Update Master Server status - send message to MasterServer via network
//			        	H2.netSetServerStatus(newMasterServerName, newMasterServerIpAddress, "Available", false);
						polled = true;
						pingCount = maxPingCount;
					} else {
						// Sleep for 5 seconds and try again
						try {
							TimeUnit.SECONDS.sleep(5);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						pingCount++;
					}
	        	}

	        	if (!polled) {
		        	MasterMain.log.debug("MasterServer is not responding to a ping");
		        	newMasterServerIpAddress = "0.0.0.0";
					// This method opens the database, finds a MasterServer, and updates the database and global variables
					if (GlobalClass.getLastMasterServerPollTime() != null) {
						long diff = Utils.getCurrentTime().getTime() - GlobalClass.getLastMasterServerPollTime().getTime();
			        	MasterMain.log.debug("diff="+diff);
						if (diff > 60l*1000l) {
				        	MasterMain.log.debug("Connecting to database to get a new MasterServer IP address");
				        	Utils.deleteMasterServerFile(ApplicationConstants.MASTER_SERVER_IP_ADDRESS_TXT_FILE);
							newMasterServerIpAddress = pingGetSetMasterServer();
						}
					} else {
						// Initialize to force a MasterServer IP resolution
						GlobalClass.setLastMasterServerPollTime(Utils.getCurrentTime());
					}
		        	return newMasterServerIpAddress;
	        	}
				
				/*
				if (Utils.clientToServerPollServerStatus(newMasterServerName, newMasterServerIpAddress)) {
					// Refresh GlobalClass variables
		        	MasterMain.log.debug("Refresh GlobalClass variables");
		        	//MasterMain.log.debug("MasterSlaveTimerThread: "+"Available!");
					GlobalClass.setServerMasterIpAddress(newMasterServerIpAddress);
					GlobalClass.setServerMasterName(newMasterServerName);
					GlobalClass.setLastPollTime(Utils.getCurrentTime());
//					// Update Master Server status - send message to MasterServer via network
//		        	H2.netSetServerStatus(newMasterServerName, newMasterServerIpAddress, "Available", false);
				} else {
		        	MasterMain.log.debug("MasterServer is not responding to a ping");
		        	newMasterServerIpAddress = "0.0.0.0";
					// This method opens the database, finds a MasterServer, and updates the database and global variables
					if (GlobalClass.getLastMasterServerPollTime() != null) {
						long diff = Utils.getCurrentTime().getTime() - GlobalClass.getLastMasterServerPollTime().getTime();
			        	MasterMain.log.debug("diff="+diff);
						if (diff > 60l*1000l) {
				        	MasterMain.log.debug("Connecting to database to get a new MasterServer IP address");
				        	Utils.deleteMasterServerFile(ApplicationConstants.MASTER_SERVER_IP_ADDRESS_TXT_FILE);
							newMasterServerIpAddress = pingGetSetMasterServer();
						}
					} else {
						// Initialize to force a MasterServer IP resolution
						GlobalClass.setLastMasterServerPollTime(Utils.getCurrentTime());
					}
		        	return newMasterServerIpAddress;
				}
				*/				
			} catch (NumberFormatException | IOException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return newMasterServerIpAddress;
		}
	}
	
	public static String pingGetSetMasterServer() throws SQLException, SocketException, UnknownHostException {

		InetAddress IP = InetAddress.getLocalHost();
    	
		boolean noMasterServer = true;
		boolean closeDb = false;
		
		String newMasterServerName = null;
		String newMasterServerIpAddress = null;
		String myIpAddress = Utils.getLowIpAddress();
    	String myName = IP.getHostName();

		// Get a database connection
    	if (GlobalClass.getConection() == null) {
    		GlobalClass.setConection(H2.Connector());
//    		closeDb = true;
    	}
	
		while (noMasterServer) {
			// Determine if you can ping the current MasterServer
	        try {
				// Test to determine if it is responding to a ping and update status accordingly
				newMasterServerIpAddress = H2.getMasterServerIpAddress();
				newMasterServerName = H2.getMasterServerName(newMasterServerIpAddress);

		        // Determine if the poller should be running on this server
		        if (newMasterServerIpAddress.isEmpty()) {
		        	// No current MasterServer is active
		        	newMasterServerIpAddress = myIpAddress;
		        	newMasterServerName = myName;
		        	
					// Refresh GlobalClass variables
		        	MasterMain.log.debug("Refresh GlobalClass variables assigning this server as the new MasterServer");
					GlobalClass.setServerMasterIpAddress(newMasterServerIpAddress);
					GlobalClass.setServerMasterName(newMasterServerName);
					GlobalClass.setLastMasterServerPollTime(Utils.getCurrentTime());
					GlobalClass.setLastPollTime(Utils.getCurrentTime());
					
					// Update Master Server status Access database directly
		        	H2.setServerStatus(newMasterServerName, newMasterServerIpAddress, "Available");
		        	
		        	// Write to masterServerIpAddress configuration file
		        	Utils.writeMasterServerFile(ApplicationConstants.MASTER_SERVER_IP_ADDRESS_TXT_FILE, newMasterServerName, newMasterServerIpAddress);
		        	
		        	// Exit while loop
		        	noMasterServer = false;
		        } else{
					// Test to determine if the selected MasterServer it is responding to a ping and update status accordingly
		        	MasterMain.log.debug( "Test to determine if " + newMasterServerIpAddress + " is responding to a ping and update status accordingly");
					
					if (Utils.clientToServerPollServerStatus(newMasterServerName, newMasterServerIpAddress)) {
						// Refresh GlobalClass variables
			        	MasterMain.log.debug("Refresh GlobalClass variables");
			        	//MasterMain.log.debug("MasterSlaveTimerThread: "+"Available!");
						GlobalClass.setServerMasterIpAddress(newMasterServerIpAddress);
						GlobalClass.setServerMasterName(newMasterServerName);
						GlobalClass.setLastPollTime(Utils.getCurrentTime());
						
						// Update MasterServer status - Access database directly
			        	H2.setServerStatus(newMasterServerName, newMasterServerIpAddress, "Available");
			        	
			        	// Write to masterServerIpAddress configuration file
			        	Utils.writeMasterServerFile(ApplicationConstants.MASTER_SERVER_IP_ADDRESS_TXT_FILE, newMasterServerName, newMasterServerIpAddress);
			        	
			        	// Exit while loop
			        	noMasterServer = false;
					} else {
						// Disable MasterServer as currently identified in the database
						MasterMain.log.debug( "Disable selected MasterServer");
						
						// Update Master Server status Access database directly
						MasterMain.log.debug( "Update Master Server status Access database directly");
			        	H2.setServerStatus(newMasterServerName, newMasterServerIpAddress, "Off Line");
			        	
			        	// Delete the masterServerIpAddress configuration file
			        	Utils.deleteMasterServerFile(ApplicationConstants.MASTER_SERVER_IP_ADDRESS_TXT_FILE);
					}
		        }
			} catch (NumberFormatException | IOException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Checkpoint the database
		H2.checkpointDatabase();

		// Close the database connection if opened within this method
/*		
		if (closeDb) {
			GlobalClass.getConection().close();
			GlobalClass.setConection(null);
		}
*/		
			
    	return newMasterServerIpAddress;
	}
	
}