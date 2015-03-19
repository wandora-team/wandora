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
package org.wandora.application.tools.extractors.word;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.wandora.application.Wandora;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Options;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */
public class SimpleWordMatchingExtractor extends AbstractWordExtractor {

    private final String SI_BASE = "http://wandora.org/si/simple-word/";
    private final String BN_SUFFIX = "(simple word extractor)";
    private SimpleWordConfiguration config;
    
    @Override
    public String getName(){
        return "Simple Word Matching Extractor";
    }
    @Override
    public String getDescription(){
        return "Matches given words and regular expressions to topic data and"
                + "creates associations between matched topics and given words.";
    }
    
    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void configure(Wandora w, Options o, String p) throws TopicMapException {

        SimpleWordConfigurationDialog d = new SimpleWordConfigurationDialog(w);
        d.openDialog(config);

        if (d.wasAccepted()) {
            config = d.getConfiguration();
        }

    }

    @Override
    WordConfiguration getConfig() {
        if(config == null) config = new SimpleWordConfiguration();
        return config;
    }
    
    @Override
    protected float isMatch(Object needle, String haystack) {

        if (needle instanceof Pattern) {

            Pattern p = (Pattern) needle;

            return p.matcher(haystack).find() ? 1f : 0f;

        } else if (needle instanceof String) {

            String s = (String) needle;

            int index = haystack.indexOf(s);
            boolean isMatch = (index != -1);

            if (config.getMatchWords() && isMatch) {

                if (index > 0
                        && Character.isLetterOrDigit(haystack.charAt(index - 1))) {
                    isMatch = false;
                }

                if (index + s.length() < haystack.length()
                        && Character.isLetterOrDigit(haystack.charAt(index + s.length()))) {
                    isMatch = false;
                }

            }

            return isMatch ? 1f : 0f;

        } else {
            throw new UnsupportedOperationException("Match operation failed.");
        }

    }

    @Override
    protected Object formNeedle(String word) {
        
        Object needle;
        
        if (config.getRegex()) {
            try {
                needle = Pattern.compile(word);
            } catch (PatternSyntaxException pse) {
                throw new IllegalArgumentException("Invalid regex syntax for "
                        + "pattern \"" + word + "\": " + pse.getMessage());
            }
        } else if (!config.getCaseSensitive()) {
            needle = word.toLowerCase();
        } else {
            needle = word;
        }
        
        return needle;
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
