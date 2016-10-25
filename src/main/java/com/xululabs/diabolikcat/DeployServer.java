package com.xululabs.diabolikcat;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.ArrayList;
import java.util.Map;

import twitter4j.Twitter;

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
		router.route().handler(
				CorsHandler.create("*").allowedMethod(HttpMethod.GET)
						.allowedMethod(HttpMethod.POST)
						.allowedMethod(HttpMethod.OPTIONS)
						.allowedHeader("Content-Type, Authorization"));
		// registering different route handlers
		 this.registerHandlers();
		server.requestHandler(router::accept).listen(port, host);
	}
	
	/**
	 *  For Registering different Routes
	 */
	public void registerHandlers(){
		  router.get("/").blockingHandler(this::welcomeRoute);
		  router.route(HttpMethod.POST, "/searchTweets").blockingHandler(this::searchTweets);
		  
		
	}
	/**
	 * Welcome route
	 * @param routingContext
	 */
	public void welcomeRoute(RoutingContext routingContext){
		  routingContext.response().end("<h1> Welcome To Diabolikcat </h1>"); 
	}
	
	/**
	 * search for the tweets
	 * @param routingContext
	 */
	public void searchTweets(RoutingContext routingContext){
		String search_term = (routingContext.request().getParam("search_term") == null)? "cat" : routingContext.request().getParam("search_term");
		ArrayList<Map<String, Object>> tweetList = this.getTweetsList(this.getTwitterInstance(), search_term);
		routingContext.response().end(tweetList.toString());
	}
	
	
	public ArrayList<Map<String, Object>> getTweetsList(Twitter twitter, String search_term){
        
		ArrayList<Map<String, Object>> list = twiiter4jApi.getTweetsList(twitter, search_term);
		return list;
	}
	public Twitter getTwitterInstance(){
		return  twiiter4jApi.getTwitterInstance();
	}

}
