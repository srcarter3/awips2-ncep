/**
 * 
 */
package gov.noaa.nws.ncep.edex.common.metparameters;


import javax.measure.quantity.Dimensionless;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

import tec.uom.se.AbstractUnit;

/**
 * Maps to the Bufrmos parameter severe6hr - new GEMPAK alias US06
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
 public class UncondProbOf06HrSevereWeather extends AbstractMetParameter<Dimensionless> 
        implements ISerializableObject {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 7565653710046867602L;

	public UncondProbOf06HrSevereWeather()throws Exception {
		 super(AbstractUnit.ONE);
	}
 }