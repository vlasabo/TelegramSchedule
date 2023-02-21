package com.doctrine7.tgbot.model.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomBannedUserException extends RuntimeException {
    private final String message;
}
