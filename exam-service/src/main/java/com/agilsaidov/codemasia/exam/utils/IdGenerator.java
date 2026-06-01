package com.agilsaidov.codemasia.exam.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class IdGenerator {
    private static final String PREFIX = "EXM-";
    private static final int RANDOM_LENGTH = 10;

    private final SecureRandom secureRandom =  new SecureRandom();
    private final char[] charSet = new char[]{
            'a','b','c','d','e','f','g','h','l','m','n','o',
            'p','q','r','s','t','u','v','w','x','y','z',
            '1','2','3','4','5','6','7','8','9'
    };

    public String generateExamId() {
        StringBuilder sb = new StringBuilder(PREFIX.length() + RANDOM_LENGTH);
        sb.append(PREFIX);
        
        for( int i = 0; i < RANDOM_LENGTH; i++ ) {
            sb.append(charSet[secureRandom.nextInt(charSet.length)]);
        }
        return sb.toString();
    }
}
