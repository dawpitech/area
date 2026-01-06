package com.uwu.area

object ApiRoutes {
    const val BASE = "http://192.168.1.20:8080"
    const val SIGNIN = "/auth/sign-in"
    const val SIGNUP = "/auth/signup"

    const val WORKFLOWS = "/workflows"
    const val GITHUB_INIT = "/providers/github/auth/init"
    const val GITHUB_TOKEN = "/providers/github/auth/token"
}