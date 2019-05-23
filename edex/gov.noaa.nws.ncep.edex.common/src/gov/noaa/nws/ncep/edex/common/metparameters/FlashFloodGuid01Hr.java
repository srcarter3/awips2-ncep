package gov.noaa.nws.ncep.edex.common.metparameters;

import javax.measure.quantity.Length;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

import si.uom.SI;

/**
 * Maps to the GEMPAK parameter FF01
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize

public class FlashFloodGuid01Hr
        extends AbstractMetParameter<Length>
        implements ISerializableObject {

    private static final long serialVersionUID = -2077019493131152860L;

    public FlashFloodGuid01Hr() {
        super(SI.METRE);
    }

}
