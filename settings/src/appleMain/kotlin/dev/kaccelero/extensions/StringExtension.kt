package dev.kaccelero.extensions

import platform.Foundation.NSString

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun String.toNSString() = this as NSString

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun NSString.toKString() = this as String
