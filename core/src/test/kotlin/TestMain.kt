import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestMain : StringSpec({
    "always passes" {
        true shouldBe true
    }
})
