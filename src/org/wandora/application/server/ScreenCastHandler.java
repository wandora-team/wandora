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

package org.wandora.application.server;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.application.Wandora;
import org.wandora.utils.Options;

/**
 *
 * @author akivela
 */


public class ScreenCastHandler implements WebAppHandler {

    private String format="jpeg";


    
    public boolean getPage(WandoraWebApp app, WandoraWebAppServer server, String target, HttpServletRequest request, HttpServletResponse response) {
        try {
            Wandora w = Wandora.getWandora();
            Rectangle rect = new Rectangle(w.getX(), w.getY(), w.getWidth(), w.getHeight());
            Robot r = new Robot();
            BufferedImage img = r.createScreenCapture(rect);
            OutputStream out = response.getOutputStream();
            response.setContentType("image/"+format);
            ImageIO.write(img,format,out);
            out.close();
        }
        catch(Exception e) {
            server.log(e);
            server.writeResponse(response,HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Exception processing request",e);
        }
        return true;
    }

    public void init(WandoraWebApp app, WandoraWebAppServer server, Options options) {
    }

    public void save(WandoraWebApp app, WandoraWebAppServer server, Options options) {
    }

    public void start(WandoraWebApp app, WandoraWebAppServer server) {
    }

    public void stop(WandoraWebApp app, WandoraWebAppServer server) {
    }

    public ConfigComponent getConfigComponent(WandoraWebApp app, WandoraWebAppServer server) {
        return null;
    }
    
}
