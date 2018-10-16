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
 */


package org.wandora.application.tools.extractors.csv;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.CSVParser;
import org.wandora.utils.CSVParser.Table;

/**
 *
 * @author akivela
 */


public abstract class AbstractCSVExtractor extends AbstractExtractor implements WandoraTool {


	private static final long serialVersionUID = 1L;
	
	
	protected static char csvStringCharacter = '"';
    protected static char csvLineSeparator = '\n';
    protected static char csvValueSeparator = ',';
    protected static String csvEncoding = "UTF-8";
    
    
    
    // -------------------------------------------------------------------------
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        CSVExtractorConfiguration conf = new CSVExtractorConfiguration();
        conf.setStringCharacter(csvStringCharacter);
        conf.setLineSeparator(csvLineSeparator);
        conf.setValueSeparator(csvValueSeparator);
        conf.setEncoding(csvEncoding);
        conf.open(wandora);
        if(conf.wasAccepted()) {
            csvStringCharacter = conf.getStringCharacter();
            csvLineSeparator = conf.getLineSeparator();
            csvValueSeparator = conf.getValueSeparator();
            csvEncoding = conf.getEncoding();
        }
    }
    
    // -------------------------------------------------------------------------
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        CSVParser parser = new CSVParser();
        parser.setEncoding(csvEncoding);
        parser.setLineSeparator(csvLineSeparator);
        parser.setValueSeparator(csvValueSeparator);
        parser.setStringCharacter(csvStringCharacter);
        Table table = parser.parse(f);
        return _extractTopicsFrom(table, tm);
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        CSVParser parser = new CSVParser();
        parser.setEncoding(csvEncoding);
        parser.setLineSeparator(csvLineSeparator);
        parser.setValueSeparator(csvValueSeparator);
        parser.setStringCharacter(csvStringCharacter);
        Table table = parser.parse(u.openStream());
        return _extractTopicsFrom(table, tm);
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        CSVParser parser = new CSVParser();
        parser.setEncoding(csvEncoding);
        parser.setLineSeparator(csvLineSeparator);
        parser.setValueSeparator(csvValueSeparator);
        parser.setStringCharacter(csvStringCharacter);
        Table table = parser.parse(new ByteArrayInputStream(str.getBytes()));
        return _extractTopicsFrom(table, tm);
    }
    
    
    
    public abstract boolean _extractTopicsFrom(Table table, TopicMap tm) throws Exception;
    
    
    
    // -------------------------------------------------------------------------
    
    

    
    
}
