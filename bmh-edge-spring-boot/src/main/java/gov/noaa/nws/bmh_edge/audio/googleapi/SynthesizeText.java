/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.noaa.nws.bmh_edge.audio.googleapi;

// Imports the Google Cloud client library
import com.google.cloud.texttospeech.v1beta1.AudioConfig;
import com.google.cloud.texttospeech.v1beta1.AudioEncoding;
import com.google.cloud.texttospeech.v1beta1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1beta1.SynthesisInput;
import com.google.cloud.texttospeech.v1beta1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1beta1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1beta1.VoiceSelectionParams;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Google Cloud TextToSpeech API sample application.
 * https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/texttospeech/cloud-client/
 */
public class SynthesizeText {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(SynthesizeText.class);

	/**
	 * Demonstrates using the Text to Speech client to synthesize text or ssml.
	 *
	 * @param text            the raw text to be synthesized. (e.g., "Hello there!")
	 * @param filename the filename
	 * @return the boolean
	 * @throws Exception             on TextToSpeechClient Errors.
	 */
	public Boolean synthesizeText(String text, String filename) throws Exception {

		if (text.isEmpty()) {
			throw new Exception("Empty String");
		} else if (!createOutputDirectory(filename)) {
			throw new Exception("Unable to Create MP3 Output Directory");
		}

		// Instantiates a client
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
			// Set the text input to be synthesized
			SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

			// Build the voice request
			VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("en-US") // languageCode =
																									// "en_us"
					.setSsmlGender(SsmlVoiceGender.FEMALE) // ssmlVoiceGender = SsmlVoiceGender.FEMALE
					.build();

			// Select the type of audio file you want returned
			AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3) // MP3 audio.
					.build();

			// Perform the text-to-speech request
			SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

			// Get the audio contents from the response
			ByteString audioContents = response.getAudioContent();

			// Write the response to the output file.
			try (OutputStream out = new FileOutputStream(filename)) {
				out.write(audioContents.toByteArray());
				logger.info(String.format("Audio content written to file %s", filename));
			} catch (Exception ex) {
				logger.error(String.format("%s", ex.getMessage()));
				return Boolean.FALSE;
			}

			return Boolean.TRUE;
		}
	}
	
	/**
	 * Creates the output directory.
	 *
	 * @param filename the filename
	 * @return the boolean
	 */
	private Boolean createOutputDirectory(String filename) {
		File file = new File(filename);
		File parent = file.getParentFile();
		Boolean ret = true;
		
		if (!file.exists() && !parent.exists()) {
			ret = parent.mkdirs();
		} 
		
		return ret;
	}
}