package com.cai.bridge


@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
annotation class Bridge(val isMock: Boolean = false)