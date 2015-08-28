/*
 * Copyright (C) 2015 Tomas Machalek
 *
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
 */
package net.orzo.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.orzo.JerseyGuiceServletConfig;
import net.orzo.Service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;

/**
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 *
 */
@Singleton
public class HttpServer implements Service {

	/**
	 * 
	 */
	private final JerseyGuiceServletConfig guiceJerseyConfig;
	
	/**
	 * 
	 */
	private final RestServiceConfig config;
	
	/**
	 * 
	 */
	private Server httpServer;
	
	/**
	 * 
	 */
	private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
	
	/**
	 * 
	 */
	public HttpServer(RestServiceConfig config,
			JerseyGuiceServletConfig guiceJerseyConfig) {
		this.config = config;
		this.guiceJerseyConfig = guiceJerseyConfig;
	}

	/**
	 * 
	 */
	@Override
	public void start() throws Exception {
		if (this.config.getHttpPort() == 0) {
			throw new HttpServerException("No HTTP port specified");
		}
		InetAddress addr = InetAddress.getByName(config.getHttpHost());
		InetSocketAddress isa = new InetSocketAddress(addr,
				this.config.getHttpPort());
		this.httpServer = new Server(isa);

		ContextHandlerCollection handlerCollection = new ContextHandlerCollection();

		ResourceHandler staticHandler = new ResourceHandler();
		staticHandler.setDirectoriesListed(false);
		staticHandler.setWelcomeFiles(new String[] { "index.html" });
		staticHandler.setResourceBase(getClass().getClassLoader()
				.getResource("net/orzo/webui").toExternalForm());

		ServletContextHandler restApiHandler = new ServletContextHandler(
				handlerCollection, "/api", ServletContextHandler.NO_SESSIONS);

		restApiHandler.addEventListener(this.guiceJerseyConfig);
		restApiHandler.addFilter(GuiceFilter.class, "/*", null);
		restApiHandler.addServlet(DefaultServlet.class, "/*");

		HandlerList handlerList = new HandlerList();
		handlerList.addHandler(restApiHandler);
		handlerList.addHandler(staticHandler);

		this.httpServer.setHandler(handlerList);
		this.httpServer.start();
	}

	/**
	 * 
	 */
	public void stop() {
		try {
			this.httpServer.stop();

		} catch (Exception ex) {
			logger.error("Failed to stop server properly", ex);
		}
		
	}
	
	
}
