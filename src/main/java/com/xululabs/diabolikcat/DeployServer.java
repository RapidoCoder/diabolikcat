package com.xululabs.diabolikcat;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
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
  int retweet_total;
  int followers_total;

  public DeployServer() {

    this.host = "localhost";
    this.port = 8383;
    this.twiiter4jApi = new Twitter4jApi();
    this.retweet_total = 20;
    this.followers_total = 30;
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
    router.route(HttpMethod.GET,"/").blockingHandler(this::welcomeRoute);
    router.route(HttpMethod.POST,"/searchTweets").blockingHandler(this::searchTweets);
    router.route(HttpMethod.POST, "/bestUsers").blockingHandler(this::bestUsers);
    router.route(HttpMethod.POST, "/bestTweets").blockingHandler(this::bestTweets);
    router.route(HttpMethod.POST, "/retweet").blockingHandler(this::retweetBestTweets);
    router.route(HttpMethod.POST, "/follow").blockingHandler(this::followBestUsers);
    

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
    this. enableCors(routingContext.response());
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
  public void retweetBestTweets(RoutingContext routingContext){
    String response;
    try {
      String search_term = (routingContext.request().getParam("search_term") == null) ? "cat": routingContext.request().getParam("search_term");
      int max_retweets = (routingContext.request().getParam("max_retweets") == null) ? retweet_total: Integer.parseInt(routingContext.request().getParam("max_retweets"));
      ArrayList<Map<String, Object>> tweetList = this.getTweetsList(this.getTwitterInstance(), search_term);
      ArrayList<Map<String, Object>> besttweets = this.bestTweetsProcessing(tweetList);
      Map<String, Object> retweet_response = this.retweetProcess(this.getTwitterInstance(), besttweets, max_retweets);
      response = new ObjectMapper().writeValueAsString(retweet_response);
    } catch (Exception ex) {
      response = "{status: 'error', 'msg' : " + ex.getMessage() + "}";
    }
    routingContext.response().end(response);
  }
  /**
   * use for route to follow best users 
   * @param routingContext
   */
  public void followBestUsers(RoutingContext routingContext){
    String response;
    try {
    String search_term = (routingContext.request().getParam("search_term") == null) ? "cat": routingContext.request().getParam("search_term");
    int max_followers = (routingContext.request().getParam("max_followers") == null) ? followers_total: Integer.parseInt(routingContext.request().getParam("max_followers"));
    ArrayList<Map<String, Object>> tweetList = this.getTweetsList(this.getTwitterInstance(), search_term);
    ArrayList<Map<String, Object>> best_users = this.bestUserProcessing(tweetList);
    ArrayList<Map<String,Object>> followers_response = this.followUsers(this.getTwitterInstance(), best_users, max_followers);
    response = new ObjectMapper().writeValueAsString(followers_response);
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
    Map<String, Object> users = new HashMap<String, Object>();
    for (Map<String, Object> tweet : tweetsList) {
    if (!users_followers.containsKey(tweet.get("screen_name").toString())){
    users_followers.put(tweet.get("screen_name").toString(), Integer.parseInt(tweet.get("followers_count").toString()));
    Map<String, Object> user_info = new HashMap<String, Object>();
    user_info.put("name", tweet.get("name"));
    user_info.put("screen_name", tweet.get("screen_name"));
    user_info.put("image", tweet.get("user_image"));
    user_info.put("followers_count", tweet.get("followers_count"));
    users.put(tweet.get("screen_name").toString(), user_info);
   
    }
     
    }
    ArrayList<Map<String, Object>> best_users = sortDescFollowers(users_followers ,users);
    return best_users;
  }

  public ArrayList<Map<String, Object>> bestTweetsProcessing(
      ArrayList<Map<String, Object>> tweetList) {
    Collections.sort(tweetList, mapComparator);
    return tweetList;
  }
  public Map<String, Object> retweetProcess(Twitter twitter, ArrayList<Map<String, Object>> besttweets, int retweet_total){
     Map<String, Object> response = twiiter4jApi.retweet(twitter, besttweets, retweet_total);
     return response;
  }
  
  public ArrayList<Map<String,Object>> followUsers(Twitter twitter, ArrayList<Map<String, Object>> users, int followers_total) throws TwitterException{
    ArrayList<Map<String,Object>> response = twiiter4jApi.followBestUsers(twitter, users,followers_total);
    return response;
  }
  
  

  /**
   * use to sort users according their followes
   * 
   * @param unsortMap
   * @return list of sorted users
   */
  private ArrayList<Map<String, Object>> sortDescFollowers(Map<String, Integer> unsortMap,  Map<String, Object> users) {

    LinkedList<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

    // Sorting the list based on values
    Collections.sort(list, new Comparator<Entry<String, Integer>>() {
      public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
        return o2.getValue().compareTo(o1.getValue());
      }
    });

    ArrayList<Map<String, Object>> sortedList = new ArrayList<Map<String, Object>>();
    for (Entry<String, Integer> entry : list) {
    Map<String, Object> sortedElement = (Map<String, Object>) users.get(entry.getKey()); 
    sortedList.add(sortedElement);
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
   * use to enable the cors
   * @param response
   */
  public void enableCors(HttpServerResponse response) {
      response.putHeader("content-type", "text/plain");
      response.putHeader("Access-Control-Allow-Origin", "*");
      response.putHeader("Access-Control-Allow-Methods",
              "GET, POST, OPTIONS");
      response.putHeader("Access-Control-Allow-Headers",
              "Content-Type, Authorization");
  }

  /**
   * use to get twitter instance
   * 
   * @return twiiter4j instance
   */
  public Twitter getTwitterInstance() {
    return twiiter4jApi.getTwitterInstance();
  }

}
