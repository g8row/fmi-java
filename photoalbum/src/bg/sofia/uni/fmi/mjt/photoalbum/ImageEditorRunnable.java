package bg.sofia.uni.fmi.mjt.photoalbum;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageEditorRunnable implements Runnable {
    SynchronizedQueue queue;
    AtomicInteger imagesToProcess;
    String destinationDirectory;

    ImageEditorRunnable(SynchronizedQueue queue, AtomicInteger imagesToProcess, String destinationDirectory) {
        this.queue = queue;
        this.imagesToProcess = imagesToProcess;
        this.destinationDirectory = destinationDirectory;
    }

    private Image convertToBlackAndWhite(Image image) {
        BufferedImage processedData = new BufferedImage(image.data.getWidth(), image.data.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        processedData.getGraphics().drawImage(image.data, 0, 0, null);

        return new Image(image.name, processedData);
    }

    @Override
    public void run() {
        while (imagesToProcess.decrementAndGet() >= 0) {
            Image image = null;
            while (image == null) {
                image = queue.poll();
            }
            Image toSave = convertToBlackAndWhite(image);
            try {
                ImageIO.write(toSave.data, "jpg", new File(destinationDirectory, toSave.name));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
