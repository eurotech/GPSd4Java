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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import de.taimos.gpsd4java.types.IGPSObject;
import de.taimos.gpsd4java.types.ParseException;

/**
 * @author irakli, thoeger
 */
public abstract class AbstractResultParser {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractResultParser.class);

    protected final DateFormat dateFormat; // Don't make this static!

    private final JsonParser jsonParser;

    /**
     * Create new ResultParser
     */
    protected AbstractResultParser() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        this.jsonParser = new JsonParser();
    }

    /**
     * Parse a received line into a {@link IGPSObject}
     *
     * @param line
     *            the line read from GPSd
     * @return the parsed object
     * @throws ParseException
     *             if parsing fails
     */
    public IGPSObject parse(final String line) throws ParseException {
        try {
            final JsonObject jsonObject = this.jsonParser.parse(line).getAsJsonObject();
            return this.parse(jsonObject);
        } catch (final JsonSyntaxException e) {
            throw new ParseException("Parsing failed", e);
        }
    }

    /**
     * @param json
     * @return the parsed {@link IGPSObject}
     * @throws ParseException
     */
    public abstract IGPSObject parse(final JsonObject json) throws ParseException;

    /**
     * parse a whole JSONArray into a list of IGPSObjects
     */
    @SuppressWarnings({ "unchecked", "unused" })
    protected <T extends IGPSObject> List<T> parseObjectArray(final JsonArray array, final Class<T> type)
            throws ParseException {
        try {
            if (array == null) {
                return new ArrayList<T>(10);
            }
            final List<T> objects = new ArrayList<T>(10);
            for (int i = 0; i < array.size(); i++) {
                objects.add((T) this.parse(array.get(i).getAsJsonObject()));
            }
            return objects;
        } catch (final JsonSyntaxException e) {
            throw new ParseException("Parsing failed", e);
        }
    }

    protected double parseTimestamp(final JsonObject json, final String fieldName) {
        try {
            final String text = optString(json, fieldName, null);

            if (text != null) {
                LOG.debug("{}: {}", fieldName, text);
                final Date date = this.dateFormat.parse(text);
                if (LOG.isDebugEnabled()) {
                    final String ds = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(date);
                    LOG.debug("Date: {}", ds);
                }
                return date.getTime() / 1000.0;
            }
        } catch (final Exception ex) {
            // trying to parse field as double
            double d = optDouble(json, fieldName, Double.NaN);
            if (d != Double.NaN) {
                return d;
            }
            LOG.info("Failed to parse time", ex);

        }
        return Double.NaN;
    }

    @SuppressWarnings("unchecked")
    protected <T> T optField(Class<T> clazz, JsonObject json, String field, T fallBackValue) {
        T t = fallBackValue;
        JsonElement jsonField = json.get(field);
        if (jsonField != null && !json.isJsonNull() && jsonField.isJsonPrimitive()) {
            if (jsonField.getAsJsonPrimitive().isNumber()) {
                if (clazz.isAssignableFrom(Double.class)) {
                    t = (T) Double.valueOf(jsonField.getAsDouble());
                } else if (clazz.isAssignableFrom(Integer.class)) {
                    t = (T) Integer.valueOf(jsonField.getAsInt());
                }
            } else if (jsonField.getAsJsonPrimitive().isBoolean()) {
                if (clazz.isAssignableFrom(Boolean.class)) {
                    t = (T) Boolean.valueOf(jsonField.getAsBoolean());
                }
            } else if (jsonField.getAsJsonPrimitive().isString()) {
                if (clazz.isAssignableFrom(String.class)) {
                    t = (T) jsonField.getAsString();
                }
            }
        }

        return t;
    }

    protected double optDouble(JsonObject json, String field, double fallbackValue) {
        return optField(Double.class, json, field, fallbackValue);
    }

    protected int optInt(JsonObject json, String field, int fallbackValue) {
        return optField(Integer.class, json, field, fallbackValue);
    }

    protected int optInt(JsonObject json, String field) {
        return optInt(json, field, 0);
    }

    protected boolean optBoolean(JsonObject json, String field, boolean fallbackValue) {
        return optField(Boolean.class, json, field, fallbackValue);
    }

    protected String optString(JsonObject json, String field, String fallbackValue) {
        return optField(String.class, json, field, fallbackValue);
    }

    protected String optString(JsonObject json, String field) {
        return optString(json, field, "");
    }
}
