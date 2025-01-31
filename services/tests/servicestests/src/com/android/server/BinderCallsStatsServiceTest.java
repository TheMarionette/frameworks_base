/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.server;

import static org.junit.Assert.assertEquals;

import android.os.Process;
import android.platform.test.annotations.Presubmit;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.server.BinderCallsStatsService.AuthorizedWorkSourceProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
@Presubmit
public class BinderCallsStatsServiceTest {
    @Test
    public void weTrustOurselves() {
        AuthorizedWorkSourceProvider workSourceProvider = new AuthorizedWorkSourceProvider() {
            protected int getCallingUid() {
                return Process.myUid();
            }

            protected int getCallingWorkSourceUid() {
                return 1;
            }
        };
        workSourceProvider.systemReady(InstrumentationRegistry.getContext());

        assertEquals(1, workSourceProvider.resolveWorkSourceUid());
    }

    @Test
    public void workSourceSetIfCallerHasPermission() {
        AuthorizedWorkSourceProvider workSourceProvider = new AuthorizedWorkSourceProvider() {
            protected int getCallingUid() {
                // System process uid which as UPDATE_DEVICE_STATS.
                return 1001;
            }

            protected int getCallingWorkSourceUid() {
                return 1;
            }
        };
        workSourceProvider.systemReady(InstrumentationRegistry.getContext());

        assertEquals(1, workSourceProvider.resolveWorkSourceUid());
    }

    @Test
    public void workSourceResolvedToCallingUid() {
        AuthorizedWorkSourceProvider workSourceProvider = new AuthorizedWorkSourceProvider() {
            protected int getCallingUid() {
                // UID without permissions.
                return Integer.MAX_VALUE;
            }

            protected int getCallingWorkSourceUid() {
                return 1;
            }
        };
        workSourceProvider.systemReady(InstrumentationRegistry.getContext());

        assertEquals(Integer.MAX_VALUE, workSourceProvider.resolveWorkSourceUid());
    }
}
