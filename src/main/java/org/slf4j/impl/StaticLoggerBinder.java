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
 */



package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.impl.WandoraLoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 *
 * @author akivela
 */


public class StaticLoggerBinder implements LoggerFactoryBinder {
 
    /**
     * The unique instance of this class.
     */
    private static final StaticLoggerBinder SINGLETON
        = new StaticLoggerBinder();
 
    /**
     * Return the singleton of this class.
     *
     * @return the StaticLoggerBinder singleton
     */
    public static final StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }
 
 
    /**
     * Declare the version of the SLF4J API this implementation is
     * compiled against. The value of this field is usually modified
     * with each release.
     */
    // To avoid constant folding by the compiler,
    // this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.7.6";  // !final
 
    private static final String loggerFactoryClassStr
        = WandoraLoggerFactory.class.getName();
 
    /**
     * The ILoggerFactory instance returned by the
     * {@link #getLoggerFactory} method should always be the same
     * object.
     */
    private final ILoggerFactory loggerFactory;
 
    private StaticLoggerBinder() {
        loggerFactory = new WandoraLoggerFactory();
    }
 
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }
 
    public String getLoggerFactoryClassStr() {
        return loggerFactoryClassStr;
    }
}