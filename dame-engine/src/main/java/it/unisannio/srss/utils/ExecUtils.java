/*
 * Copyright 2015 
 * 	Danilo Cianciulli 			<cianciullidanilo@gmail.com>
 * 	Emranno Francesco Sannini 	<esannini@gmail.com>
 * 	Roberto Falzarano 			<robertofalzarano@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unisannio.srss.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExecUtils {

	private final static Logger log = LoggerFactory.getLogger(ExecUtils.class);

	/**
	 * Esegue un comando
	 * 
	 * @param outputBuffer
	 *            Una volta eseguito il comando, questo buffer conterrà
	 *            l'output.
	 * @param commandAndArgs
	 *            Comando da eseguire, seguito dai parametri.
	 * @return Il codice di uscita dell'esecuzione. Viene restituito -1 se
	 *         l'esecuzione viene interrotta.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int exec(StringBuffer outputBuffer, StringBuffer errorBuffer,
			StringBuffer inputBuffer, String... commandAndArgs)
			throws IOException {
		log.debug("Command execution: " + Arrays.toString(commandAndArgs));
		ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
		Process p = pb.start();
		BufferedReader outputReader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));
		BufferedWriter inputWriter = null;
		if (inputBuffer != null) {
			inputWriter = new BufferedWriter(new OutputStreamWriter(
					p.getOutputStream()));
			inputWriter.write(inputBuffer.toString());
		}
		int exitCode;
		try {
			exitCode = p.waitFor();
		} catch (InterruptedException e) {
			log.error("Command execution interrupted: "
					+ Arrays.toString(commandAndArgs));
			outputReader.close();
			return -1;
		}
		log.debug("Exit code: " + exitCode);
		if (outputBuffer != null) {
			String line = null;
			int i = 0;
			while ((line = outputReader.readLine()) != null) {
				if (i++ > 0)
					outputBuffer.append("\n");
				outputBuffer.append(line);
			}
			log.debug("Output: " + outputBuffer.toString());
		}
		if (errorBuffer != null) {
			String line = null;
			int i = 0;
			while ((line = errorReader.readLine()) != null) {
				if (i++ > 0)
					errorBuffer.append("\n");
				errorBuffer.append(line);
			}
			log.debug("Error: " + errorBuffer.toString());
		}
		try {
			outputReader.close();
		} catch (Exception e) {
		}
		try {
			errorReader.close();
		} catch (Exception e) {
		}
		try {
			if (inputWriter != null)
				inputWriter.close();
		} catch (Exception e) {
		}
		return exitCode;
	}
}
