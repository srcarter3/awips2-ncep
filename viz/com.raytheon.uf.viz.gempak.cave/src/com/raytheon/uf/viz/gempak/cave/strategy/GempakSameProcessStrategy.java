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
package com.raytheon.uf.viz.gempak.cave.strategy;

import com.raytheon.uf.common.status.IPerformanceStatusHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.PerformanceStatus;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.gempak.cave.data.retriever.GempakCaveDataURIRetriever;
import com.raytheon.uf.viz.gempak.cave.data.retriever.GempakCaveDbDataRetriever;
import com.raytheon.uf.viz.gempak.common.Dgdriv;
import com.raytheon.uf.viz.gempak.common.data.GempakDataInput;
import com.raytheon.uf.viz.gempak.common.data.GempakDataRecord;
import com.raytheon.uf.viz.gempak.common.exception.DgdrivException;
import com.raytheon.uf.viz.ncep.grid.FloatGridData;

/**
 * GEMPAK processing strategy that performs all data processing one at a time in
 * the current process (i.e. CAVE).
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 04, 2018 54480      mapeters    Initial creation
 * Sep 26, 2018 54483      mapeters    Pass data URI and DB data retrievers to
 *                                     Dgdriv
 * Oct 23, 2018 54476      tjensen     Change cache to singleton
 *
 * </pre>
 *
 * @author mapeters
 */
public class GempakSameProcessStrategy implements IGempakProcessingStrategy {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GempakSameProcessStrategy.class);

    private static final IPerformanceStatusHandler perfLog = PerformanceStatus
            .getHandler(GempakSameProcessStrategy.class.getSimpleName() + ":");

    private static final Object LOCK = new Object();

    @Override
    public GempakDataRecord getDataRecord(GempakDataInput dataInput) {
        synchronized (LOCK) {
            long t0 = System.currentTimeMillis();
            GempakDataRecord data = null;
            Dgdriv dgdriv = new Dgdriv(dataInput,
                    new GempakCaveDataURIRetriever(),
                    new GempakCaveDbDataRetriever());
            try {
                FloatGridData floatData = dgdriv.execute();
                if (floatData != null) {
                    data = new GempakDataRecord(floatData,
                            dgdriv.getSubgSpatialObj());
                }
            } catch (DgdrivException e) {
                statusHandler.error("Error performing GEMPAK data processing",
                        e);
            }
            long t1 = System.currentTimeMillis();
            perfLog.logDuration("Performing GEMPAK data processing in CAVE",
                    t1 - t0);
            return data;
        }
    }
}
