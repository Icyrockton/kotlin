/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.KtRealSourceElementKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.chooseFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.StandardClassIds

object FirReifiedChecker : FirQualifiedAccessExpressionChecker(MppCheckerKind.Common) {
    override fun check(expression: FirQualifiedAccessExpression, context: CheckerContext, reporter: DiagnosticReporter) {
        val calleeReference = expression.calleeReference
        val typeArguments = expression.typeArguments
        val typeParameters = calleeReference.toResolvedCallableSymbol()?.typeParameterSymbols ?: return

        val count = minOf(typeArguments.size, typeParameters.size)
        for (index in 0 until count) {
            val typeArgumentProjection = typeArguments.elementAt(index)
            val source = typeArgumentProjection.source ?: calleeReference.source ?: continue

            val typeArgument = typeArgumentProjection.toConeTypeProjection().type?.fullyExpandedType(context.session) ?: continue
            val typeParameter = typeParameters[index]

            if (typeParameter.isReifiedTypeParameterOrFromKotlinArray()) {
                checkArgumentAndReport(
                    typeArgument,
                    typeParameter,
                    source,
                    isExplicit = typeArgumentProjection.source?.kind == KtRealSourceElementKind,
                    isArray = false,
                    context,
                    reporter
                )
            }
        }
    }

    private fun FirTypeParameterSymbol.isReifiedTypeParameterOrFromKotlinArray(): Boolean {
        val containingDeclaration = containingDeclarationSymbol
        return isReified ||
                containingDeclaration is FirRegularClassSymbol && containingDeclaration.classId == StandardClassIds.Array
    }

    private fun ConeKotlinType.cannotBeReified() = when (this) {
        is ConeCapturedType -> true
        is ConeDynamicType -> true
        else -> isNothing
    }

    private fun checkArgumentAndReport(
        typeArgument: ConeKotlinType,
        typeParameter: FirTypeParameterSymbol,
        source: KtSourceElement,
        isExplicit: Boolean,
        isArray: Boolean,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val fullyExpandedType = typeArgument.fullyExpandedType(context.session)
        if (fullyExpandedType.classId == StandardClassIds.Array) {
            // Type aliases can transform type arguments arbitrarily (drop, nest, etc...).
            // Therefore, we check the arguments of the expanded type, not the ones that went into the type alias.
            fullyExpandedType.typeArguments.forEach {
                if (it is ConeKotlinType) checkArgumentAndReport(it, typeParameter, source, isExplicit, isArray = true, context, reporter)
            }
            return
        }

        if (typeArgument is ConeTypeParameterType) {
            val symbol = typeArgument.lookupTag.typeParameterSymbol
            if (!symbol.isReified) {
                reporter.reportOn(
                    source,
                    if (isArray) FirErrors.TYPE_PARAMETER_AS_REIFIED_ARRAY.chooseFactory(context) else FirErrors.TYPE_PARAMETER_AS_REIFIED,
                    symbol,
                    context
                )
            }
        } else if (typeArgument is ConeDefinitelyNotNullType && isExplicit) {
            // We sometimes infer type arguments to DNN types, which seems to be ok. Only report explicit DNN types written by user.
            reporter.reportOn(source, FirErrors.DEFINITELY_NON_NULLABLE_AS_REIFIED, context)
        } else if (typeArgument.cannotBeReified()) {
            reporter.reportOn(source, FirErrors.REIFIED_TYPE_FORBIDDEN_SUBSTITUTION, typeArgument, context)
        } else if (typeArgument is ConeIntersectionType) {
            reporter.reportOn(source, FirErrors.TYPE_INTERSECTION_AS_REIFIED, typeParameter, typeArgument.intersectedTypes, context)
        }
    }

}
