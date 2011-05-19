/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.cluster.metadata;

import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.elasticsearch.cluster.metadata.AliasMetaData.newAliasMetaDataBuilder;
import static org.elasticsearch.cluster.metadata.IndexMetaData.*;
import static org.elasticsearch.cluster.metadata.MetaData.*;
import static org.elasticsearch.common.settings.ImmutableSettings.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author kimchy (shay.banon)
 */
@Test
public class ToAndFromJsonMetaDataTests {

    @Test
    public void testSimpleJsonFromAndTo() throws IOException {
        MetaData metaData = newMetaDataBuilder()
                .put(newIndexMetaDataBuilder("test1")
                        .numberOfShards(1)
                        .numberOfReplicas(2))
                .put(newIndexMetaDataBuilder("test2")
                        .settings(settingsBuilder().put("setting1", "value1").put("setting2", "value2"))
                        .numberOfShards(2)
                        .numberOfReplicas(3))
                .put(newIndexMetaDataBuilder("test3")
                        .numberOfShards(1)
                        .numberOfReplicas(2)
                        .putMapping("mapping1", MAPPING_SOURCE1))
                .put(newIndexMetaDataBuilder("test4")
                        .settings(settingsBuilder().put("setting1", "value1").put("setting2", "value2"))
                        .numberOfShards(1)
                        .numberOfReplicas(2)
                        .putMapping("mapping1", MAPPING_SOURCE1)
                        .putMapping("mapping2", MAPPING_SOURCE2))
                .put(newIndexMetaDataBuilder("test5")
                        .settings(settingsBuilder().put("setting1", "value1").put("setting2", "value2"))
                        .numberOfShards(1)
                        .numberOfReplicas(2)
                        .putMapping("mapping1", MAPPING_SOURCE1)
                        .putMapping("mapping2", MAPPING_SOURCE2)
                        .putAlias(newAliasMetaDataBuilder("alias1"))
                        .putAlias(newAliasMetaDataBuilder("alias2")))
                .put(newIndexMetaDataBuilder("test6")
                        .settings(settingsBuilder()
                                .put("setting1", "value1")
                                .put("setting2", "value2")
                                .put("index.aliases.0", "alias3")
                                .put("index.aliases.1", "alias1"))
                        .numberOfShards(1)
                        .numberOfReplicas(2)
                        .putMapping("mapping1", MAPPING_SOURCE1)
                        .putMapping("mapping2", MAPPING_SOURCE2)
                        .putAlias(newAliasMetaDataBuilder("alias1"))
                        .putAlias(newAliasMetaDataBuilder("alias2")))
                .put(newIndexMetaDataBuilder("test7")
                        .settings(settingsBuilder()
                                .put("setting1", "value1")
                                .put("setting2", "value2")
                                .put("index.aliases.0", "alias3")
                                .put("index.aliases.1", "alias1"))
                        .numberOfShards(1)
                        .numberOfReplicas(2)
                        .putMapping("mapping1", MAPPING_SOURCE1)
                        .putMapping("mapping2", MAPPING_SOURCE2)
                        .putAlias(newAliasMetaDataBuilder("alias1").filter(ALIAS_FILTER1))
                        .putAlias(newAliasMetaDataBuilder("alias2"))
                        .putAlias(newAliasMetaDataBuilder("alias4").filter(ALIAS_FILTER2)))
                .build();

        String metaDataSource = MetaData.Builder.toXContent(metaData);
        System.out.println("ToJson: " + metaDataSource);

        MetaData parsedMetaData = MetaData.Builder.fromXContent(XContentFactory.xContent(XContentType.JSON).createParser(metaDataSource));

        IndexMetaData indexMetaData = parsedMetaData.index("test1");
        assertThat(indexMetaData.numberOfShards(), equalTo(1));
        assertThat(indexMetaData.numberOfReplicas(), equalTo(2));
        assertThat(indexMetaData.settings().getAsMap().size(), equalTo(2));
        assertThat(indexMetaData.mappings().size(), equalTo(0));

        indexMetaData = parsedMetaData.index("test2");
        assertThat(indexMetaData.numberOfShards(), equalTo(2));
        assertThat(indexMetaData.numberOfReplicas(), equalTo(3));
        assertThat(indexMetaData.settings().getAsMap().size(), equalTo(4));
        assertThat(indexMetaData.settings().get("setting1"), equalTo("value1"));
        assertThat(indexMetaData.settings().get("setting2"), equalTo("value2"));
        assertThat(indexMetaData.mappings().size(), equalTo(0));

        indexMetaData = parsedMetaData.index("test3");
        assertThat(indexMetaData.numberOfShards(), equalTo(1));
        assertThat(indexMetaData.numberOfReplicas(), equalTo(2));
        assertThat(indexMetaData.settings().getAsMap().size(), equalTo(2));
        assertThat(indexMetaData.mappings().size(), equalTo(1));
        assertThat(indexMetaData.mappings().get("mapping1").source().string(), equalTo(MAPPING_SOURCE1));

        indexMetaData = parsedMetaData.index("test4");
        assertThat(indexMetaData.numberOfShards(), equalTo(1));
        assertThat(indexMetaData.numberOfReplicas(), equalTo(2));
        assertThat(indexMetaData.settings().getAsMap().size(), equalTo(4));
        assertThat(indexMetaData.settings().get("setting1"), equalTo("value1"));
        assertThat(indexMetaData.settings().get("setting2"), equalTo("value2"));
        assertThat(indexMetaData.mappings().size(), equalTo(2));
        assertThat(indexMetaData.mappings().get("mapping1").source().string(), equalTo(MAPPING_SOURCE1));
        assertThat(indexMetaData.mappings().get("mapping2").source().string(), equalTo(MAPPING_SOURCE2));

        indexMetaData = parsedMetaData.index("test5");
        assertThat(indexMetaData.numberOfShards(), equalTo(1));
        assertThat(indexMetaData.numberOfReplicas(), equalTo(2));
        assertThat(indexMetaData.settings().getAsMap().size(), equalTo(4));
        assertThat(indexMetaData.settings().get("setting1"), equalTo("value1"));
        assertThat(indexMetaData.settings().get("setting2"), equalTo("value2"));
        assertThat(indexMetaData.mappings().size(), equalTo(2));
        assertThat(indexMetaData.mappings().get("mapping1").source().string(), equalTo(MAPPING_SOURCE1));
        assertThat(indexMetaData.mappings().get("mapping2").source().string(), equalTo(MAPPING_SOURCE2));
        assertThat(indexMetaData.aliases().size(), equalTo(2));
        assertThat(indexMetaData.aliases().get("alias1").alias(), equalTo("alias1"));
        assertThat(indexMetaData.aliases().get("alias2").alias(), equalTo("alias2"));

        indexMetaData = parsedMetaData.index("test6");
        assertThat(indexMetaData.numberOfShards(), equalTo(1));
        assertThat(indexMetaData.numberOfReplicas(), equalTo(2));
        assertThat(indexMetaData.settings().getAsMap().size(), equalTo(4));
        assertThat(indexMetaData.settings().get("setting1"), equalTo("value1"));
        assertThat(indexMetaData.settings().get("setting2"), equalTo("value2"));
        assertThat(indexMetaData.mappings().size(), equalTo(2));
        assertThat(indexMetaData.mappings().get("mapping1").source().string(), equalTo(MAPPING_SOURCE1));
        assertThat(indexMetaData.mappings().get("mapping2").source().string(), equalTo(MAPPING_SOURCE2));
        assertThat(indexMetaData.aliases().size(), equalTo(3));
        assertThat(indexMetaData.aliases().get("alias1").alias(), equalTo("alias1"));
        assertThat(indexMetaData.aliases().get("alias2").alias(), equalTo("alias2"));
        assertThat(indexMetaData.aliases().get("alias3").alias(), equalTo("alias3"));

        indexMetaData = parsedMetaData.index("test7");
        assertThat(indexMetaData.numberOfShards(), equalTo(1));
        assertThat(indexMetaData.numberOfReplicas(), equalTo(2));
        assertThat(indexMetaData.settings().getAsMap().size(), equalTo(4));
        assertThat(indexMetaData.settings().get("setting1"), equalTo("value1"));
        assertThat(indexMetaData.settings().get("setting2"), equalTo("value2"));
        assertThat(indexMetaData.mappings().size(), equalTo(2));
        assertThat(indexMetaData.mappings().get("mapping1").source().string(), equalTo(MAPPING_SOURCE1));
        assertThat(indexMetaData.mappings().get("mapping2").source().string(), equalTo(MAPPING_SOURCE2));
        assertThat(indexMetaData.aliases().size(), equalTo(4));
        assertThat(indexMetaData.aliases().get("alias1").alias(), equalTo("alias1"));
        assertThat(indexMetaData.aliases().get("alias1").filter().string(), equalTo(ALIAS_FILTER1));
        assertThat(indexMetaData.aliases().get("alias2").alias(), equalTo("alias2"));
        assertThat(indexMetaData.aliases().get("alias2").filter(), nullValue());
        assertThat(indexMetaData.aliases().get("alias3").alias(), equalTo("alias3"));
        assertThat(indexMetaData.aliases().get("alias3").filter(), nullValue());
        assertThat(indexMetaData.aliases().get("alias4").alias(), equalTo("alias4"));
        assertThat(indexMetaData.aliases().get("alias4").filter().string(), equalTo(ALIAS_FILTER2));
    }

    private static final String MAPPING_SOURCE1 = "{\"mapping1\":{\"text1\":{\"type\":\"string\"}}}";
    private static final String MAPPING_SOURCE2 = "{\"mapping2\":{\"text2\":{\"type\":\"string\"}}}";
    private static final String ALIAS_FILTER1 = "{\"field1\":\"value1\"}";
    private static final String ALIAS_FILTER2 = "{\"field2\":\"value2\"}";
}
