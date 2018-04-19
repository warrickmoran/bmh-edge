package gov.noaa.nws.bmh_edge.utility;

import org.apache.camel.Exchange;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;

public class BmhPlaylistUtility {
	private String transmitterID;

	public String getTransmitterID() {
		return transmitterID;
	}


	public void setTransmitterID(String transmitterID) {
		this.transmitterID = transmitterID;
	}
	

	public BroadcastMsg transmitterCheck(Exchange exchange, BroadcastMsg message) {
		exchange.getOut().setHeader("trans_check", Boolean.FALSE);
		
		if (getTransmitterID().equalsIgnoreCase("ANY")) {
			exchange.getOut().setHeader("trans_check", Boolean.TRUE);
		}
		
		for (Transmitter trans : message.getInputMessage().getSelectedTransmitters()) {
			if (trans.getCallSign().equalsIgnoreCase(getTransmitterID())) {
				exchange.getOut().setHeader("trans_check", Boolean.TRUE);
			}
		}
		
		return message;
	}
}
