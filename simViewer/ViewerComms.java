package simViewer;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public class ViewerComms {
	private String serverHostname = "localhost";
	private InetAddress serverAddress = null;
	private int serverPort = 6000;
	private boolean initialized = false;
	private DatagramSocket socket;
	private String labXML, gridXML, firstReply;
	public LinkedHashMap<Integer, MousePlayer> mice = new LinkedHashMap<Integer, MousePlayer>();	
	private DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	private DocumentBuilder db;
	
	public ViewerComms() {
		try {
			db = fac.newDocumentBuilder();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private Thread receiver = new Thread("Receiver thread") {
		@Override
		public void run() {
			//super.run();
			while (true) {
				String msg = receiveMessage();				
				if (msg != null) {
					try {
						Document doc = db.parse(new ByteArrayInputStream(msg.getBytes()));
						Node root = doc.getChildNodes().item(0);
						
						if (root.getNodeName().equals("Robot")) {
							
							//System.out.println(msg);
							//System.exit(0);
							NamedNodeMap posAttrs =root.getChildNodes().item(1).getAttributes();
							int id = Integer.parseInt(root.getAttributes().getNamedItem("Id").getTextContent());
							double x = Double.parseDouble(posAttrs.getNamedItem("X").getTextContent());
							double y = Double.parseDouble(posAttrs.getNamedItem("Y").getTextContent());
							double dir = Double.parseDouble(posAttrs.getNamedItem("Dir").getTextContent());
							boolean wasCollision = false;
							boolean wasVisiting = false;
							//boolean wasFinished = false;
							MousePlayer mp;
							if (mice.containsKey(id)) {
								mp = mice.get(id);
								int time = Integer.parseInt(root.getAttributes().getNamedItem("Time").getTextContent());
								if (time < mp.getTime()) {
									return;
								}
								mp.setDirection(dir);
								wasCollision = mp.isColision();
								mp.getPosition().setLocation(x, y);
							}
							else {
								mp = new MousePlayer(new Point2D.Double(x,y));								
								mp.setDirection(dir);								
								mice.put(id, mp);
							}
							
							mp.setPlayerName(root.getAttributes().getNamedItem("Name").getTextContent());
							mp.setId(id);
							mp.setTime(Integer.parseInt(root.getAttributes().getNamedItem("Time").getTextContent()));
							mp.setScore(Integer.parseInt(root.getAttributes().getNamedItem("Score").getTextContent()));
							mp.setArrivalTime(Integer.parseInt(root.getAttributes().getNamedItem("ArrivalTime").getTextContent()));
							mp.setCollisions(Integer.parseInt(root.getAttributes().getNamedItem("Collisions").getTextContent()));
							mp.setReturningTime(Integer.parseInt(root.getAttributes().getNamedItem("ReturningTime").getTextContent()));
							mp.setColision(root.getAttributes().getNamedItem("Collision").getTextContent().equals("True")? true : false);
							mp.setVisitedMask(Integer.parseInt(root.getAttributes().getNamedItem("VisitedMask").getTextContent()));
							mp.setState(root.getAttributes().getNamedItem("State").getTextContent());	
							
//							if (mp.isColision() && !wasCollision) 
//								AudioClips.hit.play();	
//							
//							if (mp.getVisitedMask() != 0 && !wasVisiting) 
//								AudioClips.r2d2d.play();	
														
						}
					Thread.yield();
					}
					catch (Exception e) {
						e.printStackTrace();
					}			
				}			
			}
		}
	};
	

	public boolean sendToServer(String message) {
		return sendUDP(message, serverAddress, serverPort);
	}
	
	public boolean sendUDP(String message, InetAddress hostAddr, int port) {
		
		if (!initialized)
			initComms();
		
		if (!initialized)
			return false;
		
		byte[] buf = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, hostAddr, port);
		
		try {
			socket.send(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean initComms() {
		
		int pdiv = serverHostname.indexOf(":");
		if (pdiv != -1) {
			serverPort = Integer.valueOf(serverHostname.substring(pdiv+1)).intValue();
			serverHostname = serverHostname.substring(0,pdiv);
		}
		
		try {
            serverAddress = InetAddress.getByName(serverHostname);
            socket = new DatagramSocket();
            initialized = true;
		}
		catch(Exception e) {
			e.printStackTrace();		
		}
				
		return initialized;
	}
	
	private byte[] bufread = new byte[4096];
    
	public String receiveMessage() {
	    DatagramPacket packet = new DatagramPacket(bufread, bufread.length);
        
        try {
        	socket.setSoTimeout(2000);
        	socket.setReuseAddress(true);
        	socket.receive(packet);
        	
        	if (serverPort == 6000) {
        		serverPort = packet.getPort();        		
        	}
        	
        	return new String(packet.getData(), 0, packet.getLength()-1);
        }
        catch(Exception e) {
        	//e.printStackTrace();
        	return null;
        }        
	}
	
	public boolean isConnected() {
		return receiver.isAlive();
	}
	
	public void disconnect() {
		if (receiver != null)
			receiver.interrupt();
	}
	
	public void connect(String hostname, int port) {
		this.serverHostname = hostname;
		serverPort = port;
		initComms();

		
		sendToServer("<View/>\n");

		// I should parse this...
		firstReply = receiveMessage();

		
		// Send Lab Request
		sendToServer("<LabReq/>\n");
		labXML = receiveMessage();

		// Send Grid Request
		sendToServer("<GridReq/>");
		gridXML = receiveMessage();		

		
		if (firstReply == null || gridXML == null || labXML == null)
			return;
		
		startReceiving();
	}
	
	public void startReceiving() {
		
		receiver.start();
	}
	
	public static void main(String[] args) throws Exception {
		ViewerComms comms = new ViewerComms();		
		comms.connect("localhost", 6000);
		
		comms.sendToServer("<Start/>");		
		
	}

	public String getLabXML() {
		return labXML;
	}

	public String getGridXML() {
		return gridXML;
	}

	public int getServerPort() {
		return serverPort;
	}
}
