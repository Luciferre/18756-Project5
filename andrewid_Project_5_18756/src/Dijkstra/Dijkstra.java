package Dijkstra;

import DataTypes.GlobalVariables;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gs on 11/16/14.
 */
public class Dijkstra {

    public void Dijkstra() {

    }

    public void DijkstraAlgorithm(int source) {
        Node tmpNode;
        int minCost = Integer.MAX_VALUE;
        ArrayList<Node> neighbors = new ArrayList<Node>();

        //initialize
        for (Node n : GlobalVariables.graph.values()) {
            n.setCost(Integer.MAX_VALUE);
            n.setPrevAddress(-1);
            n.setNotVisited();
        }
        tmpNode = GlobalVariables.graph.get(source);
        tmpNode.setVisited();
        tmpNode.setCost(0);

        neighbors.add(tmpNode);
        while (!neighbors.isEmpty()) {
            neighbors.remove(tmpNode);
            for (Node node : tmpNode.getLinks()) {
                if (!node.isVisited()) {
                    if (!neighbors.contains(node))
                        neighbors.add(node);
                    if ((node.getCost()) > (tmpNode.getCost() + 1)) {
                        node.setCost(tmpNode.getCost() + 1);
                        node.setPrevAddress(tmpNode.getAddress());
                    }
                }
            }
            //update the next node with min cost
            for (Node n : neighbors) {
                if (n.getCost() < minCost) {
                    tmpNode = n;
                    minCost = n.getCost();
                }
            }
            tmpNode.setVisited();
            minCost = Integer.MAX_VALUE;
        }
    }

    public HashMap<Integer, Integer> calRoutingTable(int source) {
        HashMap<Integer, Integer> routingTable = new HashMap<Integer, Integer>();
        int prevNode, currNode;

        for (Node node : GlobalVariables.graph.values()) {
            if (node.getAddress() != source) {
                currNode = node.getAddress();
                prevNode = node.getPrevAddress();
                //find next hop
                while (true) {
                    if (prevNode == source) {
                        routingTable.put(node.getAddress(), currNode);
                        break;
                    } else {
                        currNode = prevNode;
                        prevNode = GlobalVariables.graph.get(prevNode).getPrevAddress();
                    }
                }
            }
        }
        return routingTable;
    }
}
