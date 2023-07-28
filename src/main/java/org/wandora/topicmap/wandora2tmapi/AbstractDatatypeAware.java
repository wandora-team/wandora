/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2013 Wandora Team
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
package org.wandora.topicmap.wandora2tmapi;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.tmapi.core.DatatypeAware;
import org.tmapi.core.Locator;
import org.tmapi.core.ModelConstraintException;

/**
 *
 * @author olli
 */


public abstract class AbstractDatatypeAware implements DatatypeAware {

    
    @Override
    public Locator getDatatype() {
        return new W2TLocator(W2TTopicMap.TYPE_STRING_SI);
    }

    @Override
    public abstract String getValue();
    @Override
    public abstract void setValue(String s);

    @Override
    public void setValue(Locator lctr) throws ModelConstraintException {
        setValue(lctr.toString());
    }

    @Override
    public void setValue(String string, Locator lctr) throws ModelConstraintException {
        setValue(string);
    }

    @Override
    public void setValue(BigDecimal bd) throws ModelConstraintException {
        setValue(bd.toString());
    }

    @Override
    public void setValue(BigInteger bi) throws ModelConstraintException {
        setValue(bi.toString());
    }

    @Override
    public void setValue(long l) {
        setValue(Long.toString(l));
    }

    @Override
    public void setValue(float f) {
        setValue(Float.toString(f));
    }

    @Override
    public void setValue(int i) {
        setValue(Integer.toString(i));
    }

    @Override
    public int intValue() {
        return Integer.parseInt(getValue());
    }

    @Override
    public BigInteger integerValue() {
        return new BigInteger(getValue());
    }

    @Override
    public float floatValue() {
        return Float.parseFloat(getValue());
    }

    @Override
    public BigDecimal decimalValue() {
        return new BigDecimal(getValue());
    }

    @Override
    public long longValue() {
        return Long.parseLong(getValue());
    }

    @Override
    public Locator locatorValue() {
        throw new IllegalArgumentException();
    }
    
}
