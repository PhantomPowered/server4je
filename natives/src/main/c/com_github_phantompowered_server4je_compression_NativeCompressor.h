/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_github_phantompowered_server4je_compression_NativeCompressor */

#ifndef _Included_com_github_phantompowered_server4je_compression_NativeCompressor
#define _Included_com_github_phantompowered_server4je_compression_NativeCompressor
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_github_phantompowered_server4je_compression_NativeCompressor
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_init__
        (JNIEnv *, jclass);

/*
 * Class:     com_github_phantompowered_server4je_compression_NativeCompressor
 * Method:    init
 * Signature: (ZI)J
 */
JNIEXPORT jlong JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_init__ZI
        (JNIEnv *, jobject, jboolean, jint);

/*
 * Class:     com_github_phantompowered_server4je_compression_NativeCompressor
 * Method:    free
 * Signature: (ZJ)V
 */
JNIEXPORT void JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_free
        (JNIEnv *, jobject, jboolean, jlong);

/*
 * Class:     com_github_phantompowered_server4je_compression_NativeCompressor
 * Method:    reset
 * Signature: (ZJ)V
 */
JNIEXPORT void JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_reset
        (JNIEnv *, jobject, jboolean, jlong);

/*
 * Class:     com_github_phantompowered_server4je_compression_NativeCompressor
 * Method:    process
 * Signature: (ZJJIJI)I
 */
JNIEXPORT jint JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_process
        (JNIEnv *, jobject, jboolean, jlong, jlong, jint, jlong, jint);

#ifdef __cplusplus
}
#endif
#endif