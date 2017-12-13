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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Time zone "enum" that encapsulates all the necessary information about time
 * zones for BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 27, 2014  #3617     dgilling     Initial creation
 * Oct 28, 2014  #3750     bkowal       Added method to get short timezone name
 * Nov 3, 2014   #3759     bkowal       Added a second get short timezone name method
 *                                      that allows for dst specification.
 * Mar 27, 2015  #4314     bkowal       Added {@link #getLongDisplayName()} and
 *                                      {@link #getLongDisplayName(boolean)}.
 * May 05, 2015  #4465     bkowal       Override display names of non-dst time zones.
 * Jun 22, 2015  #4481     bkowal       Added {@link #displayIdOverride}.
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public enum BMHTimeZone {

    UTC(TimeZone.getTimeZone("UTC"), "UNIVERSAL COORDINATED TIME", 0), ATLANTIC(
            TimeZone.getTimeZone("Canada/Atlantic"), "ATLANTIC", 2), ATLANTIC_NO_DST(
            TimeZone.getTimeZone("America/Puerto_Rico"), "ATLANTIC", 1,
            "Atlantic Standard Time", null), EASTERN(TimeZone
            .getTimeZone("US/Eastern"), "EASTERN", 4), EASTERN_NO_DST(TimeZone
            .getTimeZone("GMT-5"), "EASTERN", 3, "Eastern Standard Time",
            "US/Eastern"), CENTRAL(TimeZone.getTimeZone("US/Central"),
            "CENTRAL", 6), CENTRAL_NO_DST(TimeZone.getTimeZone("GMT-6"),
            "CENTRAL", 5, "Central Standard Time", "US/Central"), MOUNTAIN(
            TimeZone.getTimeZone("US/Mountain"), "MOUNTAIN", 8), MOUNTAIN_NO_DST(
            TimeZone.getTimeZone("GMT-7"), "MOUNTAIN", 7,
            "Mountain Standard Time", "US/Mountain"), PACIFIC(TimeZone
            .getTimeZone("US/Pacific"), "PACIFIC", 10), PACIFIC_NO_DST(TimeZone
            .getTimeZone("GMT-8"), "PACIFIC", 9, "Pacific Standard Time",
            "US/Pacific"), ALASKA(TimeZone.getTimeZone("US/Alaska"), "ALASKA",
            12), ALASKA_NO_DST(TimeZone.getTimeZone("GMT-9"), "ALASKA", 11,
            "Alaska Standard Time", "US/Alaska"), ALEUTIAN(TimeZone
            .getTimeZone("US/Aleutian"), "HAWAII-ALEUTIAN", 13), ALEUTIAN_NO_DST(
            TimeZone.getTimeZone("US/Hawaii"), "HAWAII-ALEUTIAN", -1,
            "Hawaii-Aleutian Standard Time", null), GUAM(TimeZone
            .getTimeZone("Pacific/Guam"), "GUAM", 14), SOMOA(TimeZone
            .getTimeZone("US/Samoa"), "SOMOA", -1);

    private static final Set<String> NO_DST_ZONES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList("GUAM", "SOMOA",
                    "UNIVERSAL COORDINATED TIME")));

    private final TimeZone tz;

    private final String uiName;

    private final int legacyTzCode;

    private final String displayOverride;

    private final String displayIdOverride;

    private BMHTimeZone(TimeZone tz, String uiName, int legacyTzCode) {
        this(tz, uiName, legacyTzCode, null, null);
    }

    private BMHTimeZone(TimeZone tz, String uiName, int legacyTzCode,
            String displayOverride, String displayIdOverride) {
        this.tz = tz;
        this.uiName = uiName;
        this.legacyTzCode = legacyTzCode;
        this.displayOverride = displayOverride;
        this.displayIdOverride = displayIdOverride;
    }

    public TimeZone getTz() {
        return tz;
    }

    public String getDisplayId() {
        return (this.displayIdOverride == null) ? this.tz.getID()
                : this.displayIdOverride;
    }

    public String getShortDisplayName() {
        return this.getShortDisplayName(this.tz.inDaylightTime(Calendar
                .getInstance().getTime()));
    }

    public String getShortDisplayName(boolean daylight) {
        return this.tz.getDisplayName(daylight, TimeZone.SHORT);
    }

    public String getLongDisplayName() {
        return this.getLongDisplayName(this.tz.inDaylightTime(Calendar
                .getInstance().getTime()));
    }

    public String getLongDisplayName(boolean daylight) {
        return (this.displayOverride == null) ? this.tz.getDisplayName(
                daylight, TimeZone.LONG) : this.displayOverride;
    }

    public static BMHTimeZone getTimeZoneByID(final String id) {
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            if (tz.getTz().getID().equals(id)) {
                return tz;
            }
        }

        throw new IllegalArgumentException("No BMHTimeZone enum value for id: "
                + id);
    }

    public String getUiName() {
        return uiName;
    }

    public int getLegacyTzCode() {
        return legacyTzCode;
    }

    /**
     * Get the list of unique selections for time zones for the UI.
     * 
     * @return The set of unique UI time zone names returned as a String array.
     */
    public static String[] getUISelections() {
        LinkedHashSet<String> uniqueValues = new LinkedHashSet<>();
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            uniqueValues.add(tz.getUiName());
        }

        return uniqueValues.toArray(new String[uniqueValues.size()]);
    }

    /**
     * Takes the UI selections of a time zone name and whether or not that time
     * zone observes daylight savings time and returns the {@code TimeZone}
     * instance that corresponds to those selections.
     * 
     * @param uiName
     *            The UI name of the time zone desired.
     * @param observesDST
     *            Whether or not this time zone observes DST.
     * @return the {@code TimeZone} instance that corresponds to the specified
     *         UI selections.
     */
    public static TimeZone getTimeZoneFromUI(String uiName, boolean observesDST) {
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            if ((uiName.equalsIgnoreCase(tz.getUiName()))
                    && (observesDST == tz.getTz().observesDaylightTime())) {
                return tz.getTz();
            }
        }

        throw new IllegalArgumentException(
                "No BMHTimeZone enum value for uiName: " + uiName
                        + ", observesDST: " + observesDST);
    }

    /**
     * Returns the corresponding {@code TimeZone} based on the specified legacy
     * system time zone code.
     * 
     * @param legacyCode
     *            The legacy time zone code from the CRS configuration.
     * @return The {@code TimeZone} instance that maps to the specified code
     *         value.
     */
    public static TimeZone getLegacyTimeZone(int legacyCode) {
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            if (legacyCode == tz.getLegacyTzCode()) {
                return tz.getTz();
            }
        }

        throw new IllegalArgumentException(
                "No BMHTimeZone enum value for legacyCode: " + legacyCode);
    }

    /**
     * Retrieves the proper {@code TimeZone} instance based on the specified
     * time zone and whether that time zone should observe daylight savings
     * time.
     * 
     * @param baseTZ
     *            The "base" time zone. Uses the raw offset to help map to the
     *            proper time zone.
     * @param observesDST
     *            Whether or not the time zone observes DST.
     * @return The {@code TimeZone} that corresponds to the specified
     *         parameters.
     */
    public static TimeZone getTimeZone(TimeZone baseTZ, boolean observesDST) {
        for (BMHTimeZone tz : BMHTimeZone.values()) {
            if ((baseTZ.getRawOffset() == tz.getTz().getRawOffset())
                    && (observesDST == tz.getTz().observesDaylightTime())) {
                return tz.getTz();
            }
        }

        throw new IllegalArgumentException(
                "No BMHTimeZone enum value for baseTZ: " + baseTZ.getID()
                        + ", observesDST: " + observesDST);
    }

    /**
     * Returns the UI name for the specified {@code TimeZone}.
     * 
     * @param tz
     *            The {@code TimeZone} to retrieve the UI name for.
     * @return The zone's UI name. Multiple time zones may have the same UI
     *         name.
     */
    public static String getTimeZoneUIName(TimeZone tz) {
        for (BMHTimeZone zone : BMHTimeZone.values()) {
            if (tz.equals(zone.getTz())) {
                return zone.getUiName();
            }
        }

        throw new IllegalArgumentException(
                "No BMHTimeZone enum value for time zone: " + tz.getID());
    }

    /**
     * Returns whether or not the specified UI time zone name is forced to
     * always disable daylight savings time.
     * 
     * @param uiTzName
     *            The UI name of the time zone.
     * @return True, if this time zone never observes DST. False, if it can.
     */
    public static boolean isForcedNoDst(String uiTzName) {
        return NO_DST_ZONES.contains(uiTzName);
    }
}
