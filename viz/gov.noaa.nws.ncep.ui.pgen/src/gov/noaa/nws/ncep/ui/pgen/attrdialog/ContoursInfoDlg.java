/*
 * gov.noaa.nws.ncep.ui.pgen.attrDialog.ContoursInfoDlg
 * 
 * Date created: October 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.attrdialog;

import gov.noaa.nws.ncep.ui.pgen.PgenStaticDataProvider;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.contoursinfo.ContourFiles;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.contoursinfo.ContourLabel;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.contoursinfo.ContourLevel;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.contoursinfo.ContourRoot;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.contoursinfo.ContoursInfo;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.contoursinfo.FcstHrs;
import gov.noaa.nws.ncep.ui.pgen.contours.IContours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

/**
 * Class for creating a dialog to edit the contours' attribute information.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date       	Ticket#		Engineer	  Description
 * ------------	----------	-----------	  ---------------------------
 * 10/09		#167		J. Wu   	  Initial Creation.
 * 07/11        #450        G. Hull       NcPathManager
 * 12/13        1084        J. Wu         Add table-control for Cint in contoursInfo.xml
 * 08/01/2015   8213        P.            CAVE>PGEN 
 *                          Chowdhuri      - Refinements to contoursInfo.xml
 * 
 * </pre>
 * 
 * @author J. Wu
 */

public class ContoursInfoDlg extends CaveJFACEDialog implements IContours {
	
	// Status handling
    private static final IUFStatusHandler handler = UFStatus.getHandler(ContoursInfoDlg.class);

    // Contours information files
    private static List<String> contoursInfoParamFilelist;

    // Contours information
    private static HashMap<String, ContoursInfo> contoursInfoTbl; 
	private static HashMap<String, ContoursInfo> contoursInfoTables;

	// The JAXB Manager
	private static SingleTypeJAXBManager<ContourRoot> cntrInfoManager;

    private Composite top = null;

    private Combo parmCombo = null;

    private Text parmTxt = null;

    private Combo levelCombo1 = null;

    private Text levelValueTxt1 = null;

    private Combo levelCombo2 = null;

    private Text levelValueTxt2 = null;

    private Combo fcsthrCombo = null;

    private Text fcsthrTxt = null;

    private Text cintTxt = null;

    private AttrDlg contoursAttrDlg = null;

    private DateTime date1 = null;

    private DateTime time1 = null;

    private DateTime date2 = null;

    private DateTime time2 = null;

    /*
     * Constructor
     */
    protected ContoursInfoDlg(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Add Accept and Cancel buttons on the dialog's button bar.
     */
    @Override
    public void createButtonsForButtonBar(Composite parent) {

        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, true);

    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Contours Attributes");
    }

    /**
     * Creates the dialog area
     *  
     */
    @Override
    public Control createDialogArea(Composite parent) {

        top = (Composite) super.createDialogArea(parent);

        GridLayout mainLayout = new GridLayout(2, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        mainLayout.horizontalSpacing = 3;
        top.setLayout(mainLayout);

	    initializeComponents(top);

        return top;
    }

    /**
     * create components for the dialog
     *  
     */
    private void initializeComponents(Composite comp) {

        GridLayout layout1 = new GridLayout(2, false);
        layout1.marginHeight = 1;
        layout1.marginWidth = 1;
        layout1.horizontalSpacing = 3;

        // Contours parameter name
        Label parmLbl = new Label(comp, SWT.NONE);
        parmLbl.setText("PARM:");

        Composite parmComp = new Composite(comp, SWT.NONE);
        parmComp.setLayout(layout1);

        parmCombo = new Combo(parmComp, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (String st : getContourParms("Parm")) {
            parmCombo.add(st);
        }
        parmCombo.add("Other");
        parmCombo.select(0);

        parmCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                updateComboText(parmCombo, parmTxt, parmCombo.getText());
                updateCintText();
            }
        });

        parmTxt = new Text(parmComp, SWT.SINGLE | SWT.BORDER);
        parmTxt.setLayoutData(new GridData(45, 15));
        parmTxt.setEditable(true);
        parmTxt.setText(parmCombo.getText());

        // Contours level 1
        Label levelLbl = new Label(comp, SWT.NONE);
        levelLbl.setText("Level 1:");

        Composite lvl1Comp = new Composite(comp, SWT.NONE);
        lvl1Comp.setLayout(layout1);

        levelCombo1 = new Combo(lvl1Comp, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (String st : getContourParms("Level")) {
            levelCombo1.add(st);
        }
        levelCombo1.add("Other");
        levelCombo1.select(0);
        levelCombo1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                updateComboText(levelCombo1, levelValueTxt1, levelCombo1.getText());
                updateCintText();
            }
        });

        levelValueTxt1 = new Text(lvl1Comp, SWT.SINGLE | SWT.BORDER);
        levelValueTxt1.setLayoutData(new GridData(45, 15));
        levelValueTxt1.setEditable(true);
        levelValueTxt1.setText(levelCombo1.getText());

        // Contours level 2
        Label levelLbl2 = new Label(comp, SWT.NONE);
        levelLbl2.setText("Level 2:");

        Composite lvl2Comp = new Composite(comp, SWT.NONE);
        lvl2Comp.setLayout(layout1);

        levelCombo2 = new Combo(lvl2Comp, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (String st : getContourParms("Level")) {
            levelCombo2.add(st);
        }
        levelCombo2.select(0);
        levelCombo2.add("Other");
        levelCombo2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                updateComboText(levelCombo2, levelValueTxt2, levelCombo2.getText());
            }
        });

        levelValueTxt2 = new Text(lvl2Comp, SWT.SINGLE | SWT.BORDER);
        levelValueTxt2.setLayoutData(new GridData(45, 15));
        levelValueTxt2.setEditable(true);
        levelValueTxt2.setText("");

        // Contours forecast hour
        Label fcsthrLbl = new Label(comp, SWT.NONE);
        fcsthrLbl.setText("Fcst Hour:");

        Composite fhrComp = new Composite(comp, SWT.NONE);
        fhrComp.setLayout(layout1);

        fcsthrCombo = new Combo(fhrComp, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (String st : getContourParms("ForecastHour")) {
            fcsthrCombo.add(st);
        }
        fcsthrCombo.add("Other");
        fcsthrCombo.select(0);

        fcsthrCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                updateComboText(fcsthrCombo, fcsthrTxt, fcsthrCombo.getText());
            }
        });

        fcsthrTxt = new Text(fhrComp, SWT.SINGLE | SWT.BORDER);
        fcsthrTxt.setLayoutData(new GridData(45, 15));
        fcsthrTxt.setEditable(true);
        fcsthrTxt.setText(fcsthrCombo.getText());

        // Contours date/time 1
        Label dateLbl = new Label(comp, SWT.NONE);
        dateLbl.setText("Time 1:");

        Composite dtComp = new Composite(comp, SWT.NONE);
        dtComp.setLayout(layout1);

        date1 = new DateTime(dtComp, SWT.BORDER | SWT.DATE);
        time1 = new DateTime(dtComp, SWT.BORDER | SWT.TIME | SWT.SHORT);

        // Contours date/time 2
        Label dateLbl2 = new Label(comp, SWT.NONE);
        dateLbl2.setText("Time 2:");

        Composite dtComp2 = new Composite(comp, SWT.NONE);
        dtComp2.setLayout(layout1);

        date2 = new DateTime(dtComp2, SWT.BORDER | SWT.DATE | SWT.TIME);
        time2 = new DateTime(dtComp2, SWT.BORDER | SWT.TIME | SWT.SHORT);

        Label cintLbl = new Label(comp, SWT.NONE);
        cintLbl.setText("Cint:");

        // Contours intervals
        cintTxt = new Text(comp, SWT.SINGLE | SWT.BORDER);
        cintTxt.setLayoutData(new GridData(100, 15));
        cintTxt.setEditable(true);
        cintTxt.setText("");
        updateCintText();

        updateContourInfoSelection((IContours) contoursAttrDlg);

    }

    /**
     * Set the location for the dialog
     */
    public int open() {

        if (this.getShell() == null) {
            this.create();
        }

        Point pt = this.getShell().getParent().getLocation();

        this.getShell().setLocation(pt.x + 350, pt.y + 50);

        return super.open();

    }

    /**
     * get Contours parameter name
     */
    public String getParm() {

        String parm = parmTxt.getText();
        if (parm == null) {
            parm = "";
        }

        return parm;
    }

    /**
     * get level
     */
    public String getLevel() {

        String level = levelValueTxt1.getText();
        if (level == null) {
            level = "";
        }

        String level2 = levelValueTxt2.getText();
        if (level2 == null) {
            level2 = "";
        }

        if (level2.trim().length() > 0) {
            level = new String(level + ":" + level2);
        }

        return level;
    }

    /**
     * get Contours forecast hour
     */
    public String getForecastHour() {

        String hr = fcsthrTxt.getText();
        if (hr == null) {
            hr = "";
        }

        return hr;
    }

    /**
     * get cint
     */
    public String getCint() {
        return cintTxt.getText();
    }

    /**
     * get time
     */
    public Calendar getTime1() {

        Calendar myTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        myTime.set(date1.getYear(), date1.getMonth(), date1.getDay(),
                time1.getHours(), time1.getMinutes(), 0);

        return myTime;

    }

    /**
     * set time
     */
    private void setTime1(Calendar time) {
        date1.setYear(time.get(Calendar.YEAR));
        date1.setMonth(time.get(Calendar.MONTH));
        date1.setDay(time.get(Calendar.DAY_OF_MONTH));
        time1.setHours(time.get(Calendar.HOUR));
        time1.setMinutes(time.get(Calendar.MINUTE));
        time1.setSeconds(0);
    }

    /**
     * get time
     */
    public Calendar getTime2() {

        Calendar myTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        myTime.set(date2.getYear(), date2.getMonth(), date2.getDay(),
                time2.getHours(), time2.getMinutes(), 0);

        return myTime;

    }

    /**
     * set time
     */
    private void setTime2(Calendar time) {
        if (time != null) {
            date2.setYear(time.get(Calendar.YEAR));
            date2.setMonth(time.get(Calendar.MONTH));
            date2.setDay(time.get(Calendar.DAY_OF_MONTH));
            time2.setHours(time.get(Calendar.HOUR));
            time2.setMinutes(time.get(Calendar.MINUTE));
            time2.setSeconds(0);
        }
    }

    /**
     * set the associated ContoursAttrDlg.
     */
    public void setContoursAttrDlg(AttrDlg contoursAttrDlg) {
        this.contoursAttrDlg = contoursAttrDlg;
    }

    /**
     * get the associated ContoursAttrDlg.
     */
    public AttrDlg getContoursAttrDlg() {
        return contoursAttrDlg;
    }

    /**
     * update the attributes of the associated ContoursAttrDlg.
     */
    private void updateContoursAttrDlg() {

        if (contoursAttrDlg instanceof ContoursAttrDlg) {
            ((ContoursAttrDlg) contoursAttrDlg).setAttributes(this);
        } else if (contoursAttrDlg instanceof OutlookAttrDlg) {
            ((OutlookAttrDlg) contoursAttrDlg).setAttributes(this);
        }

    }

    /**
     * update the attribute selections in this dialog.
     */
    private void updateContourInfoSelection(IContours attr) {

        updateComboText(parmCombo, parmTxt, attr.getParm());

        String lvl = attr.getLevel();
        int spi = lvl.indexOf(":");
        String lvl1 = new String(lvl);
        if (spi > 0) {
            lvl1 = lvl.substring(0, spi);
        }

        updateComboText(levelCombo1, levelValueTxt1, lvl1);

        String lvl2 = new String("");
        if (spi > 0 && spi < lvl.length()) {
            lvl2 = lvl.substring(spi + 1, lvl.length());
        }

        updateComboText(levelCombo2, levelValueTxt2, lvl2);

        updateComboText(fcsthrCombo, fcsthrTxt, attr.getForecastHour());

        cintTxt.setText(attr.getCint());

        setTime1(attr.getTime1());
        setTime2(attr.getTime2());

    }

    /**
     * Updates the attributes in the ContoursAttrDlg and closes this dialog.
     */
    public void okPressed() {
        updateContoursAttrDlg();
        super.okPressed();
    }

    /**
     * Reads a list of contours Info xml files by Parm
     * #R8213
     * 
     * @return - list of contour information xml files
     */
	public static final List<String> readInfoFilelistTbl()  {

    	// reads and parses contoursInfo.xml file
		
	    if (null == contoursInfoParamFilelist)  {

	    contoursInfoParamFilelist = new ArrayList<String>();

        try {

        	    String contoursInfoRoot = PgenStaticDataProvider.getProvider().getPgenLocalizationRoot() 
        	    		                + "/contoursInfo.xml";
        	    
	            String contoursInfoFile = PgenStaticDataProvider.getProvider().getFileAbsolutePath(contoursInfoRoot);

	            // See if the contours info xml files are well-formed

	            SAXParserFactory saxfactory = SAXParserFactory.newInstance();
	            saxfactory.setValidating(false);
	            saxfactory.setNamespaceAware(true);

	            SAXParser cntrInfoparser = saxfactory.newSAXParser();

	            XMLReader cntrInforeader = cntrInfoparser.getXMLReader();
	            cntrInforeader.setErrorHandler(new SimpleHandler());
	            InputSource cntrInfoSrc = new InputSource(contoursInfoFile);
	            cntrInforeader.parse(cntrInfoSrc);
	            
	            cntrInfoSrc = null;

        		String cntrInfoSrcRoot = "";

                if (null == cntrInfoManager)	{
    		        cntrInfoManager = new SingleTypeJAXBManager<ContourRoot>(ContourRoot.class);
                }

	            ContourRoot cntrInfoRoot = 
	                		cntrInfoManager.unmarshalFromXmlFile(contoursInfoFile);

	            String cntrInfoFile = null;

	                for (ContoursInfo cntrinfo : cntrInfoRoot.getCntrList()) {
						if (null != cntrinfo.getName() && !cntrinfo.getName().isEmpty()
						&&  null != cntrinfo.getParm() && !cntrinfo.getParm().isEmpty()
						&&  "path".equals(cntrinfo.getParm())
						&&  "cfiles".equals(cntrinfo.getName())) {
	                    	ContourFiles cntrfile = cntrinfo.getCfiles();
		                    if (null != cntrfile) {
		                        for (String filepath: cntrfile.getPaths()) {
		                        	if (null != filepath && !filepath.isEmpty())
		                        	{
		                        		cntrInfoSrcRoot = PgenStaticDataProvider.getProvider().getPgenLocalizationRoot() + "/" + filepath;
		                        		cntrInfoFile = PgenStaticDataProvider.getProvider().getFileAbsolutePath(cntrInfoSrcRoot);
		                        		cntrInfoSrc = new InputSource(cntrInfoFile);
		                        		cntrInforeader.parse(cntrInfoSrc);

		                                contoursInfoParamFilelist.add(filepath);
		                                
		                	            cntrInfoSrc = null;
		                	            cntrInfoFile = null;
		                        	 }
		                        }
		                    }
	                     }
	                }
	                
	             cntrInfoSrcRoot = null;
	             cntrInfoSrc = null;
		         cntrInfoparser = null;
		         saxfactory = null;
		         cntrInforeader = null;
		         
		         contoursInfoRoot = null;
		         contoursInfoFile = null;
	                
        } catch (Exception e) {

              handler.handle(Priority.ERROR, 
            		         "ContoursInfoDlg: exception reading contourInfo xml in readInfoTbl .",
            		         e);

          }	                

	   } // contoursInfoParamFileList null.
	    
	   cntrInfoManager = null;
	   
      return contoursInfoParamFilelist;

    }    
    

    /**
     * Read contours information xml files by Parm
     * 
     * @return - list of contours info objects
     */
    public static final HashMap<String, ContoursInfo> readInfoTbl()  {
    	
    // Read contoursInfo.xml file and list the entries for individual 
    // contoursInfo xml files in it
    	
	    if (null == contoursInfoTables) {

            contoursInfoTables = new HashMap<String, ContoursInfo>();

		    if (null == contoursInfoParamFilelist)  {
   	
		    	contoursInfoParamFilelist =  ContoursInfoDlg.readInfoFilelistTbl();
		    	
				// Read from the contours info xml files in the list
                try {
				    	
                    if (null == cntrInfoManager)	{
        		        cntrInfoManager = new SingleTypeJAXBManager<ContourRoot>(ContourRoot.class);
                    }

                	for (String path: contoursInfoParamFilelist) {

                		String cntrInfoParmFileRoot = PgenStaticDataProvider.getProvider().getPgenLocalizationRoot() + "/" + path;

		                String cntrInfoParmFile = PgenStaticDataProvider.getProvider().getFileAbsolutePath(cntrInfoParmFileRoot);

		                ContourRoot cntrInfoRoot = 
		                		cntrInfoManager.unmarshalFromXmlFile(cntrInfoParmFile);
		                
		                    List<ContoursInfo> cntrInfo = cntrInfoRoot.getCntrList();
		                
			                for (ContoursInfo cinfo: cntrInfo)	{
			                    if (null != cinfo.getName() && !cinfo.getName().isEmpty()
			                    && null != cinfo.getParm() && !cinfo.getParm().isEmpty()) {
			                    	
			                        contoursInfoTables.put(cinfo.getParm(), cinfo);  	
	
			                    }
			                }
			
		                }	// end foreach
			
	              }  catch (Exception e) {
	            	  
			             handler.handle(Priority.ERROR, 
		            		            "ContoursInfoDlg: exception reading contourInfo xml in readInfoTbl .",
		            		            e);
	                }


		    } // contoursInfoParamFileList end .

	    } // contoursInfoTables end .
	    
		cntrInfoManager = null;

       return contoursInfoTables;

    }


    /**
     * Read contours information document
     * 
     * @return - contours info document
     */
    public static Document readInfoTbl1() {

        Document dm = null;
        try {
            SAXReader reader = new SAXReader();
            dm = reader.read("/usr1/jwu/r1g1-6/eclipse/AAA.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dm;
    }

    /**
     * Get inputs for Contours info - such as parm, level, forecaster hour
     * 
     * @param type
     *            - name for the info
     * @return
     *  
     */
    private static List<String> getContourParms(String parm)  {
 
    	List<String> retList = new ArrayList<String>();
        
    	// Read the contours info xml files
        contoursInfoTbl = ContoursInfoDlg.readInfoTbl(); 

        if ("Parm".equals(parm))  {
        	
	        Set<String> contoursInfoTblKeys = contoursInfoTbl.keySet();
	
	        String[] keySetArray = contoursInfoTblKeys.toArray(new String[0]);
	        
	        List<String> keySetArrList = Arrays.asList(keySetArray);
	        
	        Collections.reverse(keySetArrList);
	        
	        retList.addAll(keySetArrList);

        } //  "Parm"
        
        if ("Level".equals(parm))  {
        	
        	String param = "";

        	Collection<ContoursInfo> cntrsInfoValues = contoursInfoTbl.values();
        	
	    	for (ContoursInfo cntrsInfo: cntrsInfoValues) { 
	        	if (retList.isEmpty() || !retList.contains(param)) {
	        		List<ContourLevel> levls = cntrsInfo.getLevels();
	                for (ContourLevel lv: levls) {	
	            		param = lv.getValue();
	            		if (null != param && !param.isEmpty()) {
	            			retList.add(param);
	            		}
	                }
	        	}
	    	}

        }  //  "Level"       
        
        if ("ForecastHour".equals(parm))  {
        	
			String text = "";

        	Collection<ContoursInfo> cntrsInfoValues = contoursInfoTbl.values();
        	
	    	for (ContoursInfo cntrsInfo: cntrsInfoValues) {  
	        	if (retList.isEmpty() || !retList.contains(text)) {
	        		FcstHrs fcsthrs = cntrsInfo.getFhrs();
	        		List<ContourLabel> labels = fcsthrs.getClabels();
	        		for (ContourLabel lbl: labels) {
	            		text = lbl.getText();
		        		if (null != text && !text.isEmpty()) {
		            		retList.add(text);
		            	}
	        		} // labelnodes
	        	}
	    	}

        }  //  "Fhr"


        return retList; 

    }

    /**
     * Updates the Combo/text to a selected item.
     */
    private void updateComboText(Combo cmb, Text txt, String sel) {

        // Use the current selection on the Combo if no selection is provided.
        if (sel == null) {
            sel = cmb.getText();
        }

        // Update the Text.
        txt.setText(sel);

        // Update the Combo selection.
        int index = -1;
        boolean found = false;
        for (String str : cmb.getItems()) {
            if (str.equals(sel)) {
                found = true;
                break;
            }

            index++;
        }

        if (found) {
            cmb.select(index + 1);
            if (sel.equalsIgnoreCase("Other")) {
                txt.setText("");
                txt.setEnabled(true);
            } else {
                txt.setEnabled(false);
            }
        } else {
            cmb.select(cmb.getItemCount() - 1);
            txt.setEnabled(true);
        }

    }

    /**
     * Get a list of cint values for different parms and/or levels.
     * 
     * @param
     * @return LinkedHashMap<String, String>
     */
    public static LinkedHashMap<String, String> getCints() {

        LinkedHashMap<String, String> cInts = new LinkedHashMap<String, String>();
  
		ContoursInfoDlg.readInfoTbl();
        
        Collection<ContoursInfo> cntrsInfoObjects = contoursInfoTables.values();
        
		for (ContoursInfo cntrsInfo: cntrsInfoObjects) {
          
		  List<ContourLevel> levels = cntrsInfo.getLevels();
		    	            
		    // Iterating through level elements
		    for (ContourLevel lv : levels) {
		
		        String levelValue = lv.getValue();
		        String cint =  lv.getCint();
		        String coord = lv.getCoord();
		        String ckey  = "";

		        if ( null != coord && 0 < coord.trim().length()) {
		            ckey += coord;
		            if (null != levelValue && 0 < levelValue.trim().length()) {
		                ckey += "-" + levelValue;
		            }
		
		            if (null != cint && 0 < cint.trim().length()) {
		                cInts.put(ckey, cint);
		            }
		        }
		    } // end foreach

		} // end foreach

       return cInts;
    }

    /**
     * Updates the cint based on the selection of parm and/or level.
     * 
     * First try to find a value for "parm-level"; if not, try to find a value
     * for "parm".
     * 
     */
    private void updateCintText() {

        String parm = getParm();
        String levelValue = levelValueTxt1.getText();

        String key = "";
        if (parm != null && parm.trim().length() > 0) {
            key += parm;
            if (levelValue != null && levelValue.trim().length() > 0) {
                key += "-" + levelValue;
            }
        }

        String cint = getCints().get(key);
        if (cint != null && cint.trim().length() > 0) {
            cintTxt.setText(cint);
        }
    }

}
