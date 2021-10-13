package org.koekepan.herobrineproxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.koekepan.herobrineproxy.session.*;
import org.koekepan.herobrineproxy.sps.*;

import io.socket.client.*;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;

import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.server.ServerAdapter;
import com.github.steveice10.packetlib.event.server.SessionAddedEvent;
import com.github.steveice10.packetlib.event.server.SessionRemovedEvent;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.github.steveice10.packetlib.packet.Packet;

// the main class for the proxy
// this class creates a proxy session for every client session that is connected

public class ServerProxy implements IProxySessionConstructor{


	//private ScheduledExecutorService serverPoll = Executors.newSingleThreadScheduledExecutor();

	private String spsHost = null;
	private int spsPort = 0;
	private String serverHost = null;
	private int serverPort = 0;
	private ISPSConnection spsConnection;
	
	private Server server = null;
	private Map<String, IProxySessionNew> sessions = new HashMap<String, IProxySessionNew>();
	
	public Socket socket;
	
	public ServerProxy(final String serverHost, final int serverPort, final String spsHost, final int spsPort ) {
		this.spsHost = spsHost;
		this.spsPort = spsPort;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
			
		this.spsConnection = new SPSConnection(this.spsHost, this.spsPort, this);
		this.spsConnection.connect();
		// setup proxy server and add listener to create and store/discard proxy sessions as clients connect/disconnect
		server = new Server("127.0.0.1", 25561, HerobrineProxyProtocol.class, new TcpSessionFactory());	
		
//		server.addListener(new ServerAdapter() {
//			
//			@Override
//			public void sessionAdded(SessionAddedEvent event) {
////				Session session = event.getSession();
////				ConsoleIO.println("HerobrineServerProxy::sessionAdded => A SessionAdded event occured from <"+session.getHost()+":"+session.getPort()+"> to server <"+serverHost+":"+serverPort+">");
////				IClientSession clientSession = new ClientSession(session);
////				//IProxySessionNew proxySession = new ProxySessionV3(clientSession, serverHost, serverPort);
////				IProxySessionNew proxySession = new SPSToServerProxy(spsConnection, serverHost, serverPort);
////				sessions.put(event.getSession(), proxySession);
//			}
//
//			@Override
//			public void sessionRemoved(SessionRemovedEvent event) {
////				sessions.remove(event.getSession()).disconnect();
//			}
//		});
	}
	
	
	// returns whether the proxy server is currently listening for client connections
	public boolean isListening() {
		return server != null && server.isListening();
	}


	// initializes the proxy
	public void bind() {
		server.bind(true);
	}

	
	// closes the proxy
	public void close() {
		server.close(true);
	}

	
//	public String getProxyHost() {
//		return proxyHost;
//	}
//
//	
//	public int getProxyPort() {
//		return proxyPort;
//	}


	public String getServerHost() {
		return serverHost;
	}


	public int getServerPort() {
		return serverPort;
	}

	
	public List<IProxySessionNew> getSessions() {
		return new ArrayList<IProxySessionNew>(sessions.values());
	}	
	
	@Override
	public IProxySessionNew createProxySession(String username) {
		SPSToServerProxy newProxySession = new SPSToServerProxy(this.spsConnection, this.serverHost, this.serverPort);
		newProxySession.setUsername(username);
		sessions.put(username, newProxySession);
		return newProxySession;
	}

	@Override
	public IProxySessionNew getProxySession(String username) {
		return sessions.get(username);
	}	
}
