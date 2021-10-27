/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations.persistent

import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.MetadataSource
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.Carrier
import org.jetbrains.kotlin.ir.declarations.persistent.carriers.ConstructorCarrier
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrUninitializedType
import org.jetbrains.kotlin.ir.types.impl.ReturnTypeIsNotInitializedException
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

// Auto-generated by compiler/ir/ir.tree.persistent/generator/src/org/jetbrains/kotlin/ir/persistentIrGenerator/Main.kt. DO NOT EDIT!

internal class PersistentIrConstructor(
    override val startOffset: Int,
    override val endOffset: Int,
    origin: IrDeclarationOrigin,
    override val symbol: IrConstructorSymbol,
    override val name: Name,
    visibility: DescriptorVisibility,
    returnType: IrType,
    override val isInline: Boolean,
    override val isExternal: Boolean,
    override val isPrimary: Boolean,
    override val isExpect: Boolean,
    override val containerSource: DeserializedContainerSource?,
    override val factory: PersistentIrFactory
) : IrConstructor(),
    PersistentIrDeclarationBase<ConstructorCarrier>,
    ConstructorCarrier {

    init {
        symbol.bind(this)
    }

    override var signature: IdSignature? = factory.currentSignature(this)

    override var lastModified: Int = factory.stageController.currentStage
    override var loweredUpTo: Int = factory.stageController.currentStage
    override var values: Array<Carrier>? = null
    override val createdOn: Int = factory.stageController.currentStage

    override var parentField: IrDeclarationParent? = null
    override var originField: IrDeclarationOrigin = origin
    override var removedOn: Int = Int.MAX_VALUE
    override var annotationsField: List<IrConstructorCall> = emptyList()
    private val hashCodeValue: Int = PersistentIrDeclarationBase.hashCodeCounter++
    override fun hashCode(): Int = hashCodeValue
    override fun equals(other: Any?): Boolean = (this === other)

    override var returnTypeFieldField: IrType = returnType

    private var returnTypeField: IrType
        get() = getCarrier().returnTypeFieldField
        set(v) {
            if (returnTypeField !== v) {
                setCarrier()
                returnTypeFieldField = v
            }
        }

    override var returnType: IrType
        get() = returnTypeField.let {
            if (it !== IrUninitializedType) it else throw ReturnTypeIsNotInitializedException(this)
        }
        set(c) {
            returnTypeField = c
        }

    override var typeParametersField: List<IrTypeParameter> = emptyList()

    override var typeParametersSymbolField: List<IrTypeParameterSymbol>
        get() = typeParametersField.map { it.symbol }
        set(v) {
            typeParametersField = v.map { it.owner }
        }

    override var typeParameters: List<IrTypeParameter>
        get() = getCarrier().typeParametersField
        set(v) {
            if (typeParameters !== v) {
                setCarrier()
                typeParametersField = v
            }
        }

    override var dispatchReceiverParameterField: IrValueParameter? = null

    override var dispatchReceiverParameterSymbolField: IrValueParameterSymbol?
        get() = dispatchReceiverParameterField?.symbol
        set(v) {
            dispatchReceiverParameterField = v?.owner
        }

    override var dispatchReceiverParameter: IrValueParameter?
        get() = getCarrier().dispatchReceiverParameterField
        set(v) {
            if (dispatchReceiverParameter !== v) {
                setCarrier()
                dispatchReceiverParameterField = v
            }
        }

    override var extensionReceiverParameterField: IrValueParameter? = null

    override var extensionReceiverParameterSymbolField: IrValueParameterSymbol?
        get() = extensionReceiverParameterField?.symbol
        set(v) {
            extensionReceiverParameterField = v?.owner
        }

    override var extensionReceiverParameter: IrValueParameter?
        get() = getCarrier().extensionReceiverParameterField
        set(v) {
            if (extensionReceiverParameter !== v) {
                setCarrier()
                extensionReceiverParameterField = v
            }
        }

    override var valueParametersField: List<IrValueParameter> = emptyList()

    override var valueParametersSymbolField: List<IrValueParameterSymbol>
        get() = valueParametersField.map { it.symbol }
        set(v) {
            valueParametersField = v.map { it.owner }
        }

    override var valueParameters: List<IrValueParameter>
        get() = getCarrier().valueParametersField
        set(v) {
            if (valueParameters !== v) {
                setCarrier()
                valueParametersField = v
            }
        }

    override var bodyField: IrBody? = null

    override var body: IrBody?
        get() = getCarrier().bodyField
        set(v) {
            if (body !== v) {
                if (v is PersistentIrBodyBase<*>) {
                    v.container = this
                }
                setCarrier()
                bodyField = v
            }
        }

    override var contextReceiverParametersCount: Int = 0

    override var metadata: MetadataSource? = null

    override var visibilityField: DescriptorVisibility = visibility

    override var visibility: DescriptorVisibility
        get() = getCarrier().visibilityField
        set(v) {
            if (visibility !== v) {
                setCarrier()
                visibilityField = v
            }
        }

    @ObsoleteDescriptorBasedAPI
    override val descriptor: ClassConstructorDescriptor
        get() = symbol.descriptor
}
