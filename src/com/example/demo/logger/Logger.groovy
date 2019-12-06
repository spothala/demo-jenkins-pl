package com.example.demo.logger

@Singleton
class Logger implements Serializable {
    static def out = System.out
    
    static void info(String message) {
        out.println '[INFO] ' + message
    }

    static void debug(String message) {
        out.println '[DEBUG] ' + message
    }

    static void error(String message) {
        out.println '[ERROR] ' + message
    }
}
