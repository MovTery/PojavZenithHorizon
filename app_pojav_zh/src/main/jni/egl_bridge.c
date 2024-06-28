//
// Modifiled by Vera-Firefly on 23.04.2024.
//
#include <jni.h>
#include <assert.h>
#include <dlfcn.h>

#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>

#include <EGL/egl.h>
#include <GL/osmesa.h>
#include "ctxbridges/egl_loader.h"
#include "ctxbridges/osmesa_loader.h"

#ifdef GLES_TEST
#include <GLES2/gl2.h>
#endif

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/rect.h>
#include <string.h>
#include <environ/environ.h>
#include <android/dlext.h>
#include "utils.h"
#include "ctxbridges/gl_bridge.h"
#include "ctxbridges/bridge_tbl.h"
#include "ctxbridges/osm_bridge.h"
#include "ctxbridges/renderer_config.h"

#define GLFW_CLIENT_API 0x22001
/* Consider GLFW_NO_API as Vulkan API */
#define GLFW_NO_API 0
#define GLFW_OPENGL_API 0x30001
// region OSMESA internals

// This means that the function is an external API and that it will be used
#define EXTERNAL_API __attribute__((used))
// This means that you are forced to have this function/variable for ABI compatibility
#define ABI_COMPAT __attribute__((unused))

//POJAVLAUNCHER_NSBYPASS_H
#ifndef POJAVLAUNCHER_NSBYPASS_H
#define POJAVLAUNCHER_NSBYPASS_H

void* load_turnip_vulkan();

#endif

// If you do not want to use a framebuffer
// delete the following definition
// After that, it will be stored in normal memory
#ifndef FRAME_BUFFER_SUPPOST
#define FRAME_BUFFER_SUPPOST

void* gbuffer;

#endif

// endregion OSMESA internals
struct PotatoBridge {

    /* EGLContext */ void* eglContext;
    /* EGLDisplay */ void* eglDisplay;
    /* EGLSurface */ void* eglSurface;
/*
    void* eglSurfaceRead;
    void* eglSurfaceDraw;
*/
};
EGLConfig config;
struct PotatoBridge potatoBridge;

int (*vtest_main_p) (int argc, char** argv);
void (*vtest_swap_buffers_p) (void);
void bigcore_set_affinity();

void* egl_make_current(void* window);



EXTERNAL_API void pojavTerminate() {
    printf("EGLBridge: Terminating\n");

    switch (pojav_environ->config_renderer) {
        case RENDERER_GL4ES: {
            eglMakeCurrent_p(potatoBridge.eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
            eglDestroySurface_p(potatoBridge.eglDisplay, potatoBridge.eglSurface);
            eglDestroyContext_p(potatoBridge.eglDisplay, potatoBridge.eglContext);
            eglTerminate_p(potatoBridge.eglDisplay);
            eglReleaseThread_p();

            potatoBridge.eglContext = EGL_NO_CONTEXT;
            potatoBridge.eglDisplay = EGL_NO_DISPLAY;
            potatoBridge.eglSurface = EGL_NO_SURFACE;
        } break;

            //case RENDERER_VIRGL:
        case RENDERER_VK_ZINK: {
            // Nothing to do here
        } break;
        case RENDERER_VK_ZINK_PREF: {
            // Nothing to do here
        } break;
    }
}

JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_utils_JREUtils_setupBridgeWindow(JNIEnv* env, ABI_COMPAT jclass clazz, jobject surface) {
    pojav_environ->pojavWindow = ANativeWindow_fromSurface(env, surface);
    if(pojav_environ->config_renderer == RENDERER_VK_ZINK) {
        if(br_setup_window != NULL) br_setup_window();
    } else if(pojav_environ->config_renderer == RENDERER_GL4ES) {
        if(getenv("POJAV_SPARE_BRIDGE") != NULL) {
            gl_setup_window();
        } else {
            if(br_setup_window != NULL) br_setup_window();
        }
    }
}

JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_utils_JREUtils_releaseBridgeWindow(ABI_COMPAT JNIEnv *env, ABI_COMPAT jclass clazz) {
    ANativeWindow_release(pojav_environ->pojavWindow);
}

/*If you don't want your renderer for
the Mesa class to crash in your launcher
don't touch the code here
*/
EXTERNAL_API void* pojavGetCurrentContext() {
    if(pojav_environ->config_renderer == RENDERER_VK_ZINK) {
        return br_get_current();
    } else if(pojav_environ->config_renderer == RENDERER_GL4ES){
        if(getenv("POJAV_SPARE_BRIDGE") != NULL) {
            return (void *)eglGetCurrentContext_p();
        } else {
            return br_get_current();
        }
    } else if(pojav_environ->config_renderer == RENDERER_VK_ZINK_PREF
        || pojav_environ->config_renderer == RENDERER_VIRGL) {
        return (void *)OSMesaGetCurrentContext_p();
    }
}

//Switches specifically provided for other renderers
void loadSymbols() {
    switch (pojav_environ->config_renderer) {
        case RENDERER_VIRGL:
            dlsym_OSMesa();
            dlsym_EGL();
            break;
        case RENDERER_VK_ZINK_PREF:
            dlsym_OSMesa();
            break;
    }
}

bool loadSymbolsVirGL() {
    pojav_environ->config_renderer = RENDERER_VIRGL;
    loadSymbols();

    char* fileName = calloc(1, 1024);

    sprintf(fileName, "%s/libvirgl_test_server.so", getenv("POJAV_NATIVEDIR"));
    void *handle = dlopen(fileName, RTLD_LAZY);
    printf("VirGL: libvirgl_test_server = %p\n", handle);
    if (!handle) {
        printf("VirGL: %s\n", dlerror());
    }
    vtest_main_p = dlsym(handle, "vtest_main");
    vtest_swap_buffers_p = dlsym(handle, "vtest_swap_buffers");

    free(fileName);
}

static void set_vulkan_ptr(void* ptr) {
    char envval[64];
    sprintf(envval, "%"PRIxPTR, (uintptr_t)ptr);
    setenv("VULKAN_PTR", envval, 1);
}

void load_vulkan() {
    if(getenv("POJAV_ZINK_PREFER_SYSTEM_DRIVER") == NULL && android_get_device_api_level() >= 28) {
    // the loader does not support below that
#ifdef ADRENO_POSSIBLE
        void* result = load_turnip_vulkan();
        if(result != NULL) {
            printf("AdrenoSupp: Loaded Turnip, loader address: %p\n", result);
            set_vulkan_ptr(result);
            return;
        }
#endif
    }
    printf("OSMDroid: loading vulkan regularly...\n");
    void* vulkan_ptr = dlopen("libvulkan.so", RTLD_LAZY | RTLD_LOCAL);
    printf("OSMDroid: loaded vulkan, ptr=%p\n", vulkan_ptr);
    set_vulkan_ptr(vulkan_ptr);
}

void renderer_load_config() {
    if(getenv("POJAV_SPARE_BRIDGE") == NULL) {
        pojav_environ->config_renderer = RENDERER_VK_ZINK;
        set_osm_bridge_tbl();
        printf("Bridge: Set osm bridge tbl\n");
    } else {
         pojav_environ->config_renderer = RENDERER_VK_ZINK_PREF;
         loadSymbols();
         printf("Bridge: Use old bridge\n");
    }
}

int pojavInitOpenGL() {
    // Only affects GL4ES as of now
    const char *forceVsync = getenv("FORCE_VSYNC");
    if (strcmp(forceVsync, "true") == 0)
        pojav_environ->force_vsync = true;

    // NOTE: Override for now.
    const char *renderer = getenv("POJAV_BETA_RENDERER");
    const char *ldrivermodel = getenv("LOCAL_DRIVER_MODEL");
    if (strncmp("opengles", renderer, 8) == 0) {
        pojav_environ->config_renderer = RENDERER_GL4ES;
        if(getenv("POJAV_SPARE_BRIDGE") == NULL) {
            set_gl_bridge_tbl();
        }
    } else if (strcmp(renderer, "mesa_3d") == 0) {
        if(strcmp(ldrivermodel, "driver_zink") == 0) {
            setenv("GALLIUM_DRIVER","zink",1);
            printf("Bridge: Use Zink Renderer\n");
            renderer_load_config();
            if(getenv("POJAV_LEGACY_ZINK_ALLOW") == NULL)
                load_vulkan();
        }
        if(strcmp(ldrivermodel, "driver_virgl") == 0) {
            printf("Bridge: Use VirglRenderer\n");
            pojav_environ->config_renderer = RENDERER_VIRGL;
            setenv("GALLIUM_DRIVER","virpipe",1);
            setenv("OSMESA_NO_FLUSH_FRONTBUFFER","1",false);
            if(strcmp(getenv("OSMESA_NO_FLUSH_FRONTBUFFER"),"1") == 0) {
                printf("VirGL: OSMesa buffer flush is DISABLED!\n");
            }
            loadSymbolsVirGL();
        }
        if(strcmp(ldrivermodel, "driver_panfrost") == 0) {
            printf("Bridge: Use Panfrost Renderer\n");
            setenv("GALLIUM_DRIVER", "panfrost", 1);
            setenv("PAN_DEBUG","gofaster", 0);
            renderer_load_config();
        }
        if(strcmp(ldrivermodel, "driver_freedreno") == 0) {
            setenv("GALLIUM_DRIVER", "freedreno", 1);
            setenv("MESA_LOADER_DRIVER_OVERRIDE", "kgsl", 1);
            printf("Bridge: Use Freedreno renderer\n");
            renderer_load_config();
        }
        if(strcmp(ldrivermodel, "driver_softpipe") == 0) {
            setenv("GALLIUM_DRIVER", "softpipe", 1);
            setenv("LIBGL_ALWAYS_SOFTWARE", "1", 1);
            printf("Bridge: Use Softpipe renderer\n");
            renderer_load_config();
        }
        if(strcmp(ldrivermodel, "driver_llvmpipe") == 0) {
            setenv("GALLIUM_DRIVER", "llvmpipe", 1);
            setenv("LIBGL_ALWAYS_SOFTWARE", "1", 1);
            printf("Bridge: Use LLVMpipe renderer\n");
            renderer_load_config();
        }
    }
    if(pojav_environ->config_renderer == RENDERER_VK_ZINK) {
        if(br_init()) {
            br_setup_window();
        }
    } else if(pojav_environ->config_renderer == RENDERER_GL4ES) {
        if(getenv("POJAV_SPARE_BRIDGE") != NULL) {
            if(gl_init()) {
                gl_setup_window();
                return 1;
            }
            return 0;
        } else {
            if(br_init()) {
                br_setup_window();
            }
        }
    }
    if (pojav_environ->config_renderer == RENDERER_VIRGL) {
        if (potatoBridge.eglDisplay == NULL || potatoBridge.eglDisplay == EGL_NO_DISPLAY) {
            potatoBridge.eglDisplay = eglGetDisplay_p(EGL_DEFAULT_DISPLAY);
            if (potatoBridge.eglDisplay == EGL_NO_DISPLAY) {
                printf("EGLBridge: Error eglGetDefaultDisplay() failed: %p\n", eglGetError_p());
                return 0;
            }
        }

        printf("EGLBridge: Initializing\n");
        // printf("EGLBridge: ANativeWindow pointer = %p\n", pojav_environ->pojavWindow);
        //(*env)->ThrowNew(env,(*env)->FindClass(env,"java/lang/Exception"),"Trace exception");
        if (!eglInitialize_p(potatoBridge.eglDisplay, NULL, NULL)) {
            printf("EGLBridge: Error eglInitialize() failed: %s\n", eglGetError_p());
            return 0;
        }

        static const EGLint attribs[] = {
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                // Minecraft required on initial 24
                EGL_DEPTH_SIZE, 24,
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_NONE
        };

        EGLint num_configs;
        EGLint vid;

        if (!eglChooseConfig_p(potatoBridge.eglDisplay, attribs, &config, 1, &num_configs)) {
            printf("EGLBridge: Error couldn't get an EGL visual config: %s\n", eglGetError_p());
            return 0;
        }

        assert(config);
        assert(num_configs > 0);

        if (!eglGetConfigAttrib_p(potatoBridge.eglDisplay, config, EGL_NATIVE_VISUAL_ID, &vid)) {
            printf("EGLBridge: Error eglGetConfigAttrib() failed: %s\n", eglGetError_p());
            return 0;
        }

        ANativeWindow_setBuffersGeometry(pojav_environ->pojavWindow, 0, 0, vid);

        eglBindAPI_p(EGL_OPENGL_ES_API);

        potatoBridge.eglSurface = eglCreateWindowSurface_p(potatoBridge.eglDisplay, config, pojav_environ->pojavWindow, NULL);

        if (!potatoBridge.eglSurface) {
            printf("EGLBridge: Error eglCreateWindowSurface failed: %p\n", eglGetError_p());
            //(*env)->ThrowNew(env,(*env)->FindClass(env,"java/lang/Exception"),"Trace exception");
            return 0;
        }

        // sanity checks
        {
            EGLint val;
            assert(eglGetConfigAttrib_p(potatoBridge.eglDisplay, config, EGL_SURFACE_TYPE, &val));
            assert(val & EGL_WINDOW_BIT);
        }

        printf("EGLBridge: Initialized!\n");
        printf("EGLBridge: ThreadID=%d\n", gettid());
        printf("EGLBridge: EGLDisplay=%p, EGLSurface=%p\n",
/* window==0 ? EGL_NO_CONTEXT : */
               potatoBridge.eglDisplay,
               potatoBridge.eglSurface
        );
        if (pojav_environ->config_renderer != RENDERER_VIRGL) {
            return 1;
        }
    }

    if (pojav_environ->config_renderer == RENDERER_VIRGL) {
        // Init EGL context and vtest server
        const EGLint ctx_attribs[] = {
                EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL_NONE
        };
        EGLContext* ctx = eglCreateContext_p(potatoBridge.eglDisplay, config, NULL, ctx_attribs);
        printf("VirGL: created EGL context %p\n", ctx);

        pthread_t t;
        pthread_create(&t, NULL, egl_make_current, (void *)ctx);
        usleep(100*1000); // need enough time for the server to init
    }

    if (pojav_environ->config_renderer == RENDERER_VK_ZINK_PREF
        || pojav_environ->config_renderer == RENDERER_VIRGL) {
        if(OSMesaCreateContext_p == NULL) {
            printf("OSMDroid: %s\n",dlerror());
            return 0;
        }
        if (getenv("POJAV_EXP_FRAME_BUFFER") != NULL) {
#ifdef FRAME_BUFFER_SUPPOST
            printf("OSMDroid: width=%i;height=%i, reserving %i bytes for frame buffer\n",
               pojav_environ->savedWidth, pojav_environ->savedHeight,
               pojav_environ->savedWidth * 4 * pojav_environ->savedHeight);
            gbuffer = calloc(pojav_environ->savedWidth *4, pojav_environ->savedHeight +1);
            if (gbuffer) {
                printf("OSMDroid: created frame buffer\n");
                return 1;
            } else {
                printf("OSMDroid: can't generate frame buffer\n");
                return 0;
            }
#else
            printf("[WORNING]: Macro FRAME_BUFFER_SUPPOST is undefined,defult to close\n");
#endif
        } else {
            printf("OSMDroid: do not set frame buffer\n");
        }
    }

    return 0;
}

EXTERNAL_API int pojavInit() {
    ANativeWindow_acquire(pojav_environ->pojavWindow);
    pojav_environ->savedWidth = ANativeWindow_getWidth(pojav_environ->pojavWindow);
    pojav_environ->savedHeight = ANativeWindow_getHeight(pojav_environ->pojavWindow);
    ANativeWindow_setBuffersGeometry(pojav_environ->pojavWindow,pojav_environ->savedWidth,pojav_environ->savedHeight,AHARDWAREBUFFER_FORMAT_R8G8B8X8_UNORM);
    pojavInitOpenGL();
    return 1;
}

EXTERNAL_API void pojavSetWindowHint(int hint, int value) {
    if (hint != GLFW_CLIENT_API) return;
    switch (value) {
        case GLFW_NO_API:
            pojav_environ->config_renderer = RENDERER_VULKAN;
            /* Nothing to do: initialization is handled in Java-side */
            // pojavInitVulkan();
            break;
        case GLFW_OPENGL_API:
            /* Nothing to do: initialization is called in pojavCreateContext */
            // pojavInitOpenGL();
            break;
        default:
            printf("GLFW: Unimplemented API 0x%x\n", value);
            abort();
    }
}

ANativeWindow_Buffer buf;
int32_t stride;
bool stopSwapBuffers;
EXTERNAL_API void pojavSwapBuffers() {
    if (stopSwapBuffers) {
        return;
    }
    if(pojav_environ->config_renderer == RENDERER_VK_ZINK) {
        br_swap_buffers();
    } else if(pojav_environ->config_renderer == RENDERER_GL4ES) {
        if(getenv("POJAV_SPARE_BRIDGE") != NULL) {
            gl_swap_buffers();
        } else {
            br_swap_buffers();
        }
    } else if(pojav_environ->config_renderer == RENDERER_VIRGL) {
        glFinish_p();
        vtest_swap_buffers_p();
    } else if(pojav_environ->config_renderer == RENDERER_VK_ZINK_PREF) {
        OSMesaContext ctx = OSMesaGetCurrentContext_p();
        if(ctx == NULL) {
            printf("Zink: attempted to swap buffers without context!");
        }
        OSMesaMakeCurrent_p(ctx,buf.bits,GL_UNSIGNED_BYTE,pojav_environ->savedWidth,pojav_environ->savedHeight);
        // OSMesaMakeCurrent_p(ctx,gbuffer,GL_UNSIGNED_BYTE,pojav_environ->savedWidth,pojav_environ->savedHeight);
        glFinish_p();
        ANativeWindow_unlockAndPost(pojav_environ->pojavWindow);
        ANativeWindow_lock(pojav_environ->pojavWindow,&buf,NULL);
    }
}

void* egl_make_current(void* window) {
    EGLBoolean success = eglMakeCurrent_p(
            potatoBridge.eglDisplay,
            window==0 ? (EGLSurface *) 0 : potatoBridge.eglSurface,
            window==0 ? (EGLSurface *) 0 : potatoBridge.eglSurface,
            /* window==0 ? EGL_NO_CONTEXT : */ (EGLContext *) window
    );

    if (success == EGL_FALSE) {
        printf("EGLBridge: Error: eglMakeCurrent() failed: %p\n", eglGetError_p());
    } else {
        printf("EGLBridge: eglMakeCurrent() succeed!\n");
    }

    if (pojav_environ->config_renderer == RENDERER_VIRGL) {
        printf("VirGL: vtest_main = %p\n", vtest_main_p);
        printf("VirGL: Calling VTest server's main function\n");
        vtest_main_p(3, (const char*[]){"vtest", "--no-loop-or-fork", "--use-gles", NULL, NULL});
    }
}

EXTERNAL_API void pojavMakeCurrent(void* window) {
    if(getenv("POJAV_BIG_CORE_AFFINITY") != NULL) bigcore_set_affinity();
    if(pojav_environ->config_renderer == RENDERER_VK_ZINK) {
        br_make_current((basic_render_window_t*)window);
    } else if(pojav_environ->config_renderer == RENDERER_GL4ES) {
        if(getenv("POJAV_SPARE_BRIDGE") != NULL) {
            gl_make_current((gl_render_window_t*)window);
        } else {
            br_make_current((basic_render_window_t*)window);
        }
    } else if(pojav_environ->config_renderer == RENDERER_VIRGL
        || pojav_environ->config_renderer == RENDERER_VK_ZINK_PREF) {
        printf("OSMDroid: making current\n");
        if (getenv("POJAV_EXP_FRAME_BUFFER") != NULL) {
#ifdef FRAME_BUFFER_SUPPOST
            OSMesaMakeCurrent_p((OSMesaContext)window,gbuffer,GL_UNSIGNED_BYTE,pojav_environ->savedWidth,pojav_environ->savedHeight);
#else
            printf("[ERROR]: Macro FRAME_BUFFER_SUPPOST is undefined\n");
#endif
        } else {
            OSMesaMakeCurrent_p((OSMesaContext)window,setbuffer,GL_UNSIGNED_BYTE,pojav_environ->savedWidth,pojav_environ->savedHeight);
        }
        if (pojav_environ->config_renderer == RENDERER_VK_ZINK_PREF) {
            ANativeWindow_lock(pojav_environ->pojavWindow,&buf,NULL);
            OSMesaPixelStore_p(OSMESA_ROW_LENGTH,buf.stride);
            stride = buf.stride;
            //ANativeWindow_unlockAndPost(pojav_environ->pojavWindow);
            OSMesaPixelStore_p(OSMESA_Y_UP,0);
        }
        printf("OSMDroid: vendor: %s\n",glGetString_p(GL_VENDOR));
        printf("OSMDroid: renderer: %s\n",glGetString_p(GL_RENDERER));
        glClear_p(GL_COLOR_BUFFER_BIT);
        glClearColor_p(0.4f, 0.4f, 0.4f, 1.0f);

        // Trigger a texture creation, which then set VIRGL_TEXTURE_ID
        int pixelsArr[4];
        glReadPixels_p(0, 0, 1, 1, GL_RGB, GL_INT, &pixelsArr);

        pojavSwapBuffers();
        return;
    }
}

EXTERNAL_API void* pojavCreateContext(void* contextSrc) {
    if (pojav_environ->config_renderer == RENDERER_VULKAN) {
        return (void *) pojav_environ->pojavWindow;
    }

    if (pojav_environ->config_renderer == RENDERER_VK_ZINK) {
        return br_init_context((basic_render_window_t*)contextSrc);
    } else if (pojav_environ->config_renderer == RENDERER_GL4ES) {
        if(getenv("POJAV_SPARE_BRIDGE") != NULL) {
            return gl_init_context(contextSrc);
        } else {
            return br_init_context((basic_render_window_t*)contextSrc);
        }
    } else if (pojav_environ->config_renderer == RENDERER_VK_ZINK_PREF
        || pojav_environ->config_renderer == RENDERER_VIRGL) {
        printf("OSMDroid: generating context\n");
        void* ctx = OSMesaCreateContext_p(OSMESA_RGBA,contextSrc);
        printf("OSMDroid: context=%p\n",ctx);
        return ctx;
    }
}

EXTERNAL_API JNIEXPORT jlong JNICALL
Java_org_lwjgl_vulkan_VK_getVulkanDriverHandle(ABI_COMPAT JNIEnv *env, ABI_COMPAT jclass thiz) {
    printf("EGLBridge: LWJGL-side Vulkan loader requested the Vulkan handle\n");
    // The code below still uses the env var because
    // 1. it's easier to do that
    // 2. it won't break if something will try to load vulkan and osmesa simultaneously
    if(getenv("VULKAN_PTR") == NULL) load_vulkan();
    return strtoul(getenv("VULKAN_PTR"), NULL, 0x10);
}

#ifdef FRAME_BUFFER_SUPPOST
EXTERNAL_API JNIEXPORT void JNICALL
Java_org_lwjgl_opengl_GL_nativeRegalMakeCurrent(JNIEnv *env, jclass clazz) {
    if (getenv("POJAV_EXP_FRAME_BUFFER") != NULL && pojav_environ->config_renderer != RENDERER_VK_ZINK) {
        /*printf("Regal: making current");
    
        RegalMakeCurrent_func *RegalMakeCurrent = (RegalMakeCurrent_func *) dlsym(RTLD_DEFAULT, "RegalMakeCurrent");
        RegalMakeCurrent(potatoBridge.eglContext);*/

        printf("regal removed\n");
        abort();
    }
}

EXTERNAL_API JNIEXPORT jlong JNICALL
Java_org_lwjgl_opengl_GL_getGraphicsBufferAddr(JNIEnv *env, jobject thiz) {
    if (getenv("POJAV_EXP_FRAME_BUFFER") != NULL && pojav_environ->config_renderer != RENDERER_VK_ZINK) {
        return &gbuffer;
    }
}

EXTERNAL_API JNIEXPORT jintArray JNICALL
Java_org_lwjgl_opengl_GL_getNativeWidthHeight(JNIEnv *env, jobject thiz) {
    if (getenv("POJAV_EXP_FRAME_BUFFER") != NULL && pojav_environ->config_renderer != RENDERER_VK_ZINK) {
        jintArray ret = (*env)->NewIntArray(env,2);
        jint arr[] = {pojav_environ->savedWidth, pojav_environ->savedHeight};
        (*env)->SetIntArrayRegion(env,ret,0,2,arr);
        return ret;
    }
}
#endif

EXTERNAL_API void pojavSwapInterval(int interval) {
    if(pojav_environ->config_renderer == RENDERER_VK_ZINK) {
        br_swap_interval(interval);
    } else if(pojav_environ->config_renderer == RENDERER_GL4ES) {
        if(getenv("POJAV_SPARE_BRIDGE") != NULL) {
            gl_swap_interval(interval);
        } else {
            br_swap_interval(interval);
        }
    } else if(pojav_environ->config_renderer == RENDERER_VIRGL) {
        eglSwapInterval_p(potatoBridge.eglDisplay, interval);
    } else if(pojav_environ->config_renderer == RENDERER_VK_ZINK_PREF) {
        printf("eglSwapInterval: NOT IMPLEMENTED YET!\n");
        // Nothing to do here
    }
}

