//
// Created by Vera-Firefly on 20.08.2024.
//

#ifndef VIRGL_BRIDGE_H
#define VIRGL_BRIDGE_H

void* virglGetCurrentContext();
void loadSymbolsVirGL();
int virglInit();
void virglSwapBuffers();
void virglMakeCurrent(void* window);
void* virglCreateContext(void* contextSrc);
void virglSwapInterval(int interval);

#endif //VIRGL_BRIDGE_H
