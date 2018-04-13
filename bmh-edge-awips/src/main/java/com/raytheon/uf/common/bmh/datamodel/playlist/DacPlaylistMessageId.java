/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.bmh.datamodel.playlist;

import java.nio.file.Path;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsg;
import com.raytheon.uf.common.bmh.trace.ITraceable;

/**
 * 
 * Xml representation of a playlist message that is sent from the playlist
 * manager to the comms manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 01, 2014  3285     bsteffen    Initial creation
 * Jul 14, 2014  3286     dgilling    Implement hashCode()/equals().
 * Aug 18, 2014  3540     dgilling    Implement toString().
 * Jan 05, 2015  3913     bsteffen    Handle future replacements.
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Apr 07, 2015  4293     bkowal      Added {@link #timestamp} to be written
 *                                    to the playlist.
 * May 13, 2015  4429     rferrel     Implement {@link ITraceable}.
 * Feb 04, 2016  5308     bkowal      Promoted {@link #path}.
 * Mar 08, 2016  5382     bkowal      Added {@link #version}.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class DacPlaylistMessageId implements ITraceable {

    public static final String CURRENT_VERSION = "1.0";

    @XmlAttribute
    protected long broadcastId;

    /**
     * Broadcast timestamp - Indicates when the associated {@link BroadcastMsg}
     * was last updated.
     */
    @XmlAttribute
    private Long timestamp;

    @XmlAttribute
    protected String traceId;

    @XmlElement
    private String version;

    @XmlElement
    protected Calendar expire;

    protected transient Path path;

    public DacPlaylistMessageId() {

    }

    public DacPlaylistMessageId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    public long getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Calendar getExpire() {
        return expire;
    }

    public void setExpire(Calendar expire) {
        this.expire = expire;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DacPlaylistMessageId [broadcastId=");
        builder.append(broadcastId);
        if (this.timestamp != null) {
            builder.append(", timestamp=");
            builder.append(this.timestamp);
        }
        if (expire != null) {
            builder.append(",expire=");
            builder.append(expire.getTime().toString());
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return new Long(broadcastId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DacPlaylistMessageId other = (DacPlaylistMessageId) obj;
        if (broadcastId != other.broadcastId) {
            return false;
        }
        return true;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
