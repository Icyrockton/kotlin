// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// KJS_WITH_FULL_RUNTIME
package foo

class P {
    val simpleProp: Int = 100
    val anotherProp: Int = 100
    val propWithGetter: Int
        get() = 1
    fun func() = "2"
}

fun box(): String {
    val expectedKeys = setOf("simpleProp", "anotherProp")
    assertEquals(expectedKeys, P().keys())

    assertEquals(expectedKeys, object {
        val simpleProp: Int = 100
        val anotherProp: Int = 100
        val propWithGetter: Int
            get() = 1
        fun func() = "2"
    }.keys())

    return "OK"
}

fun Any.keys(): Set<String> = (js("Object").keys(this) as Array<String>).toSet()
