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
package com.ovea.system.util;

import org.hyperic.jni.ArchNotSupportedException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class SigarLoader {

    private SigarLoader() {
    }

    public static Sigar newSigar() {
        return Loader.newSigar();
    }

    public static Sigar instance() {
        return Loader.SIGAR;
    }

    private static final class Loader {

        private static final Sigar SIGAR;

        static {
            try {
                File tmp = new File(System.getProperty("java.io.tmpdir"), "ovea-system-sigar");
                if (!tmp.exists()) {
                    tmp.mkdirs();
                }
                org.hyperic.sigar.SigarLoader loader = new org.hyperic.sigar.SigarLoader(Sigar.class);
                File lib = new File(tmp, loader.getLibraryName());
                File lockFile = new File(lib + ".lock");
                FileChannel channel = null;
                FileLock lock = null;

                try {
                    channel = new RandomAccessFile(lockFile, "rw").getChannel();
                    lock = channel.lock();
                    if (!lib.exists()) {
                        InputStream in = null;
                        OutputStream out = null;
                        try {
                            String name = loader.getLibName();
                            if (name == null) {
                                name = loader.getDefaultLibName();
                            }
                            name = "sigar/" + org.hyperic.sigar.SigarLoader.getLibraryPrefix() + name + org.hyperic.sigar.SigarLoader.getLibraryExtension();
                            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
                            if (in == null) {
                                throw new IllegalStateException("SIGAR library " + name + " not found in classpath");
                            }
                            in = new BufferedInputStream(in);
                            out = new BufferedOutputStream(new FileOutputStream(lib));
                            byte[] buffer = new byte[8192];
                            int c;
                            while ((c = in.read(buffer)) != -1) {
                                out.write(buffer, 0, c);
                            }
                        } finally {
                            if (out != null)
                                try {
                                    out.close();
                                } catch (IOException ignored) {
                                }
                            if (in != null)
                                try {
                                    in.close();
                                } catch (IOException ignored) {
                                }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                } finally {
                    if (lock != null)
                        try {
                            lock.release();
                        } catch (IOException ignored) {
                        }
                    if (channel != null)
                        try {
                            channel.close();
                        } catch (IOException ignored) {
                        }
                    lockFile.delete();
                }
                System.setProperty(loader.getPackageName() + ".path", tmp.getAbsolutePath());
                Sigar.load();
                SIGAR = newSigar();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        SIGAR.close();
                    }
                });
            } catch (ArchNotSupportedException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (SigarException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        static Sigar newSigar() {
            return new Sigar();
        }
    }
}
