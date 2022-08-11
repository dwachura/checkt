package io.dwsoft.checkt.core

// TODO:
//  * create DSL methods
//  * collection validation (info about index/place of invalid element)
//  * support for suspension (for translation [and validation/checks ???])
//  * interface Validator for DTOs (idea for later ???)
//  * lazy scope (evaluated when result called) ???

class ValidationScope private constructor(
    private val name: Displayed?,
    private val enclosingScope: ValidationScope? = null
) {
    /**
     * Public constructor for root scopes.
     * Note that root scope's [name] is optional and empty by default.
     */
    constructor(name: Displayed? = null) : this(name, null)

    val result: ValidationResult
        get() {
            val thisResult = errors.toValidationResult()
            val enclosedResults =
                when {
                    enclosedScopes.isNotEmpty() ->
                        enclosedScopes.map { it.result }.reduce { v1, v2 -> v1 + v2 }

                    else -> ValidationResult.Success
                }
            return thisResult + enclosedResults
        }

    private val validationPath: NamingPath =
        when (enclosingScope) {
            null -> name.toNamingPath()
            else -> {
                name?.let { enclosingScope.validationPath + it }
                // in any normal circumstances it shouldn't happen as nested scope construction
                // is possible only from inside of other ValidationScope
                    ?: throw IllegalStateException("'name' cannot be empty for nested scopes")
            }
        }

    private val errors: MutableList<ValidationError<*, *, *>> = mutableListOf()
    private val enclosedScopes: MutableList<ValidationScope> = mutableListOf()

    /**
     * Creates new [named][newScopeName] scope enclosed by this one.
     */
    fun enclose(newScopeName: Displayed): ValidationScope =
        if (enclosedScopes.any { it.name == newScopeName }) {
            throw IllegalArgumentException("Scope named '${newScopeName.value}' is already enclosed")
        } else {
            ValidationScope(newScopeName, this)
                .also { enclosedScopes += it }
        }

    internal fun <V, K : Check.Key, P : Check.Params> V.checkAgainst(
        rule: ValidationRule<V, K, P>,
    ): ValidationError<V, K, P>? {
        val value = this
        val check = rule.check
        val errorDetailsBuilder = rule.errorDetailsBuilder
        return check(value)
            .takeIf { passes -> !passes }
            ?.let {
                val errorDetailsBuilderContext = ErrorDetailsBuilderContext(value, validationPath, check.context)
                val errorDetails = errorDetailsBuilder(errorDetailsBuilderContext)
                ValidationError(value, check.context, validationPath, errorDetails)
            }
            ?.also { errors.add(it) }
    }
}

fun ValidationScope.throwIfFailure() = result.throwIfFailure()
