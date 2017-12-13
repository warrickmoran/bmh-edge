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
package com.raytheon.uf.common.time;

import java.sql.Timestamp;
import java.util.Date;

import com.raytheon.uf.common.serialization.IDeserializationContext;
import com.raytheon.uf.common.serialization.ISerializationContext;
import com.raytheon.uf.common.serialization.ISerializationTypeAdapter;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;
import com.raytheon.uf.common.time.FormattedDate.FormattedDateSerializer;

/**
 * Extend CommutativeTimestamp for backward compatibility. Can delete once
 * 16.4.1 is no longer used. Also need to delete python versions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2015 4486       rjpeter     Initial creation
 * Jun 24, 2016 5696       rjpeter     Extend CommutativeTimestamp for compatibility.
 * 
 * </pre>
 * 
 * @author rjpeter
 */
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = FormattedDateSerializer.class)
@Deprecated
public class FormattedDate extends CommutativeTimestamp {

    private static final long serialVersionUID = 1L;

    public FormattedDate() {
        super();
    }

    public FormattedDate(long date) {
        super(date);
    }

    public FormattedDate(Date date) {
        super(date);
    }

    public FormattedDate(Timestamp date) {
        super(date);
    }

    @Deprecated
    public static class FormattedDateSerializer implements
            ISerializationTypeAdapter<FormattedDate> {

        @Override
        public FormattedDate deserialize(IDeserializationContext deserializer)
                throws SerializationException {
            long t = deserializer.readI64();
            return new FormattedDate(t);
        }

        @Override
        public void serialize(ISerializationContext serializer,
                FormattedDate object) throws SerializationException {
            serializer.writeI64(object.getTime());
        }

    }
}
