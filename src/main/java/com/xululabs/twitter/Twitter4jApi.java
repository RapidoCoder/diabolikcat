package com.xululabs.twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class Twitter4jApi {
  Query query;
  QueryResult result;

  /**
   * use to get twitter instance
   * 
   * @return Twiiter Instance
   */
  public Twitter getTwitterInstance() {

    Twitter twitter = new TwitterFactory().getInstance();
    return twitter;
  }

  /**
   * use to get all tweets for search term
   * 
   * @param twitter
   * @param search_term
   * @return arraylist of tweets
   * @throws TwitterException
   */

  public ArrayList<Map<String, Object>> getTweetsList(Twitter twitter, String search_term) throws TwitterException {
    ArrayList<Map<String, Object>> tweetsList = new ArrayList<Map<String, Object>>();
    query = new Query(search_term);
    result = twitter.search(query);
    for (Status status : result.getTweets()) {
    Map<String, Object> tweet = new HashMap<String, Object>();
    tweet.put("screen_name", status.getUser().getScreenName());
    tweet.put("text", status.getText());
    tweet.put("retweeted_count", status.getRetweetCount());
    tweet.put("followers_count", status.getUser().getFollowersCount());
    tweet.put("tweet_id", status.getId());
    tweetsList.add(tweet);
    }
    twitter = null;
    return tweetsList;
  }
  /**
   * use to retweet best tweets using twitter api
   * @param twitter
   * @param tweets
   * @return map of retweeted and already retweeted tweets
   */
  public Map<String, Object> retweet(Twitter twitter, ArrayList<Map<String, Object>> tweets) {
    ArrayList<Map<String, Object>> retweet_tweets = new ArrayList<Map<String, Object>>();
    ArrayList<Map<String, Object>>  retweeted_tweets= new ArrayList<Map<String, Object>>();
    Map<String, Object> response = new  HashMap<String, Object>();
    for(Map<String, Object> tweet : tweets){
        try{
          twitter.retweetStatus(Long.parseLong(tweet.get("tweet_id").toString()));  
          retweet_tweets.add(tweet);
          
        }catch(Exception ex){
        retweeted_tweets.add(tweet);
        }
    }
    response.put("retweets", retweet_tweets);
    response.put("already_retweeted", retweeted_tweets);
    twitter = null;
    return response;
    
  }

}
