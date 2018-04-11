package gov.noaa.nws.bmh_edge.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;

public class BroadcastMsgGroupUtility {
	private static final Logger logger = LoggerFactory.getLogger(BroadcastMsgGroupUtility.class);	
	public void extract(BroadcastMsgGroup msgGroup) {
		if (msgGroup != null) {
			for (Long ids : msgGroup.getIds()) {
				logger.info(String.format("Message ID %d", ids));
			}
		}
	}
}
