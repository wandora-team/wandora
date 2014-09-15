package org.wandora.application.tools.extractors.flickr;

import java.awt.Frame;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.tools.extractors.flickr.FlickrExtractor.RequestFailure;
import org.wandora.application.tools.extractors.flickr.FlickrExtractor.UserCancellation;
import org.wandora.utils.IObox;

public class FlickrState {

    public String Frob;
    public String Token;
    public String PermissionLevel;
    public String AuthedUserName;
    public String LastUserName;
    public String LastUserID;
    public TreeMap<String, String> Users;
    public TreeMap<String, String> Groups;
    
    public static final String
            PermNone = "none",
            PermRead = "read",
            PermWrite = "write",
            PermDelete = "delete",
            ApiKey = "1eab422260e1c488c998231f290330eb", // "38e1943f013d3625295b7549d3d2898a", //"1eab422260e1c488c998231f290330eb",
            ApiSecret = "d2094033862921ac", // "e50f5106e5684bad", //"d2094033862921ac",
            RESTbase = "https://api.flickr.com/services/rest/";
    public static int getAuthLevel(String authLevel) {
        if(authLevel.equals(FlickrState.PermNone))
            return 0;
        if(authLevel.equals(FlickrState.PermRead))
            return 1;
        if(authLevel.equals(FlickrState.PermWrite))
            return 2;
        if(authLevel.equals(FlickrState.PermDelete))
            return 3;
        
        return -1;
    }
    public static String getAuthLevel(int authLevel) {
        switch(authLevel)
        {
            case 0:
                return FlickrState.PermNone;
            case 1:
                return FlickrState.PermRead;
            case 2:
                return FlickrState.PermWrite;
            case 3:
                return FlickrState.PermDelete;
            default:
                return null;
        }
    }

    public FlickrState() {
        super();
        Users = new TreeMap<String, String>();
        Groups = new TreeMap<String, String>();

        PermissionLevel = PermNone;
    }
    public static String makeRESTURL(Map<String, String> params) {
        return makeRESTURL(params, FlickrState.RESTbase);
    }
    public static String makeRESTURL(Map<String, String> params, String baseUrl) {
        char introducer = '?';
        StringBuilder bldr = new StringBuilder(baseUrl);
        
        for(Map.Entry<String, String> e : params.entrySet())
        {
            bldr.append(introducer + e.getKey() + '=' + e.getValue());
            introducer = '&';
        }
        
        return bldr.toString();
    }   

    public JSONObject unauthorizedCall(String method, SortedMap<String, String> args) throws RequestFailure {
        args.put("method", method);
        args.put("api_key", ApiKey);
        args.put("format", "json");
        args.put("nojsoncallback", "1");
        try {
            return new JSONObject(IObox.doUrl(new URL(makeRESTURL(args))));
        } catch (MalformedURLException e) {
            throw new RequestFailure("", e);
        } catch (JSONException e) {
            throw new RequestFailure("", e);
        } catch (IOException e) {
            throw new RequestFailure("", e);
        }
    }

    public void authenticate(String perms, Frame dlgParent) throws RequestFailure, UserCancellation {
        if (Frob == null) {
            Frob = FlickrExtractor.getFrob();
        }
        if (Token == null || !validToken(perms)) {
            String loginURL = getLoginURL(perms);
            TokenRequestDialog dlg = new TokenRequestDialog(dlgParent, true, loginURL, perms, this);
            dlg.setVisible(true);
            if (dlg.cancelled()) {
                throw new UserCancellation("User cancelled authorization");
            }
            Token = requestToken();
        }
    }

    public JSONObject authorizedCall(String method, SortedMap<String, String> args, String perms, Frame dlgParent) throws RequestFailure, UserCancellation {
        authenticate(perms, dlgParent);

        args.put("method", method);
        args.put("auth_token", Token);
        args.put("api_key", ApiKey);
        args.put("format", "json");
        args.put("nojsoncallback", "1");
        args.put("api_sig", FlickrExtractor.createSignature(args));
        String url = makeRESTURL(args);
        try {
            return new JSONObject(IObox.doUrl(new URL(url)));
        } catch (MalformedURLException e) {
            throw new RequestFailure("Malformed url:\n" + url, e);
        } catch (JSONException e) {
            throw new RequestFailure("Invalid response JSON, url:\n" + url, e);
        } catch (IOException e) {
            throw new RequestFailure("Failure with call, url:\n" + url, e);
        }
    }

    public boolean validToken(String neededPerms) throws RequestFailure {
        if (Token == null) {
            return false;
        }
        if (neededPerms.equals(PermNone)) {
            return true;
        }
        TreeMap<String, String> args = new TreeMap<String, String>();
        args.put("method", "flickr.auth.checkToken");
        args.put("api_key", ApiKey);
        args.put("auth_token", Token);
        args.put("format", "json");
        args.put("nojsoncallback", "1");
        args.put("api_sig", FlickrExtractor.createSignature(args));
        try {
            String response = IObox.doUrl(new URL(makeRESTURL(args)));
            JSONObject obj = new JSONObject(response);
            if (!obj.getString("stat").equals("ok")) {
                if (obj.getInt("code") == 98) {
                    return false;
                }
                throw new RequestFailure(String.valueOf(obj.getInt("code")) + ": " + obj.getString("message"));
            }
            String tokenPerms = FlickrUtils.searchString(obj, "auth.perms._content");
            PermissionLevel = tokenPerms;

            return getAuthLevel(tokenPerms) >= getAuthLevel(neededPerms);
        } catch (JSONException e) {
            throw new RequestFailure("Received malformed json", e);
        } catch (MalformedURLException e) {
            throw new RequestFailure("Attempted to construct malformed URL", e);
        } catch (IOException e) {
            throw new RequestFailure("IOException while requesting token check", e);
        }
    }

    public String requestToken() throws RequestFailure {
        TreeMap<String, String> args = new TreeMap<String, String>();
        args.put("method", "flickr.auth.getToken");
        args.put("api_key", ApiKey);
        args.put("frob", Frob);
        args.put("nojsoncallback", "1");
        args.put("format", "json");
        args.put("api_sig", FlickrExtractor.createSignature(args));
        try {
            JSONObject obj = new JSONObject(IObox.doUrl(new URL(makeRESTURL(args))));
            if (!obj.getString("stat").equals("ok")) {
                throw new RequestFailure("Error while trying to request authorization token.\nDid you follow the url and authorize Wandora?\n\n" + String.valueOf(obj.getInt("code")) + ": " + obj.getString("message"));
            }

            LastUserID = FlickrUtils.searchString(obj, "auth.user.nsid");
            LastUserName = FlickrUtils.searchString(obj, "auth.user.username");
            PermissionLevel = FlickrUtils.searchString(obj, "auth.perms._content");
            return FlickrUtils.searchString(obj, "auth.token._content");
        } catch (MalformedURLException e) {
            throw new RequestFailure("Attempted to create malformed URL", e);
        } catch (JSONException e) {
            throw new RequestFailure("Received invalid json data", e);
        } catch (IOException e) {
            throw new RequestFailure("Error while connecting", e);
        }
    }

    public String getLoginURL(String perms) throws RequestFailure {
        TreeMap<String, String> args = new TreeMap<String, String>();

        args.put("api_key", ApiKey);
        args.put("perms", perms);
        args.put("frob", Frob);
        args.put("api_sig", FlickrExtractor.createSignature(args));

        return makeRESTURL(args, "https://www.flickr.com/services/auth/");
    }
}
