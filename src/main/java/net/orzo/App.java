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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import net.orzo.scripting.SourceCode;
import net.orzo.tools.ResourceLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry class. Handles command line parameters and runs the calculation.
 * 
 * @author Tomas Machalek <tomas.machalek@gmail.com>
 */
public final class App {
	
	protected final Properties props;
	
	private final Options cliOptions;

	private final static String USERENV_PATH = "net/orzo/userenv.js";

	private final static String DATALIB_SCRIPT = "net/orzo/datalib.js";

	private final static String DEMO_SCRIPT = "net/orzo/demo1.js";
	
	private final static String CALCULATION_SCRIPT = "net/orzo/calculation.js";

	/**
	 * 
	 */
	public App() {
		this.props = new Properties();		
		this.cliOptions = new Options();		
		this.cliOptions.addOption("v", false, "shows version information");
		this.cliOptions.addOption("d", false, "runs a demo program");
		this.cliOptions
				.addOption("g", true,
						"custom path to a Logback configuration XML file (default is ./logback.xml)");
		this.cliOptions
				.addOption(
						"l",
						true,
						"custom datalib file (advanced feature - modules should cover most of the cases)");
		this.cliOptions.addOption("h", false, "print help");
		this.cliOptions
				.addOption(
						"m",
						true,
						"additional module path (the directory where your script is located is always included)");
		this.cliOptions
				.addOption("t", false,
						"writes a code template to a specified file or to the standard output");		
	}
	
	/**
	 * 
	 */
	private CommandLine init(String[] args) throws IOException, ParseException {
		this.props.load(App.class.getClassLoader().getResourceAsStream("net/orzo/app.properties"));		
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
				System.exit(0);
				
			} else if (cmd.hasOption("v")) {
				System.out.printf("Orzo.js version %s\n", app.props.get("orzo.version"));
				System.exit(0);

			} else if (cmd.hasOption("t")) {
				String templateSrc = new ResourceLoader()
						.getResourceAsString("net/orzo/template1.js");

				if (cmd.getArgs().length > 0) {
					FileWriter fr = new FileWriter(cmd.getArgs()[0]);
					fr.write(templateSrc);
					fr.close();

				} else {
					System.out.println(templateSrc);
				}
				System.exit(0);
			}

			// Logger initialization
			if (cmd.hasOption("g")) {
				System.setProperty("logback.configurationFile",
						cmd.getOptionValue("g"));

			} else {
				System.setProperty("logback.configurationFile", "./logback.xml");
			}
			log = LoggerFactory.getLogger(App.class);

			CalculationParams params = new CalculationParams();
			if (System.getProperty("orzodir") != null) { // defined by exe4j executable				
				params.orzoModulesPath = new File(String.format("%s%slib",
						System.getProperty("orzodir"), File.separator)).getAbsolutePath();
			}
			params.userenvScript = SourceCode.fromResource(USERENV_PATH);
			params.calculationScript = SourceCode.fromResource(CALCULATION_SCRIPT);

			// datalib file
			if (cmd.hasOption("s")) {
				params.datalibScript = SourceCode.fromFile(new File(cmd
						.getOptionValue("s")));

			} else {
				params.datalibScript = SourceCode.fromResource(DATALIB_SCRIPT);
			}

			// custom CommonJS modules path
			if (cmd.hasOption("m")) {
				params.optionalModulesPath = cmd.getOptionValue("m");
			}

			// user script and derived CommonJS modules path
			if (cmd.getArgs().length > 0) {
				File userScriptFile = new File(cmd.getArgs()[0]);
				params.userScript = SourceCode.fromFile(userScriptFile);
				params.workingDirModulesPath = userScriptFile.getParent();

			} else if (cmd.hasOption("d")) {
				System.err
						.printf("Running demo script %s.\nUse the -h parameter for more information.\n",
								DEMO_SCRIPT);
				params.userScript = SourceCode.fromResource(DEMO_SCRIPT);
				params.workingDirModulesPath = ".";
				
			} else {
				System.err.println("Invalid parameters. Try -h for more information.");
				System.exit(1);
			}
			
			if (cmd.getArgs().length > 0) {
				params.inputValues = Arrays.copyOfRange(cmd.getArgs(), 1,
						cmd.getArgs().length);
			} else {
				params.inputValues = new String[0];
			}			

			Runtime.getRuntime().addShutdownHook(new ShutdownHook(app));
			Calculation proc = new Calculation(params);
			proc.run();

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
