/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.internal.management.events;

import com.hazelcast.internal.json.JsonObject;

import static com.hazelcast.internal.management.events.EventMetadata.EventType.WAN_SYNC_PROGRESS_UPDATE;

public class WanSyncProgressUpdateEvent extends AbstractWanEventBase {
    private final int partitionsToSync;
    private final int partitionsSynced;

    public WanSyncProgressUpdateEvent(String wanReplicationName, String targetGroupName, String mapName, int partitionsToSync,
                                      int partitionsSynced) {
        super(wanReplicationName, targetGroupName, mapName);
        this.partitionsToSync = partitionsToSync;
        this.partitionsSynced = partitionsSynced;
    }

    @Override
    public EventMetadata.EventType getType() {
        return WAN_SYNC_PROGRESS_UPDATE;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        json.add("partitionsToSync", partitionsToSync);
        json.add("partitionsSynced", partitionsSynced);
        return json;
    }
}
