/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * This library is free software; you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 */
package com.ovea.system.util;

/** Provide simplified platform information. */
public final class Platform {
    public static final int UNSPECIFIED = -1;
    public static final int MAC = 0;
    public static final int LINUX = 1;
    public static final int WINDOWS = 2;
    public static final int SOLARIS = 3;
    public static final int FREEBSD = 4;
    public static final int OPENBSD = 5;
    public static final int WINDOWSCE = 6;

    private static final int osType;
    
    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            osType = LINUX;
        } 
        else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            osType = MAC;
        }
        else if (osName.startsWith("Windows CE")) {
            osType = WINDOWSCE;
        }
        else if (osName.startsWith("Windows")) {
            osType = WINDOWS;
        }
        else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
            osType = SOLARIS;
        }
        else if (osName.startsWith("FreeBSD")) {
            osType = FREEBSD;
        }
        else if (osName.startsWith("OpenBSD")) {
            osType = OPENBSD;
        }
        else {
            osType = UNSPECIFIED;
        }
    }
    private Platform() { }
    public static int getOSType() {
        return osType;
    }
    public static boolean isMac() {
        return osType == MAC;
    }
    public static boolean isLinux() {
        return osType == LINUX;
    }
    public static boolean isWindowsCE() {
        return osType == WINDOWSCE;
    }
    public static boolean isWindows() {
        return osType == WINDOWS || osType == WINDOWSCE;
    }
    public static boolean isSolaris() {
        return osType == SOLARIS;
    }
    public static boolean isFreeBSD() {
        return osType == FREEBSD;
    }
    public static boolean isOpenBSD() {
        return osType == OPENBSD;
    }
    public static boolean isX11() {
        return !Platform.isWindows() && !Platform.isMac();
    }
    public static boolean deleteNativeLibraryAfterVMExit() {
        return osType == WINDOWS;
    }
    public static boolean hasRuntimeExec() {
        if (isWindowsCE() && "J9".equals(System.getProperty("java.vm.name")))
            return false;
        return true;
    }
    public static boolean is64Bit() {
        String model = System.getProperty("sun.arch.data.model");
        if (model != null)
            return "64".equals(model);
        String arch = System.getProperty("os.arch").toLowerCase();
        if ("x86_64".equals(arch)
            || "ppc64".equals(arch)
            || "sparcv9".equals(arch)
            || "amd64".equals(arch)) {
            return true;
        }
        // assume false, may be an error
        return false;
    }
}
