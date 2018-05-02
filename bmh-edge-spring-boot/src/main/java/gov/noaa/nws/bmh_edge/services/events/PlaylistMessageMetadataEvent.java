package gov.noaa.nws.bmh_edge.services.events;

import org.springframework.context.ApplicationEvent;

import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;

public class PlaylistMessageMetadataEvent extends ApplicationEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DacPlaylistMessageMetadata message;

	public PlaylistMessageMetadataEvent(Object source, DacPlaylistMessageMetadata message) {
		super(source);
		this.message = message;
	}

	public DacPlaylistMessageMetadata getMessage() {
		return message;
	}
}
