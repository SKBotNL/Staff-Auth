package net.trueog.staffauth.exception.login

class DifferentIpException(val redirectUrl: String) : RuntimeException()