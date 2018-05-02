package gov.noaa.nws.bmh_edge.utility;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;
import gov.noaa.nws.bmh_edge.services.NormalPlaylistService;
import gov.noaa.nws.bmh_edge.services.events.PlayListIngestEvent;

public class BmhPlaylistUtility {
	private String transmitterID;
	@Resource
	private NormalPlaylistService service;
	
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;

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
		
		PlayListIngestEvent event = new PlayListIngestEvent(this,"PLAY");
		
		applicationEventPublisher.publishEvent(event);
	}
	
	public void addMessage(DacPlaylistMessageMetadata message) throws Exception {
		getService().add(message);
		
		if (getService().getCurrent() != null) {
		
			PlayListIngestEvent event = new PlayListIngestEvent(this,"PLAY");
		
			applicationEventPublisher.publishEvent(event);
		}
	}
}
