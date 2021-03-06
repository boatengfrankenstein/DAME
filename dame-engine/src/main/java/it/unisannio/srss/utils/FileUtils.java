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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Danilo Cianciulli
 *
 */
public final class FileUtils {
	private final static Logger log = LoggerFactory.getLogger(FileUtils.class);

	/**
	 * Verifica che un file esista e che possa essere letto.
	 * 
	 * @param filePath
	 *            Il percorso del file.
	 * @return L'istanza di {@link java.io.File} che rappresenta il file.
	 * @throws FileNotFoundException
	 *             Se il file non esiste o non può essere letto.
	 */
	public static File checkFile(String filePath) throws FileNotFoundException {
		return checkFile(new File(filePath));
	}

	/**
	 * Verifica che un file esista e che possa essere letto.
	 * 
	 * @param file
	 *            Il percorso del file.
	 * @return Se il file esiste e può essere letto, restituisce la stessa
	 *         istanza di file passata come parametro.
	 * @throws FileNotFoundException
	 *             Se il file non esiste o non può essere letto.
	 */
	public static File checkFile(File file) throws FileNotFoundException {
		if (!file.canRead() || !file.isFile()) {
			String err = "Could not find or read the file: "
					+ file.getAbsolutePath();
			log.error(err);
			throw new FileNotFoundException(err);
		}
		return file;
	}

	/**
	 * Verifica che un file esista, che possa essere letto e che possa essere
	 * eseguito.
	 * 
	 * @param filePath
	 *            Il percorso del file.
	 * @return L'istanza di {@link java.io.File} che rappresenta il file.
	 * @throws FileNotFoundException
	 *             Se il file non esiste o non può essere letto.
	 * @throws IOException
	 *             Se il file esiste ma non può essere eseguito.
	 */
	public static File checkFileForExecution(String filePath)
			throws FileNotFoundException, IOException {
		return checkFileForExecution(new File(filePath));
	}

	/**
	 * Verifica che un file esista, che possa essere letto e che possa essere
	 * eseguito.
	 * 
	 * @param file
	 *            Il percorso del file
	 * @return Se il file esiste, può essere letto e può essere eseguit,
	 *         restituisce la stessa istanza di file passata come parametro.
	 * @throws FileNotFoundException
	 *             Se il file non esiste o non può essere letto.
	 * @throws IOException
	 *             Se il file esiste ma non può essere eseguito.
	 */
	public static File checkFileForExecution(File file)
			throws FileNotFoundException, IOException {
		checkFile(file);
		if (!file.canExecute()) {
			String err = "Could not execute the file: "
					+ file.getAbsolutePath();
			log.error(err);
			throw new IOException(err);
		}
		return file;
	}

	/**
	 * Verifica che una directory esista e che possa essere letta.
	 * 
	 * @param dirPath
	 *            Il percorso della directory.
	 * @return L'istanza di {@link java.io.File} che rappresenta la directory.
	 * @throws FileNotFoundException
	 *             Se la directory non esiste o non può essere letta.
	 */
	public static File checkDir(String dirPath) throws FileNotFoundException {
		return checkDir(new File(dirPath));
	}

	/**
	 * Verifica che una directory esista e che possa essere letta.
	 * 
	 * @param dir
	 *            Il percorso della directory.
	 * @return Se la directory esiste e può essere letta, restituisce la stessa
	 *         istanza passata come parametro.
	 * @throws FileNotFoundException
	 *             Se la directory non esiste o non può essere letta.
	 */
	public static File checkDir(File dir) throws FileNotFoundException {
		if (!dir.isDirectory() || !dir.canRead()) {
			String err = "Could not find or read the directory: "
					+ dir.getAbsolutePath();
			log.error(err);
			throw new FileNotFoundException(err);
		}
		return dir;
	}

	/**
	 * Verifica che una directory esista e che sia accessibile in scrittura. Se
	 * specificato, la directory viene creata.
	 * 
	 * @param dirPath
	 *            Il percorso della directory.
	 * @param create
	 *            <code>true</code> per creare la directory se non esiste,
	 *            <code>false</code> per non crearla.
	 * @return L'istanza di {@link java.io.File} che rappresenta la directory
	 *         (eventualmente creata).
	 * @throws IOException
	 *             Se la directory non può essere acceduta in scrittura o se non
	 *             è possibile crearla.
	 * @throws FileNotFoundException
	 *             Se la directory non esiste e si è scelto di non crearla.
	 */
	public static File checkDirForWriting(String dirPath, boolean create)
			throws IOException, FileNotFoundException {
		return checkDirForWriting(new File(dirPath), create);
	}

	/**
	 * Verifica che una directory esista e che sia accessibile in scrittura. Se
	 * specificato, la directory viene creata.
	 * 
	 * @param dir
	 *            Il percorso della directory.
	 * @param create
	 *            <code>true</code> per creare la directory se non esiste,
	 *            <code>false</code> per non crearla.
	 * @return Se la directory (eventualmente creata) esiste e può essere letta,
	 *         restituisce la stessa istanza passata come parametro.
	 * @throws IOException
	 *             Se la directory non può essere acceduta in scrittura o se non
	 *             è possibile crearla.
	 * @throws FileNotFoundException
	 *             Se la directory non esiste e si è scelto di non crearla.
	 */
	public static File checkDirForWriting(File dir, boolean create)
			throws IOException, FileNotFoundException {
		if (dir.isDirectory() && dir.canWrite())
			return dir;
		if (dir.isDirectory() && !dir.canWrite()) {
			String err = "Could not write to the directory: "
					+ dir.getAbsolutePath();
			log.error(err);
			throw new IOException(err);
		}
		if (!create) {
			String err = "Could not find the given directory: "
					+ dir.getAbsolutePath();
			log.error(err);
			throw new FileNotFoundException(err);
		} else if (!dir.mkdirs()) {
			String err = "Could not create the directory: "
					+ dir.getAbsolutePath();
			log.error(err);
			throw new IOException(err);
		}
		return dir;
	}

}
