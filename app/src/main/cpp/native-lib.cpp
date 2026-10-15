// Connection test only. Member 4 extends this file with the real engine calls
// (initGame, getLocations, getSuspects, collectClue, askQuestion, getJournal, getHint, accuse ...).
// JNI names follow: Java_com_example_thelastalibi_NativeBridge_<methodName>
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_thelastalibi_NativeBridge_stringFromJNI(JNIEnv* env, jobject /* this */) {
    std::string message = "C++ engine connected: The Last Alibi";
    return env->NewStringUTF(message.c_str());
}
