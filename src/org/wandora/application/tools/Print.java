/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wandora.application.tools;


import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.topicpanels.DockingFramePanel;
import org.wandora.application.gui.topicpanels.TopicPanel;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akikivela
 */
public class Print extends AbstractWandoraTool implements WandoraTool {

    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicPanel topicPanel = wandora.getTopicPanel();
        Component printable = null;
        if(topicPanel != null) {
            if(topicPanel instanceof DockingFramePanel) {
                DockingFramePanel dockingPanel = (DockingFramePanel) topicPanel;
                TopicPanel currentTopicPanel = dockingPanel.getCurrentTopicPanel();
                if(currentTopicPanel != null) {
                    printable = currentTopicPanel.getGui();
                }
            }
        }
        if(printable == null) {
            printable = wandora;
        }
        print(printable);
    }
    
    
    

    
    
    public void print(Component component) {
        if(component != null) {
            BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            if(component instanceof JComponent) {
                ((JComponent) component).setDoubleBuffered(false);
            }
            component.paint(g);
            if(component instanceof JComponent) {
                ((JComponent) component).setDoubleBuffered(true);
            }
            printImage(image);
        }
    }
    
    
    
    public void printImage(final BufferedImage image) {
        if(image != null) {

            PrinterJob pj = PrinterJob.getPrinterJob();
            pj.setJobName("Wandora");

            pj.setPrintable(
                    new Printable() {
                        @Override
                        public int print(Graphics pg, PageFormat pageFormat, int pageNum) {
                            if(pageNum > 0) {
                                return Printable.NO_SUCH_PAGE;
                            }

                            Graphics2D g2 = (Graphics2D) pg;

                            double scaleX = pageFormat.getImageableWidth() / image.getWidth();
                            double scaleY = pageFormat.getImageableHeight() / image.getHeight();
                            // Maintain aspect ratio, 2 as a maximum
                            double scale = Math.min(2, Math.min(scaleX, scaleY));
                            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                            g2.scale(scale, scale);
                            g2.drawImage(image, 0, 0, null);

                            return Printable.PAGE_EXISTS;
                        }
                    }
            );
            if (pj.printDialog() == false) {
                return;
            }

            try {
                pj.print();
            } 
            catch (PrinterException ex) {
                log(ex);
            }
        }
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/print.png");
    }
    
    @Override
    public String getName() {
        return "Print";
    }

    @Override
    public String getDescription() {
        return "Print current topic panel.";
    } 

    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
