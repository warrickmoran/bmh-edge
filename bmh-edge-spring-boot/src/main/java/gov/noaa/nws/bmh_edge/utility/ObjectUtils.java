package gov.noaa.nws.bmh_edge.utility;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
//
// Manipulate Java Objects
/**
 * The Class ObjectUtils.
 */
//
public class ObjectUtils {
	
	/**
	 * Gets the field names and values.
	 *
	 * @param obj the obj
	 * @param publicOnly the public only
	 * @return the field names and values
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	//Get fields and values from an Object 
	public static Map<String, Object> getFieldNamesAndValues(final Object obj, boolean publicOnly)
			throws IllegalArgumentException, IllegalAccessException {
		Class<? extends Object> c1 = obj.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		Field[] fields = c1.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			String name = fields[i].getName();
			if (publicOnly) {
				if (Modifier.isPublic(fields[i].getModifiers())) {
					Object value = fields[i].get(obj);
					map.put(name, value);
				}
			} else {
				fields[i].setAccessible(true);
				Object value = fields[i].get(obj);
				map.put(name, value);
			}
		}
		return map;
	}
	
	/**
	 * Prints the field names and values.
	 *
	 * @param obj the obj
	 * @param publicOnly the public only
	 * @return the string buffer
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	// Print fields and values from an Object 
	public static StringBuffer printFieldNamesAndValues(final Object obj, boolean publicOnly) throws IllegalArgumentException, IllegalAccessException {
		StringBuffer ret = new StringBuffer(ObjectUtils.getFieldNamesAndValues(obj,publicOnly).toString());
		System.out.println(ObjectUtils.getFieldNamesAndValues(obj,publicOnly));
		return ret;
	}
	
	/**
	 * Prints the filed names and values.
	 *
	 * @param obj the obj
	 * @return the string buffer
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	public static StringBuffer printFiledNamesAndValues(final Object obj) throws IllegalArgumentException, IllegalAccessException {
		return printFieldNamesAndValues(obj, true);
	}
}
