package gov.noaa.nws.bmh_edge.test.bmh_edge_client;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.EnableRouteCoverage;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;

import gov.noaa.nws.bmh_edge.BmhEdgeCamelApplication;
import gov.noaa.nws.bmh_edge.audio.googleapi.SynthesizeText;
import gov.noaa.nws.bmh_edge.audio.mp3.AudioPlayer;
import gov.noaa.nws.bmh_edge.test.qpid_server.EmbeddedBroker;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = BmhEdgeCamelApplication.class)
@EnableRouteCoverage
public class BMHEdgeAudioTest {//extends CamelTestSupport {
	private static final Logger logger = LoggerFactory.getLogger(BMHEdgeAudioTest.class);
	
//	private static EmbeddedBroker broker ;
//	private static DacPlaylistMessageMetadata message;
//	@Autowired
//	private CamelContext camelContext;

	static final protected String GOOGLE_API_CONTENT = "BMH EDGE Test";
	
//	static {
//		try {
//			broker = new EmbeddedBroker();
//			
//			message = new DacPlaylistMessageMetadata();
//			message.setMessageText(GoogleTextToSpeechTest.GOOGLE_API_CONTENT);
//			message.setSoundFiles(Arrays.asList("./mp3/test.out"));
//			
//			// to remove thrift warning
//			System.setProperty("thrift.stream.maxsize", "200");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	@AfterClass
//    public static void shutdown() {
//        broker.shutdown();
//    }
//
//	// Must have to utilized beans created through dependency injection
//	// Used with ProducerTemplates.
//	@Override
//	protected CamelContext createCamelContext() throws Exception {
//		return camelContext;
//	}

	
//	@Test
//	private void testSynthesizeBroadcastMsgCreation() throws Exception {
//		MockEndpoint mock = getMockEndpoint("mock:audioresponse");
//		mock.expectedMinimumMessageCount(1);
//
//		try {
//			template.sendBody("seda:audio", message);
//			Thread.sleep(10000);
//			//ByteString resultAudio= mock.getExchanges().get(0).getIn().getBody(ByteString.class);
//		} catch (CamelExecutionException x) {
//			x.printStackTrace();
//		}
//
//		// assert expectations
//		assertMockEndpointsSatisfied();
//	}
	
	@Test
	public void testSimpleSynthesizeText() throws Exception {
		SynthesizeText tts = new SynthesizeText();
		AudioPlayer player = new AudioPlayer();
		
		tts.synthesizeText("This is a simple test of TTS", "./test.mp3");
		
		try {
			player.play("./test.mp3");
		} catch (IllegalArgumentException ex) {
			logger.warn("Catching Audio Line Exception Until We Can Properly Auto Detect"); 
		}
	}
	
	@Test 
	public void testULawAudio() throws Exception {
		AudioPlayer player = new AudioPlayer();
		
		player.play("./audio/ulaw/LWXSVRLWX_LiveMsg.ul");
	}
}
