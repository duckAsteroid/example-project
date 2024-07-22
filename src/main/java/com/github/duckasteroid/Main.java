package com.github.duckasteroid;

public class Main implements Runnable {
    private final Emitter emitter;
    private final Receiver receiver;

    public Main(Emitter emitter, Receiver receiver) {
        this.emitter = emitter;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        final String msg = emitter.emit();
        receiver.receive(msg);
    }

    public static void main(String[] args) {
        Main main = new Main(new HelloWorldEmitter(), new ConsoleReceiver());
        main.run();
    }
}
