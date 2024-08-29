// DONT_TARGET_EXACT_BACKEND: JS
// ES_MODULES

// MODULE: if
// FILE: lib.kt
@file:JsExport

@JsName("foo")
public fun foo(k: String): String = "O$k"

// FILE: entry.mjs
// ENTRY_ES_MODULE
import { foo } from "./reservedModuleNameInExportedFile-if_v5.mjs";

export function box() {
    return foo("K")
}
