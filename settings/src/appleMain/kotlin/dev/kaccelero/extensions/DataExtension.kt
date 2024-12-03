package dev.kaccelero.extensions

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.*

@OptIn(BetaInteropApi::class)
fun NSData.toNSString(usingNSKeyedArchiver: Boolean): NSString? =
    if (usingNSKeyedArchiver) NSKeyedUnarchiver.unarchiveObjectWithData(this) as? NSString
    else NSString.create(this, NSUTF8StringEncoding)

fun NSData.toNSNumber(usingNSKeyedArchiver: Boolean): NSNumber? =
    if (usingNSKeyedArchiver) NSKeyedUnarchiver.unarchiveObjectWithData(this) as? NSNumber
    else null

fun NSData.toNSUUID(usingNSKeyedArchiver: Boolean): NSUUID? =
    if (usingNSKeyedArchiver) NSKeyedUnarchiver.unarchiveObjectWithData(this) as? NSUUID
    else null

fun NSString.toNSData(usingNSKeyedArchiver: Boolean): NSData? =
    if (usingNSKeyedArchiver) NSKeyedArchiver.archivedDataWithRootObject(this)
    else dataUsingEncoding(NSUTF8StringEncoding)

fun NSNumber.toNSData(usingNSKeyedArchiver: Boolean): NSData? =
    if (usingNSKeyedArchiver) NSKeyedArchiver.archivedDataWithRootObject(this)
    else null

fun NSUUID.toNSData(usingNSKeyedArchiver: Boolean): NSData? =
    if (usingNSKeyedArchiver) NSKeyedArchiver.archivedDataWithRootObject(this)
    else null
