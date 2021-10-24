package com.varabyte.kobweb.api

/**
 * An annotation which identifies a function as one which will be called when the server starts up. The function should
 * take an ApiInitContent as its only parameter.
 */
annotation class ApiInit