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

package de.droiddrone.common;

public class OsdCommon {

    // ArduPilot OSD items
    public static final String AP_OSD_ALTITUDE = "ALTITUDE";
    public static final String AP_OSD_BAT_VOLT = "BAT_VOLT";
    public static final String AP_OSD_RSSI = "RSSI";
    public static final String AP_OSD_CURRENT = "CURRENT";
    public static final String AP_OSD_BATUSED = "BATUSED";
    public static final String AP_OSD_SATS = "SATS";
    public static final String AP_OSD_FLTMODE = "FLTMODE";
    public static final String AP_OSD_MESSAGE = "MESSAGE";
    public static final String AP_OSD_GSPEED = "GSPEED";
    public static final String AP_OSD_HORIZON = "HORIZON";
    public static final String AP_OSD_HOME = "HOME";
    public static final String AP_OSD_HEADING = "HEADING";
    public static final String AP_OSD_COMPASS = "COMPASS";
    public static final String AP_OSD_GPSLAT = "GPSLAT";
    public static final String AP_OSD_GPSLONG = "GPSLONG";
    public static final String AP_OSD_TEMP = "TEMP";
    public static final String AP_OSD_DIST = "DIST";
    public static final String AP_OSD_FLTIME = "FLTIME";
    public static final String AP_OSD_EFF = "EFF";
    public static final String AP_OSD_SIDEBARS = "SIDEBARS";
    public static final String AP_OSD_CRSSHAIR = "CRSSHAIR";
    public static final String AP_OSD_HOMEDIST = "HOMEDIST";
    public static final String AP_OSD_HOMEDIR = "HOMEDIR";
    public static final String AP_OSD_CELLVOLT = "CELLVOLT";
    public static final String AP_OSD_VTX_PWR = "VTX_PWR";
    public static final String AP_OSD_THROTTLE = "THROTTLE";
    public static final String[] AP_OSD_ITEMS = {AP_OSD_ALTITUDE, AP_OSD_BAT_VOLT, AP_OSD_RSSI,
            AP_OSD_CURRENT, AP_OSD_BATUSED, AP_OSD_SATS, AP_OSD_FLTMODE, AP_OSD_MESSAGE,
            AP_OSD_GSPEED, AP_OSD_HORIZON, AP_OSD_HOME, AP_OSD_HEADING, AP_OSD_COMPASS,
            AP_OSD_GPSLAT, AP_OSD_GPSLONG, AP_OSD_TEMP, AP_OSD_DIST, AP_OSD_FLTIME, AP_OSD_EFF,
            AP_OSD_SIDEBARS, AP_OSD_CRSSHAIR, AP_OSD_HOMEDIST, AP_OSD_HOMEDIR, AP_OSD_CELLVOLT,
            AP_OSD_VTX_PWR, AP_OSD_THROTTLE};

    public static final int PX4_OSD_CRAFT_NAME = 0;
    public static final int PX4_OSD_DISARMED = 1;
    public static final int PX4_OSD_GPS_LAT = 2;
    public static final int PX4_OSD_GPS_LON = 3;
    public static final int PX4_OSD_GPS_SATS = 4;
    public static final int PX4_OSD_GPS_SPEED = 5;
    public static final int PX4_OSD_HOME_DIST = 6;
    public static final int PX4_OSD_HOME_DIR = 7;
    public static final int PX4_OSD_MAIN_BATT_VOLTAGE = 8;
    public static final int PX4_OSD_CURRENT_DRAW = 9;
    public static final int PX4_OSD_MAH_DRAWN = 10;
    public static final int PX4_OSD_RSSI_VALUE = 11;
    public static final int PX4_OSD_ALTITUDE = 12;
    public static final int PX4_OSD_NUMERICAL_VARIO = 13;
    public static final int PX4_OSD_FLYMODE = 14;
    public static final int PX4_OSD_ESC_TMP = 15;
    public static final int PX4_OSD_PITCH_ANGLE = 16;
    public static final int PX4_OSD_ROLL_ANGLE = 17;
    public static final int PX4_OSD_CROSSHAIRS = 18;
    public static final int PX4_OSD_AVG_CELL_VOLTAGE = 19;
    public static final int PX4_OSD_HORIZON_SIDEBARS = 20;
    public static final int PX4_OSD_POWER = 21;
    public static final int PX4_OSD_MESSAGE = 1000;
    public static final int PX4_OSD_HORIZON = 1001;
    public static final int PX4_OSD_COMPASS = 1002;
    public static final int PX4_OSD_TRIP_DIST = 1003;
    public static final int PX4_OSD_FLY_TIME = 1004;
    public static final int PX4_OSD_EFFICIENCY = 1005;

    public static final int[] PX4_OSD_ITEMS = {PX4_OSD_CRAFT_NAME, PX4_OSD_DISARMED, PX4_OSD_GPS_LAT,
            PX4_OSD_GPS_LON, PX4_OSD_GPS_SATS, PX4_OSD_GPS_SPEED, PX4_OSD_HOME_DIST, PX4_OSD_HOME_DIR,
            PX4_OSD_MAIN_BATT_VOLTAGE, PX4_OSD_CURRENT_DRAW, PX4_OSD_MAH_DRAWN, PX4_OSD_RSSI_VALUE,
            PX4_OSD_ALTITUDE, PX4_OSD_NUMERICAL_VARIO, PX4_OSD_FLYMODE, PX4_OSD_ESC_TMP,
            PX4_OSD_PITCH_ANGLE, PX4_OSD_ROLL_ANGLE, PX4_OSD_CROSSHAIRS, PX4_OSD_AVG_CELL_VOLTAGE,
            PX4_OSD_HORIZON_SIDEBARS, PX4_OSD_POWER, PX4_OSD_MESSAGE, PX4_OSD_HORIZON,
            PX4_OSD_COMPASS, PX4_OSD_TRIP_DIST, PX4_OSD_FLY_TIME, PX4_OSD_EFFICIENCY};

    public enum InavOsdItems{
        OSD_RSSI_VALUE,
        OSD_MAIN_BATT_VOLTAGE,
        OSD_CROSSHAIRS,
        OSD_ARTIFICIAL_HORIZON,
        OSD_HORIZON_SIDEBARS,
        OSD_ONTIME,
        OSD_FLYTIME,
        OSD_FLYMODE,
        OSD_CRAFT_NAME,
        OSD_THROTTLE_POS,
        OSD_VTX_CHANNEL,
        OSD_CURRENT_DRAW,
        OSD_MAH_DRAWN,
        OSD_GPS_SPEED,
        OSD_GPS_SATS,
        OSD_ALTITUDE,
        OSD_ROLL_PIDS,
        OSD_PITCH_PIDS,
        OSD_YAW_PIDS,
        OSD_POWER,
        OSD_GPS_LON,
        OSD_GPS_LAT,
        OSD_HOME_DIR,
        OSD_HOME_DIST,
        OSD_HEADING,
        OSD_VARIO,
        OSD_VARIO_NUM,
        OSD_AIR_SPEED,
        OSD_ONTIME_FLYTIME,
        OSD_RTC_TIME,
        OSD_MESSAGES,
        OSD_GPS_HDOP,
        OSD_MAIN_BATT_CELL_VOLTAGE,
        OSD_SCALED_THROTTLE_POS,
        OSD_HEADING_GRAPH,
        OSD_EFFICIENCY_MAH_PER_KM,
        OSD_WH_DRAWN,
        OSD_BATTERY_REMAINING_CAPACITY,
        OSD_BATTERY_REMAINING_PERCENT,
        OSD_EFFICIENCY_WH_PER_KM,
        OSD_TRIP_DIST,
        OSD_ATTITUDE_PITCH,
        OSD_ATTITUDE_ROLL,
        OSD_MAP_NORTH,
        OSD_MAP_TAKEOFF,
        OSD_RADAR,
        OSD_WIND_SPEED_HORIZONTAL,
        OSD_WIND_SPEED_VERTICAL,
        OSD_REMAINING_FLIGHT_TIME_BEFORE_RTH,
        OSD_REMAINING_DISTANCE_BEFORE_RTH,
        OSD_HOME_HEADING_ERROR,
        OSD_COURSE_HOLD_ERROR,
        OSD_COURSE_HOLD_ADJUSTMENT,
        OSD_SAG_COMPENSATED_MAIN_BATT_VOLTAGE,
        OSD_MAIN_BATT_SAG_COMPENSATED_CELL_VOLTAGE,
        OSD_POWER_SUPPLY_IMPEDANCE,
        OSD_LEVEL_PIDS,
        OSD_POS_XY_PIDS,
        OSD_POS_Z_PIDS,
        OSD_VEL_XY_PIDS,
        OSD_VEL_Z_PIDS,
        OSD_HEADING_P,
        OSD_BOARD_ALIGN_ROLL,
        OSD_BOARD_ALIGN_PITCH,
        OSD_RC_EXPO,
        OSD_RC_YAW_EXPO,
        OSD_THROTTLE_EXPO,
        OSD_PITCH_RATE,
        OSD_ROLL_RATE,
        OSD_YAW_RATE,
        OSD_MANUAL_RC_EXPO,
        OSD_MANUAL_RC_YAW_EXPO,
        OSD_MANUAL_PITCH_RATE,
        OSD_MANUAL_ROLL_RATE,
        OSD_MANUAL_YAW_RATE,
        OSD_NAV_FW_CRUISE_THR,
        OSD_NAV_FW_PITCH2THR,
        OSD_FW_MIN_THROTTLE_DOWN_PITCH_ANGLE,
        OSD_DEBUG,
        OSD_FW_ALT_PID_OUTPUTS,
        OSD_FW_POS_PID_OUTPUTS,
        OSD_MC_VEL_X_PID_OUTPUTS,
        OSD_MC_VEL_Y_PID_OUTPUTS,
        OSD_MC_VEL_Z_PID_OUTPUTS,
        OSD_MC_POS_XYZ_P_OUTPUTS,
        OSD_3D_SPEED,
        OSD_IMU_TEMPERATURE,
        OSD_BARO_TEMPERATURE,
        OSD_TEMP_SENSOR_0_TEMPERATURE,
        OSD_TEMP_SENSOR_1_TEMPERATURE,
        OSD_TEMP_SENSOR_2_TEMPERATURE,
        OSD_TEMP_SENSOR_3_TEMPERATURE,
        OSD_TEMP_SENSOR_4_TEMPERATURE,
        OSD_TEMP_SENSOR_5_TEMPERATURE,
        OSD_TEMP_SENSOR_6_TEMPERATURE,
        OSD_TEMP_SENSOR_7_TEMPERATURE,
        OSD_ALTITUDE_MSL,
        OSD_PLUS_CODE,
        OSD_MAP_SCALE,
        OSD_MAP_REFERENCE,
        OSD_GFORCE,
        OSD_GFORCE_X,
        OSD_GFORCE_Y,
        OSD_GFORCE_Z,
        OSD_RC_SOURCE,
        OSD_VTX_POWER,
        OSD_ESC_RPM,
        OSD_ESC_TEMPERATURE,
        OSD_AZIMUTH,
        OSD_CRSF_RSSI_DBM,
        OSD_CRSF_LQ,
        OSD_CRSF_SNR_DB,
        OSD_CRSF_TX_POWER,
        OSD_GVAR_0,
        OSD_GVAR_1,
        OSD_GVAR_2,
        OSD_GVAR_3,
        OSD_TPA,
        OSD_NAV_FW_CONTROL_SMOOTHNESS,
        OSD_VERSION,
        OSD_RANGEFINDER,
        OSD_PLIMIT_REMAINING_BURST_TIME,
        OSD_PLIMIT_ACTIVE_CURRENT_LIMIT,
        OSD_PLIMIT_ACTIVE_POWER_LIMIT,
        OSD_GLIDESLOPE,
        OSD_GPS_MAX_SPEED,
        OSD_3D_MAX_SPEED,
        OSD_AIR_MAX_SPEED,
        OSD_ACTIVE_PROFILE,
        OSD_MISSION,
        OSD_SWITCH_INDICATOR_0,
        OSD_SWITCH_INDICATOR_1,
        OSD_SWITCH_INDICATOR_2,
        OSD_SWITCH_INDICATOR_3,
        OSD_TPA_TIME_CONSTANT,
        OSD_FW_LEVEL_TRIM,
        OSD_GLIDE_TIME_REMAINING,
        OSD_GLIDE_RANGE,
        OSD_CLIMB_EFFICIENCY,
        OSD_NAV_WP_MULTI_MISSION_INDEX,
        OSD_GROUND_COURSE,
        OSD_CROSS_TRACK_ERROR,
        OSD_PILOT_NAME,
        OSD_PAN_SERVO_CENTRED,
        OSD_MULTI_FUNCTION,
        OSD_ITEM_COUNT;
    }

    public static class InavVideoSystem{
        public static final int VIDEO_SYSTEM_AUTO = 0;
        public static final int VIDEO_SYSTEM_PAL = 1;
        public static final int VIDEO_SYSTEM_NTSC = 2;
        public static final int VIDEO_SYSTEM_HDZERO = 3;
        public static final int VIDEO_SYSTEM_DJIWTF = 4;
        public static final int VIDEO_SYSTEM_AVATAR = 5;
        public static final int VIDEO_SYSTEM_BFCOMPAT = 6;
        public static final int VIDEO_SYSTEM_BFCOMPAT_HD = 7;
    }

    public static class canvasSizes{
        public static final int PAL_COLS = 30;
        public static final int PAL_ROWS = 16;

        public static final int NTSC_COLS = 30;
        public static final int NTSC_ROWS = 13;

        public static final int HDZERO_COLS = 50;
        public static final int HDZERO_ROWS = 18;

        public static final int HD_AVATAR_COLS = 53;
        public static final int HD_AVATAR_ROWS = 20;

        public static final int DJI_COLS = 60;
        public static final int DJI_ROWS = 22;
    }

    public enum BtflOsdItems{
        OSD_RSSI_VALUE,
        OSD_MAIN_BATT_VOLTAGE,
        OSD_CROSSHAIRS,
        OSD_ARTIFICIAL_HORIZON,
        OSD_HORIZON_SIDEBARS,
        OSD_ITEM_TIMER_1,
        OSD_ITEM_TIMER_2,
        OSD_FLYMODE,
        OSD_CRAFT_NAME,
        OSD_THROTTLE_POS,
        OSD_VTX_CHANNEL,
        OSD_CURRENT_DRAW,
        OSD_MAH_DRAWN,
        OSD_GPS_SPEED,
        OSD_GPS_SATS,
        OSD_ALTITUDE,
        OSD_ROLL_PIDS,
        OSD_PITCH_PIDS,
        OSD_YAW_PIDS,
        OSD_POWER,
        OSD_PIDRATE_PROFILE,
        OSD_WARNINGS,
        OSD_AVG_CELL_VOLTAGE,
        OSD_GPS_LON,
        OSD_GPS_LAT,
        OSD_DEBUG,
        OSD_PITCH_ANGLE,
        OSD_ROLL_ANGLE,
        OSD_MAIN_BATT_USAGE,
        OSD_DISARMED,
        OSD_HOME_DIR,
        OSD_HOME_DIST,
        OSD_NUMERICAL_HEADING,
        OSD_NUMERICAL_VARIO,
        OSD_COMPASS_BAR,
        OSD_ESC_TMP,
        OSD_ESC_RPM,
        OSD_REMAINING_TIME_ESTIMATE,
        OSD_RTC_DATETIME,
        OSD_ADJUSTMENT_RANGE,
        OSD_CORE_TEMPERATURE,
        OSD_ANTI_GRAVITY,
        OSD_G_FORCE,
        OSD_MOTOR_DIAG,
        OSD_LOG_STATUS,
        OSD_FLIP_ARROW,
        OSD_LINK_QUALITY,
        OSD_FLIGHT_DIST,
        OSD_STICK_OVERLAY_LEFT,
        OSD_STICK_OVERLAY_RIGHT,
        OSD_PILOT_NAME,
        OSD_ESC_RPM_FREQ,
        OSD_RATE_PROFILE_NAME,
        OSD_PID_PROFILE_NAME,
        OSD_PROFILE_NAME,
        OSD_RSSI_DBM_VALUE,
        OSD_RC_CHANNELS,
        OSD_CAMERA_FRAME,
        OSD_EFFICIENCY,
        OSD_TOTAL_FLIGHTS,
        OSD_UP_DOWN_REFERENCE,
        OSD_TX_UPLINK_POWER,
        OSD_WATT_HOURS_DRAWN,
        OSD_AUX_VALUE,
        OSD_READY_MODE,
        OSD_RSNR_VALUE,
        OSD_SYS_GOGGLE_VOLTAGE,
        OSD_SYS_VTX_VOLTAGE,
        OSD_SYS_BITRATE,
        OSD_SYS_DELAY,
        OSD_SYS_DISTANCE,
        OSD_SYS_LQ,
        OSD_SYS_GOGGLE_DVR,
        OSD_SYS_VTX_DVR,
        OSD_SYS_WARNINGS,
        OSD_SYS_VTX_TEMP,
        OSD_SYS_FAN_SPEED,
        OSD_GPS_LAP_TIME_CURRENT,
        OSD_GPS_LAP_TIME_PREVIOUS,
        OSD_GPS_LAP_TIME_BEST3,
        OSD_ITEM_COUNT
    }

    public enum BtflOsdStats{
        OSD_STAT_RTC_DATE_TIME,
        OSD_STAT_TIMER_1,
        OSD_STAT_TIMER_2,
        OSD_STAT_MAX_SPEED,
        OSD_STAT_MAX_DISTANCE,
        OSD_STAT_MIN_BATTERY,
        OSD_STAT_END_BATTERY,
        OSD_STAT_BATTERY,
        OSD_STAT_MIN_RSSI,
        OSD_STAT_MAX_CURRENT,
        OSD_STAT_USED_MAH,
        OSD_STAT_MAX_ALTITUDE,
        OSD_STAT_BLACKBOX,
        OSD_STAT_BLACKBOX_NUMBER,
        OSD_STAT_MAX_G_FORCE,
        OSD_STAT_MAX_ESC_TEMP,
        OSD_STAT_MAX_ESC_RPM,
        OSD_STAT_MIN_LINK_QUALITY,
        OSD_STAT_FLIGHT_DISTANCE,
        OSD_STAT_MAX_FFT,
        OSD_STAT_TOTAL_FLIGHTS,
        OSD_STAT_TOTAL_TIME,
        OSD_STAT_TOTAL_DIST,
        OSD_STAT_MIN_RSSI_DBM,
        OSD_STAT_WATT_HOURS_DRAWN,
        OSD_STAT_MIN_RSNR,
        OSD_STAT_BEST_3_CONSEC_LAPS,
        OSD_STAT_BEST_LAP,
        OSD_STAT_FULL_THROTTLE_TIME,
        OSD_STAT_FULL_THROTTLE_COUNTER,
        OSD_STAT_AVG_THROTTLE,
        OSD_STAT_COUNT
    }

    public static class BtflVideoSystem {
        public static final int VIDEO_SYSTEM_AUTO = 0;
        public static final int VIDEO_SYSTEM_PAL = 1;
        public static final int VIDEO_SYSTEM_NTSC = 2;
        public static final int VIDEO_SYSTEM_HD = 3;
    }

    public enum BtflOsdTimerSource{
        OSD_TIMER_SRC_ON,
        OSD_TIMER_SRC_TOTAL_ARMED,
        OSD_TIMER_SRC_LAST_ARMED,
        OSD_TIMER_SRC_ON_OR_ARMED,
        OSD_TIMER_SRC_COUNT
    }
}
