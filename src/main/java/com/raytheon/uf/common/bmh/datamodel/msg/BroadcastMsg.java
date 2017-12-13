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
package com.raytheon.uf.common.bmh.datamodel.msg;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage.ReplacementType;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
//import com.raytheon.uf.common.bmh.stats.DeliveryTimeEvent;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Broadcast Message Record Object. Used to transform text to audio.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jun 23, 2014  3302     bkowal      Initial creation
 * Jul 10, 2014  3285     bsteffen    Add getAfosid()
 * Aug 29, 2014  3568     bkowal      Added query to retrieve broadcast msg by transmitter
 *                                    group, language and afos id.
 * Sep 03, 2014  3554     bsteffen    Add getUnexpiredBroadcastMsgsByAfosIDAndGroup
 * Sep 10, 2014  2585     bsteffen    Implement MAT
 * Sep 12, 2014  3588     bsteffen    Support audio fragments.
 * Oct 21, 2014  3746     rjpeter     Hibernate upgrade.
 * Oct 23, 2014  3748     bkowal      Added getBroadcastMsgsByInputMsg.
 * Nov 18, 2014  3746     rjpeter     Labeled foreign key.
 * Nov 26, 2014  3613     bsteffen    Add getBroadcastMsgsByFragmentPath
 * Dec 08, 2014  3864     bsteffen    Redo some of the playlist manager queries.
 * Mar 05, 2015  4222     bkowal      Include messages that never expire when retrieving
 *                                    unexpired messages.
 * Mar 13, 2015  4213     bkowal      Fixed {@link #GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE_QUERY}.
 * Mar 17, 2015  4160     bsteffen    Add booleans for tone status.
 * Mar 25, 2015  4290     bsteffen    Switch to global replacement.
 * Apr 02, 2015  4248     rjpeter     Made BroadcastFragment database relation a set and add ordered return methods.
 * Apr 07, 2015  4293     bkowal      Updated to include a {@link List} of {@link BroadcastContents}s.
 * Apr 15, 2015  4293     bkowal      Added {@link #forcedExpiration}.
 * Apr 16, 2015  4395     rferrel     Added {@link #ALL_UNEXPIRED_MSGS_QUERY}.
 * May 05, 2015  4456     bkowal      Added {@link #playedInterrupt}.
 * May 12, 2015  4248     rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * May 21, 2015  4397     bkowal      Added {@link #broadcast}.
 * May 29, 2015  4686     bkowal      Renamed the broadcast flag to {@link #delivered}.
 * Jul 01, 2016  5722     rjpeter     Fixed serialization of contents.
 * Jul 28, 2016  5722     rjpeter     Handled null contents.
 * </pre>
 * 
 * @author bkowal
 */

@NamedQueries({
        @NamedQuery(name = BroadcastMsg.GET_UNEXPIRED_MSGS_BY_AFOS_IDS_AND_GROUP, query = BroadcastMsg.GET_UNEXPIRED_MSGS_BY_AFOS_IDS_AND_GROUP_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE, query = BroadcastMsg.GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSGS_BY_INPUT_MSG, query = BroadcastMsg.GET_MSGS_BY_INPUT_MSG_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSG_BY_FRAGMENT_PATH, query = BroadcastMsg.GET_MSG_BY_FRAGMENT_PATH_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSG_BY_INPUT_MSG_AND_GROUP, query = BroadcastMsg.GET_MSG_BY_INPUT_MSG_AND_GROUP_QUERY),
        @NamedQuery(name = BroadcastMsg.GET_MSG_WITH_MULTI_OLD_CONTENT, query = BroadcastMsg.GET_MSG_WITH_MULTI_OLD_CONTENT_QUERY),
        @NamedQuery(name = BroadcastMsg.ALL_UNEXPIRED_MSGS, query = BroadcastMsg.ALL_UNEXPIRED_MSGS_QUERY) })
@Entity
@DynamicSerialize
@Table(name = "broadcast_msg", uniqueConstraints = { @UniqueConstraint(name = "uk_broadcast_msg_tx_group_input_msg", columnNames = {
        "transmitter_group_id", "input_message_id" }) })
@SequenceGenerator(initialValue = 1, name = BroadcastMsg.GEN, sequenceName = "broadcast_msg_seq")
public class BroadcastMsg {
    public static final String GEN = "Broadcast Msg Generator";

    public static final String GET_UNEXPIRED_MSGS_BY_AFOS_IDS_AND_GROUP = "getBroadcastMsgsByAfosIdsAndGroup";

    protected static final String GET_UNEXPIRED_MSGS_BY_AFOS_IDS_AND_GROUP_QUERY = "FROM BroadcastMsg m WHERE m.inputMessage.afosid IN :afosIDs AND (m.inputMessage.expirationTime > :expirationTime OR (m.inputMessage.expirationTime is null AND m.inputMessage.validHeader = true)) AND m.transmitterGroup = :group ORDER BY m.inputMessage.creationTime DESC";

    public static final String GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE = "getBroadcastMsgsByAfosIdGroupAndLanguage";

    protected static final String GET_MSGS_BY_AFOS_ID_GROUP_AND_LANGUAGE_QUERY = "FROM BroadcastMsg m WHERE m.inputMessage.afosid = :afosId AND m.transmitterGroup = :group AND m.inputMessage.language = :language ORDER BY m.inputMessage.creationTime DESC";

    public static final String GET_MSGS_BY_INPUT_MSG = "getBroadcastMsgsByInputMsg";

    protected static final String GET_MSGS_BY_INPUT_MSG_QUERY = "FROM BroadcastMsg m WHERE m.inputMessage.id = :inputMsgId";

    public static final String GET_MSG_BY_FRAGMENT_PATH = "getBroadcastMsgsByFragmentPath";

    protected static final String GET_MSG_BY_FRAGMENT_PATH_QUERY = "SELECT m FROM BroadcastMsg m inner join m.contents c inner join c.fragments f where f.outputName = :path";

    public static final String GET_MSG_BY_INPUT_MSG_AND_GROUP = "getBroadcastMsgByInputMsgAndGroup";

    public static final String GET_MSG_BY_INPUT_MSG_AND_GROUP_QUERY = "FROM BroadcastMsg m WHERE m.inputMessage.id = :inputMsgId AND m.transmitterGroup = :group";

    public static final String GET_MSG_WITH_MULTI_OLD_CONTENT = "getMsgWithMultiOldContent";

    protected static final String GET_MSG_WITH_MULTI_OLD_CONTENT_QUERY = "SELECT DISTINCT m FROM BroadcastMsg m inner join m.contents c WHERE c.id.timestamp < :purgeMillis and size(c) > 1";

    public static final String ALL_UNEXPIRED_MSGS = "getAllUnexpiredMessages";

    protected static final String ALL_UNEXPIRED_MSGS_QUERY = "SELECT bm FROM BroadcastMsg bm INNER JOIN bm.inputMessage m WHERE m.expirationTime IS NULL or m.expirationTime >= :currentTime";

    /* A unique auto-generated numerical id. Long = SQL BIGINT */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    private long id;

    /* The date that the record was created; mainly for auditing purposes. */
    @Column(nullable = false)
    @DynamicSerializeElement
    private Calendar creationDate;

    /* The date the record was last updated; mainly for auditing purposes. */
    @Column(nullable = false)
    @DynamicSerializeElement
    private Calendar updateDate;

    /* ===== Message Header ===== */

    @ManyToOne(optional = false)
    @JoinColumn(name = "transmitter_group_id")
    @ForeignKey(name = "fk_broadcast_msg_to_tx_group")
    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "input_message_id")
    @ForeignKey(name = "fk_broadcast_msg_to_input_msg")
    @DynamicSerializeElement
    private InputMessage inputMessage;

    @OneToMany(mappedBy = "broadcastMsg", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @Sort(type = SortType.NATURAL)
    @DynamicSerializeElement
    private SortedSet<BroadcastContents> contents;

    @Column
    @DynamicSerializeElement
    private boolean playedSameTone = false;

    @Column
    @DynamicSerializeElement
    private boolean playedAlertTone = false;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean playedInterrupt = false;

    /**
     * boolean flag indicating whether or not this message has been successfully
     * delivered to Dac Transmit. This flag is used to calculate the message
     * delivery statistics. This flag will be set when the
     * {@link DeliveryTimeEvent} statistic event is received and processed.
     */
    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean delivered = false;

    @Column(nullable = false)
    @DynamicSerializeElement
    private boolean forcedExpiration = false;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the creationDate
     */
    public Calendar getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the updateDate
     */
    public Calendar getUpdateDate() {
        return updateDate;
    }

    /**
     * @param updateDate
     *            the updateDate to set
     */
    public void setUpdateDate(Calendar updateDate) {
        this.updateDate = updateDate;
    }

    /**
     * @return the transmitterGroup
     */
    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    /**
     * @param transmitterGroup
     *            the transmitterGroup to set
     */
    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    /**
     * @return the inputMessage
     */
    public InputMessage getInputMessage() {
        return inputMessage;
    }

    /**
     * @param inputMessage
     *            the inputMessage to set
     */
    public void setInputMessage(InputMessage inputMessage) {
        this.inputMessage = inputMessage;
    }

    /**
     * @return the contents
     */
    public Set<BroadcastContents> getContents() {
        return contents;
    }

    public BroadcastContents getLatestBroadcastContents() {
        if (this.contents == null || this.contents.isEmpty()) {
            return null;
        }

        return this.contents.first();
    }

    /**
     * Retrieves and removes previous {@link BroadcastContents} records until
     * there is only one record left. Returns {@code null} when only one or no
     * records remain. Primarily exists for purging old audio files associated
     * with messages that do not have an expiration date/time.
     * 
     * @return the previous {@link BroadcastContents} record that was removed.
     */
    public BroadcastContents getAndRemovePreviousContents() {
        if (this.contents == null || this.contents.isEmpty()
                || this.contents.size() == 1) {
            return null;
        }

        BroadcastContents lastContents = this.contents.last();
        this.contents.remove(lastContents);
        return lastContents;
    }

    public void addBroadcastContents(BroadcastContents broadcastContents) {
        if (this.contents == null) {
            this.contents = new TreeSet<>();
        }
        broadcastContents.setBroadcastMsg(this);
        this.contents.add(broadcastContents);
    }

    /**
     * @param contents
     *            the contents to set
     */
    public void setContents(Set<BroadcastContents> contents) {
        if (contents == null) {
            this.contents = null;
        } else if (contents instanceof SortedSet) {
            this.contents = (SortedSet<BroadcastContents>) contents;
        } else {
            this.contents = new TreeSet<>(contents);
        }

        if (this.contents != null && this.contents.isEmpty() == false) {
            for (BroadcastContents broadcastContents : this.contents) {
                broadcastContents.setBroadcastMsg(this);
            }
        }
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        BroadcastContents contents = this.getLatestBroadcastContents();
        if (contents == null || contents.getFragments() == null
                || contents.getFragments().isEmpty()) {
            return false;
        }
        for (BroadcastFragment fragment : contents.getFragments()) {
            if (!fragment.isSuccess()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convenience method, equivalent to getInput().getAfosid();
     * 
     * @return the afosid
     */
    public String getAfosid() {
        if (inputMessage == null) {
            return null;
        } else {
            return inputMessage.getAfosid();
        }
    }

    public boolean isPlayedSameTone() {
        return playedSameTone;
    }

    public void setPlayedSameTone(boolean playedSameTone) {
        this.playedSameTone = playedSameTone;
    }

    public boolean isPlayedAlertTone() {
        return playedAlertTone;
    }

    public void setPlayedAlertTone(boolean playedAlertTone) {
        this.playedAlertTone = playedAlertTone;
    }

    /**
     * @return the playedInterrupt
     */
    public boolean isPlayedInterrupt() {
        return playedInterrupt;
    }

    /**
     * @param playedInterrupt
     *            the playedInterrupt to set
     */
    public void setPlayedInterrupt(boolean playedInterrupt) {
        this.playedInterrupt = playedInterrupt;
    }

    /**
     * @return the delivered
     */
    public boolean isDelivered() {
        return delivered;
    }

    /**
     * @param delivered
     *            the delivered to set
     */
    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean getForcedExpiration() {
        return forcedExpiration;
    }

    public void setForcedExpiration(boolean forcedExpiration) {
        this.forcedExpiration = forcedExpiration;
    }

    public Calendar getEffectiveTime() {
        return inputMessage.getEffectiveTime();
    }

    public Calendar getExpirationTime() {
        return inputMessage.getExpirationTime();
    }

    public boolean isActive() {
        return inputMessage.getActive();
    }

    public ReplacementType getReplacementType() {
        return inputMessage.getReplacementType();
    }

    public boolean isPeriodic() {
        return inputMessage.isPeriodic();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = (prime * result) + (int) (id ^ (id >>> 32));
        result = (prime * result)
                + ((inputMessage == null) ? 0 : inputMessage.hashCode());
        result = (prime * result)
                + ((transmitterGroup == null) ? 0 : transmitterGroup.hashCode());
        return result;
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
        BroadcastMsg other = (BroadcastMsg) obj;
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (inputMessage == null) {
            if (other.inputMessage != null) {
                return false;
            }
        } else if (!inputMessage.equals(other.inputMessage)) {
            return false;
        }
        if (transmitterGroup == null) {
            if (other.transmitterGroup != null) {
                return false;
            }
        } else if (!transmitterGroup.equals(other.transmitterGroup)) {
            return false;
        }
        return true;
    }

}

