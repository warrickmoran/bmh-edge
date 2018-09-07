package gov.noaa.nws.bmh_edge.utility;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="bmh.transmitter")
public class YAMLBmhConfig {
	String id;
	String lineOut;
	String id_msg;
	
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
	public String getId_msg() {
		return id_msg;
	}
	public void setId_msg(String id_msg) {
		this.id_msg = id_msg;
	}
}
