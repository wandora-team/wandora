
package org.wandora.application.gui.simple;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.Scrollable;
import javax.swing.SwingConstants;


/**
 *
 * @author akivela
 */
public class ScrollableSimplePanel extends SimplePanel implements Scrollable {

    private static final long serialVersionUID = 1L;


    /** Creates a new instance of ScrollableSimplePanel */
    public ScrollableSimplePanel() {
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Container c=this.getParent();
        if(c==null) return true;
        if(c.getWidth()>300) return true;
        else return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if(orientation==SwingConstants.VERTICAL) return visibleRect.height;
        else return visibleRect.width;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

}
