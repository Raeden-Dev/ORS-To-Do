package com.raeden.ors_to_do.modules.dependencies.services;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalActivityTracker implements NativeKeyListener, NativeMouseMotionListener {

    // Thread-safe timestamp
    private static final AtomicLong lastActivityTime = new AtomicLong(System.currentTimeMillis());
    private static boolean isRegistered = false;

    public static void init() {
        if (isRegistered) return;
        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }
            GlobalActivityTracker tracker = new GlobalActivityTracker();
            GlobalScreen.addNativeKeyListener(tracker);
            GlobalScreen.addNativeMouseMotionListener(tracker);
            isRegistered = true;
        } catch (NativeHookException ex) {
            System.err.println("Failed to register Global Activity Tracker");
            ex.printStackTrace();
        }
    }

    public static long getLastActivityTime() {
        return lastActivityTime.get();
    }

    // Call this when the user clicks "Start" on the timer so it doesn't instantly auto-pause
    public static void resetActivityTime() {
        lastActivityTime.set(System.currentTimeMillis());
    }

    // --- Listener Events ---
    @Override public void nativeKeyPressed(NativeKeyEvent e) { updateActivity(); }
    @Override public void nativeKeyReleased(NativeKeyEvent e) { updateActivity(); }
    @Override public void nativeKeyTyped(NativeKeyEvent e) { updateActivity(); }
    @Override public void nativeMouseMoved(NativeMouseEvent e) { updateActivity(); }
    @Override public void nativeMouseDragged(NativeMouseEvent e) { updateActivity(); }

    private void updateActivity() {
        lastActivityTime.set(System.currentTimeMillis());
    }
}