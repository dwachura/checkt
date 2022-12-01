package io.dwsoft.checkt.core

import io.dwsoft.checkt.core.ValidationPath.Segment

// TODO:
//  * kdocs over validation scope + readme
//  * conditional rules
//  * fail-fast mode
//  * support for suspension (for translation [and validation/checks ???]) - available after context receivers removal
//  * split ValidationScope to [executed/terminated] scope (immutable, without methods that can modify result)
//    and not consumed one (mutable, current implementation)
//  * restrict creation of Check.Params to Check scope (prevent creation of Param subtypes outside Check class) - possible???

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

    val validationPath: ValidationPath =
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
     * [name][ValidationPath.lastToken].
     */
    fun enclose(segment: Segment): ValidationScope =
        enclosedScopes.find { it.validationPath.head == segment }?.let {
            throw IllegalArgumentException(
                "Scope named ${it.validationPath.lastToken()} is already enclosed"
            )
        } ?: ValidationScope(segment, this).also { enclosedScopes += it }

    internal fun <C : Check<V, P, C>, V, P : Check.Params<C>> checkValueAgainstRule(
        value: V,
        rule: ValidationRule<C, V, P>,
    ): ValidationError<C, V, P>? {
        val check = rule.check
        return when (check(value)) {
            false -> {
                val errorDetailsBuilderContext =
                    ErrorDetailsBuilderContext(value, validationPath, check)
                val errorDetails =
                    rule.errorDetails(errorDetailsBuilderContext)
                ValidationError(value, rule.validationContext, validationPath, errorDetails)
                    .also { errors += it }
            }
            true -> null
        }
    }
}
