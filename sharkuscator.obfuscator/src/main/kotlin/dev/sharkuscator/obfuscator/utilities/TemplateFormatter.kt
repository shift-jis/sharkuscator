package dev.sharkuscator.obfuscator.utilities

import dev.sharkuscator.obfuscator.extensions.unicodify
import java.util.regex.Matcher
import java.util.regex.Pattern

object TemplateFormatter {
    private fun replacePlaceholders(template: String, placeholders: String, tokens: Map<String, String>): String {
        val templateMatcher = Pattern.compile(placeholders).matcher(template)
        val stringBuffer = StringBuffer()
        while (templateMatcher.find()) {
            templateMatcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(tokens[templateMatcher.group(1)] ?: ""))
        }
        templateMatcher.appendTail(stringBuffer)
        return stringBuffer.toString()
    }

    fun formatWithTokens(template: String, tokens: Map<String, String>): String {
        val placeholders = String.format("\\$(%s)", tokens.keys.joinToString("|") { key -> key.unicodify() })
        return replacePlaceholders(template, placeholders, tokens)
    }
}
