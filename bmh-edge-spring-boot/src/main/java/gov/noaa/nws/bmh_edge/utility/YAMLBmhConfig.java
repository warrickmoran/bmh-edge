package gov.noaa.nws.bmh_edge.utility;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="bmh.transmitter")
public class YAMLBmhConfig {
	String id;
	String lineOut;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLineOut() {
		return lineOut;
	}
	public void setLineOut(String lineOut) {
		this.lineOut = lineOut;
	}
}
