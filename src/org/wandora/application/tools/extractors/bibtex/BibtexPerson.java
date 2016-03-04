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
 * BibtexPerson.java
 *
 * Created on 17. lokakuuta 2007, 11:11
 *
 */

package org.wandora.application.tools.extractors.bibtex;
import java.util.*;
import java.util.regex.*;
/**
 *
 * @author olli
 */
public class BibtexPerson {
    
    private String firstName;
    private String lastName;
    private String initials;
    
    /** Creates a new instance of BibtexPerson */
    public BibtexPerson() {
    }
    public BibtexPerson(String firstName,String initials,String lastName) {
        setFirstName(firstName);
        setInitials(initials);
        setLastName(lastName);
    }
    
    public String getFirstName(){return firstName;}
    public void setFirstName(String firstName){this.firstName=firstName;}
    public String getLastName(){return lastName;}
    public void setLastName(String lastName){this.lastName=lastName;}
    public String getInitials(){return initials;}
    public void setInitials(String initials){this.initials=initials;}
    
}
