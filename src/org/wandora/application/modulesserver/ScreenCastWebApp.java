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
 */
package org.wandora.application.modulesserver;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.application.Wandora;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet;
import org.wandora.modules.usercontrol.User;

/**
 *
 * @author olli
 */


public class ScreenCastWebApp extends AbstractWebApp {

    protected String format="jpeg";

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o=settings.get("format");
        if(o!=null) format=o.toString().trim();
        super.init(manager, settings);
    }
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }
    
    
    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, String action, User user) throws ServletException, IOException, ActionException {
        try {
            Wandora w = Wandora.getWandora();
            
            GraphicsDevice gd=null;
            GraphicsConfiguration gc=w.getGraphicsConfiguration();
            if(gc!=null) gd=gc.getDevice();            
            
            Rectangle rect = new Rectangle(w.getX(), w.getY(), w.getWidth(), w.getHeight());
            Robot r = null;
            if(gd!=null) {
                r=new Robot(gd);
                Rectangle bounds=gc.getBounds();
                rect = new Rectangle(rect.x-bounds.x, rect.y-bounds.y, rect.width, rect.height);
            }
            else r=new Robot();
            
            BufferedImage img = r.createScreenCapture(rect);
            OutputStream out = resp.getOutputStream();
            resp.setContentType("image/"+format);
            ImageIO.write(img,format,out);
            out.close();
            return true;
        }
        catch(Exception e) {
            logging.error(e);
            return false;
        }
    }
    
}
