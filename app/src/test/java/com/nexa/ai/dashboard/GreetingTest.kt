package com.nexa.ai.dashboard

import com.nexa.ai.dashboard.Greeting.Period
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {

    @Test
    fun periodBoundaries() {
        assertEquals(Period.Night, Greeting.periodFor(0))
        assertEquals(Period.Night, Greeting.periodFor(4))
        assertEquals(Period.Morning, Greeting.periodFor(5))
        assertEquals(Period.Morning, Greeting.periodFor(11))
        assertEquals(Period.Noon, Greeting.periodFor(12))
        assertEquals(Period.Noon, Greeting.periodFor(15))
        assertEquals(Period.Evening, Greeting.periodFor(16))
        assertEquals(Period.Evening, Greeting.periodFor(20))
        assertEquals(Period.Night, Greeting.periodFor(21))
        assertEquals(Period.Night, Greeting.periodFor(23))
    }

    @Test
    fun bengaliIsTheDefaultLanguageText() {
        assertEquals("শুভ সকাল, Rahul", Greeting.headline("bn", "Rahul", 8))
        assertEquals("শুভ সন্ধ্যা, Rahul", Greeting.headline("bn", "Rahul", 18))
        assertEquals("আজ কীভাবে তোমাকে সাহায্য করতে পারি?", Greeting.subline("bn"))
    }

    @Test
    fun englishAlternative() {
        assertEquals("Good morning, Rahul", Greeting.headline("en", "Rahul", 8))
        assertEquals("How can I help you today?", Greeting.subline("en"))
    }

    @Test
    fun namesAreNeverTranslatedOrChanged() {
        assertEquals("শুভ দুপুর, Rahul Sen", Greeting.headline("bn", "Rahul Sen", 13))
    }

    @Test
    fun trPicksTheRequestedLanguage() {
        assertEquals("a", Greeting.tr("en", "a", "b"))
        assertEquals("b", Greeting.tr("bn", "a", "b"))
    }
}
