package com.eulerity.hackathon.imagefinder.model;

import java.util.Arrays;

public class ImageData {

    private String imageUrl;
    private String pageLink;
    private String [] pathToLink;
    private int frequency;
    
    public ImageData(String imageUrl, String pageLink, String [] pathToLink) {
        super();
        this.imageUrl = imageUrl;
        this.pageLink = pageLink;
        this.pathToLink = pathToLink;
        frequency = 1;
    }
    
    public ImageData(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getPageLink() {
        return pageLink;
    }
    public void setPageLink(String pageLink) {
        this.pageLink = pageLink;
    }
    public String[] getPathToLink() {
        return pathToLink;
    }
    public void setPathToLink(String[] pathToLink) {
        this.pathToLink = pathToLink;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "ImageData [imageUrl=" + imageUrl + ", pageLink=" + pageLink + ", pathToLink="
                + Arrays.toString(pathToLink) + "]";
    }

    
}
