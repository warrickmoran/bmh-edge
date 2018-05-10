package gov.noaa.nws.bmh_edge.audio.mp3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.tones.GeneratedTonesBuffer;

//
// http://www.javazoom.net/mp3spi/documents.html
//
public class MP3Player {
	private static final Logger logger = LoggerFactory.getLogger(MP3Player.class);
	private static Object lock;
	private static AtomicBoolean interrupt;
	AudioInputStream din;
	
	static {
		lock = new Object();
		interrupt = new AtomicBoolean();
	}

	/**
	 * @return the din
	 */
	public AudioInputStream getDin() {
		return din;
	}

	/**
	 * @param din the din to set
	 */
	public void setDin(AudioInputStream din) {
		this.din = din;
	}

	public void play(BroadcastMsg message) throws Exception {
		play(message.getInputMessage().getOriginalFile());
	}

	public void play(String filename) throws Exception {
		synchronized(MP3Player.lock) {
			MP3Player.interrupt.set(true);
			play(new File(filename));
		}
	}
	
	public void play(byte[] stream) throws IOException, LineUnavailableException {
//		AudioFormat format =
//		    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//		    				8000,
//		                    16,
//		                    1,
//		                    16/1,
//		                    16,
//		                    false);
		AudioFormat format = new AudioFormat(Encoding.ULAW,
                8000, 8, 1, 1, 8000, true);

//		AudioFormat format =
//			    new AudioFormat(8000,
//			                    8,
//			                    1,
//			                    true,
//			                    false);
		AudioInputStream in =
			    new AudioInputStream(new ByteArrayInputStream(stream), format, stream.length);
		
		setDin(AudioSystem.getAudioInputStream(format, in));
		rawplay(format);
		in.close();
	}

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

	private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}
}
