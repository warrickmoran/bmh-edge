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

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Google Cloud TextToSpeech API sample application.
 * https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/texttospeech/cloud-client/
 */
public class SynthesizeText {
	private static final Logger logger = LoggerFactory.getLogger(SynthesizeText.class);
	private static final String OUTPUT_FILENAME = "/tmp/output.mp3";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	private String outputDir;
	
	public String getOutputDir() throws Exception {
		if (outputDir.isEmpty()) {
			throw new Exception("Invalid Output Directory Configuration");
		}
		return outputDir;
	}
	
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	
	// [START tts_synthesize_text]
	/**
	 * Demonstrates using the Text to Speech client to synthesize text or ssml.
	 * 
	 * @param text
	 *            the raw text to be synthesized. (e.g., "Hello there!")
	 * @throws Exception
	 *             on TextToSpeechClient Errors.
	 */
	public  String synthesizeText(String text) throws Exception {
		
		if (text.isEmpty()) {
			throw new Exception("Empty String");
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
			
			String outputFilename = String.format("%s/output%s.mp3", getOutputDir(),timeStamp());
			

			// Write the response to the output file.
			try (OutputStream out = new FileOutputStream(outputFilename)) {
				out.write(audioContents.toByteArray());
				logger.info(String.format("Audio content written to file %s", outputFilename));
			}

			return outputFilename;
		}
	}
	// [END tts_synthesize_text]

	// [START tts_synthesize_ssml]
	/**
	 * Demonstrates using the Text to Speech client to synthesize text or ssml.
	 *
	 * Note: ssml must be well-formed according to:
	 * (https://www.w3.org/TR/speech-synthesis/ Example: <speak>Hello there.</speak>
	 * 
	 * @param ssml
	 *            the ssml document to be synthesized. (e.g., "<?xml...")
	 * @throws Exception
	 *             on TextToSpeechClient Errors.
	 */
	public void synthesizeSsml(String ssml) throws Exception {
		// Instantiates a client
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
			// Set the ssml input to be synthesized
			SynthesisInput input = SynthesisInput.newBuilder().setSsml(ssml).build();

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
			String outputFilename = String.format("%s/output%s.mp3", getOutputDir(),timeStamp());

			// Write the response to the output file.
			try (OutputStream out = new FileOutputStream(outputFilename)) {
				out.write(audioContents.toByteArray());
				logger.info(String.format("Audio content written to file %s", outputFilename));
			}
		}
	}
	// [END tts_synthesize_ssml]
	
	private String timeStamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
      
        return sdf.format(timestamp);
	}
}