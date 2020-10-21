/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.persistent.carriers

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

// Auto-generated by compiler/ir/ir.tree/src/org/jetbrains/kotlin/ir/persistentIrGenerator/Main.kt. DO NOT EDIT!

internal interface ErrorDeclarationCarrier : DeclarationCarrier{

    override fun clone(): ErrorDeclarationCarrier {
        return ErrorDeclarationCarrierImpl(
            lastModified,
            parentField,
            originField,
            annotationsField
        )
    }
}

internal class ErrorDeclarationCarrierImpl(
    override val lastModified: Int,
    override var parentField: IrDeclarationParent?,
    override var originField: IrDeclarationOrigin,
    override var annotationsField: List<IrConstructorCall>
) : ErrorDeclarationCarrier
