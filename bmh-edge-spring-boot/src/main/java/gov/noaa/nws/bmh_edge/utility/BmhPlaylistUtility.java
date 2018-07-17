package gov.noaa.nws.bmh_edge.utility;

import java.io.File;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;
import com.raytheon.uf.common.serialization.SerializationUtil;

import gov.noaa.nws.bmh_edge.services.InterruptPlaylistService;
import gov.noaa.nws.bmh_edge.services.NormalPlaylistService;

/**
 * The Class BmhPlaylistUtility.
 */
public class BmhPlaylistUtility {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(BmhPlaylistUtility.class);
	
	/** The transmitter ID. */
	private String transmitterID;
	
	/** The service. */
	@Resource
	private NormalPlaylistService service;
	
	/** The interrupt service. */
	@Resource
	private InterruptPlaylistService interruptService;

	/**
	 * Gets the transmitter ID.
	 *
	 * @return the transmitter ID
	 */
	public String getTransmitterID() {
		return transmitterID;
	}


	/**
	 * Sets the transmitter ID.
	 *
	 * @param transmitterID the new transmitter ID
	 */
	public void setTransmitterID(String transmitterID) {
		this.transmitterID = transmitterID;
	}
	

	/**
	 * Gets the service.
	 *
	 * @return the service
	 */
	public NormalPlaylistService getService() {
		return service;
	}


	/**
	 * Sets the service.
	 *
	 * @param service the new service
	 */
	public void setService(NormalPlaylistService service) {
		this.service = service;
	}


	/**
	 * Transmitter check.
	 *
	 * @param exchange the exchange
	 * @param message the message
	 * @return the dac playlist
	 */
	public DacPlaylist transmitterCheck(Exchange exchange, DacPlaylist message) {
		exchange.getOut().setHeader("trans_check", Boolean.FALSE);
		
		if (getTransmitterID().equalsIgnoreCase("ANY")) {
			exchange.getOut().setHeader("trans_check", Boolean.TRUE);
		} else if (getTransmitterID().equalsIgnoreCase(message.getTransmitterGroup())) {
			exchange.getOut().setHeader("trans_check", Boolean.TRUE);
		}
		
		return message;
	}
	
	/**
	 * Update playlist.
	 *
	 * @param playlist the playlist
	 * @throws Exception the exception
	 */
	public void updatePlaylist(DacPlaylist playlist) throws Exception {
		getService().add(playlist);
		
		startBroadcast();
	}
	
	/**
	 * Adds the message.
	 *
	 * @param message the message
	 * @throws Exception the exception
	 */
	public void addMessage(DacPlaylistMessageMetadata message) throws Exception {
		// set recognized to false which will allow TTS
		message.setRecognized(false);
		String onlyFileName = BmhPlaylistUtility.extractFileName(message.getSoundFiles().get(0));
		
		if (onlyFileName.isEmpty()) {
			throw new Exception("DacPlalistMessageMetadata missing Audio Filename");
		} else {
			message.getSoundFiles().set(0, onlyFileName);
		}
		
		getService().add(message);
		
		if (getService().getCurrent() != null) {
			startBroadcast();
		}
	}
	
	/**
	 * Adds the message.
	 *
	 * @param message the message
	 * @param fileName the file name
	 * @throws Exception the exception
	 */
	public void addMessage(byte[] message, String fileName) throws Exception {
		logger.info("Added Audio Message");
		byte[] audio = (byte[]) SerializationUtil.transformFromThrift(message);
		getService().add(audio, fileName);
	}
	
	/**
	 * Extract file name.
	 *
	 * @param filename the filename
	 * @return the string
	 */
	public static String extractFileName(String filename) {
		File file = new File (filename);
		return file.getName();
	}
	
	/**
	 * Start broadcast.
	 */
	private void startBroadcast() {
		try {
			logger.info("Play Event Received");
			if (!service.getActive().get() && (service.getCurrent() != null)) {
				logger.info("Activating Broadcast Cycle");
				service.broadcastCycle();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
