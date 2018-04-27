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
package com.raytheon.uf.common.bmh.tones;

/**
 * Exception indicating that tone generation has failed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 17, 2014 3383       bkowal      Initial creation
 * Oct 17, 2014 3655       bkowal      Move tones to common.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ToneGenerationException extends Exception {

    private static final long serialVersionUID = -3469735517725095080L;

    /**
     * @param message
     */
    public ToneGenerationException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ToneGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
