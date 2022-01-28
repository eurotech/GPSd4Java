package de.taimos.gpsd4java.backend;

/*
 * #%L
 * GPSd4Java
 * %%
 * Copyright (C) 2011 - 2012 Taimos GmbH
 * Copyright (C) 2022 - Eurotech S.p.a.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collections;

import com.google.gson.JsonObject;

import de.taimos.gpsd4java.types.ATTObject;
import de.taimos.gpsd4java.types.DeviceObject;
import de.taimos.gpsd4java.types.DevicesObject;
import de.taimos.gpsd4java.types.ENMEAMode;
import de.taimos.gpsd4java.types.EParity;
import de.taimos.gpsd4java.types.GSTObject;
import de.taimos.gpsd4java.types.IGPSObject;
import de.taimos.gpsd4java.types.ParseException;
import de.taimos.gpsd4java.types.PollObject;
import de.taimos.gpsd4java.types.SATObject;
import de.taimos.gpsd4java.types.SKYObject;
import de.taimos.gpsd4java.types.TPVObject;
import de.taimos.gpsd4java.types.VersionObject;
import de.taimos.gpsd4java.types.WatchObject;
import de.taimos.gpsd4java.types.subframes.ALMANACObject;
import de.taimos.gpsd4java.types.subframes.EPHEM1Object;
import de.taimos.gpsd4java.types.subframes.EPHEM2Object;
import de.taimos.gpsd4java.types.subframes.EPHEM3Object;
import de.taimos.gpsd4java.types.subframes.ERDObject;
import de.taimos.gpsd4java.types.subframes.HEALTH2Object;
import de.taimos.gpsd4java.types.subframes.HEALTHObject;
import de.taimos.gpsd4java.types.subframes.IONOObject;
import de.taimos.gpsd4java.types.subframes.SUBFRAMEObject;

/**
 * This class is used to parse responses from GPSd<br>
 *
 * @author thoeger
 */
public class ResultParser extends AbstractResultParser {

    /**
     * parse {@link JsonObject} into {@link IGPSObject}
     *
     * @param json
     *            the {@link JsonObject} to parse
     * @return the parsed object
     * @throws ParseException
     *             if parsing fails
     */
    @Override
    public IGPSObject parse(final JsonObject json) throws ParseException {
        IGPSObject gps = null;
        final String clazz = optString(json, "class");

        if (TPVObject.NAME.equals(clazz)) {
            gps = this.parseTPV(json);
        } else if (SKYObject.NAME.equals(clazz)) {
            gps = this.parseSKY(json);
        } else if (GSTObject.NAME.equals(clazz)) {
            gps = this.parseGST(json);
        } else if (ATTObject.NAME.equals(clazz)) {
            gps = this.parseATT(json);
        } else if (SUBFRAMEObject.NAME.equals(clazz)) {
            gps = this.parseSUBFRAME(json);
        } else if (VersionObject.NAME.equals(clazz)) {
            gps = this.parseVERSION(json);
        } else if (DevicesObject.NAME.equals(clazz)) {
            gps = this.parseDEVICES(json);
        } else if (DeviceObject.NAME.equals(clazz)) {
            gps = this.parseDEVICE(json);
        } else if (WatchObject.NAME.equals(clazz)) {
            gps = this.parseWATCH(json);
        } else if (PollObject.NAME.equals(clazz)) {
            gps = this.parsePOLL(json);
        } else if (json.has("PRN")) { // SATObject
            gps = this.parsePRN(json);
        } else if (json.has("deltai")) { // ALMANACObject
            gps = this.parseALMANAC(json);
        } else if (json.has("IODC")) { // EPHEM1Object
            gps = this.parseEPHEM1(json);
        } else if (json.has("Crs")) { // EPHEM2Object
            gps = this.parseEPHEM2(json);
        } else if (json.has("IDOT")) { // EPHEM3Object
            gps = this.parseEPHEM3(json);
        } else if (json.has("ERD30")) { // ERDObject
            gps = this.parseERD(json);
        } else if (json.has("SVH32")) { // HEALTHObject
            gps = this.parseHEALTH(json);
        } else if (json.has("WNa")) { // HEALTH2Object
            gps = this.parseHEALTH2(json);
        } else if (json.has("WNlsf")) { // IONOObject
            gps = this.parseIONO(json);
        } else {
            throw new ParseException("Invalid object class: " + clazz);
        }
        return gps;
    }

    protected IGPSObject parseIONO(final JsonObject json) {
        IGPSObject gps;
        final IONOObject iono = new IONOObject();
        iono.setAlpha0(optDouble(json, "a0", Double.NaN));
        iono.setAlpha1(optDouble(json, "a1", Double.NaN));
        iono.setAlpha2(optDouble(json, "a2", Double.NaN));
        iono.setAlpha3(optDouble(json, "a3", Double.NaN));
        iono.setBeta0(optDouble(json, "b0", Double.NaN));
        iono.setBeta1(optDouble(json, "b1", Double.NaN));
        iono.setBeta2(optDouble(json, "b2", Double.NaN));
        iono.setBeta3(optDouble(json, "b3", Double.NaN));
        iono.setA0(optDouble(json, "A0", Double.NaN));
        iono.setA1(optDouble(json, "A1", Double.NaN));
        iono.setTot(optDouble(json, "tot", Double.NaN));
        iono.setWNt(optInt(json, "WNt"));
        iono.setLeap(optInt(json, "ls"));
        iono.setWNlsf(optInt(json, "WNlsf"));
        iono.setDN(optInt(json, "DN"));
        iono.setLsf(optInt(json, "lsf"));
        gps = iono;
        return gps;
    }

    protected IGPSObject parseHEALTH2(final JsonObject json) {
        IGPSObject gps;
        final HEALTH2Object health2 = new HEALTH2Object();
        health2.setToa(optInt(json, "toa"));
        health2.setWNa(optInt(json, "WNa"));
        for (int index = 1; index <= 24; index++) {
            health2.setSVbyIndex(index - 1, optInt(json, "SV" + index));
        }
        gps = health2;
        return gps;
    }

    protected IGPSObject parseHEALTH(final JsonObject json) {
        IGPSObject gps;
        final HEALTHObject health = new HEALTHObject();
        health.setData_id(optInt(json, "data_id"));
        for (int index = 1; index <= 32; index++) {
            health.setSVbyIndex(index - 1, optInt(json, "SV" + index));
        }
        for (int index = 0; index <= 7; index++) {
            health.setSVHbyIndex(index, optInt(json, "SVH" + (index + 25)));
        }
        gps = health;
        return gps;
    }

    protected IGPSObject parseERD(final JsonObject json) {
        IGPSObject gps;
        final ERDObject erd = new ERDObject();
        erd.setAi(optInt(json, "ai"));
        for (int index = 1; index <= 30; index++) {
            erd.setERDbyIndex(index - 1, optInt(json, "ERD" + index));
        }
        gps = erd;
        return gps;
    }

    protected IGPSObject parseEPHEM3(final JsonObject json) {
        IGPSObject gps;
        final EPHEM3Object emphem3 = new EPHEM3Object();
        emphem3.setIODE(optInt(json, "IODE"));
        emphem3.setIDOT(optDouble(json, "IDOT", Double.NaN));
        emphem3.setCic(optDouble(json, "Cic", Double.NaN));
        emphem3.setOmega0(optDouble(json, "Omega0", Double.NaN));
        emphem3.setCis(optDouble(json, "Cis", Double.NaN));
        emphem3.setI0(optDouble(json, "i0", Double.NaN));
        emphem3.setCrc(optDouble(json, "Crc", Double.NaN));
        emphem3.setOmega(optDouble(json, "omega", Double.NaN));
        emphem3.setOmegad(optDouble(json, "Omegad", Double.NaN));
        gps = emphem3;
        return gps;
    }

    protected IGPSObject parseEPHEM2(final JsonObject json) {
        IGPSObject gps;
        final EPHEM2Object emphem2 = new EPHEM2Object();
        emphem2.setIODE(optInt(json, "IODE"));
        emphem2.setCrs(optDouble(json, "Crs", Double.NaN));
        emphem2.setDeltan(optDouble(json, "deltan", Double.NaN));
        emphem2.setM0(optDouble(json, "M0", Double.NaN));
        emphem2.setCuc(optDouble(json, "Cuc", Double.NaN));
        emphem2.setE(optDouble(json, "e", Double.NaN));
        emphem2.setCus(optDouble(json, "Cus", Double.NaN));
        emphem2.setSqrtA(optInt(json, "sqrtA"));
        emphem2.setToe(optInt(json, "toe"));
        emphem2.setFIT(optInt(json, "FIT"));
        emphem2.setAODO(optInt(json, "AODO"));
        gps = emphem2;
        return gps;
    }

    protected IGPSObject parseEPHEM1(final JsonObject json) {
        IGPSObject gps;
        final EPHEM1Object emphem1 = new EPHEM1Object();
        emphem1.setWN(optInt(json, "WN"));
        emphem1.setIODC(optInt(json, "IODC"));
        emphem1.setL2(optInt(json, "L2"));
        emphem1.setUra(optDouble(json, "ura", Double.NaN));
        emphem1.setHlth(optDouble(json, "hlth", Double.NaN));
        emphem1.setL2P(optInt(json, "L2P"));
        emphem1.setTgd(optDouble(json, "Tgd", Double.NaN));
        emphem1.setToc(optInt(json, "toc"));
        emphem1.setAf2(optDouble(json, "af2", Double.NaN));
        emphem1.setAf1(optDouble(json, "af1", Double.NaN));
        emphem1.setAf0(optDouble(json, "af0", Double.NaN));
        gps = emphem1;
        return gps;
    }

    protected IGPSObject parseALMANAC(final JsonObject json) {
        IGPSObject gps;
        final ALMANACObject almanac = new ALMANACObject();
        almanac.setID(optInt(json, "ID"));
        almanac.setHealth(optInt(json, "Health"));
        almanac.setE(optDouble(json, "e", Double.NaN));
        almanac.setToa(optInt(json, "toa"));
        almanac.setDeltai(optDouble(json, "deltai", Double.NaN));
        almanac.setOmegad(optDouble(json, "Omegad", Double.NaN));
        almanac.setSqrtA(optDouble(json, "sqrtA", Double.NaN));
        almanac.setOmega0(optDouble(json, "Omega0", Double.NaN));
        almanac.setOmega(optDouble(json, "omega", Double.NaN));
        almanac.setM0(optDouble(json, "M0", Double.NaN));
        almanac.setAf0(optDouble(json, "af0", Double.NaN));
        almanac.setAf1(optDouble(json, "af1", Double.NaN));
        gps = almanac;
        return gps;
    }

    protected IGPSObject parsePRN(final JsonObject json) {
        IGPSObject gps;
        final SATObject sat = new SATObject();
        sat.setPRN(optInt(json, "PRN", -1));
        sat.setAzimuth(optInt(json, "az", -1));
        sat.setElevation(optInt(json, "el", -1));
        sat.setSignalStrength(optInt(json, "ss", -1));
        sat.setUsed(optBoolean(json, "used", false));
        gps = sat;
        return gps;
    }

    protected IGPSObject parsePOLL(final JsonObject json) throws ParseException {
        IGPSObject gps;
        // for gpsd version > 3.5
        final PollObject poll = new PollObject();
        if (json.has("time")) {
            poll.setTimestamp(this.parseTimestamp(json, "time"));
        } else if (json.has("timestamp")) {
            poll.setTimestamp(optDouble(json, "timestamp", Double.NaN));
        } else {
            // fallback to current timestamp
            poll.setTimestamp(System.currentTimeMillis());
        }

        poll.setActive(optInt(json, "active", 0));

        if (json.has("tpv")) {
            poll.setFixes(this.parseObjectArray(json.getAsJsonArray("tpv"), TPVObject.class));
        } else if (json.has("fixes")) {
            poll.setFixes(this.parseObjectArray(json.getAsJsonArray("fixes"), TPVObject.class));
        } else {
            poll.setFixes(Collections.<TPVObject>emptyList());
        }

        if (json.has("sky")) {
            poll.setSkyviews(this.parseObjectArray(json.getAsJsonArray("sky"), SKYObject.class));
        } else if (json.has("skyviews")) {
            poll.setSkyviews(this.parseObjectArray(json.getAsJsonArray("skyviews"), SKYObject.class));
        } else {
            poll.setSkyviews(Collections.<SKYObject>emptyList());
        }

        if (json.has("gst")) {
            poll.setGst(this.parseObjectArray(json.getAsJsonArray("gst"), GSTObject.class));
        } else {
            poll.setGst(Collections.<GSTObject>emptyList());
        }
        gps = poll;
        return gps;
    }

    protected IGPSObject parseWATCH(final JsonObject json) {
        IGPSObject gps;
        final WatchObject watch = new WatchObject();
        watch.setEnable(optBoolean(json, "enable", true));
        watch.setDump(optBoolean(json, "json", false));
        gps = watch;
        return gps;
    }

    protected IGPSObject parseDEVICE(final JsonObject json) {
        IGPSObject gps;
        final DeviceObject dev = new DeviceObject();
        dev.setPath(optString(json, "path", null));
        dev.setActivated(this.parseTimestamp(json, "activated"));
        dev.setDriver(optString(json, "driver", null));
        dev.setBps(optInt(json, "bps", 0));
        dev.setParity(EParity.fromString(optString(json, "parity")));
        dev.setStopbit(optInt(json, "stopbit"));
        dev.setNativeMode(optInt(json, "native", 0) == 1);
        dev.setCycle(optInt(json, "cycle"));
        dev.setMincycle(optInt(json, "mincycle"));
        gps = dev;
        return gps;
    }

    protected IGPSObject parseDEVICES(final JsonObject json) throws ParseException {
        IGPSObject gps;
        final DevicesObject devs = new DevicesObject();
        devs.setDevices(this.parseObjectArray(json.getAsJsonArray("devices"), DeviceObject.class));
        gps = devs;
        return gps;
    }

    protected IGPSObject parseVERSION(final JsonObject json) {
        IGPSObject gps;
        final VersionObject ver = new VersionObject();
        ver.setRelease(optString(json, "release", null));
        ver.setRev(optString(json, "rev", null));
        ver.setProtocolMajor(optDouble(json, "proto_major", 0));
        ver.setProtocolMinor(optDouble(json, "proto_minor", 0));
        gps = ver;
        return gps;
    }

    protected IGPSObject parseSUBFRAME(final JsonObject json) throws ParseException {
        IGPSObject gps;
        final SUBFRAMEObject subframe = new SUBFRAMEObject();
        subframe.setDevice(optString(json, "device", null));
        subframe.setMSBs(optInt(json, "TOW17"));
        subframe.setSatelliteNumber(optInt(json, "tSV"));
        subframe.setSubframeNumber(optInt(json, "frame"));
        subframe.setScaled(optBoolean(json, "scaled", false));
        subframe.setPageid(optInt(json, "pageid"));
        if (json.has("system_message")) {
            subframe.setSystemMessage(optString(json, "system_message"));
        } else if (json.has(ALMANACObject.NAME)) {
            subframe.setAlmanac((ALMANACObject) this.parse(json.getAsJsonObject(ALMANACObject.NAME)));
        } else if (json.has(EPHEM1Object.NAME)) {
            subframe.setEphem1((EPHEM1Object) this.parse(json.getAsJsonObject(EPHEM1Object.NAME)));
        } else if (json.has(EPHEM2Object.NAME)) {
            subframe.setEphem2((EPHEM2Object) this.parse(json.getAsJsonObject(EPHEM2Object.NAME)));
        } else if (json.has(EPHEM3Object.NAME)) {
            subframe.setEphem3((EPHEM3Object) this.parse(json.getAsJsonObject(EPHEM3Object.NAME)));
        } else if (json.has(ERDObject.NAME)) {
            subframe.setErd((ERDObject) this.parse(json.getAsJsonObject(ERDObject.NAME)));
        } else if (json.has(HEALTHObject.NAME)) {
            subframe.setHealth((HEALTHObject) this.parse(json.getAsJsonObject(HEALTHObject.NAME)));
        } else if (json.has(HEALTH2Object.NAME)) {
            subframe.setHealth2((HEALTH2Object) this.parse(json.getAsJsonObject(HEALTH2Object.NAME)));
        } else if (json.has(IONOObject.NAME)) {
            subframe.setIono((IONOObject) this.parse(json.getAsJsonObject(IONOObject.NAME)));
        } else {
            AbstractResultParser.LOG.error("Unknown subframe: {}", json.toString());
        }
        gps = subframe;
        return gps;
    }

    protected IGPSObject parseATT(final JsonObject json) {
        IGPSObject gps;
        final ATTObject att = new ATTObject();
        att.setTag(optString(json, "tag", null));
        att.setDevice(optString(json, "device", null));
        att.setTimestamp(this.parseTimestamp(json, "time"));
        att.setHeading(optDouble(json, "heading", Double.NaN));
        att.setPitch(optDouble(json, "pitch", Double.NaN));
        att.setYaw(optDouble(json, "yaw", Double.NaN));
        att.setRoll(optDouble(json, "roll", Double.NaN));
        att.setDip(optDouble(json, "dip", Double.NaN));
        att.setMag_len(optDouble(json, "mag_len", Double.NaN));
        att.setMag_x(optDouble(json, "mag_x", Double.NaN));
        att.setMag_y(optDouble(json, "mag_y", Double.NaN));
        att.setMag_z(optDouble(json, "mag_z", Double.NaN));
        att.setAcc_len(optDouble(json, "acc_len", Double.NaN));
        att.setAcc_x(optDouble(json, "acc_x", Double.NaN));
        att.setAcc_y(optDouble(json, "acc_y", Double.NaN));
        att.setAcc_z(optDouble(json, "acc_z", Double.NaN));
        att.setGyro_x(optDouble(json, "gyro_x", Double.NaN));
        att.setGyro_y(optDouble(json, "gyro_y", Double.NaN));
        att.setDepth(optDouble(json, "depth", Double.NaN));
        att.setTemperature(optDouble(json, "temperature", Double.NaN));
        att.setMagState(optString(json, "mag_st", null));
        att.setRollState(optString(json, "roll_st", null));
        att.setPitchState(optString(json, "pitch_st", null));
        att.setYawState(optString(json, "yaw_st", null));
        gps = att;
        return gps;
    }

    protected IGPSObject parseGST(final JsonObject json) {
        IGPSObject gps;
        final GSTObject gst = new GSTObject();
        gst.setTag(optString(json, "tag", null));
        gst.setDevice(optString(json, "device", null));
        gst.setTimestamp(this.parseTimestamp(json, "time"));
        gst.setRms(optDouble(json, "rms", Double.NaN));
        gst.setMajor(optDouble(json, "major", Double.NaN));
        gst.setMinor(optDouble(json, "minor", Double.NaN));
        gst.setOrient(optDouble(json, "orient", Double.NaN));
        gst.setLat(optDouble(json, "lat", Double.NaN));
        gst.setLon(optDouble(json, "lon", Double.NaN));
        gst.setAlt(optDouble(json, "alt", Double.NaN));
        gps = gst;
        return gps;
    }

    protected IGPSObject parseSKY(final JsonObject json) throws ParseException {
        IGPSObject gps;
        final SKYObject sky = new SKYObject();
        sky.setTag(optString(json, "tag", null));
        sky.setDevice(optString(json, "device", null));
        sky.setTimestamp(this.parseTimestamp(json, "time"));
        sky.setLongitudeDOP(optDouble(json, "xdop", Double.NaN));
        sky.setLatitudeDOP(optDouble(json, "ydop", Double.NaN));
        sky.setAltitudeDOP(optDouble(json, "vdop", Double.NaN));
        sky.setTimestampDOP(optDouble(json, "tdop", Double.NaN));
        sky.setHorizontalDOP(optDouble(json, "hdop", Double.NaN));
        sky.setSphericalDOP(optDouble(json, "pdop", Double.NaN));
        sky.setHypersphericalDOP(optDouble(json, "gdop", Double.NaN));
        sky.setSatellites(this.parseObjectArray(json.getAsJsonArray("satellites"), SATObject.class));
        gps = sky;
        return gps;
    }

    protected IGPSObject parseTPV(final JsonObject json) {
        IGPSObject gps;
        final TPVObject tpv = new TPVObject();
        tpv.setTag(optString(json, "tag", null));
        tpv.setDevice(optString(json, "device", null));
        tpv.setTimestamp(this.parseTimestamp(json, "time"));
        tpv.setTimestampError(optDouble(json, "ept", Double.NaN));
        tpv.setLatitude(optDouble(json, "lat", Double.NaN));
        tpv.setLongitude(optDouble(json, "lon", Double.NaN));
        tpv.setAltitude(optDouble(json, "alt", Double.NaN));
        tpv.setLongitudeError(optDouble(json, "epx", Double.NaN));
        tpv.setLatitudeError(optDouble(json, "epy", Double.NaN));
        tpv.setAltitudeError(optDouble(json, "epv", Double.NaN));
        tpv.setCourse(optDouble(json, "track", Double.NaN));
        tpv.setSpeed(optDouble(json, "speed", Double.NaN));
        tpv.setClimbRate(optDouble(json, "climb", Double.NaN));
        tpv.setCourseError(optDouble(json, "epd", Double.NaN));
        tpv.setSpeedError(optDouble(json, "eps", Double.NaN));
        tpv.setClimbRateError(optDouble(json, "epc", Double.NaN));
        tpv.setMode(ENMEAMode.fromInt(optInt(json, "mode", 0)));
        gps = tpv;
        return gps;
    }
}
