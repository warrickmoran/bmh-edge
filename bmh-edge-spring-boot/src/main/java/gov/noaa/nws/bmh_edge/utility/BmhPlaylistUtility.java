package gov.noaa.nws.bmh_edge.utility;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

import gov.noaa.nws.bmh_edge.services.InterruptPlaylistService;
import gov.noaa.nws.bmh_edge.services.NormalPlaylistService;
import gov.noaa.nws.bmh_edge.services.events.PlayListIngestEvent;

public class BmhPlaylistUtility {
	private static final Logger logger = LoggerFactory.getLogger(BmhPlaylistUtility.class);
	private String transmitterID;
	@Resource
	private NormalPlaylistService service;
	@Resource
	private InterruptPlaylistService interruptService;

	public String getTransmitterID() {
		return transmitterID;
	}


	public void setTransmitterID(String transmitterID) {
		this.transmitterID = transmitterID;
	}
	

	public NormalPlaylistService getService() {
		return service;
	}


	public void setService(NormalPlaylistService service) {
		this.service = service;
	}


	public DacPlaylist transmitterCheck(Exchange exchange, DacPlaylist message) {
		exchange.getOut().setHeader("trans_check", Boolean.FALSE);
		
		if (getTransmitterID().equalsIgnoreCase("ANY")) {
			exchange.getOut().setHeader("trans_check", Boolean.TRUE);
		} else if (getTransmitterID().equalsIgnoreCase(message.getTransmitterGroup())) {
			exchange.getOut().setHeader("trans_check", Boolean.TRUE);
		}
		
		return message;
	}
	
	public void updatePlaylist(DacPlaylist playlist) throws Exception {
		getService().add(playlist);
		
		startBroadcast();
	}
	
	public void addMessage(DacPlaylistMessageMetadata message) throws Exception {
		// set recognized to false which will allow TTS
		message.setRecognized(false);
		
		getService().add(message);
		
		if (getService().getCurrent() != null) {
			startBroadcast();
		}
	}
	
	private void startBroadcast() {
		try {
			logger.info("Play Event Received");
			if (!service.getActive().get() && (service.getCurrent() != null)) {
				logger.info("Activating Broadcast Cycle");
				service.broadcastCycle();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
