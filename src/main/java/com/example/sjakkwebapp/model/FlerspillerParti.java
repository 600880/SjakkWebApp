package com.example.sjakkwebapp.model;

import spill.Parti;

public class FlerspillerParti {
    public Parti parti;
    public String hvit;
    public String svart;

    public FlerspillerParti(String hvit, String svart) {
        this.parti = new Parti();
        this.hvit = hvit;
        this.svart = svart;
    }
}
