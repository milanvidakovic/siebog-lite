package siebog.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.vfs.VirtualFile;

public class FileUtils {
	public static String read(File file) throws IOException {
		return read(new FileInputStream(file));
	}

	public static String read(String resource, int dummy) throws IOException {
		return read(FileUtils.class.getResourceAsStream(resource));
	}

	public static String read(InputStream in) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			StringBuilder str = new StringBuilder(in.available());
			String line;
			String nl = "";
			while ((line = reader.readLine()) != null) {
				str.append(nl);
				if (nl.length() == 0)
					nl = "\n";
				str.append(line);
			}
			return str.toString();
		}
	}

	public static void write(File file, String data) throws IOException {
		try (PrintWriter out = new PrintWriter(file)) {
			out.print(data);
		}
	}

	public static File createTempFile(String data) throws IOException {
		File f = File.createTempFile("siebog", null);
		if (data != null)
			write(f, data);
		return f;
	}
	
	public static File getFile(Class<?> c, String prefix, String fileName) {
		File f = null;
		
		URL url = c.getResource(prefix + fileName);
		
		if (url != null) {
			if (url.toString().startsWith("vfs:/")) {
				try {
					URLConnection conn = new URL(url.toString()).openConnection();
					VirtualFile vf = (VirtualFile)conn.getContent();
					f = vf.getPhysicalFile();
				} catch (Exception ex) {
					ex.printStackTrace();
					f = new File(".");
				}
			} else {
				try {
					f = new File(url.toURI());
				} catch (URISyntaxException e) {
					e.printStackTrace();
					f = new File(".");
				}
			}
		} else {
			f = new File(fileName);
		}
				
		return f;
	}
}
