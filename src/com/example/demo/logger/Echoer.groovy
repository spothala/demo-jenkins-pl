package com.example.demo.logger

class Echoer implements Serializable {
    private static stream

    // prints to Jenkins console
    static echo(message) {
        this.stream.echo message
    }

    // Sets output stream
    static setStream(out) {
        this.stream = out
    }
}
