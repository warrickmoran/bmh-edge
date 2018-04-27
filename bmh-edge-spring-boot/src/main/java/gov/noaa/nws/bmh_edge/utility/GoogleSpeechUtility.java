package gov.noaa.nws.bmh_edge.utility;

import java.io.File;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

import gov.noaa.nws.bmh_edge.audio.googleapi.SynthesizeText;
import gov.noaa.nws.bmh_edge.services.PlaylistService;

public class GoogleSpeechUtility {
	private static final Logger logger = LoggerFactory.getLogger(GoogleSpeechUtility.class);
	private SynthesizeText synthesizeText;
//	@Resource
//	private PlaylistService service;
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
//		if ((message != null) && (service != null)) {
		if ((message != null)) {
			// block duplicate processing of messages... this occurs during playlist
			// replacement
//			if (!service.getBroadcast().contains(message.getBroadcastId())) {
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
//			} else {
//				logger.info(String.format("Message %s exists within Broadcast Cycle", message.getBroadcastId()));
//			}
		} else {
			logger.error(String.format("Playlist Metadata || Playlist Service == NULL)"));
			//throw new Exception("Empty BMH Message");
		}
		return message;
	}
}
