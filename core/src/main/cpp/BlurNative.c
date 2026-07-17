/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Donny Yale
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * ===========================================
 * Project: QmBlurView
 * Created Date: 2025-10-21
 * Author: Donny Yale
 * GitHub: https://github.com/QmDeve/QmBlurView
 * Website: https://blurview.qmdeve.com
 * ===========================================
 */

#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <android/bitmap.h>

#define LOG_TAG "libbitmaputils"
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static unsigned short const qmblur_mul[255] =
{
        512,512,456,512,328,456,335,512,405,328,271,456,388,335,292,512,
        454,405,364,328,298,271,496,456,420,388,360,335,312,292,273,512,
        482,454,428,405,383,364,345,328,312,298,284,271,259,496,475,456,
        437,420,404,388,374,360,347,335,323,312,302,292,282,273,265,512,
        497,482,468,454,441,428,417,405,394,383,373,364,354,345,337,328,
        320,312,305,298,291,284,278,271,265,259,507,496,485,475,465,456,
        446,437,428,420,412,404,396,388,381,374,367,360,354,347,341,335,
        329,323,318,312,307,302,297,292,287,282,278,273,269,265,261,512,
        505,497,489,482,475,468,461,454,447,441,435,428,422,417,411,405,
        399,394,389,383,378,373,368,364,359,354,350,345,341,337,332,328,
        324,320,316,312,309,305,301,298,294,291,287,284,281,278,274,271,
        268,265,262,259,257,507,501,496,491,485,480,475,470,465,460,456,
        451,446,442,437,433,428,424,420,416,412,408,404,400,396,392,388,
        385,381,377,374,370,367,363,360,357,354,350,347,344,341,338,335,
        332,329,326,323,320,318,315,312,310,307,304,302,299,297,294,292,
        289,287,285,282,280,278,275,273,271,269,267,265,263,261,259
};

static unsigned char const qmblur_shr[255] =
{
        9, 11, 12, 13, 13, 14, 14, 15, 15, 15, 15, 16, 16, 16, 16, 17,
        17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19,
        19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21,
        21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
        21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22,
        22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
        22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
        24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24
};

void qmblurJob(unsigned char* src,
                  unsigned int w,
                  unsigned int h,
                  unsigned int radius,
                  int cores,
                  int core,
                  int step,
                  int rounds)
{
    int rnd;
    unsigned int x, y, xp, yp, i;
    unsigned int sp;
    unsigned int qm_start;
    unsigned char* qm_ptr;

    unsigned char* src_ptr;
    unsigned char* dst_ptr;

    unsigned long sum_r;
    unsigned long sum_g;
    unsigned long sum_b;
    unsigned long sum_in_r;
    unsigned long sum_in_g;
    unsigned long sum_in_b;
    unsigned long sum_out_r;
    unsigned long sum_out_g;
    unsigned long sum_out_b;

    unsigned int wm = w - 1;
    unsigned int hm = h - 1;
    unsigned int w4 = w * 4;
    unsigned int div = (radius * 2) + 1;
    unsigned int mul_sum = qmblur_mul[radius];
    unsigned char shr_sum = qmblur_shr[radius];

    // Fixed-size stack buffer sized for the largest radius the mul/shr tables
    // support (254) — 1527 bytes, cannot overflow the thread stack. A per-call
    // malloc/free here made all workers contend on the heap lock inside the
    // parallel section, partially serializing the blur.
    unsigned char qm[(2 * 254 + 1) * 3];

    if (step == 1)
    {
        int minY = core * h / cores;
        int maxY = (core + 1) * h / cores;

        // All rounds run here, inside one call: a horizontal pass only mixes pixels
        // within a row, so this worker's row band needs no synchronization with other
        // bands between rounds. The caller used to redispatch + barrier per round;
        // looping here instead keeps the band cache-hot and pays dispatch once.
        for (rnd = 0; rnd < rounds; rnd++)
        for(y = minY; y < maxY; y++)
        {
            sum_r = sum_g = sum_b =
            sum_in_r = sum_in_g = sum_in_b =
            sum_out_r = sum_out_g = sum_out_b = 0;

            src_ptr = src + w4 * y;

            for(i = 0; i <= radius; i++)
            {
                qm_ptr    = &qm[ 3 * i ];
                qm_ptr[0] = src_ptr[0];
                qm_ptr[1] = src_ptr[1];
                qm_ptr[2] = src_ptr[2];
                sum_r += src_ptr[0] * (i + 1);
                sum_g += src_ptr[1] * (i + 1);
                sum_b += src_ptr[2] * (i + 1);
                sum_out_r += src_ptr[0];
                sum_out_g += src_ptr[1];
                sum_out_b += src_ptr[2];
            }

            for(i = 1; i <= radius; i++)
            {
                if (i <= wm) src_ptr += 4;
                qm_ptr = &qm[ 3 * (i + radius) ];
                qm_ptr[0] = src_ptr[0];
                qm_ptr[1] = src_ptr[1];
                qm_ptr[2] = src_ptr[2];
                sum_r += src_ptr[0] * (radius + 1 - i);
                sum_g += src_ptr[1] * (radius + 1 - i);
                sum_b += src_ptr[2] * (radius + 1 - i);
                sum_in_r += src_ptr[0];
                sum_in_g += src_ptr[1];
                sum_in_b += src_ptr[2];
            }

            sp = radius;
            xp = radius;
            if (xp > wm) xp = wm;
            src_ptr = src + 4 * (xp + y * w);
            dst_ptr = src + y * w4;
            for(x = 0; x < w; x++)
            {
                // No clamp needed. "* mul_sum >> shr_sum" divides the weighted sum by
                // the kernel weight total (r+1)^2 — see qmblur_mul/qmblur_shr above,
                // which are StackBlur's original mul_table/shg_table (Mario Klingemann,
                // 2004). Those table pairs are chosen so that even the worst case —
                // every pixel in the window at 255 — comes out as exactly 255 after
                // the multiply+shift, for every radius they cover. The result cannot
                // exceed 255, so clamping it was dead work in the hottest loop; the
                // reference StackBlur stores these unclamped as well.
                dst_ptr[0] = (unsigned char)((sum_r * mul_sum) >> shr_sum);
                dst_ptr[1] = (unsigned char)((sum_g * mul_sum) >> shr_sum);
                dst_ptr[2] = (unsigned char)((sum_b * mul_sum) >> shr_sum);
                dst_ptr += 4;

                sum_r -= sum_out_r;
                sum_g -= sum_out_g;
                sum_b -= sum_out_b;

                qm_start = sp + div - radius;
                if (qm_start >= div) qm_start -= div;
                qm_ptr = &qm[3 * qm_start];

                sum_out_r -= qm_ptr[0];
                sum_out_g -= qm_ptr[1];
                sum_out_b -= qm_ptr[2];

                if(xp < wm)
                {
                    src_ptr += 4;
                    ++xp;
                }

                qm_ptr[0] = src_ptr[0];
                qm_ptr[1] = src_ptr[1];
                qm_ptr[2] = src_ptr[2];

                sum_in_r += src_ptr[0];
                sum_in_g += src_ptr[1];
                sum_in_b += src_ptr[2];
                sum_r    += sum_in_r;
                sum_g    += sum_in_g;
                sum_b    += sum_in_b;

                ++sp;
                if (sp >= div) sp = 0;
                qm_ptr = &qm[sp*3];

                sum_out_r += qm_ptr[0];
                sum_out_g += qm_ptr[1];
                sum_out_b += qm_ptr[2];
                sum_in_r  -= qm_ptr[0];
                sum_in_g  -= qm_ptr[1];
                sum_in_b  -= qm_ptr[2];
            }

        }
        return;
    }

    if (step == 2)
    {
        int minX = core * w / cores;
        int maxX = (core + 1) * w / cores;

        // Same as step 1: vertical rounds only mix pixels within a column, so all
        // rounds for this worker's column band run in one call, no barriers between.
        for (rnd = 0; rnd < rounds; rnd++)
        for(x = minX; x < maxX; x++)
        {
            sum_r =    sum_g =    sum_b =
            sum_in_r = sum_in_g = sum_in_b =
            sum_out_r = sum_out_g = sum_out_b = 0;

            src_ptr = src + 4 * x;
            for(i = 0; i <= radius; i++)
            {
                qm_ptr    = &qm[i * 3];
                qm_ptr[0] = src_ptr[0];
                qm_ptr[1] = src_ptr[1];
                qm_ptr[2] = src_ptr[2];
                sum_r           += src_ptr[0] * (i + 1);
                sum_g           += src_ptr[1] * (i + 1);
                sum_b           += src_ptr[2] * (i + 1);
                sum_out_r       += src_ptr[0];
                sum_out_g       += src_ptr[1];
                sum_out_b       += src_ptr[2];
            }
            for(i = 1; i <= radius; i++)
            {
                if(i <= hm) src_ptr += w4;

                qm_ptr = &qm[3 * (i + radius)];
                qm_ptr[0] = src_ptr[0];
                qm_ptr[1] = src_ptr[1];
                qm_ptr[2] = src_ptr[2];
                sum_r += src_ptr[0] * (radius + 1 - i);
                sum_g += src_ptr[1] * (radius + 1 - i);
                sum_b += src_ptr[2] * (radius + 1 - i);
                sum_in_r += src_ptr[0];
                sum_in_g += src_ptr[1];
                sum_in_b += src_ptr[2];
            }

            sp = radius;
            yp = radius;
            if (yp > hm) yp = hm;
            src_ptr = src + 4 * (x + yp * w);
            dst_ptr = src + 4 * x;
            for(y = 0; y < h; y++)
            {
                // No clamp needed. "* mul_sum >> shr_sum" divides the weighted sum by
                // the kernel weight total (r+1)^2 — see qmblur_mul/qmblur_shr above,
                // which are StackBlur's original mul_table/shg_table (Mario Klingemann,
                // 2004). Those table pairs are chosen so that even the worst case —
                // every pixel in the window at 255 — comes out as exactly 255 after
                // the multiply+shift, for every radius they cover. The result cannot
                // exceed 255, so clamping it was dead work in the hottest loop; the
                // reference StackBlur stores these unclamped as well.
                dst_ptr[0] = (unsigned char)((sum_r * mul_sum) >> shr_sum);
                dst_ptr[1] = (unsigned char)((sum_g * mul_sum) >> shr_sum);
                dst_ptr[2] = (unsigned char)((sum_b * mul_sum) >> shr_sum);
                dst_ptr += w4;

                sum_r -= sum_out_r;
                sum_g -= sum_out_g;
                sum_b -= sum_out_b;

                qm_start = sp + div - radius;
                if(qm_start >= div) qm_start -= div;
                qm_ptr = &qm[3 * qm_start];

                sum_out_r -= qm_ptr[0];
                sum_out_g -= qm_ptr[1];
                sum_out_b -= qm_ptr[2];

                if(yp < hm)
                {
                    src_ptr += w4;
                    ++yp;
                }

                qm_ptr[0] = src_ptr[0];
                qm_ptr[1] = src_ptr[1];
                qm_ptr[2] = src_ptr[2];

                sum_in_r += src_ptr[0];
                sum_in_g += src_ptr[1];
                sum_in_b += src_ptr[2];
                sum_r    += sum_in_r;
                sum_g    += sum_in_g;
                sum_b    += sum_in_b;

                ++sp;
                if (sp >= div) sp = 0;
                qm_ptr = &qm[sp*3];

                sum_out_r += qm_ptr[0];
                sum_out_g += qm_ptr[1];
                sum_out_b += qm_ptr[2];
                sum_in_r  -= qm_ptr[0];
                sum_in_g  -= qm_ptr[1];
                sum_in_b  -= qm_ptr[2];
            }
        }
    }
}

JNIEXPORT void JNICALL Java_com_qmdeve_blurview_BlurNative_blur(JNIEnv* env, jclass clzz, jobject bitmapOut, jint radius, jint threadCount, jint threadIndex, jint round, jint rounds) {
    AndroidBitmapInfo   infoOut;
    void*               pixelsOut;

    int ret;

    if ((ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) != 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if (infoOut.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        LOGE("==> %d", infoOut.format);
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) != 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return;
    }

    int h = infoOut.height;
    int w = infoOut.width;

    qmblurJob((unsigned char*)pixelsOut, w, h, radius, threadCount, threadIndex, round, rounds);
    AndroidBitmap_unlockPixels(env, bitmapOut);
}