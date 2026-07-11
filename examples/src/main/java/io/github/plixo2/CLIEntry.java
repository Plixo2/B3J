package io.github.plixo2;

import io.github.plixo2.samples.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import io.github.plixo2.samples.*;

public class CLIEntry {

    Map<String, Runnable> examples = samples(
            sample("HelloFloor",    new HelloFloor()::main),
            sample("Joints",        new Joints()::main),
            sample("Determinism",   new Determinism()::main),
            sample("HeightField",   new HeightField()::main),
            sample("TriangleMesh",  new TriangleMesh()::main),
            sample("Queries",       new Queries()::main),
            sample("Controller",    new Controller()::main)
    );


    void main(String[] args) {
        this.examples.get(name(args)).run();
    }

    String name(String[] args) {
        if (args.length == 0) {
            return io();
        }
        return fromArg(args[0]);
    }

    String io() {

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

    String fromArg(String arg) {

        for (var s : this.examples.keySet()) {
            if (s.equalsIgnoreCase(arg)) {
                return s;
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