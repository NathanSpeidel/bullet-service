/*
 *  Copyright 2018, Oath Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.rest.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yahoo.bullet.bql.BulletQueryBuilder;
import com.yahoo.bullet.bql.parser.ParsingException;
import com.yahoo.bullet.common.BulletConfig;
import com.yahoo.bullet.rest.query.BQLException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Service
public class PreprocessingService {
    private static final String WINDOW_KEY_STRING = "window";
    private static final BulletQueryBuilder QUERY_BUILDER = new BulletQueryBuilder(new BulletConfig());
    private static final Gson GSON = new GsonBuilder().create();
    @Value("${bullet.max.concurrent.queries}")
    private int maxConcurrentQueries;

    /**
     * Convert this query to a valid JSON Bullet Query if it is currently a BQL query.
     *
     * @param query The query to convert.
     * @return The valid JSON Bullet query.
     * @throws BQLException when there is an error with the BQL conversion.
     */
    public String convertIfBQL(String query) throws Exception {
        try {
            if (query == null || query.trim().charAt(0) == '{') {
                return query;
            } else {
                return QUERY_BUILDER.buildJson(query);
            }
        } catch (ParsingException | UnsupportedOperationException e) {
            throw new BQLException(e);
        }
    }
}
