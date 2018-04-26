package gov.noaa.nws.bmh_edge.utility;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;

import gov.noaa.nws.bmh_edge.services.PlaylistService;
import gov.noaa.nws.bmh_edge.services.PlaylistService.CustomSpringEvent;

public class BmhPlaylistUtility {
	private String transmitterID;
	@Resource
	private PlaylistService service;
	
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;

	public String getTransmitterID() {
		return transmitterID;
	}


	public void setTransmitterID(String transmitterID) {
		this.transmitterID = transmitterID;
	}
	

	public PlaylistService getService() {
		return service;
	}


	public void setService(PlaylistService service) {
		this.service = service;
	}


	public DacPlaylist transmitterCheck(Exchange exchange, DacPlaylist message) {
		exchange.getOut().setHeader("trans_check", Boolean.FALSE);
		
		if (getTransmitterID().equalsIgnoreCase("ANY")) {
			exchange.getOut().setHeader("trans_check", Boolean.TRUE);
		} else if (getTransmitterID() == message.getTransmitterGroup()) {
			exchange.getOut().setHeader("trans_check", Boolean.TRUE);
		}
		
		return message;
	}
	
	public void updatePlaylist(DacPlaylist playlist) throws Exception {
		getService().add(playlist);
		
		PlaylistService.CustomSpringEvent event = getService().new CustomSpringEvent(playlist,"PLAY");
		
		applicationEventPublisher.publishEvent(event);
	}
	
	public void addMessage(DacPlaylistMessageMetadata message) {
		getService().add(message);
		
		PlaylistService.CustomSpringEvent event = getService().new CustomSpringEvent(message,"PLAY");
		
		applicationEventPublisher.publishEvent(event);
	}
}
