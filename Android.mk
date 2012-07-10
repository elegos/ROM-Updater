LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_NAME := libGson
LOCAL_MODULE_TAGS := optional
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libGson:Gson/gson-2.2.2.jar
include $(BUILD_MULTI_PREBUILT)


include $(CLEAR_VARS)
LOCAL_PACKAGE_NAME := RomUpdater
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := shared
LOCAL_SRC_FILES := \
	$(call all-java-files-under, google) \
	$(call all-java-files-under, src) \

LOCAL_STATIC_JAVA_LIBRARIES := libGson

# LOCAL_JAVACFLAGS += -Xlint:unchecked

include $(BUILD_PACKAGE)

