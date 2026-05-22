package net.trueog.staffauth.exception.setup

import net.trueog.staffauth.model.SetupStage

class IncorrectSetupStageException(val correctSetupStage: SetupStage) : RuntimeException()