package gov.noaa.nws.bmh_edge.services;

import java.util.concurrent.CompletableFuture;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import com.raytheon.uf.common.bmh.dac.tones.TonesGenerator;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

/**
 * The Class InterruptPlaylistService.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class InterruptPlaylistService extends PlaylistServiceAbstract {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(InterruptPlaylistService.class);

	/** The broadcast. */
	DacPlaylistMessageMetadata broadcast;

	/**
	 * Instantiates a new interrupt playlist service.
	 */
	public InterruptPlaylistService() {
		super();
	}

	/**
	 * Gets the broadcast.
	 *
	 * @return the broadcast
	 */
	public DacPlaylistMessageMetadata getBroadcast() {
		return broadcast;
	}

	/**
	 * Sets the broadcast.
	 *
	 * @param broadcast the new broadcast
	 */
	public void setBroadcast(DacPlaylistMessageMetadata broadcast) {
		this.broadcast = broadcast;
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

		getActive().set(true);

		logger.info("Starting Interrupt Broadcast Cycle");

		try {
			printCurrentPlaylist();
		} catch (JAXBException e1) {
			logger.error(e1.getMessage());
		}
		// check for setRecognized to determine if audio file is available
		getCurrent().getMessages().forEach((playList) -> {
			try {
				logger.info(String.format("Playing Interrupt Broadcast -> %s", playList.getTraceId()));
				play(playList);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		});

		logger.info("Exiting Broadcast Cycle");
		return CompletableFuture.completedFuture(getCurrent());
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#add(com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist)
	 */
	public void add(DacPlaylist playlist) throws Exception {
		logger.info(String.format("Setting Interrupt Playlist -> %s", playlist.getTraceId()));

		setCurrent(playlist);
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#add(com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata)
	 */
	public void add(DacPlaylistMessageMetadata message) throws Exception {
		logger.info(String.format("Adding %d from BroadcastCycle", message.getBroadcastId()));

		setBroadcast(message);
		if (googleSpeech != null) {
			googleSpeech.createTextToSpeechBean(message);
		} else {
			logger.error("Google Speech Not Available");
		}
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#remove(java.lang.Long)
	 */
	@Override
	protected Boolean remove(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#isExpired(java.lang.Long)
	 */
	@Override
	protected Boolean isExpired(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.noaa.nws.bmh_edge.services.PlaylistServiceAbstract#play(com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId)
	 */
	@Override
	protected void play(DacPlaylistMessageId id) throws Exception {
		if (getBroadcast().getBroadcastId() == id.getBroadcastId()) {
			if (getBroadcast().isRecognized()) {
				logger.info(String.format("Playing Message -> %d", id.getBroadcastId()));
				logger.info(String.format("Message Content -> %s", getBroadcast().getMessageText()));
				
				if(getBroadcast().isAlertTone()) {
					logger.info(String.format("Playing AlertTone -> %s", getBroadcast().getMessageText()));
					getPlayer().play(TonesGenerator.getOnlyAlertTones().getAlertTones());
				}
				
				if (getBroadcast().isSAMETones()) {
					logger.info(String.format("Playing SameTone -> %s", getBroadcast().getMessageText()));
					getPlayer().play(TonesGenerator.getSAMEAlertTones(getBroadcast().getSAMEtone(), getBroadcast().isAlertTone(), true, 3).getSameTones());
					
				}
				
				// play mp3
				getPlayer().play(getBroadcast().getSoundFiles().get(0));
				
				if(getBroadcast().isSAMETones()) {
					logger.info(String.format("Playing EndTones -> %s", getBroadcast().getMessageText()));
					getPlayer().play(TonesGenerator.getEndOfMessageTones(3).array());
					
				}
			}
		} else {
			if (isExpired(id.getBroadcastId())) {
				logger.info(String.format("Message Expired -> %d", id.getBroadcastId()));
			} else {
				logger.error(String.format("Message Unavailable -> %d", id.getBroadcastId()));
			}
		}
	}
}
