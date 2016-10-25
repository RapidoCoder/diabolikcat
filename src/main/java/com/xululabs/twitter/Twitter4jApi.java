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
			   tweetsList.add(tweet);
			}
		twitter = null;	
		return tweetsList;
	}

}
