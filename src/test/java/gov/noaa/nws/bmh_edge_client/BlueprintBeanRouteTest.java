package gov.noaa.nws.bmh_edge_client;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.jms.JmsConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.serialization.SerializationUtil;

import gov.noaa.nws.qpid_server.EmbeddedBroker;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import java.util.stream.Collectors;
//import java.util.stream.Stream;

public class BlueprintBeanRouteTest extends CamelBlueprintTestSupport {
	private static EmbeddedBroker broker ;
	private static BroadcastMsgGroup messageGroup;
	
	static {
		try {
			broker = new EmbeddedBroker();
			
			messageGroup = new BroadcastMsgGroup();
			messageGroup.setIds(Stream.of(new Long(1000),new Long(1001)).collect(Collectors.toList()));
			
			// to remove thrift warning
			System.setProperty("thrift.stream.maxsize", "200");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected String getBlueprintDescriptor() {
		return "/OSGI-INF/blueprint/blueprint-bean.xml";
	}
//
//	@Before
//	public void setUp() {
//	}
//
//	@After
//	public void tearDown() {
//		//broker.shutdown();
//	}

	@Test
	public void testRoute() throws Exception {  
		
		MockEndpoint mock = getMockEndpoint("mock:result");
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
	public void testIngestBroadcastMsgGroup() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:result");
		mock.expectedMinimumMessageCount(1);	

		try {
			byte[] serialize = SerializationUtil.transformToThrift(messageGroup);
		
			template.sendBodyAndHeader("amqp:queue:ingest?preserveMessageQos=true", serialize, "JMSDeliveryMode", javax.jms.DeliveryMode.NON_PERSISTENT);
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}
		
		// assert expectations
		assertMockEndpointsSatisfied();
		
		BroadcastMsgGroup result = mock.getExchanges().get(0).getIn().getBody(BroadcastMsgGroup.class);
		assertEquals(messageGroup.getIds(), result.getIds());	

	}

}
