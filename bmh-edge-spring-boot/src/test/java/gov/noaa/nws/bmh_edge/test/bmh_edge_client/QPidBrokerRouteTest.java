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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.serialization.SerializationUtil;

import gov.noaa.nws.bmh_edge.BmhEdgeCamelApplication;
import gov.noaa.nws.bmh_edge.test.qpid_server.EmbeddedBroker;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = BmhEdgeCamelApplication.class)
@EnableRouteCoverage
public class QPidBrokerRouteTest extends CamelTestSupport {
	private static EmbeddedBroker broker ;
	private static BroadcastMsgGroup messageGroup;	
	private static BroadcastMsg message;
	private static String playListXML; 
	
	@Autowired
    private CamelContext camelContext;
 
	static {
		try {
			broker = new EmbeddedBroker();
			
			messageGroup = new BroadcastMsgGroup();
			messageGroup.setIds(Stream.of(new Long(1000),new Long(1001)).collect(Collectors.toList()));
			message = new BroadcastMsg();
			InputMessage content = new InputMessage();
			content.setAfosid("TEST");
			
			content.setContent(GoogleTextToSpeechTest.GOOGLE_API_CONTENT);
			message.setInputMessage(content);
		
			
			messageGroup.setMessages(Arrays.asList(message));
			
			// to remove thrift warning
			System.setProperty("thrift.stream.maxsize", "200");
			
			playListXML = String.format("<bmhPlaylist><priority>1</priority></bmhPlaylist>");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
    public static void shutdown() {
        broker.shutdown();
    }
	
	// Must have to utilized beans created through dependency injection
	// Used with ProducerTemplates.   
	@Override
    protected CamelContext createCamelContext() throws Exception {
        return camelContext;
    }

	@Test
	public void camelArraySplitTest() throws Exception {  
		
		MockEndpoint mock = getMockEndpoint("mock:ingestbroadcastmsg");
		mock.expectedMinimumMessageCount(1);

		try {
			byte[] serialize = SerializationUtil.transformToThrift(messageGroup);
		
			template.sendBodyAndHeader("amqp:queue:ingest?preserveMessageQos=true", serialize, "JMSDeliveryMode", javax.jms.DeliveryMode.NON_PERSISTENT);
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}
	
		// assert expectations
		assertMockEndpointsSatisfied();
	}
	
	@Test
	public void camelJaxbTest() throws Exception {  
		
		MockEndpoint mock = getMockEndpoint("mock:ingestjaxb");
		mock.expectedMinimumMessageCount(1);

		try {
			byte[] serialize = SerializationUtil.transformToThrift(playListXML);
		
			template.sendBodyAndHeader("amqp:queue:ingest?preserveMessageQos=true", serialize, "JMSDeliveryMode", javax.jms.DeliveryMode.NON_PERSISTENT);
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}
	
		// assert expectations
		assertMockEndpointsSatisfied();
	}	
}
