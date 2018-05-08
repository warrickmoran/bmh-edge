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

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NormalPlaylistService extends PlaylistServiceAbstract {
	private static final Logger logger = LoggerFactory.getLogger(NormalPlaylistService.class);
	private ConcurrentHashMap<Long, DacPlaylistMessageMetadata> broadcast;
	private static AtomicBoolean interrupt;
	@Resource
	private InterruptPlaylistService interruptService;
	public static Object INTERRUPT_THREAD_OBJECT;
	
	static {
		interrupt = new AtomicBoolean();
		interrupt.set(false);
	}
	

	public NormalPlaylistService() {
		super();
		broadcast = new ConcurrentHashMap<Long, DacPlaylistMessageMetadata>();
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

	public static AtomicBoolean getInterrupt() {
		return interrupt;
	}

	public InterruptPlaylistService getInterruptService() {
		return interruptService;
	}

	public void setInterruptService(InterruptPlaylistService interruptService) {
		this.interruptService = interruptService;
	}

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
		if (message.isAlertTone() || message.isWarning() || message.isWatch() ) {
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
	
	protected void interrupt(Boolean interrupt) {
		getInterrupt().set(interrupt);
	}

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

	protected Boolean isExpired(Long id) {
		if (getBroadcast().containsKey(id)) {
			return getBroadcast().get(id).getExpire().compareTo(Calendar.getInstance()) < 0;
		}
		return false;
	}
	
	protected void play(DacPlaylistMessageId id) throws Exception {
		if (getInterrupt().get()) {
			playInterrupt();
			getInterrupt().set(false);
		}
		
		if (getBroadcast().containsKey(id.getBroadcastId())) {
			if (getBroadcast().get(id.getBroadcastId()).isRecognized()) {
				logger.info(String.format("Playing Message -> %d", id.getBroadcastId()));
				logger.info(String.format("Message Content -> %s",
						getBroadcast().get(id.getBroadcastId()).getMessageText()));
				getPlayer().play(getBroadcast().get(id.getBroadcastId()).getSoundFiles().get(0));
			}
		} else {
			if (isExpired(id.getBroadcastId())) {
				logger.info(String.format("Message Expired -> %d", id.getBroadcastId()));
			} else {
				logger.error(String.format("Message Unavailable -> %d", id.getBroadcastId()));
			}
		}
	}
	
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
	
	private Boolean completeInterrupt(DacPlaylist playlist) {
		String.format("Completed Interrupt Playlist %d", playlist.getTraceId());
		interrupt(false);
		return true;
	}
}
