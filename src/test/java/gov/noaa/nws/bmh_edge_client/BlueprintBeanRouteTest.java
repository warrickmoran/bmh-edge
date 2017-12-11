package gov.noaa.nws.bmh_edge_client;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.jms.JmsConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.noaa.nws.qpid_server.EmbeddedBroker;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

public class BlueprintBeanRouteTest extends CamelBlueprintTestSupport {
	private static EmbeddedBroker broker ;
	
	static {
		try {
			broker = new EmbeddedBroker();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected String getBlueprintDescriptor() {
		return "/OSGI-INF/blueprint/blueprint-bean.xml";
	}

//	@Before
//	public void setUp() {
//		
//	}
//
//	@After
//	public void tearDown() {
//		//broker.shutdown();
//	}

	@Test
	public void testRoute() throws Exception {  
		try {
			template.sendBodyAndHeader("amqp:queue:ingest?preserveMessageQos=true", "Hello World",  "JMSDeliveryMode", javax.jms.DeliveryMode.NON_PERSISTENT);
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}
		
		MockEndpoint mock = getMockEndpoint("mock:result");
		mock.expectedMinimumMessageCount(1);

		// assert expectations
		assertMockEndpointsSatisfied();
	}

}
