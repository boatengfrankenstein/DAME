package it.unisannio.srss.dame.injection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Danilo Cianciulli
 *
 */
public class CallInjector {

	private final static String SMALI_NETWORK_CALL_FILE = "network_call.smali";
	private final static String SMALI_PAYLOAD_CALL_FILE = "payload_call.smali";

	private final static String INTERNET_PERMISSION = "INTERNET";

	private final static Logger logger = LoggerFactory
			.getLogger(CallInjector.class);

	private final Map<String, Set<UsagePoint>> apkPermissions;

	private final Map<String, List<String>> payloadPermissions;

	private final String basePath;

	private final String smaliNetworkCall, smaliPayloadCall;

	public Map<String, Set<UsagePoint>> getApkPermissions() {
		return apkPermissions;
	}

	public Map<String, List<String>> getPayloadPermissions() {
		return payloadPermissions;
	}

	public String getBasePath() {
		return basePath;
	}

	/**
	 * 
	 * @param apkPermissions
	 *            Mappa che ha come chiave i permessi e come valori i punti di
	 *            utilizzo di questi nell'apk trusted
	 * @param payloadPermissions
	 *            Mappa che ha come chiave i permessi e come valori i payload
	 *            che li utilizzano
	 * @param basePath
	 * @throws FileNotFoundException
	 */
	public CallInjector(Map<String, Set<UsagePoint>> apkPermissions,
			Map<String, List<String>> payloadPermissions, String basePath)
			throws FileNotFoundException {
		this.apkPermissions = apkPermissions;
		this.payloadPermissions = payloadPermissions;
		this.basePath = basePath.endsWith(File.separator) ? basePath.substring(
				0, basePath.length() - 1) : basePath;
		this.smaliNetworkCall = importResource(SMALI_NETWORK_CALL_FILE);
		this.smaliPayloadCall = importResource(SMALI_PAYLOAD_CALL_FILE);
	}

	public void inject() {
		Set<String> permissions = payloadPermissions.keySet();
		for (String permission : permissions) {
			injectCalls(permission);
		}
		injectCalls(INTERNET_PERMISSION, false);
	}

	private File fromClassToFile(String canonicalName)
			throws FileNotFoundException {
		int len = canonicalName.indexOf(";");
		if (len == -1)
			len = canonicalName.length();
		String adjustedCanonicalName = canonicalName.substring(1, len);
		String path = basePath + File.separator + "smali" + File.separator
				+ adjustedCanonicalName + ".smali";
		logger.debug("\"" + canonicalName + "\" --> \"" + path + "\"");
		File res = new File(path);
		if (!res.canRead() || !res.isFile()) {
			String err = "Could not find the file " + path;
			logger.error(err);
			throw new FileNotFoundException(err);
		}
		return res;
	}

	private void injectCalls(String permission) {
		injectCalls(permission, true);
	}

	private void injectCalls(String permission, boolean payload) {
		Set<UsagePoint> usagePoint = apkPermissions.get(permission);
		if (usagePoint == null || usagePoint.size() == 0) {
			logger.warn("The trusted APK does not use " + permission
					+ " permission.");
			return;
		}
		File file = null;
		for (UsagePoint up : usagePoint) {
			try {
				file = fromClassToFile(up.getClazz());
				StringBuffer sb = injectCallInMethod(file, up.getMethod(),
						payload ? payloadPermissions.get(permission) : null);
				writeFile(file, sb);
			} catch (IOException e) {
				logger.warn("Skipping network call injection for class "
						+ up.getClazz());
			}
		}
	}

	private enum State {
		MATCH_METHOD, MATCH_LOCALS, MATCH_RETURN, INJECTION_DONE;
	}

	private StringBuffer injectCallInMethod(File file, String method,
			List<String> payloads) throws FileNotFoundException {
		StringBuffer res = new StringBuffer();
		Scanner sc = new Scanner(file);
		String line = "";
		Matcher matcher = null;
		Pattern methodPattern = buildMethodPattern(method);
		Pattern returnPattern = buildReturnPattern();
		Pattern localsPattern = buildLocalsPattern();
		State state = State.MATCH_METHOD;
		int locals = 0;
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			switch (state) {
			case MATCH_METHOD:
				matcher = methodPattern.matcher(line);
				if (matcher.matches() && payloads == null)
					state = State.MATCH_RETURN;
				else if (matcher.matches() && payloads != null)
					state = State.MATCH_LOCALS;
				break;
			case MATCH_LOCALS:
				matcher = localsPattern.matcher(line);
				if (matcher.matches()) {
					locals = Integer.parseInt(matcher.group(1));
					line = "    .locals " + (locals + payloads.size());
					state = State.MATCH_RETURN;
				}
				break;
			case MATCH_RETURN:
				matcher = returnPattern.matcher(line);
				if (matcher.matches()) {
					if (payloads == null) {
						res.append(smaliNetworkCall + "\n");
					} else {
						for (String payload : payloads) {
							res.append(smaliPayloadCall.replaceAll(
									"\\$\\{var_id\\}", locals++ + "")
									.replaceAll("\\$\\{payload_class\\}",
											payload));
						}
					}
					state = State.INJECTION_DONE;
				}
				break;
			case INJECTION_DONE:
				break;
			}
			res.append(line);
			if (sc.hasNextLine())
				res.append("\n");
		}
		sc.close();
		return res;

	}

	private static String importResource(String fileName)
			throws FileNotFoundException {
		logger.debug("Importing file \"" + fileName + "\" from classpath;");
		InputStream in = ClassLoader
				.getSystemResourceAsStream(SMALI_NETWORK_CALL_FILE);
		if (in == null) {
			String err = "Could not locate the file \"" + fileName
					+ "\" in the classpath";
			logger.error(err);
			throw new FileNotFoundException(err);
		}
		String res = "";
		Scanner sc = new Scanner(in);
		while (sc.hasNextLine()) {
			res += sc.nextLine();
			if (sc.hasNextLine())
				res += "\n";
		}
		sc.close();
		return res;
	}

	private static Pattern buildMethodPattern(String method) {
		return Pattern.compile("^\\s*\\.method\\s.*(?!static).*" + method
				+ "\\s*\\(.*\\)\\s*(.+)\\s*$");
	}

	private static Pattern buildLocalsPattern() {
		return Pattern.compile("^\\s*\\.locals\\s+(\\d+)\\s*$");
	}

	private static Pattern buildReturnPattern() {
		return Pattern.compile("^\\s*return(\\-.*)?\\s*$");
	}

	private static void writeFile(File file, StringBuffer text)
			throws IOException {
		if (!file.isFile() || !file.canWrite()) {
			String err = "Could not write file " + file.getAbsolutePath();
			logger.error(err);
			throw new IOException(err);
		}
		file.delete();
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		output.write(text.toString());
		output.close();
	}

}