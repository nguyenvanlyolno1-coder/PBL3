package com.ly.core;

public enum ClientState {
    BOOTING,

    NETWORK_SETUP,
    NETWORK_READY,

    LOGIN_SCREEN,
    AUTHENTICATED,

    SELECTING_EXAM,
    WAITING_EXAM,
    IN_EXAM,

    EXAM_FINISHED,
    DISCONNECTED,
    ERROR
}