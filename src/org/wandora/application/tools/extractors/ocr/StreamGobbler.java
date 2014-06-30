
package org.wandora.application.tools.extractors.ocr;

/**
 *http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 */

import java.io.*;

class StreamGobbler extends Thread {

    InputStream is;
    StringBuilder outputMessage = new StringBuilder();

    StreamGobbler(InputStream is) {
        this.is = is;
    }

    String getMessage() {
        return outputMessage.toString();
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                outputMessage.append(line).append("\n");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}