package gov.noaa.nws.bmh_edge.services;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class NormalPlaylistService.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NormalPlaylistService extends PlaylistServiceAbstract {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(NormalPlaylistService.class);
	
	/** The interrupt. */
	private static AtomicBoolean interrupt;
	
	/** The broadcast. */
	private ConcurrentHashMap<Long, DacPlaylistMessageMetadata> broadcast;
	
	/** The interrupt service. */
	@Resource
	private InterruptPlaylistService interruptService;


	static {
		interrupt = new AtomicBoolean();
		interrupt.set(false);
	}

	/**
	 * Instantiates a new normal playlist service.
	 */
	public NormalPlaylistService() {
		super();
		broadcast = new ConcurrentHashMap<Long, DacPlaylistMessageMetadata>();
	}

	/**
	 * Gets the broadcast.
	 *
	 * @return the broadcast
	 */
	public ConcurrentHashMap<Long, DacPlaylistMessageMetadata> getBroadcast() {
		return broadcast;
	}

	/**
	 * Sets the broadcast.
	 *
	 * @param broadcast            the broadcast to set
	 */
	public void setBroadcast(ConcurrentHashMap<Long, DacPlaylistMessageMetadata> broadcast) {
		this.broadcast = broadcast;
	}

	/**
	 * Gets the interrupt.
	 *
	 * @return the interrupt
	 */
	public static AtomicBoolean getInterrupt() {
		return interrupt;
	}

	/**
	 * Gets the interrupt service.
	 *
	 * @return the interrupt service
	 */
	public InterruptPlaylistService getInterruptService() {
		return interruptService;
	}

	/**
	 * Sets the interrupt service.
	 *
	 * @param interruptService the new interrupt service
	 */
	public void setInterruptService(InterruptPlaylistService interruptService) {
		this.interruptService = interruptService;
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#broadcastCycle()
	 */
	@Async
	/**
	 * http://www.baeldung.com/spring-async
	 * 
	 * @throws Exception
	 */
	public CompletableFuture<DacPlaylist> broadcastCycle() {
		if (!getBroadcast().isEmpty()) {
			getActive().set(true);

			logger.info("Starting Broadcast Cycle");

			while (getActive().get()) {
				try {
					printCurrentPlaylist();
				} catch (JAXBException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// check for setRecognized to determine if audio file is available
				getCurrent().getMessages().forEach((k) -> {
					try {
						expiration();
						play(k);

						// add pause between messages
						Thread.sleep(3000);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			}

			logger.info("Exiting Broadcast Cycle");
		} else {
			logger.error("Empty Broadcast Cycle");
		}

		return CompletableFuture.completedFuture(getCurrent());
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#add(com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist)
	 */
	public void add(DacPlaylist playlist) throws Exception {
		if ((getCurrent() != null) && (getBroadcast() != null)) {
			for (DacPlaylistMessageId id : playlist.getMessages()) {
				if (!getCurrent().getMessages().contains(id)) {
					remove(id.getBroadcastId());
				}
			}
		}

		logger.info(String.format("Setting Current Playlist -> %s", playlist.getTraceId()));
		if (!playlist.isInterrupt()) {
			setCurrent(playlist);
		} else {
			// set interrupt play list for possible play
			getInterruptService().add(playlist);
		}
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#add(com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata)
	 */
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

		// change to interrupt state for possible interrupt message
		if (message.isAlertTone() || message.isWarning() || message.isWatch()) {
			getInterruptService().getCurrent().getMessages().forEach(k -> {
				if (k.getBroadcastId() == message.getBroadcastId()) {
					// add the interrupt service to possible play
					try {
						getInterruptService().add(message);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					interrupt(true);
				}
			});
		}
	}

	/**
	 * Interrupt.
	 *
	 * @param interrupt the interrupt
	 */
	protected void interrupt(Boolean interrupt) {
		getInterrupt().set(interrupt);
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#remove(java.lang.Long)
	 */
	protected Boolean remove(Long id) {
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

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#isExpired(java.lang.Long)
	 */
	protected Boolean isExpired(Long id) {
		if (getBroadcast().containsKey(id)) {
			return getBroadcast().get(id).getExpire().compareTo(Calendar.getInstance()) < 0;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#play(com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId)
	 */
	protected void play(DacPlaylistMessageId id) throws Exception {
		if (getInterrupt().get()) {
			playInterrupt();
			getInterrupt().set(false);
		}

		if (getBroadcast().containsKey(id.getBroadcastId()) && getBroadcast().get(id.getBroadcastId()).isRecognized()) {
			DacPlaylistMessageMetadata message = getBroadcast().get(id.getBroadcastId());
			
			logger.info(String.format("Playing Message -> %d", id.getBroadcastId()));
			logger.info(
					String.format("Message Content -> %s", message.getMessageText()));
			getPlayer().play(message.getSoundFiles().get(0));

		} else {
			if (isExpired(id.getBroadcastId())) {
				logger.info(String.format("Message Expired -> %d", id.getBroadcastId()));
			} else {
				logger.error(String.format("Message Unavailable -> %d", id.getBroadcastId()));
			}
		}
	}

	/**
	 * Play interrupt.
	 */
	protected void playInterrupt() {
		CompletableFuture<DacPlaylist> future = getInterruptService().broadcastCycle();
		future.thenApply(s -> completeInterrupt(s));
		try {
			// wait for interrupt completion
			logger.info(String.format("Waiting for completion of interrupt..."));
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Complete interrupt.
	 *
	 * @param playlist the playlist
	 * @return the boolean
	 */
	private Boolean completeInterrupt(DacPlaylist playlist) {
		String.format("Completed Interrupt Playlist %d", playlist.getTraceId());
		interrupt(false);
		return true;
	}
}
