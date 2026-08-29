package org.wandora.utils.swing;


import java.awt.Dimension;
import java.awt.Graphics;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIConstants;

public class WandoraStartupImagePanel extends ImagePanel {
	private static final long serialVersionUID = 1L;
	
	public WandoraStartupImagePanel() {
		super("gui/startup_image.gif");
	}


	
	@Override
    public void paint(Graphics g) {
        super.paint(g);
        
        try {
	        Dimension imageDimensions = getImageDimension();
	        int x = (this.getSize().width - imageDimensions.width) / 2;
	        int y = (this.getSize().height - imageDimensions.height) / 2;
	        
	        String versionInfoText = Wandora.getVersionInfo();
	        g.setColor(UIConstants.wandoraBlueColorAlt);
	        g.setFont(UIConstants.wandoraVersionInfoFont);
	        int viWidth = g.getFontMetrics().stringWidth(versionInfoText);
	        int vix = x + imageDimensions.width / 2 - viWidth / 2 + 5;
	        int viy = y + imageDimensions.height - 35;
	        g.drawString(versionInfoText, vix, viy);
        }
        catch(Exception e) {
        	// Ignore
        }
    }
}
