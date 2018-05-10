package gov.noaa.nws.bmh_edge.services;

import java.io.File;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.sound.sampled.AudioSystem;
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

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.raytheon.uf.common.bmh.dac.tones.StaticTones;
import com.raytheon.uf.common.bmh.dac.tones.TonesGenerator;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageId;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

import gov.noaa.nws.bmh_edge.audio.mp3.MP3Player;
import gov.noaa.nws.bmh_edge.services.events.InterruptPlaylistMessageMetadataEvent;
import gov.noaa.nws.bmh_edge.services.events.PlaylistMessageMetadataEvent;
import gov.noaa.nws.bmh_edge.utility.GoogleSpeechUtility;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class InterruptPlaylistService extends PlaylistServiceAbstract {
	private static final Logger logger = LoggerFactory.getLogger(InterruptPlaylistService.class);

	DacPlaylistMessageMetadata broadcast;

	public InterruptPlaylistService() {
		super();
	}

	public DacPlaylistMessageMetadata getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(DacPlaylistMessageMetadata broadcast) {
		this.broadcast = broadcast;
	}

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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// check for setRecognized to determine if audio file is available
		getCurrent().getMessages().forEach((k) -> {
			try {
				logger.info(String.format("Playing Interrupt Broadcast -> %s", k.getTraceId()));
				play(k);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		logger.info("Exiting Broadcast Cycle");
		return CompletableFuture.completedFuture(getCurrent());
	}

	public void add(DacPlaylist playlist) throws Exception {
		logger.info(String.format("Setting Interrupt Playlist -> %s", playlist.getTraceId()));

		setCurrent(playlist);
	}

	public void add(DacPlaylistMessageMetadata message) throws Exception {
		logger.info(String.format("Adding %d from BroadcastCycle", message.getBroadcastId()));

		setBroadcast(message);
		if (googleSpeech != null) {
			googleSpeech.createTextToSpeechBean(message);
		} else {
			logger.error("Google Speech Not Available");
		}
	}

	@Override
	protected Boolean remove(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Boolean isExpired(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void play(DacPlaylistMessageId id) throws Exception {
		if (getBroadcast().getBroadcastId() == id.getBroadcastId()) {
			if (getBroadcast().isRecognized()) {
				logger.info(String.format("Playing Message -> %d", id.getBroadcastId()));
				logger.info(String.format("Message Content -> %s", getBroadcast().getMessageText()));

				//if (getBroadcast().isSAMETones()) {
				//	getPlayer().play(TonesGenerator.getSAMEAlertTones(sameHeader, includeAlertTone, includeSilence, samePadding));
				//} else 
				if(getBroadcast().isAlertTone()) {
					getPlayer().play(TonesGenerator.getOnlyAlertTones());
				}
				getPlayer().play(getBroadcast().getSoundFiles().get(0));
				//ByteBufferBackedInputStream end = new ByteBufferBackedInputStream(ByteBuffer.wrap(TonesGenerator.getEndOfMessageTones(sameEOMPadding)))
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
