package com.scarlatti;

/**
 * ______    __                         __           ____             __     __  __  _
 * ___/ _ | / /__ ___ ___ ___ ____  ___/ /______    / __/______ _____/ /__ _/ /_/ /_(_)
 * __/ __ |/ / -_|_-<(_-</ _ `/ _ \/ _  / __/ _ \  _\ \/ __/ _ `/ __/ / _ `/ __/ __/ /
 * /_/ |_/_/\__/___/___/\_,_/_//_/\_,_/_/  \___/ /___/\__/\_,_/_/ /_/\_,_/\__/\__/_/
 * Wednesday, 7/4/2018
 */
public class SlowService {

    private int calls = 0;

    public void doSomethingSlowly(String val) {
        try {
            Thread.sleep(1000);
            System.out.println("finished processing " + val);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while doing something slow", e);
        }
    }

    public String getSomethingSlowly(String val) {
        try {
            System.out.println("getSomethingSlowly() val = [" + val + "]");
            Thread.sleep(1000);
            return "very slowly obtained value for " + val;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while doing something slow", e);
        }
    }

    public String getSomethingSlowlyWithError(String val) {

        System.out.println("getSomethingSlowlyWithError() val = [" + val + "]");

        calls++;

        if (calls == 3) {
            throw new IllegalStateException("Calls: " + calls + " val: " + val);
        }

        try {
            Thread.sleep(1000);
            return "very slowly obtained value for " + val;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while doing something slow", e);
        }
    }
}
