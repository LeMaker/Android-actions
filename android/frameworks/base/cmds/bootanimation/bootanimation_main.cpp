/*
 * Copyright (C) 2007 The Android Open Source Project
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

#define LOG_TAG "BootAnimation"

#include <cutils/properties.h>

#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <binder/IServiceManager.h>

#include <utils/Log.h>
#include <utils/threads.h>

#include<string.h>
#if defined(HAVE_PTHREADS)
# include <pthread.h>
# include <sys/resource.h>
#endif

#include "BootAnimation.h"

using namespace android;

// ---------------------------------------------------------------------------
/**
*
*xxxxx
*xxxxx
*
************************************
*
*ActionsCode(liuxinxu, change_code)
*/
int main(int argc, char** argv)
{
#if defined(HAVE_PTHREADS)
    setpriority(PRIO_PROCESS, 0, ANDROID_PRIORITY_DISPLAY);
#endif

    char value[PROPERTY_VALUE_MAX];

    //ActionsCode(liuxinxu, new feature)
    int animationType = BOOT_ANIMATION;

    property_get("debug.sf.nobootanimation", value, "0");
    int noBootAnimation = atoi(value);
    ALOGI_IF(noBootAnimation,  "boot animation disabled");
    if (!noBootAnimation) {

        sp<ProcessState> proc(ProcessState::self());
        ProcessState::self()->startThreadPool();

        //ActionsCode(liuxinxu, new feature)
        if(argc <= 1) {
            animationType = BOOT_ANIMATION;
        } else {
            if(!(strcmp(argv[1], "poweron"))) {
                animationType = BOOT_ANIMATION;
            } else {
                animationType = SHUTDOWN_ANIMATION;
            }
        }
        //ActionsCode(liuxinxu, new feature)
        sp<BootAnimation> boot = new BootAnimation(animationType);

        //ActionsCode(liuxinxu, new_feature: add play music when booting)
        boot->playMusic();
        IPCThreadState::self()->joinThreadPool();
    }
    return 0;
}
