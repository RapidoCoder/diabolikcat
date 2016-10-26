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
    tweet.put("user_image", status.getUser().getProfileImageURL());
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
  public Map<String, Object> retweet(Twitter twitter, ArrayList<Map<String, Object>> tweets, int retweet_total) {
    
    ArrayList<Map<String, Object>> retweet_tweets = new ArrayList<Map<String, Object>>();
    int retweeted_count = 0;
    Map<String, Object> response = new  HashMap<String, Object>();
    for(Map<String, Object> tweet : tweets){
        if(retweeted_count > retweet_total-1)
            break;
        try{
          twitter.retweetStatus(Long.parseLong(tweet.get("tweet_id").toString()));  
          retweet_tweets.add(tweet);
          retweeted_count++;
        }catch(Exception ex){
        
        }
    }
    response.put("retweets", retweet_tweets);
   
    twitter = null;
    return response;
    
  }
  public ArrayList<Map<String, Object>> followBestUsers(Twitter twitter, ArrayList<Map<String, Object>> best_users, int followers_total) throws TwitterException{

    ArrayList<Map<String, Object>> users =  (best_users.size() > followers_total-1 )? new ArrayList<Map<String, Object>>(best_users.subList(0,followers_total-1)) : best_users;
    for(Map<String, Object> user : users){
    twitter.createFriendship(user.get("screen_name").toString());
    }
    twitter = null;
    return users;
  }

}
