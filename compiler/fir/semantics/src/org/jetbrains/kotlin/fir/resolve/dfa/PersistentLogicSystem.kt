/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import kotlinx.collections.immutable.*
import org.jetbrains.kotlin.fir.types.ConeInferenceContext
import org.jetbrains.kotlin.types.AbstractTypeChecker
import java.util.*
import kotlin.math.max

abstract class PersistentLogicSystem(context: ConeInferenceContext) : LogicSystem(context) {
    abstract val variableStorage: VariableStorageImpl

    override fun joinFlow(flows: Collection<PersistentFlow>, union: Boolean): MutableFlow {
        when (flows.size) {
            0 -> return MutableFlow()
            1 -> return flows.first().fork()
        }

        val commonFlow = flows.reduce { a, b ->
            // If you're debugging this assertion error, most likely cause is that a node is not
            // marked as dead when all its input edges are dead. In that case it will have an empty flow,
            // and joining that with a non-empty flow from another branch will fail.
            a.lowestCommonAncestor(b) ?: error("no common ancestor in $a, $b")
        }
        val result = commonFlow.fork()

        // If a variable was reassigned in one branch, it was reassigned at the join point.
        val reassignedVariables = mutableMapOf<RealVariable, Int>()
        for (flow in flows) {
            for ((variable, index) in flow.assignmentIndex) {
                if (result.assignmentIndex[variable] != index) {
                    // Ideally we should generate an entirely new index here, but it doesn't really
                    // matter; the important part is that it's different from `commonFlow.previousFlow`.
                    reassignedVariables[variable] = max(index, reassignedVariables[variable] ?: 0)
                }
            }
        }
        for ((variable, index) in reassignedVariables) {
            recordNewAssignment(result, variable, index)
        }

        // TODO: if `union`, then all aliases from all flows are valid so long as other flows don't have a contradicting one
        for ((from, to) in flows.first().directAliasMap) {
            // If `from -> to` is still in `result` (was not removed by the above code), then it is also in all `flows`,
            // as the only way to break aliasing is by reassignment.
            if (result.directAliasMap[from] != to && flows.all { it.unwrapVariable(from) == to }) {
                // if (p) { y = x } else { y = x } <-- after `if`, `y -> x` is in all `flows`, but not in `result`
                // (which was forked from the flow before the `if`)
                addLocalVariableAlias(result, from, to)
            }
        }

        flows.flatMapTo(mutableSetOf()) { it.knownVariables }.forEach computeStatement@{ variable ->
            val statement = if (variable in result.directAliasMap) {
                return@computeStatement // statements about alias == statements about aliased variable
            } else if (!union) {
                // if (condition) { /* x: S1 */ } else { /* x: S2 */ } // -> x: S1 | S2
                or(flows.mapTo(mutableSetOf()) { it.getTypeStatement(variable) ?: return@computeStatement })
            } else if (variable !in reassignedVariables) {
                // callAllInSomeOrder({ /* x: S1 */ }, { /* x: S2 */ }) // -> x: S1 & S2
                and(flows.mapNotNullTo(mutableSetOf()) { it.getTypeStatement(variable) })
            } else {
                // callAllInSomeOrder({ x = ...; /* x: S1 */  }, { x = ...; /* x: S2 */ }, { /* x: S3 */ }) // -> x: S1 | S2
                val byAssignment =
                    flows.groupByTo(mutableMapOf(), { it.assignmentIndex[variable] ?: -1 }, { it.getTypeStatement(variable) })
                // This throws out S3 from the above example, as the flow that makes that statement is unordered w.r.t. the other two:
                byAssignment.remove(commonFlow.assignmentIndex[variable] ?: -1)
                // In the above example each list in `byAssignment.values` only has one entry, but in general we can have something like
                //   A -> B (assigns) -> C (does not assign)
                //    \             \--> D (does not assign)
                //     \-> E (assigns)
                // in which case for {C, D, E} the result is (C && D) || E. Not sure that kind of control flow can exist
                // without intermediate nodes being created for (C && D) though.
                or(byAssignment.values.mapTo(mutableSetOf()) { and(it.filterNotNull()) ?: return@computeStatement })
            }
            if (statement?.isNotEmpty == true) {
                result.approvedTypeStatements[variable] = statement.toPersistent()
            }
        }
        // TODO: compute common implications?
        return result
    }

    override fun addLocalVariableAlias(flow: MutableFlow, alias: RealVariable, underlyingVariable: RealVariable) {
        flow.addAliases(persistentSetOf(alias), flow.unwrapVariable(underlyingVariable))
    }

    private fun MutableFlow.replaceVariable(variable: RealVariable, replacement: RealVariable?) {
        val original = directAliasMap[variable]
        if (original != null) {
            // All statements should've been made about whatever variable this is an alias to. There is nothing to replace.
            if (AbstractTypeChecker.RUN_SLOW_ASSERTIONS) {
                assert(variable !in backwardsAliasMap)
                assert(variable !in logicStatements)
                assert(variable !in approvedTypeStatements)
            }
            // `variable.dependentVariables` is not separated by flow, so it may be non-empty if aliasing of this variable
            // was broken in another flow. However, in *this* flow dependent variables should have no information attached to them.

            val siblings = backwardsAliasMap.getValue(original).remove(variable)
            directAliasMap.remove(variable)
            if (siblings.isNotEmpty()) {
                backwardsAliasMap[original] = siblings
            } else {
                backwardsAliasMap.remove(original)
            }
            if (replacement != null) {
                addLocalVariableAlias(this, replacement, original)
            }
        } else {
            val aliases = backwardsAliasMap[variable]
            // If asked to remove the variable but there are aliases, replace with a new representative for the alias group instead.
            val replacementOrNext = replacement ?: aliases?.first()
            for (dependent in variable.dependentVariables) {
                replaceVariable(dependent, replacementOrNext?.let {
                    variableStorage.copyRealVariableWithRemapping(dependent, variable, it)
                })
            }
            logicStatements.replaceVariable(variable, replacementOrNext)
            approvedTypeStatements.replaceVariable(variable, replacementOrNext)
            if (aliases != null) {
                backwardsAliasMap -= variable
                if (replacementOrNext != null) {
                    directAliasMap -= replacementOrNext
                    addAliases(aliases, replacementOrNext)
                }
            }
        }
    }

    private fun MutableFlow.addAliases(aliases: PersistentSet<RealVariable>, target: RealVariable) {
        val withoutSelf = aliases - target
        if (withoutSelf.isNotEmpty()) {
            withoutSelf.associateWithTo(directAliasMap) { target }
            backwardsAliasMap[target] = backwardsAliasMap[target]?.addAll(withoutSelf) ?: withoutSelf
        }
    }

    override fun addTypeStatement(flow: MutableFlow, statement: TypeStatement): TypeStatement? {
        if (statement.exactType.isEmpty()) return null
        val variable = statement.variable
        val oldExactType = flow.approvedTypeStatements[variable]?.exactType
        val newExactType = oldExactType?.addAll(statement.exactType) ?: statement.exactType.toPersistentSet()
        if (newExactType === oldExactType) return null
        return PersistentTypeStatement(variable, newExactType).also { flow.approvedTypeStatements[variable] = it }
    }

    override fun addImplication(flow: MutableFlow, implication: Implication) {
        val effect = implication.effect
        if (effect == implication.condition) return
        if (effect is TypeStatement && (effect.isEmpty ||
                    flow.approvedTypeStatements[effect.variable]?.exactType?.containsAll(effect.exactType) == true)
        ) return
        val variable = implication.condition.variable
        flow.logicStatements[variable] = flow.logicStatements[variable]?.add(implication) ?: persistentListOf(implication)
    }

    override fun translateVariableFromConditionInStatements(
        flow: MutableFlow,
        originalVariable: DataFlowVariable,
        newVariable: DataFlowVariable,
        transform: (Implication) -> Implication?
    ) {
        val statements = if (originalVariable.isSynthetic())
            flow.logicStatements.remove(originalVariable)
        else
            flow.logicStatements[originalVariable]
        if (statements.isNullOrEmpty()) return
        val existing = flow.logicStatements[newVariable] ?: persistentListOf()
        flow.logicStatements[newVariable] = statements.mapNotNullTo(existing.builder()) {
            // TODO: rethink this API - technically it permits constructing invalid flows
            //  (transform can replace the variable in the condition with anything)
            transform(OperationStatement(newVariable, it.condition.operation) implies it.effect)
        }.build()
    }

    private val nullableNothingType = context.session.builtinTypes.nullableNothingType.type
    private val anyType = context.session.builtinTypes.anyType.type

    override fun approveOperationStatement(flow: PersistentFlow, statement: OperationStatement): TypeStatements =
        approveOperationStatement(flow.logicStatements, statement, removeApprovedOrImpossible = false)

    override fun approveOperationStatement(
        flow: MutableFlow,
        statement: OperationStatement,
        removeApprovedOrImpossible: Boolean,
    ): TypeStatements = approveOperationStatement(flow.logicStatements, statement, removeApprovedOrImpossible)

    private fun approveOperationStatement(
        logicStatements: Map<DataFlowVariable, PersistentList<Implication>>,
        approvedStatement: OperationStatement,
        removeApprovedOrImpossible: Boolean
    ): TypeStatements {
        val result = mutableMapOf<RealVariable, MutableTypeStatement>()
        val queue = LinkedList<OperationStatement>().apply { this += approvedStatement }
        val approved = mutableSetOf<OperationStatement>()
        while (queue.isNotEmpty()) {
            val next = queue.removeFirst()
            // Defense from cycles in facts
            if (!removeApprovedOrImpossible && !approved.add(next)) continue

            val operation = next.operation
            val variable = next.variable
            if (variable.isReal()) {
                val impliedType = if (operation == Operation.EqNull) nullableNothingType else anyType
                result.getOrPut(variable) { MutableTypeStatement(variable) }.exactType.add(impliedType)
            }

            val statements = logicStatements[variable] ?: continue
            val stillUnknown = statements.removeAll {
                val knownValue = it.condition.operation.valueIfKnown(operation)
                if (knownValue == true) {
                    when (val effect = it.effect) {
                        is OperationStatement -> queue += effect
                        is TypeStatement ->
                            result.getOrPut(effect.variable) { MutableTypeStatement(effect.variable) } += effect
                    }
                }
                removeApprovedOrImpossible && knownValue != null
            }
            if (stillUnknown != statements && logicStatements is MutableMap) {
                if (stillUnknown.isEmpty()) {
                    logicStatements.remove(variable)
                } else {
                    logicStatements[variable] = stillUnknown
                }
            }
        }
        return result
    }

    override fun recordNewAssignment(flow: MutableFlow, variable: RealVariable, index: Int) {
        flow.replaceVariable(variable, null)
        flow.assignmentIndex[variable] = index
    }

    override fun isSameValueIn(a: PersistentFlow, b: MutableFlow, variable: RealVariable): Boolean =
        a.assignmentIndex[variable] == b.assignmentIndex[variable]

    override fun isSameValueIn(a: PersistentFlow, b: PersistentFlow, variable: RealVariable): Boolean =
        a.assignmentIndex[variable] == b.assignmentIndex[variable]
}

private fun TypeStatement.toPersistent(): PersistentTypeStatement = when (this) {
    is PersistentTypeStatement -> this
    else -> PersistentTypeStatement(variable, exactType.toPersistentSet())
}

@JvmName("replaceVariableInStatements")
private fun MutableMap<RealVariable, PersistentTypeStatement>.replaceVariable(from: RealVariable, to: RealVariable?) {
    val existing = remove(from) ?: return
    if (to != null) {
        put(to, existing.copy(variable = to))
    }
}

@JvmName("replaceVariableInImplications")
private fun MutableMap<DataFlowVariable, PersistentList<Implication>>.replaceVariable(from: RealVariable, to: RealVariable?) {
    val existing = remove(from)
    val toReplace = entries.mapNotNull { (variable, implications) ->
        val newImplications = if (to != null) {
            implications.replaceAll { it.replaceVariable(from, to) }
        } else {
            implications.removeAll { it.effect.variable == from }
        }
        if (newImplications != implications) variable to newImplications else null
    }
    for ((variable, implications) in toReplace) {
        if (implications.isEmpty()) {
            remove(variable)
        } else {
            put(variable, implications)
        }
    }
    if (existing != null && to != null) {
        put(to, existing.replaceAll { it.replaceVariable(from, to) })
    }
}

private inline fun <T> PersistentList<T>.replaceAll(block: (T) -> T): PersistentList<T> =
    mutate { result ->
        val it = result.listIterator()
        while (it.hasNext()) {
            it.set(block(it.next()))
        }
    }

private fun Implication.replaceVariable(from: RealVariable, to: RealVariable): Implication = when {
    condition.variable == from -> Implication(condition.copy(variable = to), effect.replaceVariable(from, to))
    effect.variable == from -> Implication(condition, effect.replaceVariable(from, to))
    else -> this
}

private fun Statement.replaceVariable(from: RealVariable, to: RealVariable): Statement =
    if (variable != from) this else when (this) {
        is OperationStatement -> copy(variable = to)
        is PersistentTypeStatement -> copy(variable = to)
        is MutableTypeStatement -> MutableTypeStatement(to, exactType)
    }
