package io.github.plixo2;

import io.github.plixo2.samples.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CLIEntry {

    Map<String, Runnable> examples = samples(
            sample("Hello Floor",    new HelloFloor()::main),
            sample("Joints",        new Joints()::main),
            sample("Controller",    new Controller()::main),
            sample("Determinism",   new Determinism()::main),
            sample("Heightfield",   new HeightField()::main),
            sample("Triangle mesh",  new TriangleMesh()::main)
    );


    void main(String[] args) throws IllegalArgumentException {
        if (args.length != 0) {
            fromArg(args[0]).forEach(Runnable::run);
        } else {
            this.examples.get(fromIO()).run();
        }
    }

    String fromIO() {

        var names = this.examples.keySet().stream().toList();
        for (var i = 0; i < names.size(); i++) {
            IO.println("[" + (i + 1) + "] " + names.get(i));
        }

        while (true) {
            int i = -1;
            try {
                i = Integer.parseInt(IO.readln("Enter example (number): "));
            } catch (NumberFormatException _) {
                // ignore
            }

            if (i < 1 || i > names.size()) {
                IO.println("Invalid number");
                continue;
            }
            return names.get(i - 1);
        }

    }

    Collection<Runnable> fromArg(String arg) throws IllegalArgumentException {
        if (arg.equalsIgnoreCase("--all") || arg.equalsIgnoreCase("-a")) {
            return this.examples.values();
        }

        for (var k_v : this.examples.entrySet()) {
            var key = k_v.getKey();
            if (key.equalsIgnoreCase(arg)) {
                return List.of(k_v.getValue());
            }
        }

        throw new IllegalArgumentException("Unknown example: " + arg);

    }


    record Sample(String name, Runnable runnable){}

    Sample sample(String name, Runnable runnable) {
        return new Sample(name, runnable);
    }

    Map<String, Runnable> samples(Sample... samples) {
        var ordered = new LinkedHashMap<String, Runnable>();
        for (var sample : samples) {
            ordered.put(sample.name, sample.runnable);
        }
        return ordered;
    }

}