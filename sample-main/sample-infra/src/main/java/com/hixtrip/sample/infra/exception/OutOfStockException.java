package com.hixtrip.sample.infra.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutOfStockException extends Exception{


    public OutOfStockException(String s) {
        log.info(s);
    }
}
