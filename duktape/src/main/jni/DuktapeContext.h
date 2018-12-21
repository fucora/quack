/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef DUKTAPE_ANDROID_DUKTAPE_CONTEXT_H
#define DUKTAPE_ANDROID_DUKTAPE_CONTEXT_H

#include <jni.h>
#include <list>
#include "duktape/duktape.h"
#include "javascript/JavaScriptObject.h"
#include "java/JavaType.h"
#include "duktape/duk_trans_socket.h"

class DuktapeContext {
public:
  explicit DuktapeContext(JavaVM* javaVM, jobject javaDuktape);
  ~DuktapeContext();
  DuktapeContext(const DuktapeContext &) = delete;
  DuktapeContext & operator=(const DuktapeContext &) = delete;

  jobject evaluate(JNIEnv* env, jstring sourceCode, jstring fileName);
  jobject compile(JNIEnv* env, jstring sourceCode, jstring fileName);

  void cooperateDebugger();
  void waitForDebugger();
  bool isDebugging();
  void pushObject(JNIEnv* env, jobject object);
  jobject popObject(JNIEnv* env) const;
  jobject getKeyString(JNIEnv* env, jlong object, jstring key);
  jobject getKeyInteger(JNIEnv* env, jlong object, jint index);
  jobject getKeyObject(JNIEnv* env, jlong object, jobject key);
  jobject call(JNIEnv* env, jlong object, jobjectArray args);
  jobject callProperty(JNIEnv* env, jlong object, jobject target, jobjectArray args);
  void setGlobalProperty(JNIEnv *env, jobject property, jobject value);

  void set(JNIEnv *env, jstring name, jobject object, jobjectArray methods);

  const JavaScriptObject* get(JNIEnv *env, jstring name, jobjectArray methods);

private:
  jobject popObject2(JNIEnv* env) const;
  void pushObject(JNIEnv* env, jlong object);

  duk_context* m_context;
  jobject m_javaDuktape;
  std::list<JavaScriptObject> m_jsObjects;
  JavaTypeMap m_javaValues;
  const JavaType* m_objectType;
  client_sock_t m_DebuggerSocket;
};

#endif // DUKTAPE_ANDROID_DUKTAPE_CONTEXT_H
