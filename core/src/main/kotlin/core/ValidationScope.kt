package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment

// TODO:
//  * readme
//  * conditional rules
//  * fail-fast mode
//  * support for suspension for checks ???

/**
 * [Named][ValidationPath] scope that groups [violations][ValidationError] related
 * in some manner (e.g. by the value for which they appeared).
 *
 * Scopes can be [enclosed][enclose] creating naming hierarchies (nested scope's name
 * is created from enclosing scope's name), which may be used to keep information
 * about a structure of validated entities - especially useful to present a location
 * for which violations occurred (e.g. field of a validated "multi-level" DTO).
 *
 * Multithreaded usage of a single scope is discouraged, as instances of this class
 * are stateful - eventual violations are stored and can be retrieved as a combined
 * [result].
 */
class ValidationScope private constructor(
    headSegment: Segment,
    enclosingScope: ValidationScope? = null,
) {
    /**
     * Public constructor for unnamed root scopes.
     */
    constructor() : this(Segment.Empty, null)

    /**
     * Public constructor for named root scopes.
     */
    constructor(name: NonBlankString) : this(Segment.Name(name), null)

    /**
     * Returns combined [result][ValidationResult] of this scope.
     *
     * Scopes enclosed by this instance are traversed and their results are merged
     * as well.
     */
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

    private val validationPath: ValidationPath =
        when (enclosingScope) {
            null -> {
                when (headSegment) {
                    Segment.Empty -> ValidationPath.unnamed
                    is Segment.Name -> ValidationPath.named(headSegment)
                    is Segment.Index ->
                        // in normal circumstances this cannot happen, as a correct
                        // root segment usage is enforced by public constructors
                        throw IllegalArgumentException(
                            "Index segment cannot be used as a name of root scope"
                        )
                }
            }
            else -> enclosingScope.validationPath + headSegment
        }

    /**
     * Creates new scope enclosed by this one with [segment] added to the enclosing
     * scope [path][ValidationScope.validationPath].
     *
     * @throws [IllegalArgumentException] when there's already enclosed scope with
     * the same [name][ValidationPath.lastToken].
     */
    fun enclose(segment: Segment): ValidationScope =
        enclosedScopes.find { it.validationPath.head == segment }?.let {
            throw IllegalArgumentException(
                "Scope named ${it.validationPath.lastToken()} is already enclosed"
            )
        } ?: ValidationScope(segment, this).also { enclosedScopes += it }

    /**
     * Verifies given value against passed [condition][rule].
     *
     * Eventual [error][ValidationError] is saved into internal structures of
     * a scope and returned.
     */
    fun <C : Check<V, P, C>, V, P : Check.Params<C>> checkValueAgainstRule(
        value: V,
        rule: ValidationRule<C, V, P>,
    ): ValidationError<C, V, P>? {
        val check = rule.check
        return when (check(value)) {
            false -> {
                val errorDetails = rule.errorDetails(
                    ErrorDetailsBuilderContext(value, validationPath, check)
                )
                val errorContext = ValidationError.Context(check, validationPath)
                ValidationError(value, errorContext, errorDetails)
                    .also { errors += it }
            }
            true -> null
        }
    }
}
