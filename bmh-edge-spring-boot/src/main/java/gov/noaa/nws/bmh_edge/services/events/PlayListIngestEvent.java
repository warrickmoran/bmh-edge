package gov.noaa.nws.bmh_edge.services.events;

import org.springframework.context.ApplicationEvent;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayListIngestEvent.
 */
public class PlayListIngestEvent extends ApplicationEvent {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The message. */
	private String message;

	/**
	 * Instantiates a new play list ingest event.
	 *
	 * @param source the source
	 * @param message the message
	 */
	public PlayListIngestEvent(Object source, String message) {
		super(source);
		this.message = message;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}
