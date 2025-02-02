/*
 * Copyright (C) 2012 The Android Open Source Project
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

package androidx.renderscript;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.HashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * The superclass for all user-defined scripts. This is only
 * intended to be used by the generated derived classes.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 **/
@Deprecated
public class ScriptC extends Script {
    private static final String TAG = "ScriptC";

    /**
     * Only intended for use by the generated derived classes.
     *
     * @param id
     * @param rs
     */
    protected ScriptC(long id, RenderScript rs) {
        super(id, rs);
    }

    /**
     * Only intended for use by the generated derived classes.
     *
     *
     * @param rs
     * @param resources
     * @param resourceID
     */
    protected ScriptC(RenderScript rs, Resources resources, int resourceID) {
        super(0, rs);
        long id = internalCreate(rs, resources, resourceID);
        if (id == 0) {
            throw new RSRuntimeException("Loading of ScriptC script failed.");
        }
        setID(id);
    }

    /**
     * Only intended for use by the generated derived classes.
     *
     * @param rs
     * @param resName
     * @param bitcode32
     * @param bitcode64
     */
    protected ScriptC(RenderScript rs, String resName, byte[] bitcode32, byte[] bitcode64) {
        super(0, rs);
        long id = 0;
        if (RenderScript.sPointerSize == 4) {
            id = internalStringCreate(rs, resName, bitcode32);
        } else {
            id = internalStringCreate(rs, resName, bitcode64);
        }
        if (id == 0) {
            throw new RSRuntimeException("Loading of ScriptC script failed.");
        }
        setID(id);
    }

    private static void throwExceptionIfSDKTooHigh(RenderScript rs) {
        String message =
                "ScriptC scripts are not supported when targeting an API Level > 34. Please refer "
                    + "to https://developer.android.com/guide/topics/renderscript/migration-guide "
                    + "for proposed alternatives.";
        Log.w(TAG, message);
        // Inlining of the Build.VERSION_CODES.UPSIDE_DOWN_CAKE constant because it is not available
        // when building with older SDKs.
        // https://developer.android.com/reference/android/os/Build.VERSION_CODES#UPSIDE_DOWN_CAKE
        final int maxSupportedTargetSDKVersion = 34;
        int targetSdkVersion = rs.getApplicationContext().getApplicationInfo().targetSdkVersion;
        if (targetSdkVersion > maxSupportedTargetSDKVersion) {
            throw new UnsupportedOperationException(message);
        }
    }

    private static synchronized long internalCreate(RenderScript rs, Resources resources, int resourceID) {
        throwExceptionIfSDKTooHigh(rs);
        byte[] pgm;
        int pgmLength;
        InputStream is = resources.openRawResource(resourceID);
        try {
            try {
                pgm = new byte[1024];
                pgmLength = 0;
                while(true) {
                    int bytesLeft = pgm.length - pgmLength;
                    if (bytesLeft == 0) {
                        byte[] buf2 = new byte[pgm.length * 2];
                        System.arraycopy(pgm, 0, buf2, 0, pgm.length);
                        pgm = buf2;
                        bytesLeft = pgm.length - pgmLength;
                    }
                    int bytesRead = is.read(pgm, pgmLength, bytesLeft);
                    if (bytesRead <= 0) {
                        break;
                    }
                    pgmLength += bytesRead;
                }
            } finally {
                is.close();
            }
        } catch(IOException e) {
            throw new Resources.NotFoundException();
        }

        String resName = resources.getResourceEntryName(resourceID);
        String cachePath = rs.getApplicationContext().getCacheDir().toString();

        //        Log.v(TAG, "Create script for resource = " + resName + ", " + pgmLength + ", " + pgm);
        //Log.v(TAG, " path = " + cachePath);
        return rs.nScriptCCreate(resName, cachePath, pgm, pgmLength);
    }

    private static synchronized long internalStringCreate(RenderScript rs, String resName, byte[] bitcode) {
        //        Log.v(TAG, "Create script for resource = " + resName);
        throwExceptionIfSDKTooHigh(rs);
        String cachePath = rs.getApplicationContext().getCacheDir().toString();
        return rs.nScriptCCreate(resName, cachePath, bitcode, bitcode.length);
    }

}
