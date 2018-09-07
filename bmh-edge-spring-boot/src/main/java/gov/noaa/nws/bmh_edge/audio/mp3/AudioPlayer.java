package gov.noaa.nws.bmh_edge.audio.mp3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.raytheon.uf.common.bmh.audio.AudioConversionException;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.audio.UnsupportedAudioFormatException;
import com.raytheon.uf.common.bmh.audio.impl.PcmAudioConverter;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;

// TODO: Auto-generated Javadoc
//
// http://www.javazoom.net/mp3spi/documents.html
/**
 * The Class AudioPlayer.
 */
//
@Component
public class AudioPlayer {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(AudioPlayer.class);

	/** The lock. */
	private static Object lock;

	/** The din. */
	private AudioInputStream din;

	// @Autowired
	// private YAMLBmhConfig myConfig;

	static {
		lock = new Object();
	}

	/**
	 * Gets the din.
	 *
	 * @return the din
	 */
	public AudioInputStream getDin() {
		return din;
	}

	/**
	 * Sets the din.
	 *
	 * @param din
	 *            the din to set
	 */
	public void setDin(AudioInputStream din) {
		this.din = din;
	}

	// public YAMLBmhConfig getMyConfig() {
	// return myConfig;
	// }
	//
	// public void setMyConfig(YAMLBmhConfig myConfig) {
	// this.myConfig = myConfig;
	// }

	/**
	 * Method for playing MP3 linked to BMH BroadcastMsg.
	 *
	 * @param message
	 *            the message
	 * @throws Exception
	 *             the exception
	 */
	public void play(BroadcastMsg message) throws Exception {
		synchronized (AudioPlayer.lock) {
			play(message.getInputMessage().getOriginalFile());
		}
	}

	/**
	 * Method for playing MP3.
	 *
	 * @param filename
	 *            the filename
	 * @throws Exception
	 *             the exception
	 */
	public void play(String filename) throws Exception {
		synchronized (AudioPlayer.lock) {
			logger.info(String.format("Audio Play using Filename --> %s", filename));
				play(new File(filename));
		}
	}

	/**
	 * Method for playing BMH tones.
	 *
	 * @param stream
	 *            the stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws LineUnavailableException
	 *             the line unavailable exception
	 * @throws UnsupportedAudioFormatException
	 * @throws AudioConversionException
	 */
	public void play(byte[] stream)
			throws IOException, LineUnavailableException, AudioConversionException, UnsupportedAudioFormatException {
		AudioFormat format = new AudioFormat(Encoding.ULAW,
                8000, 8, 1, 1, 8000, true);

		AudioInputStream in =
			    new AudioInputStream(new ByteArrayInputStream(stream), format, stream.length);
		
		setDin(AudioSystem.getAudioInputStream(format, in));
		rawplay(format);
		in.close();
	}

	/**
	 * Play.
	 *
	 * @param file
	 *            the file
	 * @return the audio input stream
	 * @throws Exception
	 *             the exception
	 */
	private AudioInputStream play(File file) throws Exception {
		// assuming MP3
		AudioInputStream in = AudioSystem.getAudioInputStream(file);
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
				baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
		setDin(AudioSystem.getAudioInputStream(decodedFormat, in));
		// Play now.
		rawplay(decodedFormat);
		in.close();

		return din;
	}

	/**
	 * Rawplay.
	 *
	 * @param targetFormat
	 *            the target format
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws LineUnavailableException
	 *             the line unavailable exception
	 */
	private void rawplay(AudioFormat targetFormat) throws IOException, LineUnavailableException, IllegalArgumentException {
		byte[] data = new byte[4096];
		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			// Start
			line.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while ((nBytesRead != -1)) {
				nBytesRead = getDin().read(data, 0, data.length);
				if (nBytesRead != -1)
					nBytesWritten = line.write(data, 0, nBytesRead);

			}
			// Stop
			line.drain();
			line.stop();
			line.close();
			getDin().close();
		} else {
			throw new LineUnavailableException();
		}
	}

	/**
	 * Gets the line.
	 *
	 * @param audioFormat
	 *            the audio format
	 * @return the line
	 * @throws LineUnavailableException
	 *             the line unavailable exception
	 */
	private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException, IllegalArgumentException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

		for (Mixer.Info mixer : AudioSystem.getMixerInfo()) {
			try {
				res = (SourceDataLine) AudioSystem.getMixer(mixer).getLine(info);
				logger.debug(String.format("Mixer Info (name): %s:%s", mixer.getName(), res));
				// forced to hard code mixer due to multiple sound cards
				// we need to fix issue with yaml configuration to pass values into 
				// AudioPlayer bean  
				if (mixer.getName().contains("hw:1")) { // need to retrieve from yaml file
					logger.info(String.format("Selected Sound Card %s:%s", mixer, info));
					break;
				} else {
					res = null;
				}
			} catch (IllegalArgumentException e) {
				logger.error(String.format("Mixer: %s - %s", mixer,e.getMessage()));
				for (Line.Info mixerInfo : AudioSystem.getMixer(mixer).getSourceLineInfo()) {
					logger.error(String.format("Mixer Line Info: %s", mixerInfo));
				}
			}
		}

		if (res != null) {
			res.open(audioFormat);
		} else {
			logger.error("No Available Sound Cards");
		}
		return res;
	}
}
