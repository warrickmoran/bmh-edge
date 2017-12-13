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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.bmh.diff.DiffTitle;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Represents a UGC Area code.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * May 30, 2014  3175     rjpeter     Initial creation
 * Jul 10, 2014  3283     bsteffen    Change transmitters from map to set.
 * Jul 17, 2014  3406     mpduff      Added id pk column, named query, removed cascade
 * Aug 14, 2014  3411     mpduff      Add areaName to unique constraint
 * Oct 21, 2014  3746     rjpeter     Hibernate upgrade.
 * Oct 24, 2014  3636     rferrel     Implement logging.
 * Nov 21, 2014  3845     bkowal      Added getAreaForTransmitter
 * Feb 23, 2015  4140     rjpeter     Named foreign key constraints.
 * May 12, 2015  4248     rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * Jul 22, 2015  4676     bkowal      Update comment documentation.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = Area.GET_AREA_FOR_CODE, query = Area.GET_AREA_FOR_CODE_QUERY),
        @NamedQuery(name = Area.GET_AREAS_FOR_TRANSMITTER, query = Area.GET_AREAS_FOR_TRANSMITTER_QUERY) })
@Entity
@Table(name = "area", uniqueConstraints = {
        @UniqueConstraint(name = "uk_area_areaCode", columnNames = { "areaCode" }),
        @UniqueConstraint(name = "uk_area_areaName", columnNames = { "areaName" }) })
@SequenceGenerator(initialValue = 1, allocationSize = 1, name = Area.GEN, sequenceName = "area_seq")
@DynamicSerialize
public class Area {
    static final String GEN = "Area Generator";

    public static final String GET_AREA_FOR_CODE = "getAreaForCode";

    protected static final String GET_AREA_FOR_CODE_QUERY = "FROM Area a WHERE a.areaCode = :areaCode";

    public static final String GET_AREAS_FOR_TRANSMITTER = "getAreaForTransmitter";

    protected static final String GET_AREAS_FOR_TRANSMITTER_QUERY = "SELECT a FROM Area a INNER JOIN a.transmitters t WHERE t.id = :transmitterId";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GEN)
    @DynamicSerializeElement
    @DiffTitle(position = 2)
    protected int id;

    /**
     * SSXNNN - 6 digit UGC area code
     * 
     * <pre>
     * SS - State
     * X - C for county code, or a numeral (i.e., 1 through 9) for a partial area code
     * NNN - county code number
     * </pre>
     */
    @Column(length = 6, nullable = false)
    @DynamicSerializeElement
    @DiffTitle(position = 1)
    @DiffString
    private String areaCode;

    @Column(length = 30, nullable = false)
    @DynamicSerializeElement
    private String areaName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "area_transmitter", joinColumns = @JoinColumn(name = "areaId"), inverseJoinColumns = @JoinColumn(name = "transmitterId"))
    @ForeignKey(name = "fk_area_tx_to_area", inverseName = "fk_area_tx_to_tx")
    @Fetch(FetchMode.SUBSELECT)
    @DynamicSerializeElement
    private Set<Transmitter> transmitters;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the areaId to set
     */
    public void setId(int id) {
        this.id = id;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public Set<Transmitter> getTransmitters() {
        return transmitters;
    }

    public void setTransmitters(Set<Transmitter> transmitters) {
        this.transmitters = transmitters;
    }

    public void addTransmitter(Transmitter transmitter) {
        if (transmitter != null) {
            if (transmitters == null) {
                transmitters = new HashSet<>();
            }

            transmitters.add(transmitter);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((areaCode == null) ? 0 : areaCode.hashCode());
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
        Area other = (Area) obj;
        if (areaCode == null) {
            if (other.areaCode != null) {
                return false;
            }
        } else if (!areaCode.equals(other.areaCode)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Area [id=").append(id).append(", areaCode=")
                .append(areaCode).append(", areaName=").append(areaName)
                .append(", transmitters=");
        if (transmitters == null) {
            sb.append(transmitters);
        } else {
            sb.append("[");
            if (transmitters.size() > 0) {
                for (Transmitter transmitter : transmitters) {
                    sb.append(transmitter.getMnemonic()).append(", ");
                }
                sb.setLength(sb.length() - 2);
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }

}
