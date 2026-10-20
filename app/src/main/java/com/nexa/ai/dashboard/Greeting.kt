package com.nexa.ai.dashboard

/** Pure text logic (unit-tested). Bengali is the default; English is the alternative. */
object Greeting {
    enum class Period { Morning, Noon, Evening, Night }

    fun periodFor(hour: Int): Period = when (hour) {
        in 5..11 -> Period.Morning
        in 12..15 -> Period.Noon
        in 16..20 -> Period.Evening
        else -> Period.Night
    }

    fun headline(lang: String, name: String, hour: Int): String {
        val period = periodFor(hour)
        return if (lang == "bn") {
            when (period) {
                Period.Morning -> "শুভ সকাল, $name"
                Period.Noon -> "শুভ দুপুর, $name"
                Period.Evening -> "শুভ সন্ধ্যা, $name"
                Period.Night -> "শুভ রাত্রি, $name"
            }
        } else {
            when (period) {
                Period.Morning -> "Good morning, $name"
                Period.Noon -> "Good afternoon, $name"
                Period.Evening -> "Good evening, $name"
                Period.Night -> "Good night, $name"
            }
        }
    }

    fun subline(lang: String): String =
        if (lang == "bn") "আজ কীভাবে তোমাকে সাহায্য করতে পারি?" else "How can I help you today?"

    /** Picks the Bengali or English variant of a UI string. */
    fun tr(lang: String, en: String, bn: String): String = if (lang == "bn") bn else en
}
