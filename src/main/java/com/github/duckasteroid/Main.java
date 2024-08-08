package com.github.duckasteroid;

import com.github.duckasteroid.impl.ServiceLocator;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A main method application that exercises our example classes
 */
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
        List<String> listArgs = Arrays.asList(args);
        String emitterName = arg(listArgs, "--emitter").orElse("HelloWorldEmitter");
        String receiverName = arg(listArgs, "--receiver").orElse("ConsoleReceiver");
        Emitter emitter = ServiceLocator.byName(Emitter.class, emitterName).getService();
        Receiver receiver = ServiceLocator.byName(Receiver.class, receiverName).getService();
        Main main = new Main(emitter, receiver);
        main.run();
    }

    public static Optional<String> arg(List<String> args, String arg) {
        int index = args.indexOf(arg) + 1;
        if (args.size() > index) {
            return Optional.of(args.get(index));
        }
        return Optional.empty();
    }

}
