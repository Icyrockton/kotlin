// MODULE: libA
// FILE: libA.kt
inline fun foo() = (object : II {}).ok()

interface I {
    fun ok() = "OK"
}

interface II: I

// MODULE: libB(libA)
// FILE: libB.kt

inline fun bar() = foo()

// MODULE: main(libB)
// FILE: main.kt

// CHECK_BREAKS_COUNT: function=box count=0
// CHECK_LABELS_COUNT: function=box name=$l$block count=0
fun box() = bar()


