#include "Util.h"

JNIEXPORT void JNICALL throwRuntimeException(JNIEnv *env, const char *msg) {
    throwException(env, "java.lang.RuntimeException", msg);
}

JNIEXPORT void JNICALL throwException(JNIEnv *env, const char *type, const char *msg) {
    jclass exceptionClass = (*env)->FindClass(env, type);
    if (exceptionClass != 0) {
        (*env)->ThrowNew(env, exceptionClass, msg);
    }
}
