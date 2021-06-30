/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
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
 * =============================================================
 */

package org.dubhe.data.util;

import lombok.Data;
import org.dubhe.data.domain.entity.DataSequence;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @description 表ID值存储
 * @date 2020-10-16
 */
@Data
public class IdAlloc {

    private Queue<Long> ids;

    private Long unUsed;

    public IdAlloc() {
        ids = new LinkedList<>();
        unUsed = 0L;
    }

    /**
     * 补充ID
     *
     * @param dataSequence
     */
    public void add(DataSequence dataSequence) {
        for (Long i = dataSequence.getStart(); i < dataSequence.getStart() + dataSequence.getStep(); i++) {
            ids.add(i);
            unUsed++;
        }
    }

    public Queue<Long> poll(int number) {
        Queue<Long> result = new LinkedList<>();
        for (int i = 0; i < number; i++) {
            result.add(ids.poll());
            unUsed--;
        }
        return result;
    }

}
