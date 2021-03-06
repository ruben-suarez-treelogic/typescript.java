/**
 *  Copyright (c) 2013-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ts.internal.io.tar.TarEntry;
import ts.internal.io.tar.TarException;
import ts.internal.io.tar.TarInputStream;

/**
 * Zip, tar.gz Utilities.
 *
 */
public class ZipUtils {

	public static final String ZIP_EXTENSION = ".zip";
	public static final String TAR_GZ_EXTENSION = ".tar.gz";
	private static final String BIN_FOLDER = "/bin";

	private ZipUtils() {
	}

	/**
	 * Returns true if the given file is a zip file and false otherwise.
	 * 
	 * @param file
	 * @return true if the given file is a zip file and false otherwise.
	 */
	public static boolean isZipFile(File file) {
		return file.isFile() && file.getName().toLowerCase().endsWith(ZIP_EXTENSION);
	}

	/**
	 * Returns true if the given file is a zip file and false otherwise.
	 * 
	 * @param file
	 * @return true if the given file is a zip file and false otherwise.
	 */
	public static boolean isTarFile(File file) {
		return file.isFile() && file.getName().toLowerCase().endsWith(TAR_GZ_EXTENSION);
	}

	/**
	 * Extract zip file to destination folder.
	 *
	 * @param file
	 *            zip file to extract
	 * @param destination
	 *            destination folder
	 */
	public static void extractZip(File file, File destination) throws IOException {
		ZipInputStream in = null;
		OutputStream out = null;
		try {
			// Open the ZIP file
			in = new ZipInputStream(new FileInputStream(file));

			// Get the first entry
			ZipEntry entry = null;

			while ((entry = in.getNextEntry()) != null) {
				String outFilename = entry.getName();

				// Open the output file
				File extracted = new File(destination, outFilename);
				if (entry.isDirectory()) {
					extracted.mkdirs();
				} else {
					// Be sure that parent file exists
					File baseDir = extracted.getParentFile();
					if (!baseDir.exists()) {
						baseDir.mkdirs();
					}

					out = new FileOutputStream(extracted);

					// Transfer bytes from the ZIP file to the output file
					byte[] buf = new byte[1024];
					int len;

					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}

					// Close the stream
					out.close();
					if (extracted.getParent().contains(BIN_FOLDER)) {
						extracted.setExecutable(true);
					}
				}
			}
		} finally {
			// Close the stream
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Extract tar.gz file to destination folder.
	 *
	 * @param file
	 *            zip file to extract
	 * @param destination
	 *            destination folder
	 */
	public static void extractTar(File file, File destination) throws IOException {
		TarInputStream in = null;
		OutputStream out = null;
		try {
			// Open the ZIP file
			in = new TarInputStream(new GZIPInputStream(new FileInputStream(file)));

			// Get the first entry
			TarEntry entry = null;

			while ((entry = in.getNextEntry()) != null) {
				String outFilename = entry.getName();

				switch (entry.getFileType()) {
				case TarEntry.DIRECTORY:
					File extractedDir = new File(destination, outFilename);
					if (extractedDir.isDirectory()) {
						extractedDir.mkdirs();
					}
					break;
				case TarEntry.FILE:
					File extractedFile = new File(destination, outFilename);
					// Be sure that parent file exists
					File baseDir = extractedFile.getParentFile();
					if (!baseDir.exists()) {
						baseDir.mkdirs();
					}

					out = new FileOutputStream(extractedFile);

					// Transfer bytes from the ZIP file to the output file
					byte[] buf = new byte[1024];
					int len;

					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}

					// Close the stream
					out.close();
					if (extractedFile.getParent().contains(BIN_FOLDER)) {
						extractedFile.setExecutable(true);
					}
					break;
				case TarEntry.LINK:
					File linkFile = new File(destination, outFilename);
					Path target = new File(linkFile.getParentFile(), entry.getLinkName()).toPath();
					Files.createLink(linkFile.toPath(), target);
					break;
				case TarEntry.SYM_LINK:
					File symLinkFile = new File(destination, outFilename);
					Path symTarget = new File(symLinkFile.getParentFile(), entry.getLinkName()).toPath();
					Files.createSymbolicLink(symLinkFile.toPath(), symTarget);
					break;
				}
			}
		} catch (TarException e) {
			throw new IOException(e);
		} finally {
			// Close the stream
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}

	}

}
