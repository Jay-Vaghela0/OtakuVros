package com.jayvaghela.otakucommunitytub.Model;

import java.util.List;

public class Story {

    private String name;
    private List<String> image;

    public Story() {
    }

    public Story(String name, List<String> image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getImage() {
        return image;
    }

    public void setImage(List<String> image) {
        this.image = image;
    }
}