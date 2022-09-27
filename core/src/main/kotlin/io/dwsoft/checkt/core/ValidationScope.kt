package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.NamingPath.Segment

// TODO:
//  * MOAR TESTS!!!
//  * create DSL methods
//  * collection validation (info about index/place of invalid element)
//  * support for suspension (for translation [and validation/checks ???])
//  * interface Validator for DTOs (idea for later ???)
//  * lazy scope (evaluated when result called) ???

class ValidationScope private constructor(
    namingSegment: Segment,
    enclosingScope: ValidationScope? = null,
) {
    /**
     * Public constructor for unnamed root scopes.
     */
    constructor() : this(Segment.Empty, null)

    /**
     * Public constructor for named root scopes.
     */
    constructor(name: Segment.Name) : this(name, null)

    val validationPath: NamingPath =
        when (enclosingScope) {
            null -> {
                when (namingSegment) {
                    Segment.Empty -> NamingPath.unnamed
                    is Segment.Name -> NamingPath.named(namingSegment)
                    is Segment.Index ->
                        // in normal circumstances this cannot happen, as a correct
                        // root segment usage is enforced by public constructors
                        throw IllegalArgumentException(
                            "Index segment cannot be used as a name of root scope"
                        )
                }
            }
            else -> enclosingScope.validationPath + namingSegment
        }

    val result: ValidationResult
        get() {
            val thisResult = errors.toValidationResult()
            val enclosedResults =
                when {
                    enclosedScopes.isNotEmpty() -> {
                        enclosedScopes
                            .map { it.result }
                            .reduce { v1, v2 -> v1 + v2 }
                    }
                    else -> ValidationResult.Success
                }
            return thisResult + enclosedResults
        }

    private val errors: MutableList<ValidationError<*, *, *>> = mutableListOf()
    private val enclosedScopes: MutableList<ValidationScope> = mutableListOf()

    /**
     * Creates new scope enclosed by this one with [segment] added to the enclosing
     * scope [path][ValidationScope.validationPath].
     *
     * @throws [IllegalArgumentException] when there's already enclosed scope with the same
     * [name][NamingPath.lastToken].
     */
    fun enclose(segment: Segment): ValidationScope =
        enclosedScopes.find { it.validationPath.head == segment }?.let {
            throw IllegalArgumentException(
                "Scope named ${it.validationPath.lastToken()} is already enclosed"
            )
        } ?: ValidationScope(segment, this).also { enclosedScopes += it }

    internal fun <V, K : Check.Key, P : Check.Params> V.checkAgainst(
        rule: ValidationRule<V, K, P>,
    ): ValidationError<V, K, P>? {
        val value = this
        val check = rule.check
        val errorDetailsBuilder = rule.errorDetailsBuilder
        return check(value)
            .takeIf { passes -> !passes }
            ?.let {
                val errorDetailsBuilderContext =
                    ErrorDetailsBuilderContext(value, validationPath, check.context)
                val errorDetails = errorDetailsBuilder(errorDetailsBuilderContext)
                ValidationError(value, check.context, validationPath, errorDetails)
            }
            ?.also { errors.add(it) }
    }
}

fun ValidationScope.throwIfFailure() = result.throwIfFailure()
