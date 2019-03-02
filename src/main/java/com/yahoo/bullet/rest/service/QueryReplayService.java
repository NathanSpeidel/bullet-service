package com.yahoo.bullet.rest.service;

import com.yahoo.bullet.common.BulletConfig;
import com.yahoo.bullet.common.RandomPool;
import com.yahoo.bullet.pubsub.PubSub;
import com.yahoo.bullet.pubsub.PubSubException;
import com.yahoo.bullet.pubsub.Publisher;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class QueryReplayService {
    // Exposed for testing only.
    @Getter(AccessLevel.PACKAGE)
    //private ConcurrentMap<String, QueryHandler> runningQueries;
    //private List<PubSubReader> consumers;
    private RandomPool<Publisher> publisherRandomPool;

    /**
     * Creates an instance that is (for now) hard-coded to write to the query replay topic.
     */
    @Autowired
    public QueryReplayService(@Value("${bullet.pubsub.config}") String config) {
        BulletConfig bulletConfig = new BulletConfig(config);
        bulletConfig.set("bullet.pubsub.kafka.request.topic.name", bulletConfig.get("bullet.pubsub.kafka.replay.topic.name"));

        publisherRandomPool = null;
        PubSub pubSub = null;
        try {
            pubSub = PubSub.from(bulletConfig);
            publisherRandomPool = new RandomPool<>(Collections.singletonList(pubSub.getPublisher()));
        } catch (PubSubException e) {
            log.error("Error traying to create custom PubSub in QueryReplayService: " + e);
        }
    }

    /**
     * Submit a query to Bullet and register it as a pending request.
     *
     * @param queryID The query ID to register request with.
     * @param query The query to register.
     */
    //public void submit(String queryID, String query, QueryHandler queryHandler) {
    public void submit(String queryID, String query) {
        Publisher publisher = publisherRandomPool.get();
        try {
            publisher.send(queryID, query);
            //runningQueries.put(queryID, queryHandler);
            //queryHandler.acknowledge();
        } catch (Exception e) {
            log.error("Caught exception sending query from custom publisher: " + e);
            //queryHandler.fail(QueryError.SERVICE_UNAVAILABLE);
        }
    }

//    /**
//     * Submit a signal query to Bullet.
//     *
//     * @param queryID The query ID to register request with.
//     * @param signal The {@link Metadata.Signal} to be submitted.
//     */
//    public void submitSignal(String queryID, Metadata.Signal signal) {
//        Publisher publisher = publisherRandomPool.get();
//        try {
//            Metadata metadata = new Metadata(signal, null);
//            PubSubMessage message = new PubSubMessage(queryID, null, metadata);
//            publisher.send(message);
//        } catch (Exception e) {
//            // Ignore failure.
//        }
//        runningQueries.remove(queryID);
//    }
//
//    /**
//     * Clears all pending requests.
//     */
//    public void killRunningQueries() {
//        runningQueries.values().forEach(QueryHandler::fail);
//        runningQueries.clear();
//    }

    /**
     * Stop all service threads and clear pending requests.
     */
    @PreDestroy
    public void close() {
//        consumers.forEach(PubSubReader::close);
//        killRunningQueries();
        publisherRandomPool.clear();
    }

//    /**
//     * Get a new unique query ID.
//     *
//     * @return A new unique query ID.
//     */
//    public static String getNewQueryID() {
//        return UUID.randomUUID().toString();
//    }
//
//    /**
//     * Get the number of running queries.
//     *
//     * @return The number of running queries.
//     */
//    public int runningQueryCount() {
//        return runningQueries.size();
//    }
}
