package Dijkstra;

import javax.swing.plaf.basic.BasicScrollPaneUI;
import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gs on 11/16/14.
 */
public class Node {
    private int cost;
    private boolean visited;
    private int address;
    private int prevAddress;
    private ArrayList<Node> links;

    public Node(int nodeAddress) {
        address = nodeAddress;
        cost = Integer.MAX_VALUE;
        visited = false;
        prevAddress = -1;
        this.links = new ArrayList<Node>();

    }

    public int getAddress() {
        return address;
    }

    public void addLink(Node node) {
        links.add(node);
    }

    public void setVisited() {
        this.visited = true;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public ArrayList<Node> getLinks() {
        return links;
    }

    public boolean isVisited() {
        return this.visited;
    }

    public int getCost() {
        return this.cost;
    }

    public void setPrevAddress(int address) {
        this.prevAddress = address;
    }

    public int getPrevAddress() {
        return this.prevAddress;
    }

    public void setNotVisited() {
        this.visited = false;
    }

}
