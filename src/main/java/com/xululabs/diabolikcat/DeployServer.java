package com.xululabs.diabolikcat;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;

import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.xululabs.twitter.Twitter4jApi;

public class DeployServer extends AbstractVerticle {

  HttpServer server;
  Router router;
  String host;
  int port;
  Twitter4jApi twiiter4jApi;

  public DeployServer() {

    this.host = "localhost";
    this.port = 8181;
    this.twiiter4jApi = new Twitter4jApi();
  }

  /**
   * Deploying the verical
   */
  @Override
  public void start() {
    server = vertx.createHttpServer();
    router = Router.router(vertx);
    // Enable multipart form data parsing
    router.route().handler(BodyHandler.create());
    router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET)
            .allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.OPTIONS)
            .allowedHeader("Content-Type, Authorization"));
    // registering different route handlers
    this.registerHandlers();
    server.requestHandler(router::accept).listen(port, host);
  }

  /**
   * For Registering different Routes
   */
  public void registerHandlers() {
    router.get("/").blockingHandler(this::welcomeRoute);
    router.route(HttpMethod.POST, "/searchTweets").blockingHandler(this::searchTweets);
    router.route(HttpMethod.POST, "/bestUsers").blockingHandler(this::bestUsers);
    router.route(HttpMethod.POST, "/bestTweets").blockingHandler(this::bestTweets);
    router.route(HttpMethod.POST, "/retweet").blockingHandler(this::retweet);

  }

  /**
   * Welcome route
   * 
   * @param routingContext
   */
  public void welcomeRoute(RoutingContext routingContext) {
    routingContext.response().end("<h1> Welcome To Diabolikcat </h1>");
  }

  /**
   * search for the tweets
   * 
   * @param routingContext
   */
  public void searchTweets(RoutingContext routingContext) {
    String response;
    try {
    String search_term = (routingContext.request().getParam("search_term") == null) ? "cat" : routingContext.request().getParam("search_term");
    response = new ObjectMapper().writeValueAsString(this.getTweetsList(this.getTwitterInstance(), search_term));
    }catch (Exception ex) {
    response = "{status: 'error', 'msg' : " + ex.getMessage() + "}";
    }
    routingContext.response().end(response);

  }

  /**
   * use to get tweets with user having high number of followers
   * 
   * @param routingContext
   */
  public void bestUsers(RoutingContext routingContext) {
    String response;
    try {
    String search_term = (routingContext.request().getParam("search_term") == null) ? "cat": routingContext.request().getParam("search_term");
    ArrayList<Map<String, Object>> tweetList = this.getTweetsList(this.getTwitterInstance(), search_term);
    ArrayList<Map<String, Object>> best_users = this.bestUserProcessing(tweetList);
    response = new ObjectMapper().writeValueAsString(best_users);
    }catch (Exception ex) {
    response = "{status: 'error', 'msg' : " + ex.getMessage() + "}";
    }
    routingContext.response().end(response);
  }
/**
 * route for best tweets
 * @param routingContext
 */
  public void bestTweets(RoutingContext routingContext) {
    String response;
    try {
    String search_term = (routingContext.request().getParam("search_term") == null) ? "cat": routingContext.request().getParam("search_term");
    ArrayList<Map<String, Object>> tweetList = this.getTweetsList(this.getTwitterInstance(), search_term);
    ArrayList<Map<String, Object>> besttweets = this.bestTweetsProcessing(tweetList);
    response = new ObjectMapper().writeValueAsString(besttweets);
    } catch (Exception ex) {
    response = "{status: 'error', 'msg' : " + ex.getMessage() + "}";
    }
    routingContext.response().end(response);
  }
  /*
   * retweet route
   */
  public void retweet(RoutingContext routingContext){
    String response;
    try {
      String search_term = (routingContext.request().getParam("search_term") == null) ? "cat": routingContext.request().getParam("search_term");
      ArrayList<Map<String, Object>> tweetList = this.getTweetsList(this.getTwitterInstance(), search_term);
      ArrayList<Map<String, Object>> besttweets = this.bestTweetsProcessing(tweetList);
      Map<String, Object> retweet_response = this.retweetProcess(this.getTwitterInstance(), besttweets);
      response = new ObjectMapper().writeValueAsString(retweet_response);
    } catch (Exception ex) {
      response = "{status: 'error', 'msg' : " + ex.getMessage() + "}";
    }
    routingContext.response().end(response);
  }

  /**
   * use to get tweetslist
   * 
   * @param twitter
   * @param search_term
   * @return arryalist of tweets map
   * @throws TwitterException
   */
  public ArrayList<Map<String, Object>> getTweetsList(Twitter twitter, String search_term) throws TwitterException {

    ArrayList<Map<String, Object>> list = twiiter4jApi.getTweetsList(twitter, search_term);
    return list;
  }

  /**
   * use to process tweetslist for best user
   * 
   * @param tweetsList
   * @return bestusers
   */
  public ArrayList<Map<String, Object>> bestUserProcessing(ArrayList<Map<String, Object>> tweetsList) {
    Map<String, Integer> users_followers = new HashMap<String, Integer>();
    for (Map<String, Object> tweet : tweetsList) {
    if (!users_followers.containsKey(tweet.get("screen_name").toString()))
      users_followers.put(tweet.get("screen_name").toString(), Integer.parseInt(tweet.get("followers_count").toString()));
    }
    ArrayList<Map<String, Object>> best_users = sortDescFollowers(users_followers);
    return best_users;
  }

  public ArrayList<Map<String, Object>> bestTweetsProcessing(
      ArrayList<Map<String, Object>> tweetList) {
    Collections.sort(tweetList, mapComparator);
    return tweetList;
  }
  public Map<String, Object> retweetProcess(Twitter twitter, ArrayList<Map<String, Object>> besttweets){
     Map<String, Object> response = twiiter4jApi.retweet(twitter, besttweets);
     return response;
  }

  /**
   * use to sort users according their followes
   * 
   * @param unsortMap
   * @return list of sorted users
   */
  private ArrayList<Map<String, Object>> sortDescFollowers(Map<String, Integer> unsortMap) {

    LinkedList<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

    // Sorting the list based on values
    Collections.sort(list, new Comparator<Entry<String, Integer>>() {
      public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
        return o2.getValue().compareTo(o1.getValue());
      }
    });

    ArrayList<Map<String, Object>> sortedList = new ArrayList<Map<String, Object>>();
    for (Entry<String, Integer> entry : list) {
    Map<String, Object> sortedElement = new HashMap<String, Object>();
    sortedElement.put("screen_name", entry.getKey());
    sortedElement.put("followers_count", entry.getValue());
    sortedList.add(sortedElement);
    // sortedMap.put(entry.getKey(), entry.getValue());
    }

    return sortedList;
  }

  /**
   * use to sort list of tweets by retweeted_count
   */
  public Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
    public int compare(Map<String, Object> m1, Map<String, Object> m2) {
      return Integer.compare(Integer.parseInt(m2.get("retweeted_count").toString()), Integer.parseInt(m1.get("retweeted_count").toString()));
    }
  };

  /**
   * use to get twitter instance
   * 
   * @return twiiter4j instance
   */
  public Twitter getTwitterInstance() {
    return twiiter4jApi.getTwitterInstance();
  }

}
