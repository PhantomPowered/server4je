#include <jni.h>

JNIEXPORT void JNICALL throwRuntimeException(JNIEnv *env, const char *msg);

JNIEXPORT void JNICALL throwException(JNIEnv *env, const char *type, const char *msg);