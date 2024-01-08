package bg.sofia.uni.fmi.mjt.photoalbum;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelMonochromeAlbumCreator implements MonochromeAlbumCreator {
    int imageProcessorsCount;
    public ParallelMonochromeAlbumCreator(int imageProcessorsCount) {
        this.imageProcessorsCount = imageProcessorsCount;
    }

    /**
     * Iterates over all files from @sourceDirectory and picks up image ones - those with extensions jpeg, jpg, and png.
     * Starts a new thread for each image and loads it into a shared data structure.
     * Starts @imageProcessorsCount threads that process the images from the mentioned above shared data structure,
     * and save them into the provided @outputDirectory. In case the @outputDirectory does not exist, it is created.
     *
     * @param sourceDirectory directory from where the image files are taken. The directory should exist,
     *                        throw the appropriate exception if there are issues with loading the files.
     * @param outputDirectory the directory where the output b&w images are stored, if it does not exist, it is created.
     */
    @Override
    public void processImages(String sourceDirectory, String outputDirectory) {
        SynchronizedQueue queue = new SynchronizedQueue();
        int imagesToProcess = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(sourceDirectory))) {
            for (Path imagePath : stream) {
                Thread.ofVirtual().start(new ImageLoaderRunnable(imagePath, queue));
                imagesToProcess++;
            }
        } catch (DirectoryIteratorException | IOException e) {
            throw new RuntimeException(e);
        }
        AtomicInteger atomicImagesToProcess = new AtomicInteger(imagesToProcess);
        for (int i = 0; i < imageProcessorsCount; i++) {
            Thread.ofPlatform().start(new ImageEditorRunnable(queue, atomicImagesToProcess, outputDirectory));
        }
    }
}
