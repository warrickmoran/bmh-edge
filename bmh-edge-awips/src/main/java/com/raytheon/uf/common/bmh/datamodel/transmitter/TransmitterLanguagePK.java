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
package com.raytheon.uf.common.bmh.datamodel.transmitter;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.datamodel.language.Language;
import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.bmh.diff.DiffTitle;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Transmitter Language Primary Key
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Oct 29, 2014 3636       rferrel     Implement logging.
 * Jan 13, 2015 3809       bkowal      Made {@link #transmitterGroup} a 
 *                                     {@link DynamicSerializeElement}.
 * May 12, 2015  4248      rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Embeddable
@DynamicSerialize
public class TransmitterLanguagePK implements Serializable {
    private static final long serialVersionUID = 1L;

    // FK to transmitterGroup
    @ManyToOne(optional = false)
    @ForeignKey(name = "fk_tx_language_to_tx_group")
    @DynamicSerializeElement
    private TransmitterGroup transmitterGroup;

    // Language: 0-English, 1-Spanish
    @Enumerated(EnumType.STRING)
    @Column(length = Language.LENGTH, nullable = false)
    @DynamicSerializeElement
    @DiffTitle(position = 1)
    @DiffString
    private Language language;

    public TransmitterGroup getTransmitterGroup() {
        return transmitterGroup;
    }

    public void setTransmitterGroup(TransmitterGroup transmitterGroup) {
        this.transmitterGroup = transmitterGroup;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((language == null) ? 0 : language.hashCode());
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
        TransmitterLanguagePK other = (TransmitterLanguagePK) obj;
        if (language != other.language) {
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
