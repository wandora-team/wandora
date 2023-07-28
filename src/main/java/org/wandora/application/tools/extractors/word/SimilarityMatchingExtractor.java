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

package org.wandora.application.tools.extractors.word;

import org.wandora.application.Wandora;
import static org.wandora.application.gui.search.SimilarityBox.getSimilarity;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;
import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;

/**
 *
 * @author Eero Lehtonen
 */


public class SimilarityMatchingExtractor extends AbstractWordExtractor{


	private static final long serialVersionUID = 1L;

	private final String SI_BASE = "http://wandora.org/si/word-similarity/";
    private static final String BN_SUFFIX = "(word similarity matching extractor)";
    
    protected SimilarityWordConfiguration config;
    
    @Override
    public String getName(){
        return "Word Similarity Matching Extractor";
    }
    @Override
    public String getDescription(){
        return "Matches given words to topic data with given similarity measure"
                + " and creates associations between matched topics and given"
                + " words.";
    }
    
    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void configure(Wandora w, Options o, String p) throws TopicMapException {

        SimilarityWordConfigurationDialog d = new SimilarityWordConfigurationDialog(w);
        d.openDialog(config);

        if (d.wasAccepted()) {
            config = d.getConfiguration();
        }

    }
    
    @Override
    WordConfiguration getConfig() {
        if(config == null) config = new SimilarityWordConfiguration();
        return config;
    }

    @Override
    protected Object formNeedle(String s) {
        if (!config.getCaseSensitive()) {
            s = s.toLowerCase();
        }
        return s;
    }

    @Override
    protected float isMatch(Object needle, String haystack) {
        InterfaceStringMetric metric = config.getStringMetric();
        float sim = getSimilarity((String)needle, haystack, metric, true);
        return (sim > config.getThreshold()) ? sim : 0f;
    }

    @Override
    protected String getBNSuffix() {
        return BN_SUFFIX;
    }
    
    @Override
    protected String getSIBase() {
        return SI_BASE;
    }

    
}
