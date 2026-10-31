package org.wandora.topicmap.undowrapper.tests;

import java.io.IOException;
import java.io.Writer;

import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public interface Test {
    public String getLabel();
    public void run() throws TopicMapException;
    public boolean isPassed();
    public void getMessages(Writer out) throws IOException;
}
