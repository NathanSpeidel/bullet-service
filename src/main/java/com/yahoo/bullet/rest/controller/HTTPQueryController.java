/*
 *  Copyright 2017, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.rest.controller;

import com.yahoo.bullet.rest.query.BQLError;
import com.yahoo.bullet.rest.query.BQLException;
import com.yahoo.bullet.rest.query.HTTPQueryHandler;
import com.yahoo.bullet.rest.query.QueryError;
import com.yahoo.bullet.rest.service.PreprocessingService;
import com.yahoo.bullet.rest.service.QueryService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class HTTPQueryController {
    @Autowired @Setter
    private QueryService queryService;
    @Autowired
    private PreprocessingService preprocessingService;

    /**
     * The method that handles POSTs to this endpoint. Consumes the HTTP request, invokes {@link QueryService} to
     * register and transmit the query to Bullet.
     *
     * @param query The JSON query.
     * @return A {@link CompletableFuture} representing the eventual result.
     */
    @PostMapping(path = "${bullet.endpoint.http}", consumes = { MediaType.TEXT_PLAIN_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    @SuppressWarnings("unchecked")
    public CompletableFuture<String> submitHTTPQuery(@RequestBody String query) {
        HTTPQueryHandler queryHandler = new HTTPQueryHandler();
        String queryID = QueryService.getNewQueryID();
        try {
            query = preprocessingService.convertIfBQL(query);
            queryService.submit(queryID, query, queryHandler);
        } catch (BQLException e) {
            queryHandler.fail(new BQLError(e));
        } catch (Exception e) {
            queryHandler.fail(QueryError.INVALID_QUERY);
        }
        return queryHandler.getResult();
    }
}
