package com.example.pytorchandroid;

public class StopWatch {

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long getElapsedTime(long timeA, long timeB) {
        return timeB - timeA;
    }
}
