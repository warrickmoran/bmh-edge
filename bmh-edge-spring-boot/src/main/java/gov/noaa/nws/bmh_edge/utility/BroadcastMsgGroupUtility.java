package gov.noaa.nws.bmh_edge.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;

public class BroadcastMsgGroupUtility {
	private static final Logger logger = LoggerFactory.getLogger(BroadcastMsgGroupUtility.class);

	public String extract(BroadcastMsgGroup msgGroup) {
		if (msgGroup != null) {
			if (msgGroup.getMessages() != null) {
				for (BroadcastMsg broadcastMsg : msgGroup.getMessages()) {
					if (broadcastMsg != null) {
						if ((broadcastMsg.getAfosid() != null)
								&& (broadcastMsg.getInputMessage().getContent() != null)) {
							logger.info(String.format("Message Group and Content: %s : %s\n", broadcastMsg.getAfosid(),
									broadcastMsg.getInputMessage().getContent()));
							return broadcastMsg.getInputMessage().getContent();
						} else
							logger.error(String.format("GetAfosID or GetContent == NULL"));
					} else {
						logger.error(String.format("BroadcastMsg == NULL)"));
					}
				}
			} else {
				logger.error(String.format("Null Messages Object for %d", msgGroup.getTraceId()));
			}
		}
		
		return "";
	}
}
