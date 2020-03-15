package dataMining;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;
import util.FileUtils;

/**
 * Sample resolver that groups samples in sub-folders according to their first digits in their ids, so that a unique folder does not get too many files.
 */
public class SamplePathResolverDistributed extends SamplePathResolver {

    public SamplePathResolverDistributed(File folder) {
        super(folder);
    }

    private File getSampleDir(long id) {
        String idString = String.valueOf(id);
        return new File(folder, idString.length() > 1 ? idString.substring(0, 2) : idString);
    }

    @Override
    public File getSampleFile(long id, String filename) {
        return new File(getSampleDir(id), filename);
    }

    @Override
    public void forEachFile(boolean parallel, Consumer<? super File> task){
        if(!folder.isDirectory()){
            throw new IllegalStateException("It must be an existing folder: " + folder);
        }
        Stream<File> subFolders = Arrays.stream(folder.listFiles());
        if (parallel) {
            subFolders = subFolders.parallel();
        }
        subFolders.forEach(subFolder -> {
            FileUtils.forEachFileWithinFolder(subFolder, false, task);
        });
    }

    public void forEachSubFolder(boolean parallel, Consumer<? super File> task){
        if(!folder.isDirectory()){
            throw new IllegalStateException("It must be an existing folder: " + folder);
        }
        Stream<File> subFolders = Arrays.stream(folder.listFiles());
        if (parallel) {
            subFolders = subFolders.parallel();
        }
        subFolders.forEach(subFolder -> {
            task.accept(subFolder);
        });
    }
}
