// IMyAidlInterface.aidl
package com.example.audioserver.KeyCommon;

// Declare any non-default types here with import statements

interface MyInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    /*void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);*/
    void startService();

    void startAudioClip(int clipId);

    void pauseAudioClip();

    void resumeAudioClip();

    void stopAudioClip();

    void stopService();
}
