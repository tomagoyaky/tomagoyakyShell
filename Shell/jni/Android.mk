LOCAL_PATH:=$(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:=zpserver.cpp
LOCAL_MODULE:=zpserver
include $(BUILD_EXECUTABLE)
