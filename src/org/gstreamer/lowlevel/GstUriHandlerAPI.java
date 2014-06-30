package org.gstreamer.lowlevel;

import org.gstreamer.URIType;
import org.gstreamer.Element;
import org.gstreamer.lowlevel.annotations.CallerOwnsReturn;

public interface GstUriHandlerAPI extends com.sun.jna.Library {
	static GstUriHandlerAPI INSTANCE = GstNative.load(GstUriHandlerAPI.class);

	@CallerOwnsReturn Element gst_element_make_from_uri(
			URIType type, String uri, String elementName);
}
