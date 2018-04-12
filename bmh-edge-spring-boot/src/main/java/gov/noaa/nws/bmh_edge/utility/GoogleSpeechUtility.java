package gov.noaa.nws.bmh_edge.utility;

import java.io.FileInputStream;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.protobuf.ByteString;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import gov.noaa.nws.bmh_edge.audio.googleapi.SynthesizeText;

public class GoogleSpeechUtility {
	static private String apiKey;

	static public ByteString createTextToSpeechBean(String content) throws Exception {
		ByteString ret = SynthesizeText.synthesizeText(content.toString());
		return ret;
	}

	static public ByteString createTextToSpeechBean(BroadcastMsg message) throws Exception {
		return createTextToSpeechBean(message.getInputMessage().getContent());
	}

	public static String getApiKey() {
		return GoogleSpeechUtility.apiKey;
	}

	public void setApiKey(String apiKey) {
		GoogleSpeechUtility.apiKey = apiKey;
	}
}
