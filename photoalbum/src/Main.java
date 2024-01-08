import bg.sofia.uni.fmi.mjt.photoalbum.ParallelMonochromeAlbumCreator;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        ParallelMonochromeAlbumCreator albumCreator = new ParallelMonochromeAlbumCreator(10);
        long startTime = System.currentTimeMillis();

        albumCreator.processImages("C:\\Users\\aliog\\IdeaProjects\\photoalbum\\images", "C:\\Users\\aliog\\IdeaProjects\\photoalbum\\bw-images");

        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");
    }
}