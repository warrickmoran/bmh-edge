package gov.noaa.nws.bmh_edge.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import gov.noaa.nws.bmh_edge.audio.googleapi.SynthesizeText;

public class GoogleSpeechUtility {
	private static final Logger logger = LoggerFactory.getLogger(GoogleSpeechUtility.class);
	static private String apiKey;

	static public String createTextToSpeechBean(String content) throws Exception {
		logger.debug(String.format("Synthesize String: %s", content));
		String ret = SynthesizeText.synthesizeText(content.toString());
		return ret;
	}

	static public String createTextToSpeechBean(BroadcastMsg message) throws Exception {
		logger.debug(String.format("Synthesize BroadcastMsg: %s", message.getInputMessage().getContent()));
		return createTextToSpeechBean(message.getInputMessage().getContent());
	}

	public static String getApiKey() {
		return GoogleSpeechUtility.apiKey;
	}

	public void setApiKey(String apiKey) {
		GoogleSpeechUtility.apiKey = apiKey;
	}
}
