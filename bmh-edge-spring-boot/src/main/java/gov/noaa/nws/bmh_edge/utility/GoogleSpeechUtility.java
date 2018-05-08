package gov.noaa.nws.bmh_edge.utility;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

import gov.noaa.nws.bmh_edge.audio.googleapi.SynthesizeText;
import gov.noaa.nws.bmh_edge.services.events.InterruptPlaylistMessageMetadataEvent;

public class GoogleSpeechUtility {
	private static final Logger logger = LoggerFactory.getLogger(GoogleSpeechUtility.class);
	private SynthesizeText synthesizeText;
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	private String audioOut;

	public SynthesizeText getSynthesizeText() {
		return synthesizeText;
	}

	public void setSynthesizeText(SynthesizeText synthesizeText) {
		this.synthesizeText = synthesizeText;
	}

	/**
	 * @return the audioOut
	 */
	public String getAudioOut() {
		return audioOut;
	}

	/**
	 * @param audioOut
	 *            the audioOut to set
	 */
	public void setAudioOut(String audioOut) {
		this.audioOut = audioOut;
	}

	public DacPlaylistMessageMetadata createTextToSpeechBean(DacPlaylistMessageMetadata message) throws Exception {
		if ((message != null) && !message.isRecognized()) {
				message.getSoundFiles().set(0,
						String.format("%s/%s.mp3", getAudioOut(), message.getSoundFiles().get(0)));
				// skip if MP3 already exists
				if (!(new File(message.getSoundFiles().get(0)).exists())) {
					if (getSynthesizeText().synthesizeText(message.getMessageText(), message.getSoundFiles().get(0))) {
						message.setRecognized(true);
					} else {
						message.setRecognized(false);
						throw new Exception("Unable to Create BMH MP3");
					}
				} else {
					message.setRecognized(true);
				}
		} else {
			logger.error(String.format("Playlist Metadata || Playlist Service == NULL || MP3 Already Created"));
		}
		return message;
	}
}
