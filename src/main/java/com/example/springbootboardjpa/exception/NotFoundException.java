package com.example.springbootboardjpa.exception;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@NoArgsConstructor
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
