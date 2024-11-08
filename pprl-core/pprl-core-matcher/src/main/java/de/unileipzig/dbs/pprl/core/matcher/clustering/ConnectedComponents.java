/*
 * Copyright © 2018 - 2021 Leipzig University (Database Research Group)
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

package de.unileipzig.dbs.pprl.core.matcher.clustering;

import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordClusterSimple;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ConnectedComponents implements Clusterer {

  @Override
  public <P extends RecordPair> Set<RecordCluster> cluster(Collection<P> recordPairs) {
    Graph<Record, DefaultEdge> g =
      GraphTypeBuilder.<Record, DefaultEdge>undirected().allowingMultipleEdges(false).allowingSelfLoops(false)
        .weighted(false)
        .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
        .buildGraph();

    recordPairs.forEach(rp -> {
      g.addVertex(rp.getLeftRecord());
      g.addVertex(rp.getRightRecord());
      g.addEdge(rp.getLeftRecord(), rp.getRightRecord());
    });

    ConnectivityInspector<Record, DefaultEdge> inspector = new ConnectivityInspector<>(g);
    Set<RecordCluster> recordCluster =
      inspector.connectedSets().stream().map(RecordClusterSimple::new).collect(Collectors.toSet());
    return recordCluster;
  }
}
