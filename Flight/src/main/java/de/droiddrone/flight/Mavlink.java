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

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_autopilot_version;
import com.MAVLink.common.msg_battery_status;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_home_position;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_value;
import com.MAVLink.common.msg_rc_channels;
import com.MAVLink.common.msg_scaled_pressure;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_system_time;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_MODE_FLAG;
import com.MAVLink.minimal.msg_heartbeat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import de.droiddrone.common.DataReader;
import de.droiddrone.common.DataWriter;
import de.droiddrone.common.FcCommon;
import de.droiddrone.common.OsdCommon;
import de.droiddrone.common.FcInfo;
import de.droiddrone.common.TelemetryData;
import de.droiddrone.common.Utils;

public class Mavlink {
    private final Serial serial;
    private final Config config;
    public final ArrayBlockingQueue<TelemetryData> telemetryOutputBuffer = new ArrayBlockingQueue<>(30);
    private final int systemId = 255;
    private final int componentId = 1;
    private final short targetSystem = 1;
    private final short targetComponent = 1;
    private final int fcVariant;
    private final int apiProtocolVersion;
    private int apiVersionMajor;
    private int apiVersionMinor;
    private int fcVersionMajor;
    private int fcVersionMinor;
    private int fcVersionPatchLevel;
    private FcInfo fcInfo;
    private boolean isMavlink2;
    private int threadsId;
    private boolean isHeartBeatReceived;
    private int sequence = 0;
    private boolean runGetOsdConfig;
    private FcParams fcParams;
    private int telemetryIntervalUs;
    private long lastAttitudeTs;
    private long lastBatteryStatusTs;
    private long lastSysStatusTs;
    private long lastGpsRawIntTs;
    private long lastGlobalPositionIntTs;
    private long lastSystemTimeTs;
    private long lastRcChannelsTs;
    private long lastScaledPressureTs;
    private long lastVfrHudTs;
    private int  platformType;
    private int batteryCellCountDetected;
    private boolean isStatusTextReceived;
    private boolean isHomePositionReceived;
    private boolean isArmed;
    private long armedTs = 0;
    private long flightTs = 0;
    private long flightTime = 0;
    private int throttle = 0;

    public Mavlink(Serial serial, Config config) {
        this.serial = serial;
        this.config = config;
        isMavlink2 = true;
        fcVariant = FcInfo.FC_VARIANT_ARDUPILOT;
        apiProtocolVersion = 0;
        apiVersionMajor = -1;
        apiVersionMinor = -1;
        fcVersionMajor = -1;
        fcVersionMinor = -1;
        fcVersionPatchLevel = -1;
        platformType = -1;
        isHeartBeatReceived = false;
        isStatusTextReceived = false;
        isHomePositionReceived = false;
        isArmed = false;
        batteryCellCountDetected = 0;
    }

    public boolean isInitialized(){
        boolean isInitialized = (apiVersionMajor != -1 && apiVersionMinor != -1
                && fcVersionMajor != -1 && fcVersionMinor != -1 && fcVersionPatchLevel != -1
                && platformType != -1);
        if (isInitialized && fcInfo == null) setFcInfo();
        return isInitialized;
    }

    private void setFcInfo(){
        fcInfo = new FcInfo(fcVariant, fcVersionMajor, fcVersionMinor, fcVersionPatchLevel, apiProtocolVersion, apiVersionMajor, apiVersionMinor, platformType);
        log(fcInfo.getFcName() + " Ver. " + fcInfo.getFcVersionStr() + " detected.");
        log("Mavlink API Ver.: " + fcInfo.getFcApiVersionStr());
    }

    public FcInfo getFcInfo(){
        return fcInfo;
    }

    public void runGetOsdConfig(){
        runGetOsdConfig = true;
    }

    public void initialize() {
        fcParams = new FcParams(this);
        telemetryOutputBuffer.clear();
        telemetryIntervalUs = 1000000 / config.getMspTelemetryRefreshRate();
        threadsId++;
        Thread mavlinkThread = new Thread(mavlinkRun);
        mavlinkThread.setDaemon(false);
        mavlinkThread.setName("mavlinkThread");
        mavlinkThread.start();
    }

    private final Runnable mavlinkRun = new Runnable() {
        public void run() {
            final int id = threadsId;
            final int timerDelayMs = 1000;
            log("Start Mavlink thread - OK");
            while (id == threadsId) {
                try {
                    if (!isInitialized()) {
                        if (isHeartBeatReceived) getFcVersion();
                        Thread.sleep(timerDelayMs);
                        continue;
                    }
                    if (runGetOsdConfig) {
                        if (fcParams.isOsdConfigInitialized()){
                            runGetOsdConfig = false;
                            fcParams.sendOsdConfig();
                        } else {
                            fcParams.initializeOsdConfig();
                        }
                    }
                    if (fcParams.isOsdConfigInitialized()) {
                        getAttitude(telemetryIntervalUs);
                        getBatteryStatus(telemetryIntervalUs * 10);
                        getSystemStatus(telemetryIntervalUs * 10);
                        getStatusText(telemetryIntervalUs * 20);
                        getGpsRawInt(telemetryIntervalUs * 10);
                        getGlobalPositionInt(telemetryIntervalUs * 10);
                        getHomePosition(telemetryIntervalUs * 50);
                        getSystemTime(telemetryIntervalUs * 10);
                        getRcChannels(telemetryIntervalUs * 10);
                        getScaledPressure(telemetryIntervalUs * 10);
                        getVfrHud(telemetryIntervalUs * 10);
                        requestFcParameter(FcCommon.AP_PARAM_VTX_POWER);
                    }
                    Thread.sleep(timerDelayMs);
                } catch (Exception e) {
                    log("Mavlink thread error: " + e);
                }
            }
        }
    };

    private void requestFcParameter(String paramIdStr){
        if (paramIdStr == null || paramIdStr.isEmpty() || paramIdStr.length() > 16) return;
        byte[] paramId = new byte[16];
        System.arraycopy(paramIdStr.getBytes(StandardCharsets.US_ASCII), 0, paramId, 0, paramIdStr.length());
        MAVLinkPacket packet = new msg_param_request_read((short)-1, targetSystem, targetComponent, paramId, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getFcVersion(){
        MAVLinkPacket packet = new msg_command_long(msg_autopilot_version.MAVLINK_MSG_ID_AUTOPILOT_VERSION, 0, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_REQUEST_MESSAGE, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getAttitude(int interval){
        if (interval > 0 && System.currentTimeMillis() - lastAttitudeTs < interval / 200) return;
        MAVLinkPacket packet = new msg_command_long(msg_attitude.MAVLINK_MSG_ID_ATTITUDE, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getBatteryStatus(int interval){
        if (interval > 0 && System.currentTimeMillis() - lastBatteryStatusTs < interval / 200) return;
        MAVLinkPacket packet = new msg_command_long(msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getSystemStatus(int interval){
        if (interval > 0 && System.currentTimeMillis() - lastSysStatusTs < interval / 200) return;
        MAVLinkPacket packet = new msg_command_long(msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getStatusText(int interval){
        if (interval > 0 && isStatusTextReceived) return;
        MAVLinkPacket packet = new msg_command_long(msg_statustext.MAVLINK_MSG_ID_STATUSTEXT, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getGpsRawInt(int interval){
        if (interval > 0 && System.currentTimeMillis() - lastGpsRawIntTs < interval / 200) return;
        MAVLinkPacket packet = new msg_command_long(msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getGlobalPositionInt(int interval){
        if (interval > 0 && System.currentTimeMillis() - lastGlobalPositionIntTs < interval / 200) return;
        MAVLinkPacket packet = new msg_command_long(msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getHomePosition(int interval){
        if (interval > 0 && isHomePositionReceived) return;
        MAVLinkPacket packet = new msg_command_long(msg_home_position.MAVLINK_MSG_ID_HOME_POSITION, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getSystemTime(int interval){
        if (interval > 0 && System.currentTimeMillis() - lastSystemTimeTs < interval / 200) return;
        MAVLinkPacket packet = new msg_command_long(msg_system_time.MAVLINK_MSG_ID_SYSTEM_TIME, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getRcChannels(int interval){
        if (interval > 0 && System.currentTimeMillis() - lastRcChannelsTs < interval / 200) return;
        MAVLinkPacket packet = new msg_command_long(msg_rc_channels.MAVLINK_MSG_ID_RC_CHANNELS, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getScaledPressure(int interval){
        if (interval > 0 && System.currentTimeMillis() - lastScaledPressureTs < interval / 200) return;
        MAVLinkPacket packet = new msg_command_long(msg_scaled_pressure.MAVLINK_MSG_ID_SCALED_PRESSURE, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    private void getVfrHud(int interval){
        if (interval > 0 && System.currentTimeMillis() - lastVfrHudTs < interval / 200) return;
        MAVLinkPacket packet = new msg_command_long(msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD, interval, 0, 0, 0, 0, 0,
                MAV_CMD.MAV_CMD_SET_MESSAGE_INTERVAL, targetSystem, targetComponent, (short)0, systemId, componentId, isMavlink2).pack();
        packet.seq = getSequence();
        serial.writeDataMavlink(packet.encodePacket());
    }

    public void processData(byte[] buf, int dataLength){
        if (dataLength < MAVLinkPacket.MAVLINK1_HEADER_LEN) return;
        byte[] data = new byte[dataLength];
        System.arraycopy(buf, 0, data, 0, dataLength);
        List<MAVLinkPacket> packets = parsePackets(data);
        if (packets == null || packets.isEmpty()) return;
        for (MAVLinkPacket packet : packets) {
            switch (packet.msgid) {
                case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT: {
                    msg_heartbeat message = new msg_heartbeat(packet);
                    if (fcParams.isOsdConfigInitialized()){
                        DataWriter buffer = new DataWriter(true);
                        buffer.writeByte((byte) message.custom_mode);
                        telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_MODE, buffer.getData()));
                    }
                    isArmed = (message.base_mode & MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) != 0;
                    if (isArmed){
                        if (armedTs == 0) armedTs = System.currentTimeMillis();
                    }else{
                        armedTs = 0;
                        flightTs = 0;
                    }
                    if (isHeartBeatReceived) break;
                    isMavlink2 = message.isMavlink2;
                    apiVersionMajor = message.isMavlink2 ? 2 : 1;
                    apiVersionMinor = message.mavlink_version;
                    platformType = message.type;
                    isHeartBeatReceived = true;
                    break;
                }
                case msg_autopilot_version.MAVLINK_MSG_ID_AUTOPILOT_VERSION: {
                    msg_autopilot_version message = new msg_autopilot_version(packet);
                    fcVersionMajor = (int) (message.flight_sw_version >> 24 & 0xFF);
                    fcVersionMinor = (int) (message.flight_sw_version >> 16 & 0xFF);
                    fcVersionPatchLevel = (int) (message.flight_sw_version >> 8 & 0xFF);
                    byte fw_type = (byte) (message.flight_sw_version & 0xFF);
                    setFcInfo();
                    break;
                }
                case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE: {
                    msg_param_value message = new msg_param_value(packet);
                    if (fcParams == null) break;
                    fcParams.setParam(message.getParam_Id(), message.param_value);
                    break;
                }
                case msg_attitude.MAVLINK_MSG_ID_ATTITUDE: {
                    msg_attitude message = new msg_attitude(packet);
                    lastAttitudeTs = System.currentTimeMillis();
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeShort((short) (Math.toDegrees(message.roll) * 10));
                    buffer.writeShort((short) (Math.toDegrees(message.pitch) * 10));
                    buffer.writeShort((short) Math.toDegrees(message.yaw));
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_ATTITUDE, buffer.getData()));
                    break;
                }
                case msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS: {
                    msg_battery_status message = new msg_battery_status(packet);
                    lastBatteryStatusTs = System.currentTimeMillis();
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeShort(message.current_battery);
                    buffer.writeInt(message.current_consumed);
                    buffer.writeByte(message.battery_remaining);
                    buffer.writeInt((int)message.fault_bitmask);
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_BATTERY_STATUS, buffer.getData()));
                    break;
                }
                case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS: {
                    msg_sys_status message = new msg_sys_status(packet);
                    lastSysStatusTs = System.currentTimeMillis();
                    DataWriter buffer = new DataWriter(true);
                    if (message.voltage_battery != Utils.UINT16_MAX) {
                        int cellCountDetected = Math.round(message.voltage_battery / 4100f);
                        if (batteryCellCountDetected < cellCountDetected) {
                            batteryCellCountDetected = cellCountDetected;
                        }
                    }
                    buffer.writeByte((byte)batteryCellCountDetected);
                    buffer.writeShort((short)message.voltage_battery);
                    buffer.writeShort(message.current_battery);
                    buffer.writeByte(message.battery_remaining);
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_SYS_STATUS, buffer.getData()));
                    break;
                }
                case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT: {
                    msg_statustext message = new msg_statustext(packet);
                    isStatusTextReceived = true;
                    if (message.getText() == null || message.getText().contains("No ap_message for mavlink")) break;
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeByte((byte) message.severity);
                    buffer.writeUTF(message.getText());
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_STATUS_TEXT, buffer.getData()));
                    break;
                }
                case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT: {
                    msg_gps_raw_int message = new msg_gps_raw_int(packet);
                    lastGpsRawIntTs = System.currentTimeMillis();
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeByte((byte)message.fix_type);
                    buffer.writeShort((short)message.vel);
                    buffer.writeByte((byte)message.satellites_visible);
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_GPS_RAW_INT, buffer.getData()));
                    break;
                }
                case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT: {
                    msg_global_position_int message = new msg_global_position_int(packet);
                    lastGlobalPositionIntTs = System.currentTimeMillis();
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeInt(message.lat);
                    buffer.writeInt(message.lon);
                    buffer.writeInt(message.relative_alt);
                    buffer.writeShort(message.vz);
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_GLOBAL_POSITION_INT, buffer.getData()));
                    break;
                }
                case msg_home_position.MAVLINK_MSG_ID_HOME_POSITION: {
                    msg_home_position message = new msg_home_position(packet);
                    isHomePositionReceived = true;
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeInt(message.latitude);
                    buffer.writeInt(message.longitude);
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_HOME_POSITION, buffer.getData()));
                    break;
                }
                case msg_system_time.MAVLINK_MSG_ID_SYSTEM_TIME: {
                    msg_system_time message = new msg_system_time(packet);
                    lastSystemTimeTs = System.currentTimeMillis();
                    long armingTime = 0;
                    if (armedTs != 0) armingTime = lastSystemTimeTs - armedTs;
                    if (isArmed){
                        if (flightTs == 0){
                            if (throttle >= 2) flightTs = lastSystemTimeTs;
                        }else{
                            flightTime += (lastSystemTimeTs - flightTs);
                            flightTs = lastSystemTimeTs;
                        }
                    }
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeInt((int)message.time_boot_ms);
                    buffer.writeInt((int)flightTime);
                    buffer.writeInt((int)armingTime);
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_SYSTEM_TIME, buffer.getData()));
                    break;
                }
                case msg_rc_channels.MAVLINK_MSG_ID_RC_CHANNELS: {
                    msg_rc_channels message = new msg_rc_channels(packet);
                    lastRcChannelsTs = System.currentTimeMillis();
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeByte((byte)message.rssi);
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_RC_CHANNELS, buffer.getData()));
                    break;
                }
                case msg_scaled_pressure.MAVLINK_MSG_ID_SCALED_PRESSURE: {
                    msg_scaled_pressure message = new msg_scaled_pressure(packet);
                    lastScaledPressureTs = System.currentTimeMillis();
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeShort(message.temperature);
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_SCALED_PRESSURE, buffer.getData()));
                    break;
                }
                case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD: {
                    msg_vfr_hud message = new msg_vfr_hud(packet);
                    lastVfrHudTs = System.currentTimeMillis();
                    throttle = message.throttle;
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeByte((byte)message.throttle);
                    telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_VFR_HUD, buffer.getData()));
                    break;
                }
            }
        }
    }

    private static class OsdItemParam{
        private final String osdItemName;
        private boolean isEnabled;
        private boolean isEnabledReceived;
        private byte x;
        private boolean isXReceived;
        private byte y;
        private boolean isYReceived;

        public OsdItemParam(String osdItemName){
            this.osdItemName = osdItemName;
            isEnabled = false;
            isEnabledReceived = false;
            x = 0;
            isXReceived = false;
            y = 0;
            isYReceived = false;
        }

        public String getOsdItemName(){
            return osdItemName;
        }

        public void setEnabled(boolean isEnabled){
            this.isEnabled = isEnabled;
            isEnabledReceived = true;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setX(byte x) {
            this.x = x;
            isXReceived = true;
        }

        public byte getX() {
            return x;
        }

        public void setY(byte y) {
            this.y = y;
            isYReceived = true;
        }

        public byte getY() {
            return y;
        }

        public boolean isInitialized(){
            return isEnabledReceived && isXReceived && isYReceived;
        }
    }

    private static class FcParams{
        private final Mavlink mavlink;
        private final OsdItemParam[] osdItems = new OsdItemParam[OsdCommon.AP_OSD_ITEMS.length];
        boolean osd1Enabled;
        boolean osd1EnabledReceived;
        byte osd1TxtRes;
        boolean osd1TxtResReceived;
        byte osdUnits;
        boolean osdUnitsReceived;
        byte osdMsgTime;
        boolean osdMsgTimeReceived;
        byte osdWarnRssi;
        boolean osdWarnRssiReceived;
        byte osdWarnNumSat;
        boolean osdWarnNumSatReceived;
        float osdWarnBatVolt;
        boolean osdWarnBatVoltReceived;
        float osdWarnAvgCellVolt;
        boolean osdWarnAvgCellVoltReceived;
        byte osdCellCount;
        boolean osdCellCountReceived;

        private FcParams(Mavlink mavlink) {
            this.mavlink = mavlink;
            int count = OsdCommon.AP_OSD_ITEMS.length;
            for (int i = 0; i < count; i++) {
                osdItems[i] = new OsdItemParam(OsdCommon.AP_OSD_ITEMS[i]);
            }
        }

        private String getOsdItemNameFromEn(String osdEnParam){
            return osdEnParam.substring(5, osdEnParam.length() - 3);
        }

        private String getOsdItemNameFromXY(String osdXYParam){
            return osdXYParam.substring(5, osdXYParam.length() - 2);
        }

        private void setOsdItemParam(String paramId, float paramValue){
            if (paramId == null) return;
            int l = paramId.length();
            if (l < 8) return;
            String osdItemName;
            OsdItemParam osdItemParam;
            if (paramId.substring(l - 3).equals("_EN")){
                osdItemName = getOsdItemNameFromEn(paramId);
                osdItemParam = getOsdItemParamFromName(osdItemName);
                if (osdItemParam == null) return;
                osdItemParam.setEnabled((int)paramValue != 0);
            } else if (paramId.substring(l - 2).equals("_X")){
                osdItemName = getOsdItemNameFromXY(paramId);
                osdItemParam = getOsdItemParamFromName(osdItemName);
                if (osdItemParam == null) return;
                osdItemParam.setX((byte)paramValue);
            } else if (paramId.substring(l - 2).equals("_Y")){
                osdItemName = getOsdItemNameFromXY(paramId);
                osdItemParam = getOsdItemParamFromName(osdItemName);
                if (osdItemParam == null) return;
                osdItemParam.setY((byte)paramValue);
            }
        }

        private OsdItemParam getOsdItemParamFromName(String osdItemName){
            for (OsdItemParam item : osdItems){
                if (item.getOsdItemName().equals(osdItemName)) return item;
            }
            return null;
        }

        public void initializeOsdConfig(){
            new Thread(() -> {
                if (!osd1EnabledReceived) mavlink.requestFcParameter(FcCommon.AP_PARAM_OSD1_ENABLE);
                if (!osd1TxtResReceived) mavlink.requestFcParameter(FcCommon.AP_PARAM_OSD1_TXT_RES);
                if (!osdUnitsReceived) mavlink.requestFcParameter(FcCommon.AP_PARAM_OSD_UNITS);
                if (!osdMsgTimeReceived) mavlink.requestFcParameter(FcCommon.AP_PARAM_OSD_MSG_TIME);
                if (!osdWarnRssiReceived) mavlink.requestFcParameter(FcCommon.AP_PARAM_OSD_W_RSSI);
                if (!osdWarnNumSatReceived) mavlink.requestFcParameter(FcCommon.AP_PARAM_OSD_W_NSAT);
                if (!osdWarnBatVoltReceived) mavlink.requestFcParameter(FcCommon.AP_PARAM_OSD_W_BATVOLT);
                if (!osdWarnAvgCellVoltReceived) mavlink.requestFcParameter(FcCommon.AP_PARAM_OSD_W_AVGCELLV);
                if (!osdCellCountReceived) mavlink.requestFcParameter(FcCommon.AP_PARAM_OSD_CELL_COUNT);
                for (OsdItemParam item : osdItems){
                    if (item.isInitialized()) continue;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        //
                    }
                    String osdItemName = item.getOsdItemName();
                    if (!item.isEnabledReceived) mavlink.requestFcParameter("OSD1_" + osdItemName + "_EN");
                    if (!item.isXReceived) mavlink.requestFcParameter("OSD1_" + osdItemName + "_X");
                    if (!item.isYReceived) mavlink.requestFcParameter("OSD1_" + osdItemName + "_Y");
                }
            }).start();
        }

        public boolean isOsdConfigInitialized(){
            if (!osd1EnabledReceived || !osd1TxtResReceived || !osdUnitsReceived
                    || !osdMsgTimeReceived || !osdWarnRssiReceived || !osdWarnNumSatReceived
                    || !osdWarnBatVoltReceived || !osdWarnAvgCellVoltReceived || !osdCellCountReceived) return false;
            for (OsdItemParam item : osdItems){
                if (!item.isInitialized()) {
                    return false;
                }
            }
            return true;
        }

        public void sendOsdConfig(){
            DataWriter buffer = new DataWriter(true);
            buffer.writeBoolean(osd1Enabled);
            buffer.writeByte(osd1TxtRes);
            buffer.writeByte(osdUnits);
            buffer.writeByte(osdMsgTime);
            buffer.writeByte(osdWarnRssi);
            buffer.writeByte(osdWarnNumSat);
            buffer.writeFloat(osdWarnBatVolt);
            buffer.writeFloat(osdWarnAvgCellVolt);
            buffer.writeByte(osdCellCount);
            int osdItemsCount = osdItems.length;
            buffer.writeByte((byte) osdItemsCount);
            for (OsdItemParam osdItem : osdItems) {
                buffer.writeBoolean(osdItem.isEnabled());
                buffer.writeByte(osdItem.getX());
                buffer.writeByte(osdItem.getY());
            }
            mavlink.telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_OSD_CONFIG, buffer.getData()));
        }

        public void setParam(String paramId, float paramValue){
            switch (paramId){
                case FcCommon.AP_PARAM_OSD1_ENABLE:
                    osd1Enabled = ((int)paramValue != 0);
                    osd1EnabledReceived = true;
                    break;
                case FcCommon.AP_PARAM_OSD1_TXT_RES:
                    osd1TxtRes = (byte)paramValue;
                    osd1TxtResReceived = true;
                    break;
                case FcCommon.AP_PARAM_OSD_UNITS:
                    osdUnits = (byte)paramValue;
                    osdUnitsReceived = true;
                    break;
                case FcCommon.AP_PARAM_OSD_MSG_TIME:
                    osdMsgTime = (byte)paramValue;
                    osdMsgTimeReceived = true;
                    break;
                case FcCommon.AP_PARAM_OSD_W_RSSI:
                    osdWarnRssi = (byte)paramValue;
                    osdWarnRssiReceived = true;
                    break;
                case FcCommon.AP_PARAM_OSD_W_NSAT:
                    osdWarnNumSat = (byte)paramValue;
                    osdWarnNumSatReceived = true;
                    break;
                case FcCommon.AP_PARAM_OSD_W_BATVOLT:
                    osdWarnBatVolt = paramValue;
                    osdWarnBatVoltReceived = true;
                    break;
                case FcCommon.AP_PARAM_OSD_W_AVGCELLV:
                    osdWarnAvgCellVolt = paramValue;
                    osdWarnAvgCellVoltReceived = true;
                    break;
                case FcCommon.AP_PARAM_OSD_CELL_COUNT:
                    osdCellCount = (byte)paramValue;
                    osdCellCountReceived = true;
                    break;
                case FcCommon.AP_PARAM_VTX_POWER:
                    DataWriter buffer = new DataWriter(true);
                    buffer.writeShort((short) Math.round(paramValue));
                    mavlink.telemetryOutputBuffer.offer(new TelemetryData(FcCommon.DD_AP_VTX_POWER, buffer.getData()));
                    break;
                default:
                    if (paramId.contains("OSD1_") &&
                            (paramId.contains("_EN") || paramId.contains("_X") || paramId.contains("_Y"))) {
                        setOsdItemParam(paramId, paramValue);
                    }
                    break;
            }
        }
    }

    private List<MAVLinkPacket> parsePackets(byte[] data){
        DataReader reader = new DataReader(data, false);
        List<MAVLinkPacket> packets = new ArrayList<>();
        while (reader.getRemaining() > 0) {
            int magic = reader.readUnsignedByteAsInt();
            int payloadLength = reader.readUnsignedByteAsInt();
            boolean isMavlink2 = false;
            if (magic == MAVLinkPacket.MAVLINK_STX_MAVLINK2) {
                isMavlink2 = true;
            } else if (magic != MAVLinkPacket.MAVLINK_STX_MAVLINK1) {
                return packets;
            }
            MAVLinkPacket packet = new MAVLinkPacket(payloadLength, isMavlink2);
            if (isMavlink2) {
                if (reader.getSize() < MAVLinkPacket.MAVLINK2_NONPAYLOAD_LEN) return packets;
                packet.incompatFlags = reader.readUnsignedByteAsInt();
                if (packet.incompatFlags != 0) return packets;
                packet.compatFlags = reader.readUnsignedByteAsInt();
                packet.seq = reader.readUnsignedByteAsInt();
                packet.sysid = reader.readUnsignedByteAsInt();
                packet.compid = reader.readUnsignedByteAsInt();
                packet.msgid = reader.readInt24AsInt();
                byte[] payloadData = new byte[payloadLength];
                reader.read(payloadData, 0, payloadLength);
                packet.payload = new MAVLinkPayload();
                packet.payload.putArray(payloadData);
                int crc1 = reader.readUnsignedByteAsInt();
                int crc2 = reader.readUnsignedByteAsInt();
                if (!packet.generateCRC(payloadLength)) return packets;
                if (packet.crc.getLSB() != crc1 || packet.crc.getMSB() != crc2) return packets;
            } else {
                if (reader.getSize() < MAVLinkPacket.MAVLINK1_NONPAYLOAD_LEN) return packets;
                packet.seq = reader.readUnsignedByteAsInt();
                packet.sysid = reader.readUnsignedByteAsInt();
                packet.compid = reader.readUnsignedByteAsInt();
                packet.msgid = reader.readUnsignedByteAsInt();
                byte[] payloadData = new byte[payloadLength];
                reader.read(payloadData, 0, payloadLength);
                packet.payload = new MAVLinkPayload();
                packet.payload.putArray(payloadData);
                int crc1 = reader.readUnsignedByteAsInt();
                int crc2 = reader.readUnsignedByteAsInt();
                if (!packet.generateCRC(payloadLength)) return packets;
                if (packet.crc.getLSB() != crc1 || packet.crc.getMSB() != crc2) return packets;
            }
            packets.add(packet);
        }
        return packets;
    }

    private synchronized int getSequence(){
        int seq = sequence;
        sequence++;
        if (sequence >= 256) sequence = 0;
        return seq;
    }

    private void disableIntervalMessages(){
        getAttitude(-1);
        getBatteryStatus(-1);
        getSystemStatus(-1);
        getStatusText(-1);
        getGpsRawInt(-1);
        getGlobalPositionInt(-1);
        getHomePosition(-1);
        getSystemTime(-1);
        getRcChannels(-1);
        getScaledPressure(-1);
        getVfrHud(-1);
    }

    public void close(){
        disableIntervalMessages();
        threadsId++;
        apiVersionMajor = -1;
        apiVersionMinor = -1;
        fcVersionMajor = -1;
        fcVersionMinor = -1;
        fcVersionPatchLevel = -1;
        platformType = -1;
        isHeartBeatReceived = false;
        fcParams = null;
        telemetryOutputBuffer.clear();
        batteryCellCountDetected = 0;
    }
}
