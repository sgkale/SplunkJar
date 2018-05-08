package com.scb.logging.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import com.splunk.HttpService;
import com.splunk.SSLSecurityProtocol;

/**
 * Log4j Appender for sending events to Splunk via REST
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class SplunkRestAppender extends AppenderSkeleton {

	public static final String STREAM = "stream";
	public static final String SIMPLE = "simple";

	// connection settings
	private String user ;
	private String pass;
	private String host ;
	private int port ;
	private String delivery = STREAM; // stream or simple
	
	Properties p;

	// event meta data
	private String metaSource = "";
	private String metaSourcetype = "";
	private String metaIndex = "";
	private String metaHostRegex = "";
	private String metaHost = "";

	// queuing settings
	private String maxQueueSize;
	private boolean dropEventsOnQueueFull;

	private SplunkRestInput sri;
	private RestEventData red = new RestEventData();

	/**
	 * Constructor
	 */
	public SplunkRestAppender() {
		FileReader reader;
		InputStream input = null;
		
		p=new Properties(); 
		try {
			String filename = "login.properties";
    		input = SplunkRestAppender.class.getClassLoader().getResourceAsStream(filename);
    		if(input==null){
    	            System.out.println("Sorry, unable to find " + filename);
    		    return;
    		}
    		p.load(input);
    		user=p.getProperty("user");
    		pass=p.getProperty("pass");
    		host=p.getProperty("host");
    		port=Integer.parseInt(p.getProperty("port"));
    		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	public SplunkRestAppender(Layout layout) {

		this.layout = layout;
	}

	/**
	 * Log the message
	 */
	@Override
	protected void append(LoggingEvent event) {
		//System.out.println(" "+user+" "+pass+" "+host+" "+port+" "+red );
		try {
			if (sri == null) {
				HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
				System.out.println(" "+user+" "+pass+" "+host+" "+port+" "+red );
				sri = new SplunkRestInput(user, pass, host, port, red,delivery.equals(STREAM) ? true : false);
				sri.setMaxQueueSize(maxQueueSize);
				sri.setDropEventsOnQueueFull(dropEventsOnQueueFull);
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorHandler
			.error("Couldn't establish REST service for SplunkRestAppender named \""
					+ this.name + "\".");
			return;
		}

		String formatted = layout.format(event);
		if (delivery.equals(STREAM))
			sri.streamEvent(formatted);
		else if (delivery.equals(SIMPLE))
			sri.sendEvent(formatted);
		else {
			errorHandler
			.error("Unsupported delivery setting for SplunkRestAppender named \""
					+ this.name + "\".");
			return;
		}

	}

	/**
	 * Clean up resources
	 */
	synchronized public void close() {


		closed = true;
		if (sri != null) {
			try {
				sri.closeStream();
				sri = null;
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				sri = null;
			}
		}

	}

	public boolean requiresLayout() {
		return true;
	}

	public String getUser() {
		return user;
	}



	public String getPass() {
		return pass;
	}



	public String getHost() {
		return host;
	}



	public int getPort() {
		return port;
	}



	public String getDelivery() {
		return delivery;
	}

	public void setDelivery(String delivery) {
		this.delivery = delivery;
	}

	public String getMetaSource() {
		return metaSource;
	}

	public void setMetaSource(String metaSource) {
		this.metaSource = metaSource;
		red.setSource(metaSource);
	}

	public String getMetaSourcetype() {
		return metaSourcetype;
	}

	public void setMetaSourcetype(String metaSourcetype) {
		this.metaSourcetype = metaSourcetype;
		red.setSourcetype(metaSourcetype);
	}

	public String getMetaIndex() {
		return metaIndex;
	}

	public void setMetaIndex(String metaIndex) {
		this.metaIndex = metaIndex;
		red.setIndex(metaIndex);
	}

	public String getMetaHostRegex() {
		return metaHostRegex;
	}

	public void setMetaHostRegex(String metaHostRegex) {
		this.metaHostRegex = metaHostRegex;
		red.setHostRegex(metaHostRegex);
	}

	public String getMetaHost() {
		return metaHost;
	}

	public void setMetaHost(String metaHost) {
		this.metaHost = metaHost;
		red.setHost(metaHost);
	}

	public String getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(String maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public boolean isDropEventsOnQueueFull() {
		return dropEventsOnQueueFull;
	}

	public void setDropEventsOnQueueFull(boolean dropEventsOnQueueFull) {
		this.dropEventsOnQueueFull = dropEventsOnQueueFull;
	}

}