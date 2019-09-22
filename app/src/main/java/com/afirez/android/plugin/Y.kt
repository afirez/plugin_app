package com.afirez.android.plugin

typealias F<T, R> = (T) -> R

class RF<T, R>(val p: (RF<T, R>) -> F<T, R>)

fun <T, R> y(f: (F<T, R>) -> F<T, R>): F<T, R> {
    val r = RF<T, R> { rf -> f { rf.p(rf)(it) } }
    return r.p(r)
}

fun fac(f: F<Int, Int>) = { x: Int -> if (x <= 1) 1 else x * f(x - 1) }

fun fib(f: F<Int, Int>) = { x: Int -> if (x <= 2) 1 else f(x - 1) + f(x - 2) }

fun main(args: Array<String>) {
    println("\nfac(1..10) : ")
    for (i in 1..10) {
        print( "${y(::fac)(i)}  ")
    }

    println("\n\nfac(1..10) : ")
    for (i in 1..10) {
        print( "${y(::fib)(i)}  ")
    }
}