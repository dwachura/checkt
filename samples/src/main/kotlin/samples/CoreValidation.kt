package io.dwsoft.checkt.samples

import io.dwsoft.checkt.core.ValidationResult
import io.dwsoft.checkt.core.ValidationStatus
import io.dwsoft.checkt.core.checks.bePositive
import io.dwsoft.checkt.core.checks.inRange
import io.dwsoft.checkt.core.checks.lessThan
import io.dwsoft.checkt.core.checks.matchesRegex
import io.dwsoft.checkt.core.checks.notBlank
import io.dwsoft.checkt.core.getOrThrow
import io.dwsoft.checkt.core.joinToString
import io.dwsoft.checkt.core.requireUnlessNull
import io.dwsoft.checkt.core.the
import io.dwsoft.checkt.core.validate
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val dto =
        BuyerProfile(
            firstName = "JohnJohnJohn",
            lastName = "                     ",
//            email = "john.doe@gmail.com",
            email = "john.doe",
            mobileNumber = "+11000111666",
            address = BuyerProfile.Address(
                street = "            ",
                houseNumber = -123,
                city = "Faketown                  123",
                postCode = "abcdeabcde123",
                country = "123",
            ),
            company = BuyerProfile.Company(
                name = "          ",
                vatNumber = "             ",
            )
        )

    (dto.validate().getOrThrow() as? ValidationStatus.Invalid)
        ?.violations?.map {
            "${it.context.path.joinToString(includeRoot = false)} - ${it.errorMessage} (was '${it.value}')"
        }?.forEach { println(it) }
        ?: println(dto)
}

private data class BuyerProfile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val mobileNumber: String,
    val address: Address,
    val company: Company?,
) {
    data class Address(
        val street: String,
        val houseNumber: Int,
        val city: String,
        val postCode: String,
        val country: String,
    )

    data class Company(
        val name: String,
        val vatNumber: String,
    )

    suspend fun validate(): ValidationResult =
        validate {
            require(::firstName) {
                +notBlank() whenValid {
                    the.length { +lessThan(10) }
                }
            }
            require(::lastName) {
                subject {
                    +notBlank()
                } whenValid {
                    the.length { +lessThan(10) }
                }
            }
            require(::email) { +matchesRegex("^\\w+(\\.\\w+)*@(\\w+\\.)+\\w+$".toRegex()) }
            require(::mobileNumber) { +matchesRegex("\\+\\d{9,16}".toRegex()) }

            require(::address) {
                require(the::street) {
                    +notBlank() whenValid {
                        the.length { +inRange(1..20) }
                    }
                }
                require(the::houseNumber) { +bePositive() }
                require(the::city) {
                    +notBlank() whenValid {
                        the.length { +inRange(1..20) }
                    }
                }
                require(the::postCode) {
                    +notBlank() whenValid {
                        +matchesRegex("^\\S{1,10}$".toRegex())
                    }
                }
                require(the::country) {
                    +notBlank() whenValid {
                        +matchesRegex("^[A-Z]{2}$".toRegex())
                    }
                }
            }

            requireUnlessNull(::company) {
                require(the::name) { +notBlank() }
                require(the::vatNumber) { +notBlank() }
            }
        }
}
