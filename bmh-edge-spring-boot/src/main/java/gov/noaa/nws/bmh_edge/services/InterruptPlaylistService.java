package gov.noaa.nws.bmh_edge.services;

import java.io.File;
import java.io.StringWriter;
import java.util.Calendar;
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

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class InterruptPlaylistService implements ApplicationListener<InterruptPlaylistMessageMetadataEvent> {
	private static final Logger logger = LoggerFactory.getLogger(InterruptPlaylistService.class);
	private MP3Player player;
	private ConcurrentHashMap<Long, DacPlaylistMessageMetadata> broadcast;
	private DacPlaylist current;
	private DacPlaylist interrupt;

	@Resource
	GoogleSpeechUtility googleSpeech;

	private static AtomicBoolean active;

	public InterruptPlaylistService() {
		active = new AtomicBoolean();
		broadcast = new ConcurrentHashMap<Long, DacPlaylistMessageMetadata>();
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
		InterruptPlaylistService.active = active;
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

	public DacPlaylist getInterrupt() {
		return interrupt;
	}

	public void setInterrupt(DacPlaylist interrupt) {
		this.interrupt = interrupt;
	}

	@Async
	/**
	 * http://www.baeldung.com/spring-async
	 * 
	 * @throws Exception
	 */
	public void broadcastCycle() throws Exception {
		if (!getBroadcast().isEmpty()) {
			getActive().set(true);

			logger.info("Starting Broadcast Cycle");

			while (getActive().get()) {
				printCurrentPlaylist();
				// check for setRecognized to determine if audio file is available                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
				getCurrent().getMessages().forEach((k) -> {
					try {
						expiration();
						play(k);

						if (getInterrupt() != null) {
							logger.info(
									String.format("Playing Interrupt Broadcast -> %s", getInterrupt().getTraceId()));
							setInterrupt(null);
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
		if (!playlist.isInterrupt()) {
			if ((getCurrent() != null) && (getBroadcast() != null)) {
				for (DacPlaylistMessageId id : playlist.getMessages()) {
					if (!getCurrent().getMessages().contains(id)) {
						remove(id.getBroadcastId());
					}
				}
			}

			logger.info(String.format("Setting Current Playlist -> %s", playlist.getTraceId()));

			setCurrent(playlist);
		} else {
			logger.info(String.format("Receive Interrupt Playlist -> %s", playlist.getTraceId()));
			setInterrupt(playlist);
		}
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
				logger.info(String.format("Message Expiration -> %d", k.getBroadcastId()));
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

	private void play(DacPlaylistMessageId id) throws Exception {
		if (player == null) {
			player = new MP3Player();
		}

		if (getBroadcast().containsKey(id.getBroadcastId())) {
			if (getBroadcast().get(id.getBroadcastId()).isRecognized()) {
				logger.info(String.format("Playing Message -> %d", id.getBroadcastId()));
				logger.info(String.format("Message Content -> %s",
						getBroadcast().get(id.getBroadcastId()).getMessageText()));
				player.play(getBroadcast().get(id.getBroadcastId()).getSoundFiles().get(0));
			}
		} else {
			if (isExpired(id.getBroadcastId())) {
				logger.info(String.format("Message Expired -> %d", id.getBroadcastId()));
			} else {
				logger.error(String.format("Message Unavailable -> %d", id.getBroadcastId()));
			}
		}
	}

	private void printCurrentPlaylist() throws JAXBException {
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

	@Override
	public void onApplicationEvent(InterruptPlaylistMessageMetadataEvent event) {
		DacPlaylistMessageMetadata message = event.getMessage();

		logger.info(String.format("Receive Message with Interrupt -> %d", message.getBroadcastId()));

		if (getInterrupt() != null) {
			Stream<DacPlaylistMessageId> interruptMsgs = getInterrupt().getMessages().stream()
					.filter(p -> p.getBroadcastId() == message.getBroadcastId());
			if (interruptMsgs.count() > 0) {
				logger.info(String.format("Message -> %d is for Interrupt Playlist -> %s", message.getBroadcastId(),
						getInterrupt().getTraceId()));

			}
		}
	}
}
