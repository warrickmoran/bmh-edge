package gov.noaa.nws.bmh_edge.test.bmh_edge_client;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.EnableRouteCoverage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import gov.noaa.nws.bmh_edge.BmhEdgeCamelApplication;
import gov.noaa.nws.bmh_edge.test.qpid_server.EmbeddedBroker;
import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;


@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = BmhEdgeCamelApplication.class)
@EnableRouteCoverage
public class GoogleTextToSpeechTest extends CamelTestSupport {
	private static final Logger logger = LoggerFactory.getLogger(GoogleTextToSpeechTest.class);
	@Autowired
	private CamelContext camelContext;
	private static EmbeddedBroker broker ;

	static final private String GOOGLE_API_CONTENT = "BMH EDGE Test";
	
	static {
		try {
			broker = new EmbeddedBroker();
			
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
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}

		// assert expectations
		assertMockEndpointsSatisfied();
	}

}
