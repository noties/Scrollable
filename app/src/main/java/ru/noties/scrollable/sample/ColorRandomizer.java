package ru.noties.scrollable.sample;

import android.os.SystemClock;

import java.util.Random;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public class ColorRandomizer {

    private final Random mRandom;
    private final int[] mColors;
    private final int mMax;

    public ColorRandomizer(int[] colors) {
        this.mRandom = new Random(SystemClock.elapsedRealtime());
        this.mColors = colors;
        this.mMax = mColors.length - 1;
    }

    public int next() {
        final int index = mRandom.nextInt(mMax);
        return mColors[index];
    }
}
