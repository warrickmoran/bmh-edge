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
package com.raytheon.uf.common.bmh.audio.impl.algorithm;

/**
 * Modernized version of:
 * http://thorntonzone.com/manuals/Compression/Fax,%20IBM%20MMR/MMSC/mmsc
 * /uk/co/mmscomputing/sound/ (less C++-like).
 * 
 * Note: this version only supports ulaw and pcm; excluding alaw because it is
 * not used within BMH.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 9, 2015  4365       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class UlawToPCMAlgorithm {

    private static final int[] ulawtable = { 0x8482, 0x8486, 0x848a, 0x848e,
            0x8492, 0x8496, 0x849a, 0x849e, 0x84a2, 0x84a6, 0x84aa, 0x84ae,
            0x84b2, 0x84b6, 0x84ba, 0x84be, 0x84c1, 0x84c3, 0x84c5, 0x84c7,
            0x84c9, 0x84cb, 0x84cd, 0x84cf, 0x84d1, 0x84d3, 0x84d5, 0x84d7,
            0x84d9, 0x84db, 0x84dd, 0x84df, 0x04e1, 0x04e2, 0x04e3, 0x04e4,
            0x04e5, 0x04e6, 0x04e7, 0x04e8, 0x04e9, 0x04ea, 0x04eb, 0x04ec,
            0x04ed, 0x04ee, 0x04ef, 0x04f0, 0xc4f0, 0x44f1, 0xc4f1, 0x44f2,
            0xc4f2, 0x44f3, 0xc4f3, 0x44f4, 0xc4f4, 0x44f5, 0xc4f5, 0x44f6,
            0xc4f6, 0x44f7, 0xc4f7, 0x44f8, 0xa4f8, 0xe4f8, 0x24f9, 0x64f9,
            0xa4f9, 0xe4f9, 0x24fa, 0x64fa, 0xa4fa, 0xe4fa, 0x24fb, 0x64fb,
            0xa4fb, 0xe4fb, 0x24fc, 0x64fc, 0x94fc, 0xb4fc, 0xd4fc, 0xf4fc,
            0x14fd, 0x34fd, 0x54fd, 0x74fd, 0x94fd, 0xb4fd, 0xd4fd, 0xf4fd,
            0x14fe, 0x34fe, 0x54fe, 0x74fe, 0x8cfe, 0x9cfe, 0xacfe, 0xbcfe,
            0xccfe, 0xdcfe, 0xecfe, 0xfcfe, 0x0cff, 0x1cff, 0x2cff, 0x3cff,
            0x4cff, 0x5cff, 0x6cff, 0x7cff, 0x88ff, 0x90ff, 0x98ff, 0xa0ff,
            0xa8ff, 0xb0ff, 0xb8ff, 0xc0ff, 0xc8ff, 0xd0ff, 0xd8ff, 0xe0ff,
            0xe8ff, 0xf0ff, 0xf8ff, 0x0000, 0x7c7d, 0x7c79, 0x7c75, 0x7c71,
            0x7c6d, 0x7c69, 0x7c65, 0x7c61, 0x7c5d, 0x7c59, 0x7c55, 0x7c51,
            0x7c4d, 0x7c49, 0x7c45, 0x7c41, 0x7c3e, 0x7c3c, 0x7c3a, 0x7c38,
            0x7c36, 0x7c34, 0x7c32, 0x7c30, 0x7c2e, 0x7c2c, 0x7c2a, 0x7c28,
            0x7c26, 0x7c24, 0x7c22, 0x7c20, 0xfc1e, 0xfc1d, 0xfc1c, 0xfc1b,
            0xfc1a, 0xfc19, 0xfc18, 0xfc17, 0xfc16, 0xfc15, 0xfc14, 0xfc13,
            0xfc12, 0xfc11, 0xfc10, 0xfc0f, 0x3c0f, 0xbc0e, 0x3c0e, 0xbc0d,
            0x3c0d, 0xbc0c, 0x3c0c, 0xbc0b, 0x3c0b, 0xbc0a, 0x3c0a, 0xbc09,
            0x3c09, 0xbc08, 0x3c08, 0xbc07, 0x5c07, 0x1c07, 0xdc06, 0x9c06,
            0x5c06, 0x1c06, 0xdc05, 0x9c05, 0x5c05, 0x1c05, 0xdc04, 0x9c04,
            0x5c04, 0x1c04, 0xdc03, 0x9c03, 0x6c03, 0x4c03, 0x2c03, 0x0c03,
            0xec02, 0xcc02, 0xac02, 0x8c02, 0x6c02, 0x4c02, 0x2c02, 0x0c02,
            0xec01, 0xcc01, 0xac01, 0x8c01, 0x7401, 0x6401, 0x5401, 0x4401,
            0x3401, 0x2401, 0x1401, 0x0401, 0xf400, 0xe400, 0xd400, 0xc400,
            0xb400, 0xa400, 0x9400, 0x8400, 0x7800, 0x7000, 0x6800, 0x6000,
            0x5800, 0x5000, 0x4800, 0x4000, 0x3800, 0x3000, 0x2800, 0x2000,
            0x1800, 0x1000, 0x0800, 0x0000, };

    public static byte[] convert(final byte[] src) {
        if (src == null || src.length <= 0) {
            throw new IllegalArgumentException(
                    "Required argument src has not been provided.");
        }

        byte[] convertedAudio = new byte[src.length << 1];
        convert(src, convertedAudio);

        return convertedAudio;
    }

    public static void convert(final byte[] src, byte[] destination) {
        convert(src, 0, src.length, destination, 0);
    }

    public static void convert(final byte[] src, int srcOffset, int srcLength,
            byte[] destination) {
        convert(src, srcOffset, srcLength, destination, 0);
    }

    public static void convert(final byte[] src, byte[] destination,
            int dstOffset) {
        convert(src, 0, src.length, destination, dstOffset);
    }

    public static void convert(final byte[] src, int srcOffset, int srcLength,
            byte[] destination, int dstOffset) {
        if (src == null || src.length <= 0) {
            throw new IllegalArgumentException(
                    "Required argument src has not been provided.");
        }
        if (destination == null || destination.length <= 0) {
            throw new IllegalArgumentException(
                    "Required argument destination has not been provided.");
        }
        if (dstOffset < 0) {
            throw new IllegalArgumentException(
                    "Required argument dstOffset must be > 0.");
        }

        /*
         * ensure that there is enough room in the destination array.
         */
        int allowedLength = destination.length - dstOffset;
        int requiredLength = (srcLength - srcOffset) << 1;
        if (allowedLength < requiredLength) {
            StringBuilder sb = new StringBuilder(
                    "The provided destination array beginning at offset ");
            sb.append(dstOffset)
                    .append(" is not large enough to hold the result of the conversion.");

            throw new IllegalStateException(sb.toString());
        }

        for (int i = srcOffset; i < (srcOffset + srcLength); i++) {
            int value = ulawtable[src[i] & 0x00FF];
            destination[dstOffset++] = (byte) ((value >> 8) & 0x00FF); // little-endian
            destination[dstOffset++] = (byte) (value & 0x00FF);
        }
    }
}