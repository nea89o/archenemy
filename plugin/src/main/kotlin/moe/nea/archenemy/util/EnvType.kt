package moe.nea.archenemy.util

enum class EnvType(val classifier: String?, val mcp: Int) {
    CLIENT("client", 0),
    SERVER("server", 1),
    COMBINED(null, 2),
    DATAGEN("server", 1)
    ;
}