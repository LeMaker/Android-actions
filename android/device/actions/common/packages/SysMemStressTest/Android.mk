LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := SysMemStressTest
LOCAL_PROGUARD_ENABLED :=full
include $(BUILD_PACKAGE)
