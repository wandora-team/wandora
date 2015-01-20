

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
 * SketchTemplate.java
 *
 */



package org.wandora.application.gui.topicpanels.processing;

import java.awt.*;
import java.awt.event.*;
import org.wandora.topicmap.*;
import processing.core.*;
import org.wandora.application.gui.topicpanels.*;

/**
 *
 * @author olli
 */
public class SketchTemplate extends PApplet implements ActionListener, ComponentListener {
    private Topic topic;
    private TopicMap topicmap;
    private ProcessingTopicPanel processingPanel = null;


    public SketchTemplate() {
        this.addComponentListener(this);
    }


    public void wandoraInit(Topic topic, ProcessingTopicPanel processingPanel) {
	this.topic = topic;
	this.topicmap = topic.getTopicMap();
	this.processingPanel = processingPanel;
    }


    public Topic getCurrentTopic() {
        return topic;
    }
    public TopicMap getTopicMap() {
        return topicmap;
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        resizeParentComponent();
    }

    public void componentMoved(ComponentEvent e) {
        resizeParentComponent();
    }

    public void componentShown(ComponentEvent e) {
        resizeParentComponent();
    }

    public void componentHidden(ComponentEvent e) {
        resizeParentComponent();
    }
    private void resizeParentComponent() {
        /*
        System.out.println("Component resized!");
        Component p = this.getParent();
        if(p != null) {
            Dimension size = new Dimension(this.getWidth()+2, this.getHeight()+2);
            System.out.println("    new dimension: "+size);
            p.setPreferredSize(size);
	    p.setMinimumSize(size);
            p.setMaximumSize(size);
        }
         *
         */
    }

    @Override
    public void size(int w, int h) {
        Dimension size = new Dimension(w,h);
        this.setMinimumSize(size);
        this.setPreferredSize(size);
        this.setMaximumSize(size);
        Component p = this.getParent();
        if(p != null) {
            p.setPreferredSize(size);
	    p.setMinimumSize(size);
            p.setMaximumSize(size);
        }
        super.size(w, h);
    }

    @Override
    public void size(int w, int h, String renderer) {
        Dimension size = new Dimension(w,h);
        this.setMinimumSize(size);
        this.setPreferredSize(size);
        this.setMaximumSize(size);
        Component p = this.getParent();
        if(p != null) {
            p.setPreferredSize(size);
	    p.setMinimumSize(size);
            p.setMaximumSize(size);
        }
        super.size(w, h, renderer);
    }

    @Override
    public void size(int w, int h, String renderer, String path) {
        Dimension size = new Dimension(w,h);
        this.setMinimumSize(size);
        this.setPreferredSize(size);
        this.setMaximumSize(size);
        Component p = this.getParent();
        if(p != null) {
            p.setPreferredSize(size);
	    p.setMinimumSize(size);
            p.setMaximumSize(size);
        }
        super.size(w, h, renderer, path);
    }
}
