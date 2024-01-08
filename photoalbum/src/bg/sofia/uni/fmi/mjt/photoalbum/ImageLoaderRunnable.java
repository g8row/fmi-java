package bg.sofia.uni.fmi.mjt.photoalbum;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class ImageLoaderRunnable implements Runnable {
    Path imagePath;
    SynchronizedQueue queue;
    ImageLoaderRunnable(Path imagePath, SynchronizedQueue queue) {
        this.imagePath = imagePath;
        this.queue = queue;
    }

    public Image loadImage(Path imagePath) {
        try {
            BufferedImage imageData = ImageIO.read(imagePath.toFile());
            return new Image(imagePath.getFileName().toString(), imageData);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to load image %s", imagePath), e);
        }
    }

    @Override
    public void run() {
        queue.put(loadImage(imagePath));
    }
}
