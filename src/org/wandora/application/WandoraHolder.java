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
 *
 * 
 * 
 * WandoraHolder.java
 *
 * Created on 13. heinäkuuta 2005, 12:09
 */

package org.wandora.application;

/**
 * This class is a wrapper for Wandora. Purpose of it is to be able to
 * pass a reference to Wandora before it is actually constructed. This can
 * be achieved by creating a new instance of WandoraAdminHolder without Wandora.
 * Then later setting Wandora in the holder. This was needed when wandora
 * and most of its tools were initialized and/or implemented with bean shell
 * scripting language. Currently this class has little use.
 *
 * @author olli
 */
public class WandoraHolder {
    
    private Wandora wandora=null;
    
    /** Creates a new instance of WandoraAdminHolder */
    public WandoraHolder() {
    }
    public WandoraHolder(Wandora wandora) {
        this.wandora=wandora;
    }
    
    public void setWandoraAdmin(Wandora wandora){this.wandora=wandora;}
    public Wandora getWandoraAdmin(){return wandora;};
    
}
