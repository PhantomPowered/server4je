#include <zlib.h>
#include <stdlib.h>

#include "Util.h"
#include "com_github_phantompowered_server4je_compression_NativeCompressor.h"

static jfieldID finishedFieldRuntimeId;
static jfieldID processedFieldRuntimeId;

JNIEXPORT void JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_init__(JNIEnv *env, jclass class) {
    finishedFieldRuntimeId = (*env)->GetFieldID(env, class, "finished", "Z");
    processedFieldRuntimeId = (*env)->GetFieldID(env, class, "processed", "I");
}

JNIEXPORT jlong JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_init__ZI(JNIEnv *env, jobject obj, jboolean inflate, jint level) {
    z_stream *stream = calloc(1, sizeof(z_stream));
    int status = inflate ? inflateInit(stream) : deflateInit(stream, level);

    if (status != Z_OK) {
        throwRuntimeException(env, "Unable to initialize stream.");
    }

    return (jlong) stream;
}

JNIEXPORT void JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_free(JNIEnv *env, jobject obj, jboolean inflate, jlong ctx) {
    z_stream *stream = (z_stream *) ctx;
    int status = inflate ? inflateEnd(stream) : deflateEnd(stream);

    free(stream);
    if (status != Z_OK) {
        throwRuntimeException(env, "Could not free stream");
    }
}

JNIEXPORT void JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_reset(JNIEnv *env, jobject obj, jboolean inflate, jlong ctx) {
    z_stream *stream = (z_stream *) ctx;
    int status = inflate ? inflateReset(stream) : deflateReset(stream);

    (*env)->SetBooleanField(env, obj, finishedFieldRuntimeId, 0);
    (*env)->SetIntField(env, obj, processedFieldRuntimeId, 0);

    if (status != Z_OK) {
        throwRuntimeException(env, "Unable to reset stream");
    }
}

JNIEXPORT jint JNICALL Java_com_github_phantompowered_server4je_compression_NativeCompressor_process(JNIEnv *env, jobject obj, jboolean action, jlong ctx, jlong source,
                                                                                                     jint sourceLength, jlong target, jint targetLength) {
    z_stream *stream = (z_stream *) ctx;

    stream->avail_in = sourceLength;
    stream->next_in = (Byte *) source;
    stream->avail_out = targetLength;
    stream->next_out = (Byte *) target;

    int status = action ? inflate(stream, Z_PARTIAL_FLUSH) : deflate(stream, Z_FINISH);
    switch (status) {
        case Z_OK:
            (*env)->SetIntField(env, obj, processedFieldRuntimeId, sourceLength - stream->avail_in);
            break;
        case Z_STREAM_END:
            (*env)->SetBooleanField(env, obj, finishedFieldRuntimeId, 1);
            break;
        default:
            throwRuntimeException(env, "Unable to process correctly");
    }

    return targetLength - stream->avail_out;
}