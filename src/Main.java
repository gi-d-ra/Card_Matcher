import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*
 * In the screenshot in the center of the poker table there are several cards.
 * By comparing several pixels of the cards program determines
 * what card cards are there and prints info about them to the console.
 *
 * (Using multithreading)
 *
 * Suits:
 * d - diamonds
 * h - hearts
 * s - spades
 * c - clubs
 *
 * (All screenshots in the directory /resources/imgs)
 */

public class Main {

    //map for result
    static final ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<>();

    //image part with cards
    static BufferedImage img_cut;

    //list with all threads
    private static final ArrayList<MyThread> myThreads = new ArrayList<>();

    //pixel offset for each card
    private static final int[] SHIFT = {143, 215, 287, 358, 430, 492};

    //Path to screenshots
    static String path = "src\\resources\\imgs";

    public static void main(String[] args) {
        try {
            File file = new File(path);
            String[] images = file.list();
            assert images != null;
            for (String s : images) {
                try {
                    StringBuilder buffer = new StringBuilder();
                    img_cut = ImageIO.read(new File(file.getPath() + "\\" + s));
                    int i = 0;
                    Color checkColor = new Color(img_cut.getRGB(SHIFT[i] + 42, 600));

                    //check if the card is on the table (card by card)
                    while (checkColor.equals(Color.WHITE) || checkColor.equals(new Color(120, 120, 120))) {
                        //create new thread to work with 1 card
                        myThreads.add(new MyThread(SHIFT[i], 585, 62, 88, i));
                        myThreads.get(i).start();
                        i++;
                        //take a new pixel to check
                        checkColor = new Color(img_cut.getRGB(SHIFT[i] + 42, 600));
                    }

                    //form string for console ("file name" - "cards")
                    buffer.append(s).append(" - ");
                    //waiting for the threads
                    try {
                        for (Thread t : myThreads)
                            t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (map) {
                        for (int j = 0; j < i; j++) {
                            //add to string for console card info
                            buffer.append(map.get(j));
                        }
                        //print info to console
                        System.out.println(buffer.toString());
                        //interrupting alive threads
                        for (Thread t : myThreads)
                            if (t.isAlive())
                                t.interrupt();
                        //cleaning thread list
                        myThreads.clear();
                    }
                } catch (IOException e) {
                    System.out.println("Error! File exception.");
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Error! Empty directory.");
        }
    }

    public static class MyThread extends Thread {
        BufferedImage image;
        String buff;

        public MyThread(int x, int y, int width, int height, int name) {
            super(name + "");
            this.image = img_cut.getSubimage(x, y, width, height);
        }

        public void run() {
            int result = 0b0000;
            boolean blackFlag = false;
            boolean suitFlag = false;
            Color color = new Color(image.getRGB(41, 65)); //328,650
            //set color and suit
            if (isBlack(color)) {
                blackFlag = true;
                color = new Color(image.getRGB(48, 60));
                if (isBlack(color)) {
                    suitFlag = true;
                }
            } else {
                color = new Color(image.getRGB(41, 55));
                if (isRed(color)) {
                    suitFlag = true;
                }
            }
            color = new Color(image.getRGB(14, 28));
            //check first pixel
            if (isBlack(color) || isRed(color)) {
                result += 8;
            }
            color = new Color(image.getRGB(10, 13));
            //check second pixel
            if (isBlack(color) || isRed(color)) {
                result += 4;
            }
            if (result == 12 || result == 0) {
                color = new Color(image.getRGB(9, 25));
                //check third pixel
                if (isBlack(color) || isRed(color)) {
                    result += 2;
                }
                if (result == 14 || result == 12) {
                    color = new Color(image.getRGB(10, 21));
                    //check forth pixel
                    if (isBlack(color) || isRed(color)) {
                        result += 1;
                    }
                }
            }
            if (result == 8) {
                color = new Color(image.getRGB(14, 23));
                //check third pixel
                if (isBlack(color) || isRed(color)) {
                    result += 2;
                    //get forth pixel
                    color = new Color(image.getRGB(18, 14));
                } else {
                    //get forth pixel
                    color = new Color(image.getRGB(18, 23));

                }
                //check forth pixel
                if (isBlack(color) || isRed(color)) {
                    result += 1;
                }
            }
            if (result == 4) {
                color = new Color(image.getRGB(10, 25));
                //check third pixel
                if (isBlack(color) || isRed(color)) {
                    result += 2;
                    color = new Color(image.getRGB(17, 23));
                    //check forth pixel
                    if (isBlack(color) || isRed(color)) {
                        result += 1;
                    }
                }
            }
            synchronized (map) {
                getResult(blackFlag, suitFlag, result);
            }
        }

        public synchronized void getResult(boolean blackFlag, boolean suitFlag, int result) {
            //interpret the result into card info
            switch (result) {
                case 0 -> buff = "4";
                case 2 -> buff = "A";
                case 4 -> buff = "Q";
                case 6 -> buff = "K";
                case 7 -> buff = "10";
                case 8 -> buff = "3";
                case 9 -> buff = "J";
                case 10 -> buff = "2";
                case 11 -> buff = "7";
                case 12 -> buff = "9";
                case 13 -> buff = "6";
                case 14 -> buff = "5";
                case 15 -> buff = "8";
            }
            if (blackFlag) {
                if (suitFlag) {
                    buff += "s";
                } else {
                    buff += "c";
                }
            } else {
                if (suitFlag) {
                    buff += "d";
                } else {
                    buff += "h";
                }
            }
            //put info in the result map
            map.put(Integer.parseInt(this.getName()), this.buff);
        }

        public static boolean isBlack(Color color) {
            return
                    (color.getRed() < 130 && color.getGreen() < 130 && color.getBlue() < 130)
                            || (color.getRed() == 168 && color.getGreen() == 168 && color.getBlue() == 169);
        }

        public static boolean isRed(Color color) {
            return (color.getRed() < 255 && color.getGreen() < 140 && color.getBlue() < 140) &&
                    (color.getRed() > 190 && color.getGreen() > 50 && color.getBlue() > 50) ||
                    (color.getRed() < 105 && color.getGreen() < 40 && color.getBlue() < 40) &&
                            (color.getRed() > 89 && color.getGreen() > 28 && color.getBlue() > 28);
        }
    }
}


