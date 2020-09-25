#include <openssl/evp.h>

#include "Util.h"
#include "com_github_phantompowered_server4je_cipher_NativeCrypto.h"

JNIEXPORT jlong JNICALL Java_com_github_phantompowered_server4je_cipher_NativeCrypto_init(JNIEnv *env, jobject obj, jboolean encrypt, jbyteArray key) {
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    if (ctx == NULL) {
        throwException(env, "java/lang/OutOfMemoryError", "Unable to allocate cipher");
        return 0;
    }

    jbyte *keyAsBytes = (*env)->GetPrimitiveArrayCritical(env, key, NULL);
    if (keyAsBytes == NULL) {
        EVP_CIPHER_CTX_free(ctx);
        throwException(env, "java/lang/OutOfMemoryError", "Unable to get key bytes");
        return 0;
    }

    int status = EVP_CipherInit(ctx, EVP_aes_128_cfb8(), (unsigned char *) keyAsBytes, (unsigned char *) keyAsBytes, encrypt);
    (*env)->ReleasePrimitiveArrayCritical(env, key, keyAsBytes, 0);

    if (status != 1) {
        EVP_CIPHER_CTX_free(ctx);
        throwRuntimeException(env, "Unable to initialize openssl");
        return 0;
    }

    return (jlong) ctx;
}

JNIEXPORT void JNICALL Java_com_github_phantompowered_server4je_cipher_NativeCrypto_free(JNIEnv *env, jobject obj, jlong ctx) {
    EVP_CIPHER_CTX_free((EVP_CIPHER_CTX *) ctx);
}

JNIEXPORT void JNICALL Java_com_github_phantompowered_server4je_cipher_NativeCrypto_process(JNIEnv *env, jobject obj, jlong ctx, jlong source, jlong target, jint length) {
    EVP_CipherUpdate((EVP_CIPHER_CTX *) ctx, (unsigned char *) target, (int *) &length, (unsigned char *) source, length);
}