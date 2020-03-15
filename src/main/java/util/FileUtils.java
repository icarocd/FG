package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.mutable.MutableInt;
import com.google.common.base.Preconditions;

public class FileUtils extends org.apache.commons.io.FileUtils {

	public static void createDirectory(String parentFolder, String name) {
		new File(parentFolder, name).mkdirs();
	}

	public static void cleanOtherwiseCreateDirectory(File folder) {
		if (folder.exists()) {
			cleanDirectory(folder);
		} else {
			folder.mkdirs();
		}
	}

	/**
	 * Cleans a directory without deleting it.
	 * PS: this overrides {@link org.apache.commons.io.FileUtils#cleanDirectory(File)} which doesn't work for huge folders.
	 * @param directory directory to clean
	 */
	public static void cleanDirectory(File directory) {
		if(!directory.exists())
			throw new IllegalArgumentException(directory + " does not exist");
		if(!directory.isDirectory())
			throw new IllegalArgumentException(directory + " is not a directory");

		Logs.finest("Cleaning dir: "+directory);
		final Path rootPath = directory.toPath();
		try {
		    Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
		        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		            Files.delete(file);
		            return FileVisitResult.CONTINUE;
		        }
		        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		            if(!dir.equals(rootPath))
		                Files.delete(dir);
		            return FileVisitResult.CONTINUE;
		        }
		    });
		} catch(IOException e) {
		    throw new RuntimeException(e);
		}
	}

	public static boolean isNotEmptyDir(File dir) {
	    if(!dir.isDirectory())
	        return false;
	    try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath())) {
	        return dirStream.iterator().hasNext();
	    } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	public static File[] getFiles(String folderPath) {
		File folder = new File(folderPath);
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("The File is not a directory! " + folderPath);
		}
		return folder.listFiles();
	}

	public static List<File> getFilesRecursively(File folder, boolean ensureOrder) {
		List<File> container = new ArrayList<>();
		getFilesRecursively(folder, container, ensureOrder ? createFileComparatorByPath() : null);
		return container;
	}

	public static void getFilesRecursively(File folder, List<File> container, Comparator<File> order) {
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("The File is not a directory! " + folder);
		}
		File[] files = folder.listFiles();
		if(order != null){
			Arrays.sort(files, order);
		}
		for (File file : files) {
			if (file.isDirectory()) {
				getFilesRecursively(file, container, order);
			} else if (file.isFile()) {
				container.add(file);
			}
		}
	}

	public static Comparator<File> createFileComparatorByPath() {
		return (o1, o2) -> o1.getPath().compareTo(o2.getPath());
	}

	public static InputStream getFileFromClasspath(String filePath) {
		return FileUtils.class.getClassLoader().getResourceAsStream(filePath);
	}

	public static LineIterator lineIteratorOfFile(String filePath) {
		return lineIteratorOfFile(new File(filePath));
	}

	public static LineIterator lineIteratorOfFile(File file) {
		try {
			return lineIterator(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static LineIterator lineIteratorOfFileFromClasspath(String filePath) {
		return lineIterator(getFileFromClasspath(filePath));
	}

	private static LineIterator lineIterator(InputStream inputStream) {
		try {
			return IOUtils.lineIterator(inputStream, Charset.defaultCharset());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String readFileToString(String pathname) {
		try {
			return readFileToString(new File(pathname));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void closeQuietly(ObjectInput stream) {
		if(stream != null){
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}
	public static void closeQuietly(ObjectOutput stream) {
		if(stream != null){
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
	}
	public static void closeQuietly(OutputStream stream) {
		if(stream != null){
			try {
				stream.close();
			} catch (Exception e) {
			}
		}
	}
	public static void closeQuietly(LineIterator lineIterator) {
		LineIterator.closeQuietly(lineIterator);
	}

	public static List<String> readLines(File f) {
		try {
			return org.apache.commons.io.FileUtils.readLines(f);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<String> readLines(String filepath) {
		return readLines(new File(filepath));
	}

	public static String readFirstLine(File file) {
		try (Stream<String> lines = Files.lines(file.toPath())) {
		    return lines.findFirst().get();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String readLine(File file, int lineNumber) {
		Preconditions.checkArgument(lineNumber >= 1);
		try (Stream<String> lines = Files.lines(file.toPath())) {
		    if(lineNumber > 1){
		    	return lines.skip(lineNumber - 1).findFirst().get();
		    }
			return lines.findFirst().get();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Writer createWriterToFile(String file) {
		return createWriterToFile(new File(file));
	}

	public static Writer createWriterToFile(File file) {
		try {
		    mkDirsForFile(file);
			return new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static PrintStream createPrintStreamToFile(String file) {
		return createPrintStreamToFile(new File(file));
	}

	public static PrintStream createPrintStreamToFile(File file) {
		return createPrintStreamToFile(file, false);
	}
	public static PrintStream createPrintStreamToFile(File file, boolean createDir) {
		try {
			if(createDir)
				mkDirsForFile(file);
			return new PrintStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedReader createReaderFromFile(File file) {
		try {
			return new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static RandomAccessFile createRandomAccessFile(File file, String mode) {
		try {
			return new RandomAccessFile(file, mode);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static Scanner createScannerFromFile(File file) {
		try {
			return new Scanner(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void mkDirs(String folder) {
		mkDirs(new File(folder));
	}

	public static void mkDirs(File folder) {
		if(folder != null)
			folder.mkdirs();
	}

	public static void mkDirsForFile(String filepath) {
		mkDirsForFile(new File(filepath));
	}

	public static void mkDirsForFile(File file) {
		mkDirs(file.getParentFile());
	}

	public static boolean deleteQuietly(String file) {
		return file == null ? false : deleteQuietly(new File(file));
	}

	public static File createTempFile(String filename) {
		return new File(getTempDirectory(), filename);
	}

	public static boolean isFolder(String file) {
		return new File(file).isDirectory();
	}

	public static boolean exists(String path){
		return new File(path).exists();
	}

	public static String getParent(String filePath) {
		return new File(filePath).getParent();
	}

	public static void clean(MappedByteBuffer byteBuffer) {
		if (byteBuffer == null) {
			return;
		}
		// we could use type cast and call functions without reflection code,
		// but import from sun.* package is risky for non-SUN virtual machine.
		// try { ((sun.nio.ch.DirectBuffer)cb).cleaner().clean(); } catch (Exception e) { e.printStackTrace(); }
		try {
			Method cleaner = byteBuffer.getClass().getMethod("cleaner");
			cleaner.setAccessible(true);
			Method clean = Class.forName("sun.misc.Cleaner").getMethod("clean");
			clean.setAccessible(true);
			clean.invoke(cleaner.invoke(byteBuffer));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns an iterator for the files within folder, including internal folders. The iterator does not return the folders, just files.
	 * PS: This implementation aims at avoiding putting in memory all files for iteration.
	 */
	public static Iterable<File> iterableFiles(File folder) {
		assertDirectoryExists(folder);
		return com.google.common.io.Files.fileTreeTraverser().postOrderTraversal(folder).filter(f -> f.isFile());
	}

	/**
	 * Performs a task over each file within a folder. The task is performed only over files; folders are not considered.
	 * PS: This implementation aims at avoiding putting in memory all files for iteration.
	 */
	public static void forEachFileWithinFolder(File folder, boolean recursive, Consumer<? super File> task) {
	    forEachFileWithinFolder_interruptable(folder, recursive, f -> {
	        task.accept(f);
	        return true;
	    });
	}
	public static void forEachFileWithinFolder_interruptable(File folder, boolean recursive, Predicate<? super File> task) {
		assertDirectoryExists(folder);
		try {
			Files.walkFileTree(folder.toPath(), EnumSet.noneOf(FileVisitOption.class), recursive ? Integer.MAX_VALUE : 1,
				new SimpleFileVisitor<Path>(){
					public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
						File f = path.toFile();
						if(f.isFile()) {
							boolean continue_ = task.test(f);
							if(!continue_){
							    return FileVisitResult.TERMINATE;
							}
						}
						return FileVisitResult.CONTINUE;
					}
				}
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void assertDirectoryExists(File folder){
		if(!folder.isDirectory())
			throw new IllegalArgumentException(folder + " must be an existing folder");
	}

	public static Stream<File> streamOfFiles(File folder) {
		return streamOfFiles(folder, Integer.MAX_VALUE);
	}
	public static Stream<File> streamOfFiles(File folder, int maxDepth) {
        try {
            assertDirectoryExists(folder);
            return Files.walk(folder.toPath(), maxDepth).map(Path::toFile).filter(File::isFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static <T> Stream<T> streamOfFiles(File folder, Function<? super File, ? extends T> mapper) {
        return streamOfFiles(folder).map(mapper);
    }

    /** IMPORTANT: you need to close the returned Stream, either manually or using a try-with-resources block, otherwise the resource is left open! */
    public static Stream<String> lines(Path path, Charset charset) {
    	try {
            return Files.lines(path, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static Stream<String> lines(File file) {
    	return lines(file.toPath(), Charset.defaultCharset());
    }
    public static <T> Stream<T> lines(File file, Function<String,T> transformer) {
    	return lines(file.toPath(), transformer);
    }
    public static <T> Stream<T> lines(Path path, Function<String,T> transformer) {
        return lines(path, Charset.defaultCharset()).map(transformer);
    }
    public static Stream<Long> linesOfLongs(Path path) {
        return lines(path, line -> Long.valueOf(line));
    }

    public static <T extends Comparable<T>> SortedSet<T> linesAsSortedSet(Path path, Function<String,T> transformer) {
		try( Stream<T> stream = lines(path, transformer) ){
			return DataStructureUtils.asSortedSet(stream);
		}
    }
    public static SortedSet<Long> linesOfLongsSortedSet(Path path){
        try( Stream<Long> lines = linesOfLongs(path) ){
        	return DataStructureUtils.asSortedSet(lines);
        }
    }

    public static long getLineCount(File file){
    	return getLineCount(file, Charset.defaultCharset());
    }
    public static long getLineCount(File file, Charset charset){
    	try( Stream<String> stream = lines(file.toPath(), charset) ){
    		return stream.count();
    	}
    }
    public static long getLineCount(BufferedReader in){
    	return in.lines().count();
    }
    public static long getLineCount(String s) throws IOException{
    	try( BufferedReader reader = StringUtils.toBufferedReader(s); ){
    		return getLineCount(reader);
    	}
    }

	public static void forEachLine(File file, Consumer<String> consumer){
		try( Stream<String> stream = lines(file) ){
			stream.forEach(consumer);
		}
	}

	public static int countDirFiles(File folder, boolean recursive){
		if( !folder.exists() )
			return 0;
		MutableInt count = new MutableInt();
		forEachFileWithinFolder(folder, recursive, file -> count.increment());
		return count.intValue();
	}

	public static void renameOnFolder(File rootFolder, int maxDepth, boolean onlyTest, boolean walkThroughRenamedFolder, Predicate<String> filenamePredicate, BiFunction<File,String,String> nameReplacer) {
		if(maxDepth < 1)
			return;
		for(File el : rootFolder.listFiles()) {
			String name = el.getName();
			boolean rename = filenamePredicate.test(name);
			if(rename){
				File newEl = new File(el.getParentFile(), nameReplacer.apply(el,name));
				if(!el.equals(newEl)){
					Logs.info("renaming " + el + " to "+newEl);
					if(!onlyTest){
						el.renameTo(newEl);
						el = newEl;
					}
				}
			}
			if(el.isDirectory() && (!rename || walkThroughRenamedFolder))
				renameOnFolder(el, maxDepth - 1, onlyTest, walkThroughRenamedFolder, filenamePredicate, nameReplacer);
		}
	}

	public static File get(File first, String... more){
		for(String el : more)
			first = new File(first, el);
		return first;
	}
	public static File get(String first_, String... more){
		File first = new File(first_);
		for(String el : more)
			first = new File(first, el);
		return first;
	}
}
