package io.ethtweet.node.apkUpdater.exception

class UninitializedException : RuntimeException("You got to call the ApkUpdater.init(context) method!")