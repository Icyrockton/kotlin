// MODULE: common
// FILE: common.kt
expect class <!NO_ACTUAL_FOR_EXPECT, NO_ACTUAL_FOR_EXPECT{JVM}, PACKAGE_OR_CLASSIFIER_REDECLARATION{JVM}!>Foo<!>

// MODULE: main()()(common)
// FILE: test.kt
expect class <!NO_ACTUAL_FOR_EXPECT, PACKAGE_OR_CLASSIFIER_REDECLARATION!>Foo<!>
