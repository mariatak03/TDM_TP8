package com.example.mobile8;

import java.io.Serializable;

public class object implements Serializable {
    public String name;
    public int img;
    public object(String name, int img) {
        this.name=name;
        this.img=img;

    }
}