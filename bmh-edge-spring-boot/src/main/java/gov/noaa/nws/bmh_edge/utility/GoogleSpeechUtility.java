package gov.noaa.nws.bmh_edge.utility;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import gov.noaa.nws.bmh_edge.audio.googleapi.SynthesizeText;

public class GoogleSpeechUtility {
	private static final Logger logger = LoggerFactory.getLogger(GoogleSpeechUtility.class);
	private SynthesizeText synthesizeText;

	public SynthesizeText getSynthesizeText() {
		return synthesizeText;
	}

	public void setSynthesizeText(SynthesizeText synthesizeText) {
		this.synthesizeText = synthesizeText;
	}

	public String createTextToSpeechBean(String content) throws Exception {
		String ret = getSynthesizeText().synthesizeText(content.toString());
		return ret;
	}

	public String createTextToSpeechBean(BroadcastMsg message) throws Exception {
		return createTextToSpeechBean(message.getInputMessage().getContent());
	}
}
