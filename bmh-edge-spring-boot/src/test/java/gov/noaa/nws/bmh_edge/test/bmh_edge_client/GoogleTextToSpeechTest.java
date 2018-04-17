package gov.noaa.nws.bmh_edge.test.bmh_edge_client;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.EnableRouteCoverage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;

import gov.noaa.nws.bmh_edge.BmhEdgeCamelApplication;
import gov.noaa.nws.bmh_edge.test.qpid_server.EmbeddedBroker;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = BmhEdgeCamelApplication.class)
@EnableRouteCoverage
public class GoogleTextToSpeechTest extends CamelTestSupport {
	private static EmbeddedBroker broker ;
	private static BroadcastMsgGroup messageGroup;	
	private static BroadcastMsg message;
	@Autowired
	private CamelContext camelContext;

	static final private String GOOGLE_API_CONTENT = "BMH EDGE Test";
	
	static {
		try {
			broker = new EmbeddedBroker();
			
			messageGroup = new BroadcastMsgGroup();
			messageGroup.setIds(Stream.of(new Long(1000),new Long(1001)).collect(Collectors.toList()));
			message = new BroadcastMsg();
			InputMessage content = new InputMessage();
			
			content.setContent(GOOGLE_API_CONTENT);
			message.setInputMessage(content);
			
			messageGroup.setMessages(Arrays.asList(message));
			
			
			// to remove thrift warning
			System.setProperty("thrift.stream.maxsize", "200");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Must have to utilized beans created through dependency injection
	// Used with ProducerTemplates.
	@Override
	protected CamelContext createCamelContext() throws Exception {
		return camelContext;
	}
	
	@Test
	public void testSynthesizeTextCreation() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:audioresponse");
		mock.expectedMinimumMessageCount(1);

		try {
			template.sendBody("direct:audio", GOOGLE_API_CONTENT);
			//ByteString resultAudio= mock.getExchanges().get(0).getIn().getBody(ByteString.class);
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}

		// assert expectations
		assertMockEndpointsSatisfied();
	}
	
	@Test
	public void testSynthesizeBroadcastMsgCreation() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:audioresponse");
		mock.expectedMinimumMessageCount(1);

		try {
			template.sendBody("direct:audio", message);
			//ByteString resultAudio= mock.getExchanges().get(0).getIn().getBody(ByteString.class);
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}

		// assert expectations
		assertMockEndpointsSatisfied();
	}

}
