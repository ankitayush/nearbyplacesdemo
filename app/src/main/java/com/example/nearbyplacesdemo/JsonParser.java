package com.example.nearbyplacesdemo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser {
    private HashMap<String,String> parseJsonObject(JSONObject object){
        //Initialize hash map
        HashMap<String,String> dataList=new HashMap<>();

        //get name from object
        try {
            String name= object.getString( "name");
            //Get latitude from object
            String latitude =object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lat");
            //Get longitude from object
            String longitude =object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lng");
            //Put all value in HashMap
            dataList.put("name",name);
            dataList.put("lat",latitude);
            dataList.put("lng",longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Return HashMap
        return dataList;
    }

    private List<HashMap<String,String>>parseJsonArray(JSONArray jsonArray){
        //Initailize hash map list
        List<HashMap<String,String>> dataList =new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){
            try {
                //initialize the hashmap
                HashMap<String,String> data=parseJsonObject((JSONObject) jsonArray.get(i));

                //Add data in hash map list
                dataList.add(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dataList;
    }

    public List<HashMap<String,String>> parseResult(JSONObject object){
        //Initailize json Array
        JSONArray jsonArray=null;
        //Get result Array
        try {
            jsonArray=object.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return parseJsonArray(jsonArray);

    }

}