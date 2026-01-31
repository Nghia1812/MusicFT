package com.prj.musicft

import timber.log.Timber

class MyTimberTree : Timber.DebugTree() {
    // Instead of overriding log(), use this func for shorter code
    override fun createStackElementTag(element: StackTraceElement): String {
        // Trả về tag theo định dạng bạn muốn.
        return "(${element.fileName}:${element.lineNumber}).${element.methodName}"
    }

}