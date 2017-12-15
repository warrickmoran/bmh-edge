package gov.noaa.nws.qpid_server;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
 
import org.apache.qpid.server.SystemLauncher;
 
public class EmbeddedBroker
{
    private static final String INITIAL_CONFIGURATION = "qpid-embedded-initial.json";
    private SystemLauncher systemLauncher;
 
    public static void main(String args[]) {
        try {
        	EmbeddedBroker broker = new EmbeddedBroker();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public EmbeddedBroker() throws Exception {
    	start();
    }
 
    public void start() throws Exception {
        systemLauncher = new SystemLauncher();
        try {
            systemLauncher.startup(createSystemConfig());
          
            // performMessagingOperations();
        } catch (Exception x) {
        	x.printStackTrace();
        }
    }
    
    public void shutdown() {
    	systemLauncher.shutdown();
    }
 
    private Map<String, Object> createSystemConfig() {
        Map<String, Object> attributes = new HashMap<>();
        URL initialConfig = EmbeddedBroker.class.getClassLoader().getResource(INITIAL_CONFIGURATION);
        attributes.put("type", "Memory");
        attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
        attributes.put("startupLoggedToSystemOut", true);
        return attributes;
    }
}