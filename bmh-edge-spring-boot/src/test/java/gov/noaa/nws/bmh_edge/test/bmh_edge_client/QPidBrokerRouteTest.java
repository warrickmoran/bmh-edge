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
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;
import com.raytheon.uf.common.serialization.SerializationUtil;

import gov.noaa.nws.bmh_edge.BmhEdgeCamelApplication;
import gov.noaa.nws.bmh_edge.test.qpid_server.EmbeddedBroker;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = BmhEdgeCamelApplication.class)
@EnableRouteCoverage
public class QPidBrokerRouteTest extends CamelTestSupport {
	private static EmbeddedBroker broker ;
	private final static String playListXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<bmhPlaylist traceId=\"OMARWSOAX_2602\" interrupt=\"false\" latestTrigger=\"2018-04-24T15:09:20.109Z\" expired=\"2018-04-25T01:00:00Z\" start=\"2018-04-24T01:00:00Z\" created=\"2018-04-24T15:09:20.109Z\" suite=\"General HGR\" priority=\"0\" transmitterGroup=\"HGR\">\n" + 
			"    <message traceId=\"OMARWSOAX_2602\" timestamp=\"1524582379933\" broadcastId=\"2908\">\n" + 
			"        <expire>2018-04-25T01:00:00Z</expire>\n" + 
			"    </message>\n" + 
			"    <message traceId=\"OMARWSOAX_2602\" timestamp=\"1524581650252\" broadcastId=\"2902\">\n" + 
			"        <expire>2018-04-25T01:00:00Z</expire>\n" + 
			"    </message>\n" + 
			"    <trigger start=\"2018-04-24T15:09:20.109Z\"/>\n" + 
			"</bmhPlaylist>";
	private final static String playListMetaXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<bmhMessageMetadata broadcastId=\"0\">\n" + 
			"    <version>1.0</version>\n" + 
			"    <expire>2018-04-25T01:00:00Z</expire>\n" + 
			"    <name>OMARWSOAX</name>\n" + 
			"    <start>2018-04-24T01:00:00Z</start>\n" + 
			"    <messageType>OMARWSOAX</messageType>\n" + 
			"    <alertTone>false</alertTone>\n" + 
			"    <toneBlackoutEnabled>false</toneBlackoutEnabled>\n" + 
			"    <soundFile>/awips2/bmh/data/audio/180424/OMARWSOAX_ENG_Paul_2908_2908_150619.ulaw</soundFile>\n" + 
			"    <messageText>AN AREA OF LOW PRESSURE ACROSS THE FRONT RANGE OF THE ROCKIES WILL \n" + 
			"MOVE EASTWARD ACROSS THE MIDWEST TONIGHT AND TUESDAY. THIS WILL BRING \n" + 
			"A WIDE VARIETY OF WEATHER TO THE PLAINS, INCLUDING HEAVY SNOW ACROSS \n" + 
			"PORTIONS OF SOUTH DAKOTA INTO NORTHERN IOWA AND MINNESOTA, A WINTRY \n" + 
			"MIX OF RAIN, FREEZING RAIN, SLEET AND SNOW ACROSS THE LOCAL AREA, AND \n" + 
			"STRONG TO SEVERE THUNDERSTORMS FROM EASTERN KANSAS TO MISSOURI TO THE \n" + 
			"MID MISSISSIPPI AND LOWER OHIO RIVER VALLEYS LATE TONIGHT INTO \n" + 
			"TUESDAY. FOR OUR AREA, 1 TO 4 INCHES OF SNOW IS FORECAST NORTH OF \n" + 
			"INTERSTATE 80, AND SOME MINOR ICING COULD OCCUR IN SOUTHEAST NEBRASKA \n" + 
			"AND SOUTHWEST IOWA WITH PATCHY FREEZING DRIZZLE. STRONG NORTHWEST \n" + 
			"WINDS EVENTUALLY DEVELOP BEHIND THE AREA OF LOW PRESSURE, WITH MANY \n" + 
			"LOCATIONS SEEING WINDS 25 TO 45 MILES PER HOUR THROUGH THE DAY \n" + 
			"TUESDAY. LOW TEMPERATURES TONIGHT DROP INTO THE THE MID 20S TO LOWER \n" + 
			"30S. HIGHS TUESDAY WILL RANGE FROM THE UPPER 20S TO THE UPPER 30S.</messageText>\n" + 
			"    <confirm>false</confirm>\n" + 
			"    <watch>false</watch>\n" + 
			"    <warning>false</warning>\n" + 
			"    <initialRecognitionTime>1524582379032</initialRecognitionTime>\n" + 
			"    <recognized>false</recognized>\n" + 
			"</bmhMessageMetadata>";
	private final static String invalidXML = "<bmh></bmh>";
	
	@Autowired
    private CamelContext camelContext;
 
	static {
		try {
			broker = new EmbeddedBroker();
			
			// to remove thrift warning
			System.setProperty("thrift.stream.maxsize", "200");
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
	public void camelJaxbPlayListTest() throws Exception {  
		
		MockEndpoint mock = getMockEndpoint("mock:ingestjaxb");
		mock.expectedMinimumMessageCount(1);
		mock.expectedBodyReceived().body().isInstanceOf(com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist.class);

		try {
			byte[] serialize = SerializationUtil.transformToThrift(playListXML);
		
			template.sendBodyAndHeader("amqp:queue:ingest?preserveMessageQos=true", serialize, "JMSDeliveryMode", javax.jms.DeliveryMode.NON_PERSISTENT);
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}
		
		// assert expectations
		assertMockEndpointsSatisfied();		
	}	
	
	@Test
	public void camelJaxbPlayListMetaTest() throws Exception {  
		
		MockEndpoint mock = getMockEndpoint("mock:ingestjaxb");
		mock.expectedMinimumMessageCount(1);
		mock.expectedBodyReceived().body().isInstanceOf(com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata.class);

		try {
			byte[] serialize = SerializationUtil.transformToThrift(playListMetaXML);
		
			template.sendBodyAndHeader("amqp:queue:ingest?preserveMessageQos=true", serialize, "JMSDeliveryMode", javax.jms.DeliveryMode.NON_PERSISTENT);
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}
		
		// assert expectations
		assertMockEndpointsSatisfied();
	}	
	

	@Test
	public void camelJaxbInvalidTest() throws Exception {  
		
		MockEndpoint mock = getMockEndpoint("mock:ingestjaxb");
		mock.expectedMinimumMessageCount(0);
		
		try {
			byte[] serialize = SerializationUtil.transformToThrift(invalidXML);
		
			template.sendBodyAndHeader("amqp:queue:ingest?preserveMessageQos=true", serialize, "JMSDeliveryMode", javax.jms.DeliveryMode.NON_PERSISTENT);
		} catch (CamelExecutionException x) {
			x.printStackTrace();
		}
		
		// assert expectations
		assertMockEndpointsSatisfied();
	}
}
