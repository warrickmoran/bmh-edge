package gov.noaa.nws.bmh_edge.utility;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;

public class BroadcastMsgGroupUtility {
	private static final Logger logger = LoggerFactory.getLogger(BroadcastMsgGroupUtility.class);

	public List<BroadcastMsg> extract(BroadcastMsgGroup msgGroup) throws Exception {
		if (msgGroup != null) {
			if (msgGroup.getMessages() != null) {
				logger.debug(String.format("Message List Size:", msgGroup.getMessages().size()));
				return msgGroup.getMessages();
			}
		} else {
			throw new Exception(String.format("Null Messages Object for %d", msgGroup.getTraceId()));
		}

		return null;
	}

}
