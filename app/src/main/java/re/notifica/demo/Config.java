package re.notifica.demo;

/**
 * Created by joel on 03/01/2017.
 */

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;


public class Config {

    private Context mContext;


    public Config(Context context) {
        this.mContext = context;
    }


    public String getProperty(String property) {
        JsonObject result = toJsonObject(AppBaseApplication.getConfigJSONString());
        return result.get(property).getAsString();
    }

    public JsonArray getArray(String property) {
        JsonObject result = toJsonObject(AppBaseApplication.getConfigJSONString());
        return result.get(property).getAsJsonArray();
    }

    public JsonObject getObject(String property) {
        JsonObject result = toJsonObject(AppBaseApplication.getConfigJSONString());
        return result.get(property).getAsJsonObject();
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

    private static JsonObject toJsonObject(String jsonString) {
        if (jsonString == null) return null;

        JsonParser parser = new JsonParser();
        return parser.parse(jsonString).getAsJsonObject();
    }

}

