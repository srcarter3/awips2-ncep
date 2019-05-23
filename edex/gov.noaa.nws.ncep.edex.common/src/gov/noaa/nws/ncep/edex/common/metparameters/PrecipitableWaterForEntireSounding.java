package gov.noaa.nws.ncep.edex.common.metparameters;

import javax.measure.quantity.Length;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

import si.uom.SI;

/**
 * Maps to the GEMPAK parameter PWAT
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize

public class PrecipitableWaterForEntireSounding
        extends AbstractMetParameter<Length>
        implements ISerializableObject {

    private static final long serialVersionUID = -2693040170236863469L;

    public PrecipitableWaterForEntireSounding() {
        super(SI.METRE);
    }
}