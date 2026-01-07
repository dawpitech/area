package com.uwu.area

object ApiRoutes {
    const val BASE = "http://100.78.22.73:8080"
    const val SIGNIN = "/auth/sign-in"
    const val SIGNUP = "/auth/signup"

    const val WORKFLOWS = "/workflow/"
    const val WORKFLOW_CHECK = "/workflow/check"
    const val ACTIONS = "/action/"
    const val REACTIONS = "/reaction/"
    const val GITHUB_INIT = "/providers/github/auth/init"
    const val GITHUB_CHECK = "/providers/github/auth/check"
}