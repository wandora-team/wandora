/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 *
 * 
 * AbstractSliderTool.java
 *
 */


package org.wandora.application.tools.graph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleSlider;
import org.wandora.application.gui.simple.SimpleToggleButton;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;

/**
 *
 * @author akivela
 */


public abstract class AbstractSliderTool extends AbstractGraphTool implements WandoraTool, ChangeListener, MouseListener {
    
    
    private TopicMapGraphPanel graphPanel = null;
    private JWindow sliderPopup = null;
    private SimpleSlider slider = null;
    private SimpleLabel sliderLabel = null;
    private Component referenceComponent = null;
    
    
    
    
    public AbstractSliderTool(TopicMapGraphPanel gp) {
        super(gp);
        graphPanel = gp;
    }
    

    
    private void initializeSlider(TopicMapGraphPanel gp) {
        int minValue = getMinValue(gp);
        int maxValue = getMaxValue(gp);
        int defaultValue = getDefaultValue(gp);
        
        if(defaultValue < minValue) defaultValue = minValue;
        if(defaultValue > maxValue) defaultValue = maxValue;

        slider = new SimpleSlider(SimpleSlider.HORIZONTAL, minValue, maxValue, defaultValue);
        sliderLabel = new SimpleLabel();
        sliderPopup = new JWindow();

        slider.setPreferredSize(new Dimension(120, 24));
        slider.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        sliderLabel.setFont(UIConstants.smallButtonLabelFont);
        sliderLabel.setPreferredSize(new Dimension(30, 24));
        sliderLabel.setHorizontalAlignment(SimpleLabel.CENTER);
        
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(UIConstants.defaultBorderShadow));
        panel.setLayout(new BorderLayout(2,2));
        panel.add(slider, BorderLayout.CENTER);
        panel.add(sliderLabel, BorderLayout.EAST);

        sliderPopup.setLayout(new BorderLayout(2,2));
        sliderPopup.add(panel, BorderLayout.CENTER);
        sliderPopup.setSize(150, 24);

        // sliderPopup.addMouseListener(this);
        sliderPopup.setAlwaysOnTop(true);
        
        slider.addChangeListener(this);
        slider.addMouseListener(this);
    }
    
    
    
    
    @Override
    public void execute(Wandora admin, Context context) {
        // graphPanel = this.solveGraphPanel(admin, context);
        if(graphPanel != null) {
            ActionEvent ae = context.getContextEvent();
            Object o = ae.getSource();
            if(o instanceof Component) {
                referenceComponent = (Component) o;
                if(sliderPopup == null) initializeSlider(graphPanel);
                if(!getSliderVisible()) {
                    setSliderRange(getMinValue(graphPanel),getMaxValue(graphPanel));
                    setSliderValue(getDefaultValue(graphPanel));
                    setSliderLocation(referenceComponent);
                    setSliderVisible(true);
                }
                else {
                    setSliderVisible(false);
                }
            }
        }
    }
    
    
    
    @Override
    public void executeSynchronized(Wandora admin, Context context) {
        // NOTHING HERE!
    }
    
    
    
    private void setSliderLocation(Component referenceComponent) {
        if(referenceComponent != null) {
            Point loc = referenceComponent.getLocationOnScreen();
            loc = new Point(loc.x+32, loc.y);
            sliderPopup.setLocation(loc);
        }
    }
    
    
    
    private boolean getSliderVisible() {
        if(sliderPopup != null) {
            return sliderPopup.isVisible();
        }
        else {
            return false;
        }
    }
    
    
    
    private void setSliderVisible(boolean v) {
        if(sliderPopup != null) {
            sliderPopup.setVisible(v);
        }
    }
    
    
    private void setSliderValue(int value) {
        if(slider != null) {
            if(value < slider.getMinimum()) value = slider.getMinimum();
            if(value > slider.getMaximum()) value = slider.getMaximum();
            slider.setValue(value);
        }
        if(sliderLabel != null) {
            sliderLabel.setText(""+value);
        }
    }
    
    
    private void setSliderRange(int minValue, int maxValue) {
        if(slider != null) {
            slider.setMinimum(minValue);
            slider.setMaximum(maxValue);
        }
    }
    
    
    
    @Override
    public void stateChanged(ChangeEvent e) {
        SimpleSlider source = (SimpleSlider)e.getSource();
        int newValue = (int)source.getValue();
        if(sliderLabel != null) sliderLabel.setText(""+newValue);
        if(!source.getValueIsAdjusting()) {
            setValue(graphPanel, newValue);
        }
    }
    
    
    
    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        sliderPopup.setVisible(false);
        if(referenceComponent != null && referenceComponent instanceof SimpleToggleButton) {
            ((SimpleToggleButton) referenceComponent).setSelected(false);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }
    
    
 
    protected abstract int getDefaultValue(TopicMapGraphPanel graphPanel);
    protected abstract void setValue(TopicMapGraphPanel graphPanel, int newValue);
    protected abstract int getMinValue(TopicMapGraphPanel graphPanel);
    protected abstract int getMaxValue(TopicMapGraphPanel graphPanel);
    
    
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
