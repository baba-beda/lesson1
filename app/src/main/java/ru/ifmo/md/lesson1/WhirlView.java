package ru.ifmo.md.lesson1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by thevery on 11/09/14.
 * Modified by baba-beda on 15/09/14
 */
class WhirlView extends SurfaceView implements Runnable {
    int width = 240;
    int height = 320;
    int scale = 8;
    final int MAX_COLOR = 10;
    int[] palette = {0xFF191970, 0xFF000080, 0xFF6495ED, 0xFF0000CD, 0xFF4169E1, 0xFF0000FF, 0xFF1E90FF, 0xFF00BFFF, 0xFF87CEFA, 0xFF4682B4};
    SurfaceHolder holder;
    Thread thread = null;
    volatile boolean running = false;
    Bitmap screen = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

    int[] newfield = null;
    int[] newfield2 = null;

    Rect my_area;

    public WhirlView(Context context) {
        super(context);
        holder = getHolder();
    }

    public void resume() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ignore) {}
    }

    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                long startTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                updateField();
                draw(canvas);
                holder.unlockCanvasAndPost(canvas);
                long finishTime = System.nanoTime();
                Log.i("FPS", "Circle: " + (int) (1000/ ((finishTime - startTime) / 1000000)));
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        width = w/scale;
        height = h/scale;
        my_area = new Rect(0, 0, w, h);
        initField();
    }

    void initField() {
        screen = Bitmap.createScaledBitmap(screen, width, height, false);
        //field = new int[width][height];

        newfield = new int[width*height];
        Random rand = new Random();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                newfield[x + y * width] = rand.nextInt(MAX_COLOR);
            }
        }
    }

    void updateField() {
        newfield2 = new int[width * height];
        //field2 = new int[width][height];
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {

                for (int dx=-1; dx<=1; dx++) {
                    for (int dy=-1; dy<=1; dy++) {
                        int x2 = (x + dx + width) % width;
                        int y2 = (y + dy + height) % height;

                        if ((newfield[x + y * width]+1) % MAX_COLOR == newfield[x2 + y2 * width]) {
                            newfield2[x + y * width] = newfield[x2 + y2 * width];
                        }
                    }
                }
                screen.setPixel(x, y, palette[newfield[x + y * width]]);
            }
        }
        newfield = newfield2;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(Bitmap.createScaledBitmap(screen, canvas.getWidth(), canvas.getHeight(), false), null, my_area, null);
    }
}