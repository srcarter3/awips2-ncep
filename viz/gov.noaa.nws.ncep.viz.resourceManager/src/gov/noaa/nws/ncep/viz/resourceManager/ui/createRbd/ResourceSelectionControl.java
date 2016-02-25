package gov.noaa.nws.ncep.viz.resourceManager.ui.createRbd;

import gov.noaa.nws.ncep.common.dataplugin.mcidas.McidasConstants;
import gov.noaa.nws.ncep.viz.common.display.NcDisplayType;
import gov.noaa.nws.ncep.viz.common.preferences.NcepGeneralPreferencesPage;
import gov.noaa.nws.ncep.viz.common.ui.NmapCommon;
import gov.noaa.nws.ncep.viz.gempak.util.GempakGrid;
import gov.noaa.nws.ncep.viz.resources.AbstractNatlCntrsRequestableResourceData.DayReference;
import gov.noaa.nws.ncep.viz.resources.AbstractNatlCntrsRequestableResourceData.TimelineGenMethod;
import gov.noaa.nws.ncep.viz.resources.manager.AttrSetLabelsManager;
import gov.noaa.nws.ncep.viz.resources.manager.AttributeSet;
import gov.noaa.nws.ncep.viz.resources.manager.LocalRadarStationManager;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceCategory;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceDefinition;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceDefnsMngr;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceName;
import gov.noaa.nws.ncep.viz.resources.manager.SatelliteImageTypeManager;
import gov.noaa.nws.ncep.viz.ui.display.NcDisplayMngr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * Data Selection dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 01/26/10      #226        Greg Hull   Broke out from RscBndlDefnDialog
 * 04/05/10      #226        Greg Hull   Add back PGEN selection
 * 06/18/10      #273        Greg Hull   Rework for new ResourceCnfgMngr
 * 09/13/10      #307        Greg Hull   implement cycle times.
 * 09/28/10      #307        Greg Hull   save the fcst/observed mode when re-initing
 * 10/01/10      #298        B. Hebbard  handle MOS resources in updateCycleTimes()
 * 10/20/10                  X. Guo      Rename getCycleTimeStringFromDataTime to getTimeStringFromDataTime
 * 10/20/10      #277        M. Li       get model name for ensemble
 * 11/18/10       277        M. Li       get correct cycle for ensemble
 * 11/29/10                 mgamazaychikov  Changed updateCycleTime method for GEMPAK data source
 * 02/28/11      #408        Greg Hull   Replace Forecast/Observed with Filter combo
 * 04/18/11                  Greg Hull   caller sets name of the 'select' button
 * 06/07/11       #445       Xilin Guo   Data Manager Performance Improvements
 * 09/20/2011               mgamazaychikov  Made changes associated with removal of DatatypeTable class
 * 12/22/2011     #578       Greg Hull   Ensemble selection
 * 01/06/2012                S. Gurung   Add/display cycle times at 00Z only for nctaf
 * 01/10/2012                S. Gurung   Changed resource parameter name plugin to pluginName in updateCycleTimes()
 * 01/31/2012     #606       Greg Hull   Get Cycle Times, Types & Sub-Types from inventory
 * 04/08/2012     #606       Greg Hull   Don't allow selection for data that is not available
 * 04/25/2012     #606       Greg Hull   allow disabling the inventory, add Check Availability button
 * 06/06/2012     #816       Greg Hull   Alphabetize lists. Change content of listViewer to ResourceDefinitions
 * 08/26/2012     #          Greg Hull   allow for disabling resources
 * 08/29/2012     #860       Greg Hull   show latest time with attr sets
 * 12/17/2012     #957       Greg Hull   change content of attrSetListViewer from String to to AttributeSet 
 * 02/22/2013     #972       G. Hull     Only show resources for given NcDisplayType
 * 04/11/2013     #864       G. Hull     rm special case for taf and use USE_FCST_FRAME_INTERVAL_FROM_REF_TIME
 * 04/15/2013     #864       G. Hull     attach LViewers to positions and save previous width
 * 10/24/2013     #1043      G. Hull     init Select Resource GUI to highlighted rsc
 * 07/23/2014       ?        B. Yin      Handle grid analysis 
 * 07/23/2014       ?        B. Hebbard  Make extensible for NTRANS-specific subclass
 * 05/18/2015     R8048     P. Chowdhuri "Select New Resource" dialog should remember last selection
 * 05/18/2015     R7656      A. Su       Displayed the aliases of local radar stations in the menu.
 * 06/10/2015     R7656      A. Su       Rewrote the displaying logic for LocalRadar for clarity.
 * 10/15/2015     R7190      R. Reynolds Display subTypeGenerator and attributes mods.
 * 10/30/2015     R8824      A. Su       Added display aliases for Grid resource attributes.
 * 11/03/2015     R8554     P. Chowdhuri Filter was set to last filter used per data category
 * 12/03/2015     R12953    R. Reynolds  Added Mcidas constants
 * 12/17/2015     R8554     A. Su        Modified to remember last selected Resource and filter per RBD type.
 * 01/25/2016     R14142    RCReynolds   Moved mcidas related string construction out to ResourceDefinition
 * </pre>
 * 
 * @author ghull
 * @version 1
 */
public class ResourceSelectionControl extends Composite {

    protected static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ResourceSelectionControl.class);

    // All GRID resource attributes are specified in this directory.
    public static final String GRID_DATA = "ModelFcstGridContours";

    public static final String NATL_MOSAIC_DISPLAY_NAME = "NatlMosaic";

    public static final String LOCAL_RADAR_PREFIX = "LocalRadar:";

    public static final String RADAR_CATEGORY = "RADAR";

    public static final String RSC_FILTER_ALL = "All";

    public static final String GMT = "GMT";

    public static final String GDFILE = "GDFILE";

    public static final String DEFAULT = "default";

    public static final String STANDARD = "standard";

    public static final String OK = "OK";

    protected NcDisplayType displayType = null;

    protected ResourceName prevSelectedRscName = null;

    /**
     * A mapping from NcDisplayType to last selected ResourceName for this type.
     */
    protected static HashMap<NcDisplayType, ResourceName> prevDisplayType2RscName = new HashMap<NcDisplayType, ResourceName>();;

    /**
     * A mapping from Category to last selected filter for this category.
     */
    protected static HashMap<ResourceCategory, String> prevCat2SelectedFilter = new HashMap<ResourceCategory, String>();;

    /**
     * A mapping from Category to last selected ResourceName for this category.
     */
    protected static HashMap<ResourceCategory, ResourceName> prevCat2SelectedRscName = new HashMap<ResourceCategory, ResourceName>();

    /**
     * A Mcidas-specific mapping
     */
    protected static HashMap<String, String> mcidasAttrNamesNAliases = new HashMap<String, String>();

    private static ResourceCategory prevSelectedCat = ResourceCategory.NullCategory;

    protected ResourceDefnsMngr rscDefnsMngr;

    protected ResourceName selectedRscName = null;

    protected String selectedFilterString = "";

    // this list must stay in sync with the cycleTimeCombo.
    protected ArrayList<DataTime> cycleTimes = new ArrayList<DataTime>();

    protected Combo filterCombo = null;

    protected Composite sel_rsc_comp = null;

    protected Text seldRscNameTxt = null;

    protected Label availDataTimeLbl = null;

    protected Label cycleTimeLbl = null;

    protected Combo cycleTimeCombo = null;

    // For now only one of following two will be visible but we may want to
    // allow both later (and remove the Modify button from the Create RBD tab)
    protected Button addResourceBtn = null;

    protected Button replaceResourceBtn = null;

    protected Boolean replaceBtnVisible;

    protected Boolean replaceBtnEnabled;

    protected Button addToAllPanesBtn = null;

    protected Label rscTypeLbl = null;

    private Label rscTypeGroupLbl = null;

    protected ListViewer rscCatLViewer = null;

    protected ListViewer rscTypeLViewer = null;

    private ListViewer rscGroupLViewer = null;

    protected ListViewer rscAttrSetLViewer = null;

    protected static final int RSC_LIST_VIEWER_HEIGHT = 220;

    protected static Rectangle prevShellBounds = new Rectangle(0, 0, 800, 460);

    protected Boolean showLatestTimes = false;

    protected Boolean onlyShowResourcesWithData = false;

    // Used in justifying the times in the attrSetsList.
    protected Integer maxLengthOfSelectableAttrSets = 0;

    public interface IResourceSelectedListener {
        public void resourceSelected(ResourceName rscName, boolean replace,
                boolean addAllPanes, boolean done);
    }

    private Set<IResourceSelectedListener> rscSelListeners = new HashSet<IResourceSelectedListener>();

    public ResourceSelectionControl(Composite parent, Boolean replaceVisible,
            Boolean replaceEnabled, ResourceName initRscName,
            Boolean multiPane, NcDisplayType dispType) throws VizException {
        super(parent, SWT.SHADOW_NONE);

        displayType = dispType;
        prevSelectedRscName = prevDisplayType2RscName.get(displayType);

        showLatestTimes = NmapCommon.getNcepPreferenceStore().getBoolean(
                NcepGeneralPreferencesPage.ShowLatestResourceTimes);
        onlyShowResourcesWithData = false;

        rscDefnsMngr = ResourceDefnsMngr.getInstance();

        replaceBtnVisible = replaceVisible;
        replaceBtnEnabled = replaceEnabled;

        sel_rsc_comp = this;

        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        gd.verticalAlignment = SWT.FILL;
        gd.widthHint = prevShellBounds.width;
        gd.heightHint = prevShellBounds.height;
        sel_rsc_comp.setLayoutData(gd);

        sel_rsc_comp.setLayout(new FormLayout());

        sel_rsc_comp.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                prevShellBounds = sel_rsc_comp.getBounds();
            }
        });

        createSelectResourceGroup(multiPane);

        setContentProviders();
        addSelectionListeners();

        initWidgets(initRscName);
    }

    /**
     * This is a pass-thru constructor, so NtransSelectionControl can call the
     * grandparent constructor, but do its own version of the useful work of the
     * main constructor above.
     * 
     * @param Composite
     *            parent
     * 
     *            Discussion: Better solution might be to do a refactor "pull"
     *            AbstractResourceSelectionControl superclass out of
     *            ResourceSelectionControl and have NtransSelectionControl
     *            extend the former. But that could disturb existing
     *            ResourceSelectionControl, which is working and tested with
     *            non-NTRANS resources.
     */
    public ResourceSelectionControl(Composite parent) throws VizException {
        super(parent, SWT.SHADOW_NONE);
    }

    /*
     * create all the widgets in the Resource Selection (top) section of the
     * sashForm.
     */
    private void createSelectResourceGroup(Boolean multiPane) {

        rscCatLViewer = new ListViewer(sel_rsc_comp, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL);
        FormData fd = new FormData();
        fd.height = RSC_LIST_VIEWER_HEIGHT;
        fd.top = new FormAttachment(0, 75);
        fd.left = new FormAttachment(0, 10);
        fd.right = new FormAttachment(0, 110);

        // This allows a resize to change the size of the lists.
        fd.bottom = new FormAttachment(100, -125);
        rscCatLViewer.getList().setLayoutData(fd);

        Label rscCatLbl = new Label(sel_rsc_comp, SWT.NONE);
        rscCatLbl.setText("Category");
        fd = new FormData();
        fd.left = new FormAttachment(rscCatLViewer.getList(), 0, SWT.LEFT);
        fd.bottom = new FormAttachment(rscCatLViewer.getList(), -3, SWT.TOP);
        rscCatLbl.setLayoutData(fd);

        // first create the lists and then attach the label to the top of them
        rscTypeLViewer = new ListViewer(sel_rsc_comp, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL);
        fd = new FormData();
        fd.height = RSC_LIST_VIEWER_HEIGHT;
        fd.top = new FormAttachment(rscCatLViewer.getList(), 0, SWT.TOP);
        fd.left = new FormAttachment(rscCatLViewer.getList(), 8, SWT.RIGHT);
        fd.right = new FormAttachment(37, 0);

        fd.bottom = new FormAttachment(rscCatLViewer.getList(), 0, SWT.BOTTOM);
        rscTypeLViewer.getList().setLayoutData(fd);

        rscTypeLbl = new Label(sel_rsc_comp, SWT.NONE);
        rscTypeLbl.setText("Resource Type");
        fd = new FormData();
        fd.left = new FormAttachment(rscTypeLViewer.getList(), 0, SWT.LEFT);
        fd.bottom = new FormAttachment(rscTypeLViewer.getList(), -3, SWT.TOP);

        rscTypeLbl.setLayoutData(fd);

        filterCombo = new Combo(sel_rsc_comp, SWT.DROP_DOWN | SWT.READ_ONLY);
        fd = new FormData();
        fd.width = 130;
        fd.bottom = new FormAttachment(rscTypeLViewer.getList(), -30, SWT.TOP);
        fd.left = new FormAttachment(rscTypeLViewer.getList(), 0, SWT.LEFT);
        filterCombo.setLayoutData(fd);

        Label filt_lbl = new Label(sel_rsc_comp, SWT.NONE);
        filt_lbl.setText("Type Filter:");
        fd = new FormData();
        fd.left = new FormAttachment(filterCombo, 0, SWT.LEFT);
        fd.bottom = new FormAttachment(filterCombo, -3, SWT.TOP);
        filt_lbl.setLayoutData(fd);

        // first create the lists and then attach the label to the top of them
        rscGroupLViewer = new ListViewer(sel_rsc_comp, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL);
        fd = new FormData();
        fd.height = RSC_LIST_VIEWER_HEIGHT;
        fd.top = new FormAttachment(rscTypeLViewer.getList(), 0, SWT.TOP);
        fd.left = new FormAttachment(rscTypeLViewer.getList(), 8, SWT.RIGHT);
        fd.right = new FormAttachment(62, 0);

        fd.bottom = new FormAttachment(rscTypeLViewer.getList(), 0, SWT.BOTTOM);
        rscGroupLViewer.getList().setLayoutData(fd);

        rscTypeGroupLbl = new Label(sel_rsc_comp, SWT.NONE);
        rscTypeGroupLbl.setText("Resource Group");
        fd = new FormData();
        fd.left = new FormAttachment(rscGroupLViewer.getList(), 0, SWT.LEFT);
        fd.bottom = new FormAttachment(rscGroupLViewer.getList(), -3, SWT.TOP);
        rscTypeGroupLbl.setLayoutData(fd);

        rscAttrSetLViewer = new ListViewer(sel_rsc_comp, SWT.SINGLE
                | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fd = new FormData();
        fd.height = RSC_LIST_VIEWER_HEIGHT;
        fd.top = new FormAttachment(rscGroupLViewer.getList(), 0, SWT.TOP);
        fd.left = new FormAttachment(rscGroupLViewer.getList(), 8, SWT.RIGHT);
        fd.right = new FormAttachment(100, -10);
        fd.bottom = new FormAttachment(rscGroupLViewer.getList(), 0, SWT.BOTTOM);
        rscAttrSetLViewer.getList().setLayoutData(fd);

        Label rscAttrsLbl = new Label(sel_rsc_comp, SWT.NONE);
        rscAttrsLbl.setText("Resource Attributes");
        fd = new FormData();
        fd.left = new FormAttachment(rscAttrSetLViewer.getList(), 0, SWT.LEFT);
        fd.bottom = new FormAttachment(rscAttrSetLViewer.getList(), -3, SWT.TOP);
        rscAttrsLbl.setLayoutData(fd);

        availDataTimeLbl = new Label(sel_rsc_comp, SWT.None);
        availDataTimeLbl.setText("");
        fd = new FormData();
        fd.left = new FormAttachment(rscAttrSetLViewer.getList(), 0, SWT.LEFT);
        fd.top = new FormAttachment(rscAttrSetLViewer.getList(), 5, SWT.BOTTOM);
        fd.right = new FormAttachment(rscAttrSetLViewer.getList(), 0, SWT.RIGHT);
        availDataTimeLbl.setLayoutData(fd);

        seldRscNameTxt = new Text(sel_rsc_comp, SWT.SINGLE | SWT.BORDER
                | SWT.READ_ONLY);
        fd = new FormData();
        fd.top = new FormAttachment(rscCatLViewer.getList(), 40, SWT.BOTTOM);
        fd.left = new FormAttachment(rscCatLViewer.getList(), 0, SWT.LEFT);
        fd.right = new FormAttachment(75, 0);
        seldRscNameTxt.setLayoutData(fd);

        Label seld_rsc_name_lbl = new Label(sel_rsc_comp, SWT.None);
        seld_rsc_name_lbl.setText("Selected Resource Name");
        fd = new FormData();
        fd.left = new FormAttachment(seldRscNameTxt, 0, SWT.LEFT);
        fd.bottom = new FormAttachment(seldRscNameTxt, -3, SWT.TOP);
        seld_rsc_name_lbl.setLayoutData(fd);

        addResourceBtn = new Button(sel_rsc_comp, SWT.None);

        fd = new FormData();

        if (replaceBtnVisible) {
            fd.top = new FormAttachment(seldRscNameTxt, 20, SWT.BOTTOM);
            fd.right = new FormAttachment(50, -20);
        } else {
            fd.top = new FormAttachment(seldRscNameTxt, 20, SWT.BOTTOM);
            fd.left = new FormAttachment(50, 20);
        }
        addResourceBtn.setLayoutData(fd);
        addResourceBtn.setText("  Add Resource "); // Add To RBD

        replaceResourceBtn = new Button(sel_rsc_comp, SWT.None);
        fd = new FormData();
        fd.left = new FormAttachment(50, 20);
        fd.top = new FormAttachment(addResourceBtn, 0, SWT.TOP);
        replaceResourceBtn.setLayoutData(fd);
        replaceResourceBtn.setText(" Replace Resource ");

        // both for now unless we change it to be one or the other
        replaceResourceBtn.setVisible(replaceBtnVisible);

        addToAllPanesBtn = new Button(sel_rsc_comp, SWT.CHECK);
        fd = new FormData();
        fd.left = new FormAttachment(seldRscNameTxt, 40, SWT.RIGHT);
        fd.top = new FormAttachment(replaceResourceBtn, 0, SWT.TOP);
        addToAllPanesBtn.setLayoutData(fd);
        addToAllPanesBtn.setText("Add To All Panes");

        addToAllPanesBtn.setVisible(multiPane);

        // allow the user to enter any previous datatime
        cycleTimeCombo = new Combo(sel_rsc_comp, SWT.READ_ONLY);
        fd = new FormData();
        fd.left = new FormAttachment(80, 0);
        fd.right = new FormAttachment(100, -20);
        fd.top = new FormAttachment(seldRscNameTxt, 0, SWT.TOP);

        cycleTimeCombo.setLayoutData(fd);

        cycleTimeLbl = new Label(sel_rsc_comp, SWT.None);
        cycleTimeLbl.setText("Cycle Time");
        fd = new FormData();
        fd.left = new FormAttachment(cycleTimeCombo, 0, SWT.LEFT);
        fd.bottom = new FormAttachment(cycleTimeCombo, -3, SWT.TOP);
        cycleTimeLbl.setLayoutData(fd);
    }

    private void setContentProviders() {

        // input is the rscDefnsMngr and output is a list of categories based
        // on the forecast flag
        rscCatLViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {

                // don't show disabled definitions.
                return rscDefnsMngr.getResourceCategories(false,
                        new NcDisplayType[] { displayType });
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }
        });

        rscTypeLViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {

                if (selectedRscName.getRscCategory() != ResourceCategory.NullCategory) {
                    try {
                        String newFilterString = selectedFilterString;
                        if (newFilterString.equals(RSC_FILTER_ALL)) {
                            newFilterString = "";
                        }

                        // Include generated types; only include enabled types.
                        List<ResourceDefinition> rscTypes = rscDefnsMngr
                                .getResourceDefnsForCategory(
                                        selectedRscName.getRscCategory(),
                                        newFilterString, displayType, true,
                                        false);

                        return rscTypes.toArray();
                    } catch (VizException e) {
                        MessageDialog errDlg = new MessageDialog(NcDisplayMngr
                                .getCaveShell(), "Error", null,
                                "Error getting Resource Types\n"
                                        + e.getMessage(), MessageDialog.ERROR,
                                new String[] { OK }, 0);
                        errDlg.open();
                    }
                }
                return new ResourceDefinition[] {};
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }
        });

        rscTypeLViewer.setComparator(new ViewerComparator() {

            public int category(Object element) {
                ResourceDefinition rd = (ResourceDefinition) element;
                return (rd.isForecast() ? 1 : 0);
            }

            @Override
            public int compare(Viewer viewer, Object obj1, Object obj2) {
                int catComp = category(obj1) - category(obj2);
                return (catComp != 0 ? catComp : rscDefnsMngr
                        .getDefaultRscDefnComparator().compare(
                                (ResourceDefinition) obj1,
                                (ResourceDefinition) obj2));
            }
        });

        rscTypeLViewer.setLabelProvider(new LabelProvider() {

            public String getText(Object element) {
                if (element == null)
                    return "null";

                ResourceDefinition rd = (ResourceDefinition) element;
                String rdName = rd.getResourceDefnName();

                // Display aliases for LocalRadar.
                if (rd.getResourceCategory().equals(
                        ResourceCategory.RadarRscCategory)) {

                    String alias = LocalRadarStationManager.getInstance()
                            .getDisplayedName(rdName);

                    if (alias != null) {
                        rdName = alias;
                    }
                }

                return rdName;
            }
        });

        // Override the method "compare" in the class "ViewerSorter" to
        // properly sort the menu items (aliases) of local radar stations.
        rscTypeLViewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object obj1, Object obj2) {

                boolean isLocalRadar = (obj1 != null && ((ResourceDefinition) obj1)
                        .getResourceCategory().equals(
                                ResourceCategory.RadarRscCategory))
                        || (obj2 != null && ((ResourceDefinition) obj2)
                                .getResourceCategory().equals(
                                        ResourceCategory.RadarRscCategory));

                if (!isLocalRadar)
                    return super.compare(viewer, obj1, obj2);

                if (obj1 == null)
                    return 1;

                if (obj2 == null)
                    return -1;

                final String firstOnRadarMenu = NATL_MOSAIC_DISPLAY_NAME;
                String label1 = ((ResourceDefinition) obj1)
                        .getResourceDefnName();
                if (label1.equals(firstOnRadarMenu))
                    return -1;

                String label2 = ((ResourceDefinition) obj2)
                        .getResourceDefnName();
                if (label2.equals(firstOnRadarMenu))
                    return 1;

                String displayName1 = LocalRadarStationManager.getInstance()
                        .getDisplayedName(label1);
                if (displayName1 != null)
                    label1 = displayName1;

                String displayName2 = LocalRadarStationManager.getInstance()
                        .getDisplayedName(label2);
                if (displayName2 != null)
                    label2 = displayName2;

                return (label1.compareTo(label2));

            }
        });

        rscGroupLViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                String rscType = selectedRscName.getRscType();

                if (!rscType.isEmpty()) {
                    // if this resource uses attrSetGroups then get get the list
                    // of groups.
                    // (PGEN uses groups but we will list the subTypes
                    // (products) and not the single PGEN attr set group)
                    if (rscDefnsMngr.doesResourceUseAttrSetGroups(rscType)
                            && !selectedRscName.isPgenResource()) {

                        List<String> rscAttrSetsList = rscDefnsMngr
                                .getAttrSetGroupNamesForResource(rscType);

                        if (rscAttrSetsList != null
                                && !rscAttrSetsList.isEmpty()) {
                            return rscAttrSetsList.toArray();
                        }
                    } else {
                        try {
                            String[] rscGroups = rscDefnsMngr
                                    .getResourceSubTypes(rscType);

                            if (rscGroups != null && rscGroups.length != 0) {
                                return rscGroups;
                            }
                        } catch (VizException e) {
                            MessageDialog errDlg = new MessageDialog(
                                    NcDisplayMngr.getCaveShell(), "Error",
                                    null, "Error getting sub-types\n"
                                            + e.getMessage(),
                                    MessageDialog.ERROR, new String[] { OK }, 0);
                            errDlg.open();
                        }
                    }
                }
                return new String[] {};
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }
        });

        rscGroupLViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object obj1, Object obj2) {

                return super.compare(viewer, obj1, obj2);
            }
        });

        rscGroupLViewer.setLabelProvider(new LabelProvider() {

            public String getText(Object element) {

                String displayName = (String) element;

                ResourceName rscName = new ResourceName(selectedRscName);

                ResourceDefinition rscDefn = rscDefnsMngr
                        .getResourceDefinition(rscName.getRscType());

                // replace with alias
                displayName = rscDefn.getRscGroupDisplayName(displayName);

                return displayName;

            }

        });

        rscAttrSetLViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {

                if (!selectedRscName.getRscType().isEmpty()) {
                    List<AttributeSet> attrSets = rscDefnsMngr
                            .getAttrSetsForResource(selectedRscName, true);

                    maxLengthOfSelectableAttrSets = 0;

                    for (AttributeSet attributeSet : attrSets) {
                        if (attributeSet != null
                                && attributeSet.getName().length() > maxLengthOfSelectableAttrSets) {
                            maxLengthOfSelectableAttrSets = attributeSet
                                    .getName().length();
                        }
                    }
                    return attrSets.toArray(new AttributeSet[0]);
                }
                return new String[] {};
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }
        });

        rscAttrSetLViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object obj1, Object obj2) {
                AttributeSet attr1 = (AttributeSet) obj1;
                AttributeSet attr2 = (AttributeSet) obj2;

                String label1 = attr1.getName();
                if (label1.equals(DEFAULT) || label1.equals(STANDARD)) {
                    return -1;
                }

                String label2 = attr2.getName();
                if (label2.equals(DEFAULT) || label2.equals(STANDARD)) {
                    return 1;
                }

                // Display aliases for Grid resource attributes.
                boolean isGridAttributeSet = attr1.getApplicableResource()
                        .equals(GRID_DATA)
                        || attr2.getApplicableResource().equals(GRID_DATA);

                if (isGridAttributeSet) {

                    String type = selectedRscName.getRscType();
                    String group = selectedRscName.getRscGroup();
                    String alias1 = AttrSetLabelsManager.getInstance()
                            .getAlias(type, group, label1);
                    if (alias1 != null) {
                        label1 = alias1;
                    }

                    String alias2 = AttrSetLabelsManager.getInstance()
                            .getAlias(type, group, label2);
                    if (alias2 != null) {
                        label2 = alias2;
                    }
                }

                // super calls getText which can trigger a bunch of
                // inventory queries in some cases
                return (label1.compareTo(label2));
            }
        });

        rscAttrSetLViewer.setLabelProvider(new LabelProvider() {
            public String getText(Object element) {

                String attrSetName;

                ResourceName rscName = new ResourceName(selectedRscName);

                ResourceDefinition rscDefn = rscDefnsMngr
                        .getResourceDefinition(rscName.getRscType());

                if (rscDefn == null) {
                    return "";
                }

                rscDefn.setAttributeSet((AttributeSet) element);

                // replace with alias
                String originalAttrSetName = ((AttributeSet) element).getName();

                attrSetName = rscDefn
                        .getRscAttributeDisplayName(((AttributeSet) element)
                                .getName());

                if (attrSetName.endsWith(".attr")) {
                    attrSetName = attrSetName.substring(0,
                            attrSetName.length() - 5);
                }

                rscName.setRscAttrSetName(originalAttrSetName/* attrSetName */);

                // Display aliases for Grid resource attributes.
                if (rscDefn.getResourceCategory().equals(
                        ResourceCategory.GridRscCategory)) {

                    String type = selectedRscName.getRscType();
                    String group = selectedRscName.getRscGroup();
                    String alias = AttrSetLabelsManager.getInstance().getAlias(
                            type, group, attrSetName);

                    if (alias != null) {
                        attrSetName = alias;
                    }
                }

                if (!showLatestTimes || rscDefn.isForecast()) {
                    return attrSetName;
                }

                while (attrSetName.length() < maxLengthOfSelectableAttrSets) {
                    attrSetName = attrSetName + " ";
                }

                // If we aren't using the inventory then the query is too
                // slow for the gui.
                // TODO : If the inventory doesn't pan out then we could
                // either implement this in another thread and accept the
                // delay or add a 'Check Availability' button.
                if (rscName.isValid() && rscDefn.usesInventory()
                        && rscDefn.getInventoryEnabled()) {

                    try {
                        DataTime latestTime = rscDefn
                                .getLatestDataTime(rscName);

                        if (latestTime.isNull()) {
                            attrSetName = attrSetName + " (No Data)";
                        } else {
                            String latestTimeStr = NmapCommon
                                    .getTimeStringFromDataTime(latestTime, "_");

                            attrSetName = attrSetName + " (" + latestTimeStr
                                    + ")";
                        }
                    } catch (VizException e) {
                        statusHandler.handle(Priority.INFO, e.getMessage());
                    }
                }
                System.out
                        .println(" >>>>>>>>>> attrSetName >>>>>>>>>>>>>>>>>>    "
                                + attrSetName
                                + "    original name = "
                                + originalAttrSetName);
                return attrSetName;
            }
        });
    }

    /*
     * add all of the listeners for widgets on this dialog
     */
    private void addSelectionListeners() {

        rscCatLViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        StructuredSelection selectedElements = (StructuredSelection) event
                                .getSelection();
                        ResourceCategory selectedCat = (ResourceCategory) selectedElements
                                .getFirstElement();

                        if ((selectedCat != null)
                                && (selectedCat.equals(prevSelectedCat))) {
                            return;
                        }

                        selectedRscName = new ResourceName();
                        selectedRscName.setRscCategory(selectedCat);
                        prevSelectedCat = selectedCat;

                        if (prevCat2SelectedRscName.containsKey(selectedCat)) {
                            selectedRscName = prevCat2SelectedRscName
                                    .get(selectedCat);
                        }

                        updateResourceFilters();
                        updateResourceTypes();
                    }
                });

        filterCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ev) {
                String filterString = filterCombo.getText();

                if (filterString != null
                        && filterString.equals(selectedFilterString)) {
                    return;
                }

                selectedFilterString = filterString;
                prevCat2SelectedFilter.put(prevSelectedCat,
                        selectedFilterString);

                updateResourceTypes();
            }
        });

        rscTypeLViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        StructuredSelection selectedElement = (StructuredSelection) event
                                .getSelection();
                        String selectedType = ((ResourceDefinition) selectedElement
                                .getFirstElement()).getResourceDefnName();

                        String prevSelectedType = selectedRscName.getRscType();
                        if ((selectedType != null)
                                && (selectedType.equals(prevSelectedType))) {
                            return;
                        }

                        selectedRscName.setRscType(selectedType);
                        selectedRscName.setRscGroup("");
                        selectedRscName.setRscAttrSetName("");
                        selectedRscName.setCycleTime(null);

                        updateCycleTimes();

                        updateResourceGroups();
                    }
                });

        rscGroupLViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        StructuredSelection selectedElement = (StructuredSelection) event
                                .getSelection();
                        String selectedGroup = (String) selectedElement
                                .getFirstElement();

                        String prevSelectedGroup = selectedRscName
                                .getRscGroup();
                        if ((selectedGroup != null)
                                && (selectedGroup.equals(prevSelectedGroup))) {
                            return;
                        }

                        selectedRscName.setRscGroup(selectedGroup);
                        selectedRscName.setRscAttrSetName("");

                        updateResourceAttrSets();
                    }
                });

        rscAttrSetLViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        StructuredSelection selectedElement = (StructuredSelection) event
                                .getSelection();
                        String selectedAttrSetName = ((AttributeSet) selectedElement
                                .getFirstElement()).getName();

                        String prevSelectedAttrName = selectedRscName
                                .getRscAttrSetName();

                        if ((selectedAttrSetName != null)
                                && (selectedAttrSetName
                                        .equals(prevSelectedAttrName))) {
                            return;
                        }

                        selectedRscName.setRscAttrSetName(selectedAttrSetName);

                        // Mcidas only
                        ResourceName rscName = new ResourceName(selectedRscName);
                        ResourceDefinition rscDefn = rscDefnsMngr
                                .getResourceDefinition(rscName.getRscType());

                        if (rscDefn.getRscImplementation().equals(
                                McidasConstants.SATELLITE_ID)) {
                            SatelliteImageTypeManager.getInstance()
                                    .setSelectedAttrName(
                                            mcidasAttrNamesNAliases);
                        }

                        updateCycleTimes();
                        updateSelectedResource();
                    }
                });

        // get the selected rsc and add to the list.
        // ignoring the cycle time for now.
        addResourceBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ev) {
                selectResource(false, false);
            }
        });

        replaceResourceBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ev) {
                selectResource(true, false);
            }
        });

        // a double click will add the resource and close the dialog
        rscAttrSetLViewer.getList().addListener(SWT.MouseDoubleClick,
                new Listener() {
                    public void handleEvent(Event event) {
                        if (addResourceBtn.isVisible()) {
                            selectResource(false, true);
                        } else {
                            selectResource(true, true);
                        }
                    }
                });

        cycleTimeCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ev) {
                updateSelectedResource();
            }
        });
    }

    /*
     * set the initial values of the widgets.
     */
    protected void initWidgets(ResourceName initRscName) {

        if (prevSelectedRscName != null && prevSelectedRscName.isValid()) {
            selectedRscName = new ResourceName(prevSelectedRscName);
        } else {
            selectedRscName = new ResourceName(initRscName);
        }

        if (selectedRscName != null) {
            prevSelectedCat = selectedRscName.getRscCategory();
        }

        filterCombo.setItems(new String[] { RSC_FILTER_ALL });
        filterCombo.select(0);
        selectedFilterString = RSC_FILTER_ALL;

        rscCatLViewer.setInput(rscDefnsMngr);
        rscCatLViewer.refresh();
        rscCatLViewer.getList().deselectAll();

        addToAllPanesBtn.setSelection(false);

        if (selectedRscName == null
                || selectedRscName.getRscCategory() == ResourceCategory.NullCategory) {
            return;
        }

        for (int i = 0; i < rscCatLViewer.getList().getItemCount(); i++) {

            if (rscCatLViewer.getList().getItem(i)
                    .equals(selectedRscName.getRscCategory().toString())) {

                rscCatLViewer.getList().select(i);
                break;
            }
        }

        if (rscCatLViewer.getList().getSelectionCount() == 0) {
            selectedRscName = new ResourceName();
        }

        updateResourceFilters();
        updateResourceTypes();
    }

    /*
     * get a list of all the possible filter labels from all of the resources in
     * this category
     */
    protected void updateResourceFilters() {
        ResourceCategory selectedCat = selectedRscName.getRscCategory();

        List<String> filterList = rscDefnsMngr.getAllFilterLabelsForCategory(
                selectedCat, displayType);

        Collections.sort(filterList);
        filterList.add(0, RSC_FILTER_ALL);

        String[] filterArray = new String[0];
        filterArray = filterList.toArray(filterArray);
        filterCombo.setItems(filterArray);

        String prevFilter = prevCat2SelectedFilter.get(selectedCat);
        if (prevFilter != null) {
            for (int i = 0; i < filterCombo.getItemCount(); i++) {
                if (filterCombo.getItem(i).equals(prevFilter)) {
                    filterCombo.select(i);
                    selectedFilterString = prevFilter;
                    break;
                }
            }
        } else {
            filterCombo.select(0);
            selectedFilterString = RSC_FILTER_ALL;
        }
    }

    /*
     * refresh the types list based on the type in the seldResourceName use
     * seldResourceName to select the type
     */
    protected void updateResourceTypes() {

        rscTypeLViewer.setInput(rscDefnsMngr);
        rscTypeLViewer.refresh();

        org.eclipse.swt.widgets.List typeList = rscTypeLViewer.getList();
        typeList.deselectAll();

        String selectedRscType = selectedRscName.getRscType();

        if (!selectedRscType.isEmpty()) {
            String selectedDisplayType = selectedRscType;

            if (selectedRscName.getRscCategory().getCategoryName()
                    .equals(RADAR_CATEGORY)
                    && selectedRscType.startsWith(LOCAL_RADAR_PREFIX)) {

                String alias = LocalRadarStationManager.getInstance()
                        .getDisplayedName(selectedRscType);

                if (alias != null && !alias.isEmpty()) {
                    selectedDisplayType = alias;
                }
            }

            for (int i = 0; i < typeList.getItemCount(); i++) {
                String displayType = typeList.getItem(i);

                if (selectedDisplayType.equals(displayType)) {
                    typeList.select(i);
                    break;
                }
            }

            if (typeList.getSelectionCount() == 0) {
                selectedRscName.setRscType("");
                selectedRscName.setRscGroup("");
                selectedRscName.setRscAttrSetName("");
                selectedRscName.setCycleTime(null);
            }
        }

        // if no type is selected or it is not found for some reason, select the
        // first
        if (selectedRscName.getRscType().isEmpty()
                && rscTypeLViewer.getList().getItemCount() > 0) {

            typeList.select(0);
            StructuredSelection selectedElement = (StructuredSelection) rscTypeLViewer
                    .getSelection();
            String rscType = ((ResourceDefinition) selectedElement
                    .getFirstElement()).getResourceDefnName();
            selectedRscName.setRscType(rscType);
            selectedRscName.setRscGroup("");
            selectedRscName.setRscAttrSetName("");
            selectedRscName.setCycleTime(null);
        }

        updateCycleTimes();
        updateResourceGroups();
    }

    protected void updateResourceGroups() {

        rscGroupLViewer.setInput(rscDefnsMngr);
        rscGroupLViewer.refresh();

        // If a group has been selected, then select it in the list.
        // Otherwise, select the first in the list and update the
        // seldResourceName.
        if (rscGroupLViewer.getList().getItemCount() == 0) {
            if (!selectedRscName.getRscGroup().isEmpty()) {
                selectedRscName.setRscGroup("");
                selectedRscName.setRscAttrSetName("");
                selectedRscName.setCycleTime(null);
            }
        } else {
            rscGroupLViewer.getList().deselectAll();

            if (!selectedRscName.getRscGroup().isEmpty()) {
                for (int i = 0; i < rscGroupLViewer.getList().getItemCount(); i++) {

                    if (rscGroupLViewer.getList().getItem(i)
                            .equals(selectedRscName.getRscGroup())) {
                        rscGroupLViewer.getList().select(i);
                        break;
                    }
                }

                if (rscGroupLViewer.getList().getSelectionCount() == 0) {
                    selectedRscName.setRscGroup("");
                    selectedRscName.setRscAttrSetName("");
                }
            }

            // if no type is selected or it is not found for some reason, select
            // the first
            if (selectedRscName.getRscGroup().isEmpty()
                    && rscGroupLViewer.getList().getItemCount() > 0) {

                rscGroupLViewer.getList().select(0);
                StructuredSelection selectedElement = (StructuredSelection) rscGroupLViewer
                        .getSelection();

                selectedRscName.setRscGroup((String) selectedElement
                        .getFirstElement());
                selectedRscName.setRscAttrSetName("");
            }
        }
        updateResourceAttrSets();
    }

    private void updateResourceAttrSets() {
        rscAttrSetLViewer.setInput(rscDefnsMngr);
        rscAttrSetLViewer.getList().deselectAll();

        String prevSelectedAttrSetName = selectedRscName.getRscAttrSetName();
        if (!prevSelectedAttrSetName.isEmpty()) {

            for (int i = 0; i < rscAttrSetLViewer.getList().getItemCount(); i++) {

                String attrSetName = ((AttributeSet) rscAttrSetLViewer
                        .getElementAt(i)).getName();

                if (attrSetName.equals(prevSelectedAttrSetName)) {
                    rscAttrSetLViewer.getList().select(i);
                    break;
                }
            }

            if (rscAttrSetLViewer.getList().getSelectionCount() == 0) {
                selectedRscName.setRscAttrSetName("");
            }
        }

        // if no attr set is selected or it is not found for some reason, select
        // the first
        if (selectedRscName.getRscAttrSetName().isEmpty()
                && rscAttrSetLViewer.getList().getItemCount() > 0) {

            rscAttrSetLViewer.getList().select(0);
            StructuredSelection selectedElement = (StructuredSelection) rscAttrSetLViewer
                    .getSelection();

            selectedRscName.setRscAttrSetName(((AttributeSet) selectedElement
                    .getFirstElement()).getName());
        }

        updateCycleTimes();
        updateSelectedResource();
    }

    /*
     * when an attrSetName is selected and resource name, with possible cycle
     * time, is ready for selection
     */
    public void updateSelectedResource() {

        String availMsg = "Data Not Available";

        // enable/disable the Add Resource Button
        // and set the name of the Resource
        boolean enableSelections = true;

        ResourceDefinition rscDefn = rscDefnsMngr
                .getResourceDefinition(selectedRscName.getRscType());

        if (!selectedRscName.isValid() || rscDefn == null) {
            enableSelections = false;
        }

        if (enableSelections) {
            try {
                // this call will query just for the inventory params needed to
                // instantiate the resource
                // (ie imageType, productCode...) and not the actual dataTimes.
                if (this.isForecast()) {
                    if (cycleTimes.isEmpty()) {
                        enableSelections = false;
                    }
                } else if (rscDefn.isPgenResource()) {
                    availMsg = "";
                } else if (!rscDefn.isRequestable()) {
                    availMsg = "";
                } else {
                    // If we aren't using the inventory then the query is too
                    // slow for the gui.
                    // TODO : If the inventory doesn't pan out then we could
                    // either implement this in another thread and accept the
                    // delay or add a 'Check Availability' button.
                    DataTime latestTime = rscDefn
                            .getLatestDataTime(selectedRscName);

                    if (latestTime == null || latestTime.isNull()) {
                        enableSelections = false;

                    } else {
                        availMsg = "Latest Data: "
                                + NmapCommon.getTimeStringFromDataTime(
                                        latestTime, "/");
                    }
                }
            } catch (VizException e) {
                statusHandler.handle(Priority.INFO, e.getMessage());
                availMsg = "Error getting latest time.";
                enableSelections = false;
            }
        }

        if (enableSelections) {

            addResourceBtn.setEnabled(true);
            replaceResourceBtn.setEnabled(replaceBtnEnabled);

            if (this.isForecast()) {
                cycleTimeLbl.setEnabled(true);
                cycleTimeCombo.setEnabled(true);
                cycleTimeLbl.setVisible(true);
                cycleTimeCombo.setVisible(true);

                // Cycle for Ensemble
                int seldCycleTimeIndx = cycleTimeCombo.getSelectionIndex();

                // TODO : Allow the user to select 'LATEST' specifically
                if (seldCycleTimeIndx == -1) {
                    selectedRscName.setCycleTimeLatest();
                } else if (seldCycleTimeIndx < cycleTimes.size()) {
                    selectedRscName.setCycleTime(cycleTimes
                            .get(seldCycleTimeIndx));
                } else { // shoulndn't happen
                    selectedRscName.setCycleTimeLatest();
                }

                availDataTimeLbl.setVisible(false);
            } else {
                availDataTimeLbl.setVisible(true);
                availDataTimeLbl.setText(availMsg);
                cycleTimeLbl.setEnabled(false);
                cycleTimeCombo.setEnabled(false);
                cycleTimeLbl.setVisible(false);
                cycleTimeCombo.setVisible(false);
            }

            // For now, don't let the user select 'Latest'
            if (selectedRscName.isLatestCycleTime()) {

                addResourceBtn.setEnabled(false);
                replaceResourceBtn.setEnabled(false);
                seldRscNameTxt.setText("");
            } else {
                seldRscNameTxt.setText(selectedRscName.toString());
            }
        } else {
            seldRscNameTxt.setText("");
            addResourceBtn.setEnabled(false);
            replaceResourceBtn.setEnabled(false);

            availDataTimeLbl.setVisible(true);
            availDataTimeLbl.setText(availMsg);

            cycleTimeLbl.setVisible(false);
            cycleTimeCombo.setVisible(false);
        }

        prevDisplayType2RscName.put(displayType, new ResourceName(
                selectedRscName));
        prevCat2SelectedRscName.put(selectedRscName.getRscCategory(),
                new ResourceName(selectedRscName));
        prevSelectedRscName = selectedRscName;
    }

    /*
     * code for the Listeners for the Add Resource button and the double Click
     * /on the list
     */
    public void selectResource(boolean replaceRsc, boolean done) {

        boolean addToAllPanes = (addToAllPanesBtn.isVisible() && addToAllPanesBtn
                .getSelection());
        if (selectedRscName != null && selectedRscName.isValid()) {
            for (IResourceSelectedListener lstnr : rscSelListeners) {
                lstnr.resourceSelected(selectedRscName, replaceRsc,
                        addToAllPanes, done);
            }
            // "Select New Resource" dialog should remember last selection
            prevSelectedRscName = selectedRscName;
        } else {
            prevSelectedRscName = null;

            statusHandler
                    .handle(Priority.WARN, "An invalid resource selected!");
        }
    }

    public ResourceName getCurrentlySelectedResource() {
        return selectedRscName;
    }

    public void addResourceSelectionListener(IResourceSelectedListener lstnr) {
        rscSelListeners.add(lstnr);
    }

    /*
     * TODO: add a way to let the user specifically choose the "LATEST" cycle
     * time. Currently the user cannot select a forecast resource without
     * selecting an available cycle time.
     */
    public void updateCycleTimes() {
        ResourceDefinition rscDefn = rscDefnsMngr
                .getResourceDefinition(selectedRscName);

        if (rscDefn == null) {
            cycleTimeLbl.setEnabled(false);
            cycleTimeCombo.setEnabled(false);
            return;
        } else {
            cycleTimeLbl.setEnabled(true);
            cycleTimeCombo.setEnabled(true);
            cycleTimeLbl.setVisible(rscDefn.isForecast());
            cycleTimeCombo.setVisible(rscDefn.isForecast());
            availDataTimeLbl.setVisible(!rscDefn.isForecast());

            if (!isForecast()) {
                selectedRscName.setCycleTime(null);
                return;
            }
        }

        try {
            // if this is reading from gempak
            //
            // would like to use the constant in NcGridData but E dependency
            // again.
            if (rscDefn.getPluginName().equals(GempakGrid.gempakPluginName)) {

                // For a GEMPAK dataSource get gridCycleTimes from the
                // dataLocation.
                HashMap<String, String> rscParams = rscDefnsMngr
                        .getAllResourceParameters(selectedRscName);

                cycleTimeCombo.removeAll();
                cycleTimes.clear();
                try {
                    String dataLocation = null;
                    try {
                        dataLocation = GempakGrid.getGempakGridPath(rscParams
                                .get(GDFILE));
                    } catch (VizException e) {
                        throw new VizException(e);
                    }
                    String[] gridCycleTimes = GempakGrid.getGridCycleTimes(
                            dataLocation, rscParams.get(GDFILE).toLowerCase());
                    for (String gct : gridCycleTimes) {
                        String gct2DataTimeFormat = "20" + gct.substring(0, 2)
                                + "-" + gct.substring(2, 4) + "-"
                                + gct.substring(4, 6) + " "
                                + gct.substring(7, 9) + ":"
                                + gct.substring(9, 11) + ":00.0 ";
                        cycleTimes.add(0, new DataTime(gct2DataTimeFormat));
                        cycleTimeCombo.add(gct, 0);
                    }
                    if (gridCycleTimes.length > 0) {
                        cycleTimeCombo.select(0);
                    }
                } catch (VizException e) {
                    statusHandler.handle(Priority.ERROR,
                            "Error querying cycle times: "
                                    + e.getMessage().split(":")[1]);
                }

                return;
            }

            List<DataTime> availableTimes = null;

            // If the timeline is generated using frame intervals from a given
            // reference/cycle time, then get a list of selectable ref times.
            // Ideally this would also specify a way to generate the ref times
            // but its really just for nctaf right now so just do it like taf
            // needs.
            if (rscDefn.getTimelineGenMethod() == TimelineGenMethod.USE_FCST_FRAME_INTERVAL_FROM_REF_TIME) {
                availableTimes = rscDefn.getNormalizedDataTimes(
                        selectedRscName, 24 * 60);
            } else if (rscDefn.getTimelineGenMethod() == TimelineGenMethod.DETERMINE_FROM_RSC_IMPLEMENTATION) {
                int startTime = Integer.parseInt(rscDefn.getCycleReference());
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(GMT));
                cal.setTime(new Date());

                DayReference day = (rscDefn.getDayReference() != null ? rscDefn
                        .getDayReference() : DayReference.TODAY);
                switch (day) {
                case TOMORROW:
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case TODAY:
                    break;
                case YESTERDAY:
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    break;
                default:
                    cal = null;
                }
                if (cal != null) {
                    cal.set(Calendar.HOUR_OF_DAY, startTime);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    availableTimes = new ArrayList<DataTime>();
                    availableTimes.add(new DataTime(cal));
                }
            } else {
                availableTimes = rscDefn.getDataTimes(selectedRscName);
            }

            // save the currently selected cycle time.
            String curSelTime = cycleTimeCombo.getText();

            cycleTimeCombo.removeAll();
            cycleTimes.clear();

            for (int t = availableTimes.size() - 1; t >= 0; t--) {
                DataTime dt = availableTimes.get(t);
                DataTime refTime = new DataTime(dt.getRefTime());

                if (!cycleTimes.contains(refTime)) {
                    cycleTimes.add(refTime);
                    String timeStr = NmapCommon.getTimeStringFromDataTime(dt,
                            "_");
                    cycleTimeCombo.add(timeStr);
                }
            }

            for (int t = 0; t < cycleTimeCombo.getItemCount(); t++) {
                if (cycleTimeCombo.getItem(t).equals(curSelTime)) {
                    cycleTimeCombo.select(t);
                    break;
                }
            }

            if (cycleTimes.isEmpty()) {
                cycleTimeCombo.setVisible(false);
                cycleTimeLbl.setVisible(false);
                availDataTimeLbl.setVisible(true);
                availDataTimeLbl.setText("No Data Available");
            } else if (cycleTimeCombo.getSelectionIndex() == -1) {
                cycleTimeCombo.select(0);
            }

        } catch (VizException e) {
            MessageDialog errDlg = new MessageDialog(
                    NcDisplayMngr.getCaveShell(), "Error", null,
                    "Error Requesting Cycle Times:" + e.getMessage(),
                    MessageDialog.ERROR, new String[] { OK }, 0);
            errDlg.open();
            return;
        }

        return;
    }

    public void setMultiPaneEnabled(Boolean multPaneEnable) {
        addToAllPanesBtn.setVisible(multPaneEnable);
    }

    public void setReplaceEnabled(Boolean rplEnbld) {
        replaceBtnEnabled = rplEnbld;

        if (!isDisposed()) {
            updateSelectedResource();
        }
    }

    public ResourceName getPrevSelectedResource() {
        return prevSelectedRscName;
    }

    private boolean isForecast() {
        boolean gridAnalysis = false;
        StructuredSelection is = (StructuredSelection) rscAttrSetLViewer
                .getSelection();
        AttributeSet attr = (AttributeSet) is.getFirstElement();
        if (attr != null) {
            String gdattim = attr.getAttributes().get("GDATTIM");
            gridAnalysis = gdattim != null
                    && !gdattim.isEmpty()
                    && (gdattim.toUpperCase().contains("ALLF") || (gdattim
                            .toUpperCase().contains("FIRSTF") && gdattim
                            .toUpperCase().contains("LASTF")));
        }

        ResourceDefinition rscDefn = rscDefnsMngr
                .getResourceDefinition(selectedRscName.getRscType());

        return rscDefn.isForecast() && !gridAnalysis;
    }
}
