package org.gstreamer;

import org.gstreamer.lowlevel.EnumMapper;
import org.gstreamer.lowlevel.IntegerEnum;
import org.gstreamer.lowlevel.annotations.DefaultEnumValue;

public enum URIType implements IntegerEnum {
	@DefaultEnumValue
	GST_URI_UNKNOWN(0),
	GST_URI_SINK(1),
	GST_URI_SRC(2);

	URIType(int value) {
		this.value = value;
	}

	public final int intValue() {
		return value;
	}

	public final static URIType valueOf(int uritype) {
		return EnumMapper.getInstance().valueOf(uritype, URIType.class);
	}
        
        
	public final int value;
}
