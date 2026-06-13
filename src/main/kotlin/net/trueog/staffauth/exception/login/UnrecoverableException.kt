package net.trueog.staffauth.exception.login

class UnrecoverableException(val redirectUrl: String) : RuntimeException()