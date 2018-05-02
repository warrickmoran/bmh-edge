package gov.noaa.nws.bmh_edge.services.events;

import org.springframework.context.ApplicationEvent;

public class PlayListIngestEvent extends ApplicationEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;

	public PlayListIngestEvent(Object source, String message) {
		super(source);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
