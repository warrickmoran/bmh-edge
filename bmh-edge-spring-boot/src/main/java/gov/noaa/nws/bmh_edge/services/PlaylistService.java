package gov.noaa.nws.bmh_edge.services;

import java.io.File;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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

import gov.noaa.nws.bmh_edge.audio.googleapi.SynthesizeText;
import gov.noaa.nws.bmh_edge.audio.mp3.MP3Player;
import gov.noaa.nws.bmh_edge.utility.GoogleSpeechUtility;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PlaylistService {
	private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);
	private MP3Player player;
	private ConcurrentHashMap<Long, DacPlaylistMessageMetadata> broadcast;
	private DacPlaylist current;
	@Resource
	GoogleSpeechUtility googleSpeech;

	private static AtomicBoolean active;

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
		broadcast = new ConcurrentHashMap<>();
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
	 * 
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
				printCurrentPlaylist();
				// check for setRecognized to determine if audio file is available
				getCurrent().getMessages().forEach((k) -> {
					try {
						expiration();
						if (getBroadcast().containsKey(k.getBroadcastId())) {
							if (getBroadcast().get(k.getBroadcastId()).isRecognized()) {
								logger.info(String.format("Playing Message -> %d", k.getBroadcastId()));
								logger.info(String.format("Message Content -> %s", getBroadcast().get(k.getBroadcastId()).getMessageText()));
								player.play(getBroadcast().get(k.getBroadcastId()).getSoundFiles().get(0));
							}
						} else {
							if (isExpired(k.getBroadcastId()) ) {
								logger.info(String.format("Message Expired -> %d", k.getBroadcastId()));
							} else {
								logger.error(String.format("Message Unavailable -> %d", k.getBroadcastId()));
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});

				// add pause between messages
				Thread.sleep(3000);
			}

			logger.info("Exiting Broadcast Cycle");
		} else {
			logger.error("Empty Broadcast Cycle");
		}
	}

	public void add(DacPlaylist playlist) throws Exception {
		if ((getCurrent() != null) && (getBroadcast() != null)) {
			for (DacPlaylistMessageId id : playlist.getMessages()) {
				if (!getCurrent().getMessages().contains(id)) {
					remove(id.getBroadcastId());
				}
			}
		}

		logger.info(String.format("Setting Current Playlist: %s", playlist.getTraceId()));

		setCurrent(playlist);
	}

	public void add(DacPlaylistMessageMetadata message) throws Exception {
		if (getBroadcast() == null) {
			setBroadcast(new ConcurrentHashMap<Long, DacPlaylistMessageMetadata>());
		}

		logger.info(String.format("Adding %d from BroadcastCycle", message.getBroadcastId()));
		if (googleSpeech != null) {
			googleSpeech.createTextToSpeechBean(message);
		} else {
			logger.error("Google Speech Not Available");
		}
		getBroadcast().put(message.getBroadcastId(), message);
	}

	private void expiration() {
		getCurrent().getMessages().forEach((k) -> {
			if (isExpired(k.getBroadcastId())) {
				logger.info(String.format("Message Expiration - > %d", k.getBroadcastId()));
				remove(k.getBroadcastId());
			}
		});
	}

	private Boolean isExpired(Long id) {
		if (getBroadcast().containsKey(id)) {
			return getBroadcast().get(id).getExpire().compareTo(Calendar.getInstance()) < 0;
		}
		return false;
	}

	private Boolean remove(Long id) {
		Boolean ret = false;

		if (getBroadcast().containsKey(id)) {
			logger.info(String.format("Removing %d from BroadcastCycle", id));
			File mp3File = new File(getBroadcast().get(id).getSoundFiles().get(0));
			if (!mp3File.delete()) {
				logger.error(String.format("Unable to Delete MP3 File for %d", id));
			}
			if (getBroadcast().remove(id) != null) {
				ret = true;
			}
		}

		return ret;
	}

	private void printCurrentPlaylist() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(DacPlaylist.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		final StringWriter w = new StringWriter();

		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		if (getCurrent() != null) {
			jaxbMarshaller.marshal(getCurrent(), w);
			logger.info(String.format("Current Playlist: %s", w.toString()));
		} else {
			logger.info("--Empty Playlist--");
		}
	}
}
