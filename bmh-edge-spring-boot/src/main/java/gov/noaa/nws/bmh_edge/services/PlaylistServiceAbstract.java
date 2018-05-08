package gov.noaa.nws.bmh_edge.services;

import java.io.File;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

import gov.noaa.nws.bmh_edge.audio.mp3.MP3Player;
import gov.noaa.nws.bmh_edge.services.events.InterruptPlaylistMessageMetadataEvent;
import gov.noaa.nws.bmh_edge.services.events.PlaylistMessageMetadataEvent;
import gov.noaa.nws.bmh_edge.utility.GoogleSpeechUtility;

public abstract class PlaylistServiceAbstract {
	private static final Logger logger = LoggerFactory.getLogger(PlaylistServiceAbstract.class);
	private MP3Player player;

	//	private ConcurrentHashMap<Long, DacPlaylistMessageMetadata> broadcast;
	private DacPlaylist current;

	@Resource
	GoogleSpeechUtility googleSpeech;

	private static AtomicBoolean active;
	
	@Async
	/**
	 * http://www.baeldung.com/spring-async
	 * 
	 * @throws Exception
	 */
	public abstract CompletableFuture<DacPlaylist> broadcastCycle();
	
	public abstract void add(DacPlaylistMessageMetadata message) throws Exception; 
	
	public abstract void add(DacPlaylist playlist) throws Exception;
	
	protected abstract Boolean remove(Long id);
	
	protected abstract Boolean isExpired(Long id);
	
	protected abstract void play(DacPlaylistMessageId id) throws Exception;
	

	public PlaylistServiceAbstract() {
		active = new AtomicBoolean();
//		broadcast = new ConcurrentHashMap<Long, DacPlaylistMessageMetadata>();
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
		PlaylistServiceAbstract.active = active;
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
	
	protected MP3Player getPlayer() {
		if (player == null) {
			player = new MP3Player();
		}
		return player;
	}

//	/**
//	 * @return the broadcast
//	 */
//	public ConcurrentHashMap<Long, DacPlaylistMessageMetadata> getBroadcast() {
//		return broadcast;
//	}
//
//	/**
//	 * @param broadcast
//	 *            the broadcast to set
//	 */
//	public void setBroadcast(ConcurrentHashMap<Long, DacPlaylistMessageMetadata> broadcast) {
//		this.broadcast = broadcast;
//	}
	
	protected void expiration() {
		getCurrent().getMessages().forEach((k) -> {
			if (isExpired(k.getBroadcastId())) {
				logger.info(String.format("Message Expiration -> %d", k.getBroadcastId()));
				remove(k.getBroadcastId());
			}
		});
	}
	
	protected void printCurrentPlaylist() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(DacPlaylist.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		final StringWriter w = new StringWriter();

		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		if (getCurrent() != null) {
			jaxbMarshaller.marshal(getCurrent(), w);
			logger.info(String.format("Current Playlist -> %s", w.toString()));
		} else {
			logger.info("--Empty Playlist--");
		}
	}
}
