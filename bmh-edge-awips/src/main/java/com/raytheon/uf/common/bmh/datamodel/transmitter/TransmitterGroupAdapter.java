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

import com.raytheon.uf.common.bmh.datamodel.msg.ProgramSummary;
import com.raytheon.uf.common.serialization.IDeserializationContext;
import com.raytheon.uf.common.serialization.ISerializationContext;
import com.raytheon.uf.common.serialization.ISerializationTypeAdapter;
import com.raytheon.uf.common.serialization.SerializationException;

/**
 * Serialization Adapter for {@link TransmitterGroup}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 6, 2014    3173     mpduff      Initial creation
 * Aug 24, 2014   3432     mpduff      Add min/max db values
 * Sep 4, 2014    3532     bkowal      Replace min/max db with a target
 * Oct 13, 2014 3654       rjpeter     Updated to use ProgramSummary.
 * Oct 23, 2014 3617       dgilling    Updated for single time zone field.
 * Jan 08, 2015   3821     bsteffen    Rename silenceAlarm to deadAirAlarm
 * Jul 08, 2015   4636     bkowal      Encode/decode additional decibel levels.
 * Jul 13, 2015 4636       bkowal      Support separate 2.4K and 1.8K transfer tone types.
 * Nov 04, 2015 5068       rjpeter     Switch audio units from dB to amplitude.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterGroupAdapter implements
        ISerializationTypeAdapter<TransmitterGroup> {

    @Override
    public void serialize(ISerializationContext serializer,
            TransmitterGroup group) throws SerializationException {
        serializeNoTransmitter(serializer, group);
        Set<Transmitter> transmitters = group.getTransmitters();
        if (transmitters != null) {
            serializer.writeI32(transmitters.size());
            for (Transmitter t : transmitters) {
                TransmitterAdapter.serializeNoGroup(serializer, t);
            }
        } else {
            serializer.writeI32(0);
        }
    }

    @Override
    public TransmitterGroup deserialize(IDeserializationContext deserializer)
            throws SerializationException {
        TransmitterGroup tg = deserializeNoTransmitter(deserializer);
        int size = deserializer.readI32();
        if (size > 0) {
            Set<Transmitter> transmitters = new HashSet<>(size, 1);

            for (int i = 0; i < size; i++) {
                Transmitter t = TransmitterAdapter
                        .deserializeNoGroup(deserializer);
                transmitters.add(t);
            }

            tg.setTransmitters(transmitters);
        }

        return tg;
    }

    /**
     * Serialize with no Transmitter information
     * 
     * @param serializer
     *            The serializer
     * @param group
     *            The TransmitterGroup to serialize
     * @throws SerializationException
     */
    public static void serializeNoTransmitter(ISerializationContext serializer,
            TransmitterGroup group) throws SerializationException {
        serializer.writeObject(group.getDac());
        serializer.writeI32(group.getId());
        serializer.writeString(group.getName());
        serializer.writeObject(group.getPosition());
        serializer.writeBool(group.getDeadAirAlarm());
        serializer.writeObject(group.getTimeZone());
        serializer.writeI16(group.getAudioAmplitude());
        serializer.writeI16(group.getSameAmplitude());
        serializer.writeI16(group.getAlertAmplitude());
        serializer.writeI16(group.getTransferLowAmplitude());
        serializer.writeI16(group.getTransferHighAmplitude());
        serializer.writeObject(group.getProgramSummary());
    }

    /**
     * Deserialize with no Transmitter information
     * 
     * @param deserializer
     *            The deserializer
     * @return The TransmitterGroup object
     * @throws SerializationException
     */
    public static TransmitterGroup deserializeNoTransmitter(
            IDeserializationContext deserializer) throws SerializationException {
        TransmitterGroup tg = new TransmitterGroup();
        tg.setDac((Integer) deserializer.readObject());
        tg.setId(deserializer.readI32());
        tg.setName(deserializer.readString());
        tg.setPosition((int) deserializer.readObject());
        tg.setDeadAirAlarm(deserializer.readBool());
        tg.setTimeZone((String) deserializer.readObject());
        tg.setAudioAmplitude(deserializer.readI16());
        tg.setSameAmplitude(deserializer.readI16());
        tg.setAlertAmplitude(deserializer.readI16());
        tg.setTransferLowAmplitude(deserializer.readI16());
        tg.setTransferHighAmplitude(deserializer.readI16());
        tg.setProgramSummary((ProgramSummary) deserializer.readObject());

        return tg;
    }
}
