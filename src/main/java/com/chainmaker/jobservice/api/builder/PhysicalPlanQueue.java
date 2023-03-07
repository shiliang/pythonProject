package com.chainmaker.jobservice.api.builder;

import com.chainmaker.jobservice.core.optimizer.nodes.DAG;
import com.chainmaker.jobservice.core.optimizer.nodes.Node;
import com.chainmaker.jobservice.core.optimizer.plans.PhysicalPlan;

import java.util.*;

/**
 * @author gaokang
 * @date 2022-08-22 00:01
 * @description:将DAG中入度为0的节点放入队列，孩子节点入度减一，递归处理得到按广度优先的队列
 * @version: 1.0.0
 */

public class PhysicalPlanQueue {
    private Queue<Node<PhysicalPlan>> queue = new LinkedList<>();
    private HashMap<Node<PhysicalPlan>, Integer> indegreeMap = new HashMap<>();

    public Queue<Node<PhysicalPlan>> getQueue() {
        return queue;
    }

    public PhysicalPlanQueue(DAG<PhysicalPlan> dag) {
        Iterator<Node<PhysicalPlan>> it = dag.getNodes().iterator();
        while (it.hasNext()) {
            Node<PhysicalPlan> next = it.next();
            indegreeMap.put(next, next.getParents().size());
        }
    }

    public void toQueue() {
        List<Node<PhysicalPlan>> removeList = new ArrayList<>();
        for (Node<PhysicalPlan> node : indegreeMap.keySet()) {
            // 入度为0的节点入队列并从Map删除，孩子节点入度-1，
            if (indegreeMap.get(node) == 0) {
                removeList.add(node);
            }
        }
        for (Node<PhysicalPlan> node : removeList) {
            queue.offer(node);
            indegreeMap.remove(node);

            for (Node<PhysicalPlan> child : node.getChildren()) {
                indegreeMap.put(child, indegreeMap.get(child) - 1);
            }

        }
        if (!indegreeMap.isEmpty()) {
            toQueue();
        }
    }

}
