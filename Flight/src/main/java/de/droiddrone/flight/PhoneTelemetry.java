/*
 *  This file is part of DroidDrone.
 *
 *  DroidDrone is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DroidDrone is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DroidDrone.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.droiddrone.flight;

import static de.droiddrone.common.Logcat.log;

import android.content.Context;
import android.os.BatteryManager;

import java.util.concurrent.ArrayBlockingQueue;

import de.droiddrone.common.DataWriter;
import de.droiddrone.common.FcCommon;
import de.droiddrone.common.TelemetryData;

public class PhoneTelemetry {
    private final Context context;
    private final StreamEncoder streamEncoder;
    private final Camera camera;
    private final Mp4Recorder mp4Recorder;
    public final ArrayBlockingQueue<TelemetryData> telemetryOutputBuffer = new ArrayBlockingQueue<>(10);
    private int threadsId;
    private BatteryManager batteryManager = null;

    public PhoneTelemetry(Context context, StreamEncoder streamEncoder, Camera camera, Mp4Recorder mp4Recorder) {
        this.context = context;
        this.streamEncoder = streamEncoder;
        this.camera = camera;
        this.mp4Recorder = mp4Recorder;
    }

    public void initialize() {
        try {
            batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        } catch (Exception e) {
            log("BatteryManager error: " + e);
        }
        telemetryOutputBuffer.clear();
        threadsId++;
        Thread phoneTelemetryThread = new Thread(phoneTelemetryRun);
        phoneTelemetryThread.setDaemon(false);
        phoneTelemetryThread.setName("mspThread");
        phoneTelemetryThread.start();
    }

    private final Runnable phoneTelemetryRun = new Runnable() {
        public void run() {
            final int id = threadsId;
            final int timerDelayMs = 1000;
            log("Start phone telemetry thread - OK");
            while (id == threadsId) {
                try {
                    sendCameraFps();
                    sendVideoBitRate();
                    sendRecorderState();
                    sendBatteryState();
                    Thread.sleep(timerDelayMs);
                } catch (Exception e) {
                    log("Phone telemetry thread error: " + e);
                }
            }
        }
    };

    private void sendRecorderState() {
        boolean isRecording = mp4Recorder.isRecording();
        long startRecordingTimestamp = mp4Recorder.getStartRecordingTimestamp();
        int recordingTimeSec = 0;
        if (isRecording && startRecordingTimestamp > 0) {
            recordingTimeSec = (int) (System.currentTimeMillis() - startRecordingTimestamp) / 1000;
        }
        DataWriter writer = new DataWriter(false);
        writer.writeBoolean(isRecording);
        writer.writeInt(recordingTimeSec);
        telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_VIDEO_RECORDER_STATE, writer.getData()));
    }

    private void sendVideoBitRate() {
        DataWriter writer = new DataWriter(false);
        writer.writeFloat(streamEncoder.getBitRate());
        telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_VIDEO_BIT_RATE, writer.getData()));
    }

    private void sendCameraFps() {
        DataWriter writer = new DataWriter(false);
        writer.writeShort((short) camera.getFps());
        telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_CAMERA_FPS, writer.getData()));
    }

    private void sendBatteryState() {
        if (batteryManager == null) return;
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        if (batteryLevel < 0) return;
        boolean isCharging = batteryManager.isCharging();
        DataWriter writer = new DataWriter(false);
        writer.writeByte((byte) batteryLevel);
        writer.writeBoolean(isCharging);
        telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_PHONE_BATTERY_STATE, writer.getData()));
    }

    public void close(){
        threadsId++;
        telemetryOutputBuffer.clear();
    }
}
