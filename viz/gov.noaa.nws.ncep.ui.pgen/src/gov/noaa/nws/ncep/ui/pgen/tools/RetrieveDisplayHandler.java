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
package gov.noaa.nws.ncep.ui.pgen.tools;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.ui.tools.AbstractTool;

import gov.noaa.nws.ncep.ui.pgen.PgenSession;
import gov.noaa.nws.ncep.ui.pgen.controls.RetrieveActivityDialog;

/**
 * Define a handler that retrieves PGEN activities without the Palette.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Dec 18, 2017 7180        njensen     Initial Creation
 * 
 * </pre>
 * 
 */
public class RetrieveDisplayHandler extends AbstractTool {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(RetrieveDisplayHandler.class);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        String btnName = "Open";
        PgenSession.getInstance().getPgenResource().setEditable(false);

        try {
            RetrieveActivityDialog retrieveDlg = new RetrieveActivityDialog(
                    shell, btnName);
            retrieveDlg.setBlockOnOpen(true);
            retrieveDlg.open();
        } catch (VizException e) {
            statusHandler.error("Error opening Retrieve Activity Dialog", e);
        }

        return null;
    }

}