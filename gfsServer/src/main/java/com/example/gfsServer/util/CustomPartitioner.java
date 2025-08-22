package  com.example.gfsServer.util;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * Custom partitioner for GFS chunk distribution
 * This partitioner can be used to implement custom partition key strategies
 */
public class CustomPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        
        if (keyBytes == null) {
            // If no key is provided, use round-robin
            return Math.abs(Utils.murmur2(valueBytes)) % numPartitions;
        }
        
        // Use the key to determine partition
        return Math.abs(Utils.murmur2(keyBytes)) % numPartitions;
    }

    @Override
    public void close() {
        // Clean up resources if needed
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // Configure the partitioner with any additional settings
    }
} 