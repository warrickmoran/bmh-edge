package gov.noaa.nws.bmh_edge.services;

import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

import gov.noaa.nws.bmh_edge.audio.mp3.AudioPlayer;
import gov.noaa.nws.bmh_edge.utility.GoogleSpeechUtility;

/**
 * The Class PlaylistServiceAbstract.
 */
public abstract class PlaylistServiceAbstract {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(PlaylistServiceAbstract.class);
	
	/** The player. */
	@Resource
	private AudioPlayer player;
	
	/** The current. */
	private DacPlaylist current;

	/** The google speech. */
	@Resource
	protected GoogleSpeechUtility googleSpeech;

	/** The active. */
	private static AtomicBoolean active;
	
	/**
	 * Broadcast cycle.
	 *
	 * @return the completable future
	 */
	@Async
	/**
	 * http://www.baeldung.com/spring-async
	 * 
	 * @throws Exception
	 */
	public abstract CompletableFuture<DacPlaylist> broadcastCycle();
	
	/**
	 * Adds the.
	 *
	 * @param message the message
	 * @throws Exception the exception
	 */
	public abstract void add(DacPlaylistMessageMetadata message) throws Exception; 
	
	/**
	 * Adds the.
	 *
	 * @param playlist the playlist
	 * @throws Exception the exception
	 */
	public abstract void add(DacPlaylist playlist) throws Exception;
	
	/**
	 * Removes the.
	 *
	 * @param id the id
	 * @return the boolean
	 */
	protected abstract Boolean remove(Long id);
	
	/**
	 * Checks if is expired.
	 *
	 * @param id the id
	 * @return the boolean
	 */
	protected abstract Boolean isExpired(Long id);
	
	/**
	 * Play.
	 *
	 * @param id the id
	 * @throws Exception the exception
	 */
	protected abstract void play(DacPlaylistMessageId id) throws Exception;
	
	/**
	 * Instantiates a new playlist service abstract.
	 */
	public PlaylistServiceAbstract() {
		active = new AtomicBoolean();
	}

	/**
	 * Gets the active.
	 *
	 * @return the active
	 */
	public AtomicBoolean getActive() {
		return active;
	}

	/**
	 * Sets the active.
	 *
	 * @param active            the active to set
	 */
	public void setActive(AtomicBoolean active) {
		PlaylistServiceAbstract.active = active;
	}

	/**
	 * Gets the current.
	 *
	 * @return the current
	 */
	public DacPlaylist getCurrent() {
		return current;
	}

	/**
	 * Sets the current.
	 *
	 * @param current            the current to set
	 */
	public void setCurrent(DacPlaylist current) {
		this.current = current;
	}
	
	/**
	 * Gets the player.
	 *
	 * @return the player
	 */
	protected AudioPlayer getPlayer() {
		if (player == null) {
			player = new AudioPlayer();
		}
		return player;
	}
	
	protected GoogleSpeechUtility getGoogleSpeech() {
		return googleSpeech;
	}

	protected void setGoogleSpeech(GoogleSpeechUtility googleSpeech) {
		this.googleSpeech = googleSpeech;
	}

	/**
	 * Expiration.
	 */
	protected void expiration() {
		getCurrent().getMessages().forEach((playList) -> {
			if (isExpired(playList.getBroadcastId())) {
				logger.info(String.format("Message Expiration -> %d", playList.getBroadcastId()));
				remove(playList.getBroadcastId());
			}
		});
	}
	
	/**
	 * Prints the current playlist.
	 *
	 * @throws JAXBException the JAXB exception
	 */
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
