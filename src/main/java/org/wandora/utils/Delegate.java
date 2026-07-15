/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * Delegate.java
 *
 * Created on 7. tammikuuta 2005, 13:31
 */

package org.wandora.utils;

/**
 * <p>
 * Delegate is a function wrapped in an Object. Normally you will want to make
 * an anonymous inner class implementing Delegate and pass that object somewhere.
 * Delegate can be used as a generic listener (the delegate is invoked on an
 * event) or some kind of other handler. If you need to use more than one
 * parameter, you can use the com.gripstudios.utils.Tuples library.
 * </p><p>
 * If your delegate doesn't return a value, you can set it to return Object and
 * then just return null or you can set the return value to Delegate.Void and
 * return Delegate.VOID to make it clear that the return value means nothing.
 * </p><p>
 * Using Delegate class you can use some programming techniques readily available
 * in functional programming languages allthough the syntax in Java becomes
 * somewhat inconvenient.
 * </p>
 * @author olli
 */
public interface Delegate<R,P> {    
    public R invoke(P param);
    public static final class Void{
        private Void(){}
    }
    public static final Void VOID=new Void();
}
