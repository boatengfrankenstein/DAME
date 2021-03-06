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
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyUtils {

	private final static Logger log = LoggerFactory.getLogger(KeyUtils.class);

	private final static String KEYTOOL = "keytool";

	final static String ALIAS = "srss";
	final static String PASSWORD = "srss2015";
	final static String CN = "Gruppo Sicurezza";
	final static String OU = "SRSS";
	final static String O = "Unisannio";
	final static String C = "IT";
	final static String KEY_ALG = "RSA";
	final static int KEY_SIZE = 2048;
	final static int VALIDITY = 10000;

	public static File autoGenerateKeyStore() throws IOException {
		return generateKeyStore(CN, OU, O, C, ALIAS, PASSWORD, PASSWORD,
				KEY_ALG, KEY_SIZE, VALIDITY);
	}

	public static File generateKeyStore(String cn, String ou, String o,
			String c, String alias, String keypass, String storepass,
			String keyalg, int keysize, int validity) throws IOException {
		StringBuffer output = new StringBuffer();
		StringBuffer error = new StringBuffer();
		File tmpDir = Files.createTempDirectory("srss").toFile();
		tmpDir.deleteOnExit();
		String keyStorePath = tmpDir.toString();
		if (!keyStorePath.endsWith(File.separator))
			keyStorePath += File.separator;
		keyStorePath += "tmp_key_store";
		log.debug("Generating keystore in " + keyStorePath);
		int exitCode = ExecUtils.exec(output, error, null, KEYTOOL,
				"-genkeypair", "-dname", "cn=" + cn + ", ou=" + ou + ", o=" + o
						+ ", c=" + c, "-alias", alias, "-keypass", keypass,
				"-keystore", keyStorePath, "-storepass", storepass,
				"-validity", validity + "", "-keyalg", keyalg, "-keysize",
				keysize + "");
		if (exitCode != 0) {
			String err = "Error while generating the keystore (exit code "
					+ exitCode + "): " + output.toString() + "\n"
					+ error.toString();
			log.error(err);
			throw new IOException(err);
		}
		log.debug("Keystore successfully generated");
		return new File(keyStorePath);
	}
}
