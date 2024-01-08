package bg.sofia.uni.fmi.mjt.photoalbum;

import java.util.ArrayDeque;
import java.util.Queue;

public class SynchronizedQueue {
    Queue<Image> imageQueue;

    SynchronizedQueue() {
        imageQueue = new ArrayDeque<>();
    }

    public synchronized void put(Image image) {
        this.notifyAll();
        imageQueue.add(image);
    }

    public synchronized Image poll() {
        this.notifyAll();
        return imageQueue.poll();
    }

    public synchronized boolean isEmpty() {
        this.notifyAll();
        return imageQueue.isEmpty();
    }
}
