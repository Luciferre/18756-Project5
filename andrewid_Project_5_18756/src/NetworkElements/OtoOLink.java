package NetworkElements;

import DataTypes.*;
import Dijkstra.Node;

public class OtoOLink {
    private LSRNIC r1NIC = null, r2NIC = null;
    private Boolean trace = Boolean.FALSE;

    /**
     * The default constructor for a OtoOLink
     *
     * @param computerNIC
     * @param routerNIC
     * @since 1.0
     */
    public OtoOLink(LSRNIC r1NIC, LSRNIC r2NIC) {
        this.r1NIC = r1NIC;
        this.r1NIC.connectOtoOLink(this);
        this.r2NIC = r2NIC;
        this.r2NIC.connectOtoOLink(this);

        if (this.trace) {
            if (r1NIC == null)
                System.err.println("Error (OtoOLink): R1 nic is null");
            if (r1NIC == null)
                System.err.println("Error (OtoOLink): R2 nic is null");
        }

        updateGraph(r1NIC, r2NIC);
    }

    /**
     * Sends a packet from one end of the link to the other
     *
     * @param currentPacket the packet to be sent
     * @param nic           the nic the packet is being sent from
     * @since 1.0
     */
    public void sendPacket(Packet currentPacket, LSRNIC nic) {
        if (this.r1NIC.equals(nic)) {
            if (this.trace)
                if (currentPacket.getIsOAM())
                    System.out.println("(OtoOLink) Trace: sending OAM packet from router" + r1NIC.getParent().getAddress() + " to router " + r2NIC.getParent().getAddress());
                else
                    System.out.println("(OtoOLink) Trace: sending packet from router" + r1NIC.getParent().getAddress() + " to router " + r2NIC.getParent().getAddress());
            this.r2NIC.receivePacket(currentPacket);
        } else if (this.r2NIC.equals(nic)) {
            if (this.trace)
                if (currentPacket.getIsOAM())
                    System.out.println("(OtoOLink) Trace: sending OAM packet from router " + r2NIC.getParent().getAddress() + " to router " + r1NIC.getParent().getAddress());
                else
                    System.out.println("(OtoOLink) Trace: sending packet from router" + r1NIC.getParent().getAddress() + " to router " + r2NIC.getParent().getAddress());
            this.r1NIC.receivePacket(currentPacket);
        } else
            System.err.println("(OtoOLink) Error: You are trying to send a packet down a link that you are not connected to");
    }

    public void updateGraph(LSRNIC r1NIC, LSRNIC r2NIC) {
        Node node1;
        Node node2;
        if (GlobalVariables.graph.containsKey(r1NIC.getParent().getAddress())) {
            node1 = GlobalVariables.graph.get(r1NIC.getParent().getAddress());
        } else
            node1 = new Node(r1NIC.getParent().getAddress());
        if (GlobalVariables.graph.containsKey(r2NIC.getParent().getAddress())) {
            node2 = GlobalVariables.graph.get(r2NIC.getParent().getAddress());
        } else
            node2 = new Node(r2NIC.getParent().getAddress());
        //the cost between two nodes is 1
        node1.addLink(node2);
        node2.addLink(node1);

        GlobalVariables.graph.put(node1.getAddress(), node1);
        GlobalVariables.graph.put(node2.getAddress(), node2);

    }

    public int getDestAddr(LSRNIC nic) {
        if (nic == this.r1NIC)
            return r2NIC.getParent().getAddress();
        else
            return r1NIC.getParent().getAddress();
    }
}
