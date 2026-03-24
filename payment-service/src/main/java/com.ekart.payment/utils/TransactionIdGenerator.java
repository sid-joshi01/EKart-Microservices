package com.ekart.payment.utils;

import java.util.UUID;

public class TransactionIdGenerator {

    public static String generate(){
        return "txn-"+ UUID.randomUUID().toString();
    }}
