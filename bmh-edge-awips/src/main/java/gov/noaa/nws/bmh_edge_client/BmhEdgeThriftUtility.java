package gov.noaa.nws.bmh_edge_client;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;

public class BmhEdgeThriftUtility {
	
	public static byte[] serialize(Object obj) throws SerializationException {
		return SerializationUtil.transformToThrift(obj);
	}
	
	public static <T> T deserialize(Class<T> clazz, byte[] bytes) throws SerializationException {
		return SerializationUtil.transformFromThrift(clazz, bytes);
	}
	
	public static <T> T deserialize(byte[] bytes) throws SerializationException {
		return (T) SerializationUtil.transformFromThrift(bytes);
	}
}
