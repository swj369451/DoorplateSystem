package com.example.doorplatesystem.hardware;

import java.util.concurrent.atomic.AtomicBoolean;

public interface InputDevices {
    AtomicBoolean active = new AtomicBoolean(false);

    void active();
    void inactive();
}
