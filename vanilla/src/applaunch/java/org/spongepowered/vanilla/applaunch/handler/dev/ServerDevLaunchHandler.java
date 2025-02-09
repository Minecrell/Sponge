/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.applaunch.handler.dev;

import cpw.mods.modlauncher.api.ITransformingClassLoader;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.vanilla.applaunch.AppLaunchTargets;
import org.spongepowered.vanilla.applaunch.handler.AbstractVanillaLaunchHandler;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;

public final class ServerDevLaunchHandler extends AbstractVanillaLaunchHandler {

    @Override
    public String name() {
        return AppLaunchTargets.SERVER_DEVELOPMENT.getLaunchTarget();
    }

    @Override
    protected void launchService0(final String[] arguments, final ITransformingClassLoader launchClassLoader) throws Exception {
        Class.forName("org.spongepowered.vanilla.launch.DedicatedServerLaunch", true, launchClassLoader.getInstance())
                .getMethod("launch", VanillaPluginPlatform.class, Boolean.class, String[].class)
                .invoke(null, AppLaunch.pluginPlatform(), Boolean.TRUE, arguments);
    }
}
