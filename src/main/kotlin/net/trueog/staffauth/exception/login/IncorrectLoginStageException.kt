package net.trueog.staffauth.exception.login

import net.trueog.staffauth.model.LoginStage

class IncorrectLoginStageException(val correctLoginStage: LoginStage) : RuntimeException()