package net.scrumy.dartsass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import net.scrumy.dartsass.OsCheck.OSType;

@Mojo(name = "update-stylesheets", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class UpdateStylesheets extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject project;

	@Parameter(defaultValue = "${settings}", readonly = true)
	private Settings settings;

	@Parameter(property = "sassSourceDirectory", required = true)
	private String sassSourceDirectory;

	@Parameter(property = "sassOptions", required = true)
	private Map<String, String> sassOptions;

	@Parameter(property = "destination", required = true)
	private String destination;

	public void execute() throws MojoExecutionException {
		Util util = new Util(getLog(), settings);
		File dartDir = util.downloadDart();

		try {

			ArrayList<String> cmd = new ArrayList<String>();

			if (OsCheck.getOperatingSystemType() == OSType.Windows) {
				cmd.add(dartDir.getAbsolutePath() + File.separator + "dart-sass" + File.separator + "sass.bat");
			} else {
				cmd.add(dartDir.getAbsolutePath() + File.separator + "dart-sass" + File.separator + "sass");

			}

			if (sassOptions.containsKey("style")) {
				String value = sassOptions.get("style");
				if (value.startsWith(":")) {
					value = value.substring(1);
				}
				cmd.add("--style=" + value);
			}
			
			cmd.add("--load-path=" + sassSourceDirectory);
			cmd.add(sassSourceDirectory + ":" + destination);

			StringBuilder sBld = new StringBuilder();
			for (String s : cmd) {
				sBld.append(s);
				sBld.append(" ");
			}
			getLog().info("Launching Dartsass version " + Util.SASS_VERSION
					+ " process with the following command \r\n         " + sBld.toString());

			ProcessBuilder bld = new ProcessBuilder(cmd);
			bld.directory(project.getBasedir());

			Process process = bld.start();

			InputStream inStm = process.getInputStream();
			InputStream errStm = process.getErrorStream();

			byte[] buff = new byte[20000];
			while (process.isAlive()) {
				while (inStm.available() > 0) {
					int i = inStm.read(buff);
					if (i > 0) {
						String[] lines = new String(buff, 0, i).split("\\r?\\n");
						for (String line : lines) {
							getLog().info(line);
						}
					}
				}

				while (errStm.available() > 0) {
					int i = errStm.read(buff);
					if (i > 0) {
						String[] lines = new String(buff, 0, i).split("\\r?\\n");
						for (String line : lines) {
							getLog().warn(line);
						}
					}
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			getLog().info("DartSass process ended with code " + process.exitValue());

			if (process.exitValue() != 0) {
				throw new MojoExecutionException("DartSass process ended with code " + process.exitValue());
			}

		} catch (IOException e) {
			getLog().error(e);
		}
	}
}