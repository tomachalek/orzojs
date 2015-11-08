/*
 * Copyright (C) 2013 Tomas Machalek
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
package net.orzo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.orzo.service.TaskStatus;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;

import net.orzo.injection.CoreModule;
import net.orzo.injection.RestServletModule;
import net.orzo.scripting.SourceCode;
import net.orzo.service.HttpServer;
import net.orzo.service.RestServiceConfig;
import net.orzo.service.TaskManager;
import net.orzo.tools.ResourceLoader;

/**
 * Entry class. Handles command line parameters and runs the calculation.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public final class App {

	protected final Properties props;

	private final Options cliOptions;

	private final List<Service> services;

	private final static String DEMO_SCRIPT = "net/orzo/demo1.js";

	/**
	 * 
	 */
	public App() {
		this.services = new ArrayList<Service>();
		this.props = new Properties();
		this.cliOptions = new Options();
		this.cliOptions.addOption("v", false, "shows version information");
		this.cliOptions.addOption("d", false, "runs a demo program");
		this.cliOptions
				.addOption("g", true,
						"custom path to a Logback configuration XML file (default is ./logback.xml)");
		this.cliOptions.addOption("h", false, "print help");
		this.cliOptions
				.addOption(
						"m",
						true,
						"additional module path (the directory where your script is located is always included)");
		this.cliOptions
				.addOption(
						"t",
						true,
						"writes a code template to a specified file plus creates orzojs.d.ts type definition file for OrzoJS libraries.");
		this.cliOptions
				.addOption("T", false,
						"writes a code template to the standard output (without orzojs.d.ts)");

		this.cliOptions.addOption("s", true,
				"run in a server mode using provided config");
	}

	/**
	 * 
	 */
	private void startServices() throws Exception {
		for (Service service : this.services) {
			service.start();
		}
	}

	/**
	 * 
	 */
	public void stopServices() {
		for (Service service : this.services) {
			service.stop();
		}
	}

	/**
	 * 
	 */
	private CommandLine init(String[] args) throws IOException, ParseException {
		this.props.load(App.class.getClassLoader().getResourceAsStream(
				"net/orzo/app.properties"));
		return (new GnuParser()).parse(this.cliOptions, args);
	}

	/**
	 * 
	 */
	public static void main(final String[] args) {
		final App app = new App();
		Logger log = null;
		CommandLine cmd;

		try {
			cmd = app.init(args);

			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter
						.printHelp(
								"orzo [options] user_script [user_arg1 [user_arg2 [...]]]\n(to generate a template: orzo -t [file path])",
								app.cliOptions);
				
			} else if (cmd.hasOption("v")) {
				System.out.printf("Orzo.js version %s\n",
						app.props.get("orzo.version"));

			} else if (cmd.hasOption("t")) {
				String templateSrc = new ResourceLoader()
						.getResourceAsString("net/orzo/template1.js");

				File tplFile = new File(cmd.getOptionValue("t"));
				FileWriter tplWriter = new FileWriter(tplFile);
				tplWriter.write(templateSrc);
				tplWriter.close();

				File dtsFile = new File(String.format("%s/orzojs.d.ts",
						new File(tplFile.getAbsolutePath()).getParent()));
				FileWriter dtsWriter = new FileWriter(dtsFile);
				String dtsSrc = new ResourceLoader()
						.getResourceAsString("net/orzo/orzojs.d.ts");
				dtsWriter.write(dtsSrc);
				dtsWriter.close();

			} else if (cmd.hasOption("T")) {
				String templateSrc = new ResourceLoader()
						.getResourceAsString("net/orzo/template1.js");
				System.out.println(templateSrc);
				
			} else {

				// Logger initialization
				if (cmd.hasOption("g")) {
					System.setProperty("logback.configurationFile",
							cmd.getOptionValue("g"));
	
				} else {
					System.setProperty("logback.configurationFile", "./logback.xml");
				}
				log = LoggerFactory.getLogger(App.class);
				if (cmd.hasOption("s")) { // Orzo.js as a REST service
					RestServiceConfig conf = new Gson().fromJson(
							new FileReader(cmd.getOptionValue("s")),
							RestServiceConfig.class);
					Injector injector = Guice.createInjector(new CoreModule(
							conf), new RestServletModule());
					HttpServer httpServer = new HttpServer(conf,
							new JerseyGuiceServletConfig(injector));
					app.services.add(httpServer);
					Runtime.getRuntime().addShutdownHook(new ShutdownHook(app));
					app.startServices();

				} else if (cmd.hasOption("d")) { // Demo mode
					final String scriptId = "demo";
					final SourceCode demoScript = SourceCode
							.fromResource(DEMO_SCRIPT);
					System.err.printf("Running demo script %s.",
							demoScript.getName());
					CmdConfig conf = new CmdConfig(scriptId, demoScript, null);
					TaskManager tm = new TaskManager(conf);
					tm.startTaskSync(tm.registerTask(scriptId, new String[0]));
				
				} else if (cmd.getArgs().length > 0) { // Command line mode
					File userScriptFile = new File(cmd.getArgs()[0]);
					String optionalModulesPath = null;
					String[] inputValues;
					SourceCode userScript;

					// custom CommonJS modules path
					if (cmd.hasOption("m")) {
						optionalModulesPath = cmd.getOptionValue("m");
					}

					if (cmd.getArgs().length > 0) {
						inputValues = Arrays.copyOfRange(cmd.getArgs(), 1,
								cmd.getArgs().length);
					} else {
						inputValues = new String[0];
					}

					userScript = SourceCode.fromFile(userScriptFile);
					CmdConfig conf = new CmdConfig(userScript.getName(),
							userScript, optionalModulesPath);
					TaskManager tm = new TaskManager(conf);
					String taskId = tm.registerTask(userScript.getName(), inputValues);
					tm.startTaskSync(taskId);
					if (tm.getTask(taskId).getStatus() == TaskStatus.ERROR) {
						tm.getTask(taskId).getFirstError().getErrors().stream().forEach(
								(err) -> System.err.println(err));
					}



				} else {
					System.err
							.println("Invalid parameters. Try -h for more information.");
					System.exit(1);
				}
			}

		} catch (Exception ex) {
			System.err.println(ex);
			if (log != null) {
				log.error(ex.getMessage(), ex);

			} else {
				ex.printStackTrace();
			}

		} finally {

		}
	}
}
