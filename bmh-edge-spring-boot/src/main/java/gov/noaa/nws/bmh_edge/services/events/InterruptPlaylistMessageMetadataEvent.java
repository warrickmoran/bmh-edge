package gov.noaa.nws.bmh_edge.services.events;

import org.springframework.context.ApplicationEvent;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

// TODO: Auto-generated Javadoc
/**
 * The Class InterruptPlaylistMessageMetadataEvent.
 */
public class InterruptPlaylistMessageMetadataEvent extends ApplicationEvent {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The message. */
	private DacPlaylistMessageMetadata message;

	/**
	 * Instantiates a new interrupt playlist message metadata event.
	 *
	 * @param source the source
	 * @param message the message
	 */
	public InterruptPlaylistMessageMetadataEvent(Object source, DacPlaylistMessageMetadata message) {
		super(source);
		this.message = message;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public DacPlaylistMessageMetadata getMessage() {
		return message;
	}
}
