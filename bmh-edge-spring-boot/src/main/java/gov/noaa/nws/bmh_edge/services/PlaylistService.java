package gov.noaa.nws.bmh_edge.services;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

import gov.noaa.nws.bmh_edge.audio.mp3.MP3Player;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PlaylistService {
	private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);
	private MP3Player player;
	private ConcurrentHashMap<Long, DacPlaylistMessageMetadata> broadcast;
	private DacPlaylist current;
	private AtomicBoolean active;
	
	public class CustomSpringEvent extends ApplicationEvent {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String message;
	 
	    public CustomSpringEvent(Object source, String message) {
	        super(source);
	        this.message = message;
	    }
	    
	    public String getMessage() {
	        return message;
	    }
	}

	public PlaylistService() {
		active = new AtomicBoolean();
	}

	/**
	 * @return the active
	 */
	public AtomicBoolean getActive() {
		return active;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(AtomicBoolean active) {
		this.active = active;
	}

	/**
	 * @return the current
	 */
	public DacPlaylist getCurrent() {
		return current;
	}

	/**
	 * @param current
	 *            the current to set
	 */
	public void setCurrent(DacPlaylist current) {
		this.current = current;
	}

	/**
	 * @return the broadcast
	 */
	public ConcurrentHashMap<Long, DacPlaylistMessageMetadata> getBroadcast() {
		return broadcast;
	}

	/**
	 * @param broadcast
	 *            the broadcast to set
	 */
	public void setBroadcast(ConcurrentHashMap<Long, DacPlaylistMessageMetadata> broadcast) {
		this.broadcast = broadcast;
	}

	@Async
	/**
	 * http://www.baeldung.com/spring-async
	 * @throws Exception
	 */
	public void play() throws Exception {
		if (!getBroadcast().isEmpty()) {
			if (player == null) {
				player = new MP3Player();
			}

			getActive().set(true);
			
			logger.info("Starting Broadcast Cycle");

			while (getActive().get()) {
				getBroadcast()
						.forEach((k, v) -> logger.info(String.format("Playing Message -> %d", v.getBroadcastId())));
				Thread.sleep(10000);
			}
			
			logger.info("Exiting Broadcast Cycle");
		} else {
			throw new Exception("Empty Playlist");
		}
	}

	public void add(DacPlaylist playlist) throws Exception {
		if (getCurrent() != null) {
			for (DacPlaylistMessageId id : playlist.getMessages()) {
				if (!getCurrent().getMessages().contains(id)) {
					getBroadcast().remove(id.getBroadcastId());
				}
			}
		}
		
		logger.info("Updating Playlist");
		
		setCurrent(playlist);
	}

	public void add(DacPlaylistMessageMetadata message) {
		if (getBroadcast() == null) {
			setBroadcast(new ConcurrentHashMap<Long, DacPlaylistMessageMetadata>());
		}

		getBroadcast().put(message.getBroadcastId(), message);
	}
}
