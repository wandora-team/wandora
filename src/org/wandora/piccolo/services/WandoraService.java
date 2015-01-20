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
 * 
 *
 * WandoraService.java
 *
 * Created on July 9, 2004, 12:30 PM
 */

package org.wandora.piccolo.services;
import org.wandora.piccolo.WandoraManager;
import org.wandora.*;
import org.wandora.piccolo.Service;
import org.wandora.piccolo.*;
import org.wandora.utils.*;
import java.util.*;
/**
 *
 * A class extending WandoraManager that implements the Service interface to make WandoraManager available
 * to actions as a service.
 *
 * @author  olli
 */
public class WandoraService extends WandoraManager implements Service {
    public WandoraService(org.wandora.piccolo.Logger logger) {
        super(logger,new Properties(),"");
    }
    public WandoraService(org.wandora.piccolo.Logger logger,Properties properties) {
        super(logger,properties,"");
    }
    public WandoraService(org.wandora.piccolo.Logger logger,Properties properties,String prefix) {
        super(logger,properties,prefix);
    }
    
    public String getServiceType(){
        return "WandoraManager";
    }
    public String getServiceName(){
        return "WandoraManager";
    }
    
}
