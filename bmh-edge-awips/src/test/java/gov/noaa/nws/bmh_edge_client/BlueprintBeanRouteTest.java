package gov.noaa.nws.bmh_edge_client;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.Test;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.serialization.DynamicSerializationManager;
import com.raytheon.uf.common.serialization.DynamicSerializationManager.SerializationType;
import com.raytheon.uf.common.serialization.SerializationUtil;

import gov.noaa.nws.qpid_server.EmbeddedBroker;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;


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
			e.printStackTrace();
		}
	}

	@Override
	protected String getBlueprintDescriptor() {
		return "/OSGI-INF/blueprint/blueprint-bean.xml";
	}

	@Test
	public void testRoute() throws Exception {  
		
		MockEndpoint mock = getMockEndpoint("mock:result");
		DynamicSerializationManager dsm = DynamicSerializationManager
                .getManager(SerializationType.Thrift);
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
