package gov.noaa.nws.bmh_edge.audio.mp3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;

// TODO: Auto-generated Javadoc
//
// http://www.javazoom.net/mp3spi/documents.html
/**
 * The Class AudioPlayer.
 */
//
public class AudioPlayer {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(AudioPlayer.class);
	
	/** The lock. */
	private static Object lock;
	
	/** The din. */
	AudioInputStream din;

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
	 * @param din the din to set
	 */
	public void setDin(AudioInputStream din) {
		this.din = din;
	}

	/**
	 * Method for playing MP3 linked to BMH BroadcastMsg.
	 *
	 * @param message the message
	 * @throws Exception the exception
	 */
	public void play(BroadcastMsg message) throws Exception {
		synchronized(AudioPlayer.lock) { 
			play(message.getInputMessage().getOriginalFile());
		}
	}

	/**
	 * Method for playing MP3.
	 *
	 * @param filename the filename
	 * @throws Exception the exception
	 */
	public void play(String filename) throws Exception {
		synchronized(AudioPlayer.lock) {
			play(new File(filename));
		}
	}
	
	/**
	 * Method for playing BMH tones.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws LineUnavailableException the line unavailable exception
	 */
	public void play(byte[] stream) throws IOException, LineUnavailableException {
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
	 * @param file the file
	 * @return the audio input stream
	 * @throws Exception the exception
	 */
	private AudioInputStream play(File file) throws Exception {
		AudioInputStream in = AudioSystem.getAudioInputStream(file);
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
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
	 * @param targetFormat the target format
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws LineUnavailableException the line unavailable exception
	 */
	private void rawplay(AudioFormat targetFormat) throws IOException, LineUnavailableException {
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
		}

	}

	/**
	 * Gets the line.
	 *
	 * @param audioFormat the audio format
	 * @return the line
	 * @throws LineUnavailableException the line unavailable exception
	 */
	private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}
}