package io.dwsoft.checkt.core.check

import io.dwsoft.checkt.core.check.Check.Context

class RegexCheck(private val regex: Regex) :
    Check<CharSequence, RegexCheck.Key, RegexCheck.Params>
{
    override val context: Context<Key, Params> = Context.of(Key, Params(regex))

    override fun invoke(value: CharSequence): Boolean = regex.matches(value)

    object Key : Check.Key
    data class Params(val regex: Regex) : Check.Params()
}
