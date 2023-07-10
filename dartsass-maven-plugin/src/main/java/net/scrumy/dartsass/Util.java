package net.scrumy.dartsass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.archiver.tar.GZipTarFile;

import net.scrumy.dartsass.OsCheck.OSType;

public class Util {

	private final Log log;
	
	public static final String SASS_VERSION = "1.63.6";

	public Util(Log log, Settings settings) {
		this.log = log;

		for (Proxy proxy : settings.getProxies()) {
			if (proxy.isActive() && proxy.getProtocol().equalsIgnoreCase("http")) {
				System.setProperty("http.proxyHost", proxy.getHost());
				System.setProperty("http.proxyPort", Integer.toString(proxy.getPort()));
			}
			if (proxy.isActive() && proxy.getProtocol().equalsIgnoreCase("https")) {
				System.setProperty("https.proxyHost", proxy.getHost());
				System.setProperty("https.proxyPort", Integer.toString(proxy.getPort()));
			}
		}
	}

	public void copyToFile(InputStream in, File out) throws IOException {
		byte[] buffer = new byte[4096];
		int bytesIn = in.read(buffer);
		FileOutputStream fout = new FileOutputStream(out);
		while (bytesIn > 0) {
			fout.write(buffer, 0, bytesIn);
			bytesIn = in.read(buffer);
		}
		fout.close();
		in.close();
	}

	public File downloadDart() throws MojoExecutionException {
		String dartDownloadURL = "";
		switch (OsCheck.getOperatingSystemType()) {
		case Linux:
			dartDownloadURL = "https://github.com/sass/dart-sass/releases/download/"+SASS_VERSION+"/dart-sass-"+SASS_VERSION+"-linux-x64.tar.gz";
			break;
		case MacOS:
			dartDownloadURL = "https://github.com/sass/dart-sass/releases/download/"+SASS_VERSION+"/dart-sass-"+SASS_VERSION+"-macos-x64.tar.gz";
			break;
		case Windows:
			dartDownloadURL = "https://github.com/sass/dart-sass/releases/download/"+SASS_VERSION+"/dart-sass-"+SASS_VERSION+"-windows-x64.zip";
			break;

		default:
			throw new MojoExecutionException("Unable to identify OS ");
		}

		try {
			File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "dartsass-maven-plugin");
		
			
			if (new File(tempDir,"dart-sass").exists() && new File(tempDir,"version-"+SASS_VERSION+".txt").exists()) {
				log.info("Skipping download of Dart SASS - Files already exist");
				return tempDir;
			}
			
			if (tempDir.exists()) {
				FileUtils.cleanDirectory(tempDir);
			} else {
				tempDir.mkdir();
			}
			
			
			//Create version indicator file.
			FileOutputStream ver = new FileOutputStream(new File(tempDir,"version-"+SASS_VERSION+".txt"));
			ver.write(SASS_VERSION.getBytes());
			ver.close();
			

			URL url = new URL(dartDownloadURL);

			log.info("Downloading Dart SASS from " + url);

			if (OsCheck.getOperatingSystemType() == OSType.Windows) {
				File outputFile = new File(tempDir, "dartsass.zip");

				copyToFile(url.openStream(), outputFile);

				try (ZipFile file = new ZipFile(outputFile)) {
					Enumeration<ZipArchiveEntry> en = file.getEntries();
					while (en.hasMoreElements()) {
						ZipArchiveEntry entry = en.nextElement();

						File unzipedOuput = new File(tempDir, entry.getName());
						unzipedOuput.getParentFile().mkdirs();
						copyToFile(file.getInputStream(entry), unzipedOuput);
					}
				}

			} else {
				File outputFile = new File(tempDir, "dartsass.tgz");

				copyToFile(url.openStream(), outputFile);

				GZipTarFile file = new GZipTarFile(outputFile);
				Enumeration<ArchiveEntry> en = file.getEntries();
				while (en.hasMoreElements()) {
					ArchiveEntry entry = (ArchiveEntry) en.nextElement();

					File unzipedOuput = new File(tempDir, entry.getName());
					unzipedOuput.getParentFile().mkdirs();
					copyToFile(file.getInputStream(entry), unzipedOuput);

					if (entry instanceof TarArchiveEntry) {
						TarArchiveEntry tentry = (TarArchiveEntry) entry;
						unzipedOuput.setExecutable((tentry.getMode() & 0111) != 0);
					}
				}
				file.close();
			}

			return tempDir;
		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}
}
