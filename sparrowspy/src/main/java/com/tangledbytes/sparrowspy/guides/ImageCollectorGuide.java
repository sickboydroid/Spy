package com.tangledbytes.sparrowspy.guides;

import androidx.annotation.IntRange;

public class ImageCollectorGuide extends Guide {
    @IntRange(from = 1, to = 15) private static int imageQuality = 9;

    public void setImageQuality(int quality) {
        imageQuality = quality;
    }

    public int getImageMinHeight() {
        return imageQuality * 100;
    }

    public int getImageMinWidth() {
        return imageQuality * 100;
    }

}
