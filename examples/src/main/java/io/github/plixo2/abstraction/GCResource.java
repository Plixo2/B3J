package io.github.plixo2.abstraction;


/// Called by the cleaner when an resource is no longer referenced
public interface GCResource {
    void freeResource();
}
