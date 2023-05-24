package com.tangledbytes.sw.guides;

public class ImageCollectorGuide extends Guide {
    private static int imageQuality = 9;

    public void setImageQuality(int quality) {
        imageQuality = quality;
    }
    public int getImageMinHeight() {
        return imageQuality*100;
    }

    public int getImageMinWidth() {
        return imageQuality*100;
    }

}
