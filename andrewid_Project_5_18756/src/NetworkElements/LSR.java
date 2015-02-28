package NetworkElements;

import java.util.*;

import DataTypes.*;
import Dijkstra.Dijkstra;

public class LSR {
    private int address; // The AS address of this router
    private ArrayList<LSRNIC> nics = new ArrayList<LSRNIC>(); // all of the nics in this router
    private boolean isInitial = true;
    private Dijkstra dj = new Dijkstra();
    private TreeMap<Integer, NICLabelPair> LabeltoLabel = new TreeMap<Integer, NICLabelPair>(); // a map of input Label to output nic and new Label number
    private HashMap<Integer, Integer> LabeltoDest = new HashMap<Integer, Integer>(); // a map of input label to destination
    private HashMap<Integer, Integer> routingTable = new HashMap<Integer, Integer>();//the next router to get to the destination
    private ArrayList<Packet> waitedPackets = new ArrayList<Packet>();
    private ArrayList<Packet> newwaitedPackets = new ArrayList<Packet>();
    private boolean trace = false;

    private ArrayList<Integer> isEstabLSP = new ArrayList<Integer>();

    /**
     * The default constructor for an ATM router
     *
     * @param address the address of the router
     * @since 1.0
     */
    public LSR(int address) {
        this.address = address;
    }

    /**
     * The return the router's address
     *
     * @since 1.0
     */
    public int getAddress() {
        return this.address;
    }

    /**
     * Adds a nic to this router
     *
     * @param nic the nic to be added
     * @since 1.0
     */
    public void addNIC(LSRNIC nic) {
        this.nics.add(nic);
    }

    /**
     * This method processes data and OAM currentPackets that arrive from any nic with this router as a destination
     *
     * @param currentPacket the packet that arrived at this router
     * @param nic           the nic that the currentPacket arrived on
     * @since 1.0
     */
    public void receivePacket(Packet currentPacket, LSRNIC nic) {


        if (currentPacket.getIsOAM()) {
            // What's OAM for? set up LSP
            //setup
            if (currentPacket.getType().equals("Path")) {

                receivedPath(currentPacket);
                LSRNIC forwardNIC = getRoutingNic(currentPacket.getDest());
                if (forwardNIC != null) {

                    sentPath(currentPacket);
                    forwardNIC.sendPacket(currentPacket, this);
                } else if (currentPacket.getDest() == this.address) {
                    int i;
                    for (i = 1; i <= LabeltoLabel.size(); i++) {
                        if (!LabeltoLabel.containsKey(i)) {
                            break;
                        }
                    }
                    System.out.println("Trace (LSRRouter): First free LSR = " + i);
                    //LabeltoLabel.put(i, null);
                    MPLS mpls = new MPLS(i, 0, 1);
                    Packet resvPacket = new Packet(currentPacket.getDest(), currentPacket.getSource(), currentPacket.getDSCP());
                    resvPacket.addMPLSheader(mpls);
                    resvPacket.setType("Resv");
//                    resvPacket.setClasses(currentPacket.getClasses());
//                    resvPacket.setBandwidth(currentPacket.getBandwidth());
//                    resvPacket.setPHB(currentPacket.getPHB());
                    sentResv(resvPacket);
                    resvPacket.setIsOAM(true);
                    nic.sendPacket(resvPacket, this);
                }


            }
            //Resv
            else if (currentPacket.getType().equals("Resv")) {


                receivedResv(currentPacket);
                int i;
                // calculate the available input Label
                for (i = 1; i <= LabeltoLabel.size(); i++) {
                    if (!LabeltoLabel.containsKey(i)) {
                        break;
                    }
                }
                MPLS oldmpls = currentPacket.popMPLSheader();
                MPLS newmpls;
                NICLabelPair newNicLabelPair;
                if (oldmpls != null) {
                    newmpls = new MPLS(i, oldmpls.getTrafficClass(), oldmpls.getStackingBit());
                    newNicLabelPair = new NICLabelPair(nic, oldmpls.getLabel());
                } else {
                    newmpls = new MPLS(i, 0, 1);
                    newNicLabelPair = new NICLabelPair(nic, -1);

                }
                LabeltoLabel.put(i, newNicLabelPair);
                if (this.getAddress() == currentPacket.getDest()) {
//                    if (nic.reserveBandwidth(currentPacket)) {

                    Packet ResvConfPacket = new Packet(currentPacket.getDest(), currentPacket.getSource(), currentPacket.getDSCP());
                    sentResvConf(ResvConfPacket);
                    ResvConfPacket.setIsOAM(true);
                    ResvConfPacket.setType("ResvConf");
                    nic.sendPacket(ResvConfPacket, this);


                    LabeltoDest.put(i, getHashcode(currentPacket.getSource(), currentPacket.getDSCP()));
                    newwaitedPackets.clear();
                    boolean isSent = false;
                    for (Packet packet : waitedPackets) {
                        isSent = false;
                        for (Integer key : LabeltoDest.keySet()) {
                            if (LabeltoDest.get(key) == getHashcode(packet.getDest(), packet.getDSCP())) {
                                MPLS mpls = new MPLS(key, 0, 1);
                                packet.addMPLSheader(mpls);
                                LabeltoLabel.get(key).getNIC().sendPacket(packet, this);
                                // waitedPackets.remove(packet);
                                isSent = true;
                            }
                        }
                        if (isSent == false) {
                            newwaitedPackets.add(packet);
                            if (!isEstabLSP.contains(getHashcode(packet.getDest(), packet.getDSCP())))
                                setupLSP(packet);
                        }
                    }
                    waitedPackets.clear();
                    waitedPackets.addAll(newwaitedPackets);
                    for (int j = 0; j < isEstabLSP.size(); j++) {
                        if (isEstabLSP.get(j) == getHashcode(currentPacket.getSource(), currentPacket.getDSCP()))
                            isEstabLSP.remove(j);
//                        }
                    }
                } else {
//                    if(nic.reserveBandwidth(currentPacket)) {
                    currentPacket.addMPLSheader(newmpls);
                    //	System.out.println(i);
                    sentResv(currentPacket);
                    getRoutingNic(currentPacket.getDest()).sendPacket(currentPacket, this);
//                    }else{
//                        Packet ResvErrPacket = new Packet(currentPacket.getDest(), currentPacket.getSource(), currentPacket.getDSCP());
//                        sentResvErr(ResvErrPacket);
//                        ResvErrPacket.setIsOAM(true);
//                        ResvErrPacket.setType("ResvErr");
//                        nic.sendPacket(ResvErrPacket, this);
//                    }
                }


            }
            //ResvConf
            else if (currentPacket.getType().equals("ResvConf")) {
                receivedResvConf(currentPacket);
                if (this.getAddress() != currentPacket.getDest()) {

                    sentResvConf(currentPacket);
                    getRoutingNic(currentPacket.getDest()).sendPacket(currentPacket, this);
                }

            } else if (currentPacket.getType().equals("PathErr")) {
                receivedPathErr(currentPacket);
                if (this.getAddress() != currentPacket.getDest()) {
                    sentPathErr(currentPacket);
                    getRoutingNic(currentPacket.getDest()).sendPacket(currentPacket, this);
                }
            } else if (currentPacket.getType().equals("ResvErr")) {
                receivedResvErr(currentPacket);
                if (this.getAddress() != currentPacket.getDest()) {
                    sentResvErr(currentPacket);
                    getRoutingNic(currentPacket.getDest()).sendPacket(currentPacket, this);
                }
            }
        } else {
            // find the nic and new VC number to forward the currentPacket on
            // otherwise the currentPacket has nowhere to go. output to the console and drop the currentPacket
            MPLS oldmpls = currentPacket.popMPLSheader();
            MPLS newmpls;
            if (this.address != currentPacket.getDest()) {
                if (LabeltoLabel.containsKey(oldmpls.getLabel())) {
                    NICLabelPair newNicLabelPair = LabeltoLabel.get(oldmpls.getLabel());
                    newmpls = new MPLS(newNicLabelPair.getlabel(), oldmpls.getTrafficClass(), oldmpls.getStackingBit());
                    currentPacket.addMPLSheader(newmpls);
                    newNicLabelPair.getNIC().sendPacket(currentPacket, this);

                }
            } else {
                if (trace)
                    System.out.println("Trace (ATMRouter" + this.address + "): Received a packet " + currentPacket.getTraceID());
            }
        }
    }

    /**
     * This method creates a packet with the specified type of service field and sends it to a destination
     *
     * @param destination the distination router
     * @param DSCP        the differentiated services code point field
     * @since 1.0
     */
    public void createPacket(int destination, int DSCP) {
        Packet newPacket = new Packet(this.getAddress(), destination, DSCP);
        this.sendPacket(newPacket);
    }

    /**
     * This method allocates bandwidth for a specific traffic class from the current router to the destination router
     *
     * @param dest      destination router id
     * @param PHB       0=EF, 1=AF, 2=BE
     * @param Class     AF classes 1,2,3,4. (0 if EF or BE)
     * @param Bandwidth number of packets per time unit for this PHB/Class
     * @since 1.0
     */
    public void allocateBandwidth(int dest, int PHB, int Class, int Bandwidth) {

    }

    /**
     * This method forwards a packet to the correct nic or drops if at destination router
     *
     * @param newPacket The packet that has just arrived at the router.
     * @since 1.0
     */
    public void sendPacket(Packet newPacket) {
        if (isInitial == true) {
            updateRoutingTable();
            isInitial = false;
        }

        //This method should send the packet to the correct NIC.
        //no lsp and establish new lsp
        if (!LabeltoDest.containsValue(getHashcode(newPacket.getDest(), newPacket.getDSCP()))) {
            //   waitedPackets.add(newPacket);
            if (isEstabLSP.contains(getHashcode(newPacket.getDest(), newPacket.getDSCP())))
                waitedPackets.add(newPacket);
            else {
                setupLSP(newPacket);
                waitedPackets.add(newPacket);
            }
        }
        //send new packet
        else {
            for (Integer key : LabeltoDest.keySet()) {
                if (LabeltoDest.get(key) == getHashcode(newPacket.getDest(), newPacket.getDSCP())) {
                    MPLS mpls = new MPLS(key, 0, 1);
                    newPacket.addMPLSheader(mpls);
                    LabeltoLabel.get(key).getNIC().sendPacket(newPacket, this);

                }
            }

        }
    }

    /**
     * Makes each nic move its currentPackets from the output buffer across the link to the next router's nic
     *
     * @since 1.0
     */
    public void sendPackets() {
        if (isInitial == true) {
            updateRoutingTable();
            isInitial = false;
        }

        for (int i = 0; i < this.nics.size(); i++)
            this.nics.get(i).sendPackets();
    }

    /**
     * Makes each nic move all of its currentPackets from the input buffer to the output buffer
     *
     * @since 1.0
     */
    public void recievePackets() {
        for (int i = 0; i < this.nics.size(); i++)
            this.nics.get(i).recievePackets();
    }

    private void updateRoutingTable() {
        dj.DijkstraAlgorithm(this.address);
        routingTable = dj.calRoutingTable(this.address);

    }

    private LSRNIC getRoutingNic(int des) {
        for (LSRNIC nic : nics) {
            if (routingTable.get(des) != null && nic.getLinkDestAddr() == routingTable.get(des)) {
                return nic;
            }
        }
        return null;
    }

    public void setupLSP(Packet newPacket) {
        //  allocateBandwidth(newPacket.getDest(), GlobalVariables.PHB_BE, 0, 0);
        Packet setupPacket = new Packet(newPacket.getSource(), newPacket.getDest(), newPacket.getDSCP());
//        setupPacket.setBandwidth(Bandwidth);
//        setupPacket.setClasses(Class);
//        setupPacket.setPHB(PHB);
        sentPath(setupPacket);
        setupPacket.setIsOAM(true);
        setupPacket.setType("Path");
        setupPacket.setDSCP(newPacket.getDSCP());
        getRoutingNic(newPacket.getDest()).sendPacket(setupPacket, this);
        //  LabeltoLabel.put(newPacket.getSource(), new NICLabelPair(getRoutingNic(newPacket.getDest()),-1));
        isEstabLSP.add(getHashcode(newPacket.getDest(), newPacket.getDSCP()));

    }

    public int getHashcode(int dest, int dscp) {
        return dest * 100 + dscp;
    }

    /**
     * Outputs to the console that a packet has been dropped because it reached its destination
     *
     * @since 1.0
     */
    public void packetDeadEnd(Packet packet) {
      //  if (this.trace)
            System.out.println("The packet is destined for this router (" + this.address + "), taken off network " + packet.getTraceID());
    }


    /**
     * Outputs to the console that a connect message has been sent
     *
     * @since 1.0
     */
    private void sentPath(Packet packet) {
        //if (this.trace)
            System.out.println("PATH: Router " + this.address + " sent a PATH to Router " + routingTable.get(packet.getDest()));
    }

    /**
     * Outputs to the console that a setup message has been sent
     *
     * @since 1.0
     */
    private void receivedPath(Packet packet) {
    //    if (this.trace)
            System.out.println("PATH: Router " + this.address + "  received a PATH from Router " + routingTable.get(packet.getSource()));
    }

    /**
     * Outputs to the console that a connect message has been sent
     *
     * @since 1.0
     */
    private void sentResv(Packet packet) {
     //   if (this.trace)
            System.out.println("RESV: Router " + this.address + " sent a RESV to Router " + routingTable.get(packet.getDest()));
    }

    /**
     * Outputs to the console that a connect message has been sent
     *
     * @since 1.0
     */
    private void receivedResv(Packet packet) {
       // if (this.trace)
            System.out.println("RESV: Router " + this.address + "  received a RESV from Router " + routingTable.get(packet.getSource()));
    }

    /**
     * Outputs to the console that a connect message has been received
     *
     * @since 1.0
     */
    private void sentResvConf(Packet packet) {
      //  if (this.trace)
            System.out.println("RESVCONF: Router " + this.address + " sent a RESVCONF to Router " + routingTable.get(packet.getDest()));
    }


    /**
     * Outputs to the console that a connect ack message has been received
     *
     * @since 1.0
     */
    private void receivedResvConf(Packet packet) {
       // if (this.trace)
            System.out.println("RESVCONF: Router " + this.address + "  received a RESVCONF from Router " + routingTable.get(packet.getSource()));
    }

    /**
     * Outputs to the console that a wait message has been sent
     *
     * @since 1.0
     */
    private void sentPathErr(Packet packet) {
       // if (this.trace)
            System.out.println("PATHERR: Router " + this.address + " sent a PATHERR to Router " + routingTable.get(packet.getDest()));
    }

    /**
     * Outputs to the console that a wait message has been received
     *
     * @since 1.0
     */
    private void receivedPathErr(Packet packet) {
        //if (this.trace)
            System.out.println("PATHERR: Router " + this.address + "  received a PATHERR from Router " + routingTable.get(packet.getSource()));
    }

    /**
     * Outputs to the console that a wait message has been sent
     *
     * @since 1.0
     */
    private void sentResvErr(Packet packet) {
       // if (this.trace)
            System.out.println("RESVERR: Router " + this.address + " sent a RESVERR to Router " + routingTable.get(packet.getDest()));
    }

    /**
     * Outputs to the console that a wait message has been received
     *
     * @since 1.0
     */
    private void receivedResvErr(Packet packet) {
        //if (this.trace)
            System.out.println("RESVERR: Router " + this.address + "  received a RESVERR from Router " + routingTable.get(packet.getSource()));
    }


}

