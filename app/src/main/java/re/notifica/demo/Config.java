package re.notifica.demo;

/**
 * Created by joel on 03/01/2017.
 */

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class Config {

    private Context mContext;


    public Config(Context context) {
        this.mContext = context;
    }


    public String getProperty(String property) {
        try {
            JSONObject result = new JSONObject(AppBaseApplication.getConfigJSONString());
            return result.getString(property);
        } catch (JSONException e) {
            return null;
        }

    }

    public JSONArray getArray(String property) {
        try {
            JSONObject result = new JSONObject(AppBaseApplication.getConfigJSONString());
            return result.getJSONArray(property);
        } catch (JSONException e) {
            return null;
        }
    }

    public JSONObject getObject(String property) {
        try {
            JSONObject result = new JSONObject(AppBaseApplication.getConfigJSONString());
            return result.getJSONObject(property);
        } catch (JSONException e) {
            return null;
        }
    }

    private static String assetJSONFile(String filename, Context context) {
        try {
            AssetManager manager = context.getAssets();
            InputStream file = manager.open(filename);
            byte[] formArray = new byte[file.available()];
            file.read(formArray);
            file.close();

            return new String(formArray);
        } catch (IOException e) {
            return null;
        }
    }

}

