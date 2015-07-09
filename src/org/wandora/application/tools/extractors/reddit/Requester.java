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

package org.wandora.application.tools.extractors.reddit;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import java.util.ArrayList;

/**
 * 
 * A simple helper class to handle requests
 * 
 * This class wraps the Unirest request API into a queue of requests, that
 * are executed at most once per two seconds.
 * 
 * @author Eero Lehtonen
 * @author akivela
 */

public class Requester {

    private static final int REQUEST_DELAY = 2000; // 1 req / 2 s
    private static final int WAIT_DELAY = 200;
    private ArrayList<Request> requestQueue;
    private int runCounter = 0;
    private int failCounter = 0;

    
    
    public Requester() {
        requestQueue = new ArrayList();
        runCounter = 0;
        failCounter = 0;
        Unirest.clearDefaultHeaders();
        setUserAgent();
    }

    
    public static void setUserAgent() {
        Unirest.setDefaultHeader("User-Agent", "java:wandora:v20150708 (by /u/wandora_app)");
    }
    public static void setUserAgent(String userAgent) {
        Unirest.setDefaultHeader("User-Agent", userAgent);
    }
    
    
    
    public void addRequest(BaseRequest hr, ParseCallback<JsonNode> callback) {
        Request r = new Request(hr, callback);
        requestQueue.add(r); // Queue up the request.
    }

    
    
    public void runNext() {
        if(requestQueue.isEmpty()) return;

        Request currentRequest = requestQueue.remove(0);
        currentRequest.run();
        
        // Wait for minimun REQUEST_DELAY milliseconds.
        sleep(REQUEST_DELAY);
        
        // Wait until currentRequest has finished. This is necessary as the
        // currentRequest may add new requests to the queue. If we skip this wait,
        // the extraction may stop before all request are processed. There is a
        // maximum wait time of 100*WAIT_DELAY milliseconds to prevent infinite
        // loops.
        int waitCount = 0;
        do {
            sleep(WAIT_DELAY);
        }
        while(!currentRequest.runFinished && ++waitCount < 100);
    }
    
    
    
    
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        }
        catch(Exception e) {
            // WAKE UP
        }
    }
    
    
    public void clear() {
        requestQueue.clear();
    }
    
    public int size() {
        return requestQueue.size();
    }

    public boolean hasRequests(){
        return !requestQueue.isEmpty();
    }

    public int getRunCounter() {
        return runCounter;
    }
    
    public int getFailCounter() {
        return failCounter;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public class Request {
        private BaseRequest request;
        private ParseCallback<JsonNode> callback;
        public boolean runFinished = false;
        

        Request(BaseRequest r, ParseCallback<JsonNode> callback) {
            this.request = r;
            this.callback = callback;
            this.runFinished = false;
        }

        
        /*
            Running the request is asynchronous. The method returns until the 
            request has been processed. Variable runFinished is used to keep 
            track of the finishing of the request. Requester can't proceed with 
            next request until the request has been completed, failed or 
            cancelled.
        */
        public void run() {
            this.request.asJsonAsync(new Callback<JsonNode>() {
                @Override
                public void completed(HttpResponse<JsonNode> response) {
                    callback.run(response);
                    runFinished = true;
                    runCounter++;
                }

                @Override
                public void failed(UnirestException e) {
                    try {
                        HttpResponse<String> s = request.asString();
                        callback.error(e, s.getBody());
                    } 
                    catch (Exception e2) {
                        callback.error(e2);
                    }
                    runFinished = true;
                    failCounter++;
                }

                @Override
                public void cancelled() {
                    callback.error(new Exception("Request was cancelled"));
                    runFinished = true;
                }
            });
        }
    }
}
