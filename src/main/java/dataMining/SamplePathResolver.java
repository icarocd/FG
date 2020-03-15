package dataMining;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;
import util.FileUtils;

public abstract class SamplePathResolver implements Iterable<File> {

	protected File folder;

	public SamplePathResolver(File folder) {
		this.folder = folder;
	}

    public void initialize(boolean incremental) {
        if (!folder.exists()) {
            folder.mkdirs();
        } else if (!incremental) {
            FileUtils.cleanDirectory(folder);
        }
    }

    public File getRootFolder() {
        return folder;
    }

	public abstract File getSampleFile(long id, String filename);

	public void copySampleFile(long id, String filename, SamplePathResolver destineDir, boolean incremental) throws IOException {
		File destineFile = destineDir.getSampleFile(id, filename);
		if(!incremental || !destineFile.exists()){
			File originFile = getSampleFile(id, filename);
			FileUtils.copyFile(originFile, destineFile);
		}
    }

	public abstract void forEachFile(boolean parallel, Consumer<? super File> task);

	@Override
	public final Iterator<File> iterator() {
	    return FileUtils.iterableFiles(folder).iterator();
	}

	public boolean exists() {
		return folder.isDirectory();
	}

	public int countFiles(){
		return FileUtils.countDirFiles(folder, true);
	}

	public String toString() {
		return folder.toString();
	}
}
