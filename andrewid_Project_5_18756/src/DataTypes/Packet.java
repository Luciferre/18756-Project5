package DataTypes;

import java.util.LinkedList;
import java.util.Queue;

public class Packet {
    private int source, dest, DSCP; // The source and destination addresses
    private boolean OAM = false;
    private Queue<MPLS> MPLSheader = new LinkedList<MPLS>(); // all of the MPLS headers in this router
    private int traceID;
    private String type;
//    private int resendSource;
//    private int resendDest;
//    private int resendDSCP;
//    private int classes;
//    private int bandwidth;
//    private int PHB;

    /**
     * The default constructor for a packet
     *
     * @param source the source ip address of this packet
     * @param dest   the destination ip address of this packet
     * @param DSCP   Differential Services Code Point
     * @since 1.0
     */
    public Packet(int source, int dest, int DSCP) {
        try {
            this.source = source;
            this.dest = dest;
            this.DSCP = DSCP;
            this.traceID = (int) (Math.random() * 100000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an MPLS header to a packet
     *
     * @since 1.0
     */
    public void addMPLSheader(MPLS header) {
        MPLSheader.add(header);
    }

    /**
     * Pops an MPLS header from the packet
     *
     * @since 1.0
     */
    public MPLS popMPLSheader() {
        return MPLSheader.poll();
    }

    /**
     * Returns the source ip address of this packet
     *
     * @return the source ip address of this packet
     * @since 1.0
     */
    public int getSource() {
        return this.source;
    }

    /**
     * Returns the destination ip address of this packet
     *
     * @return the destination ip address of this packet
     * @since 1.0
     */
    public int getDest() {
        return this.dest;
    }

    /**
     * Set the DSCP field
     *
     * @param DSCP the DSCP field value
     * @since 1.0
     */
    public void setDSCP(int dSCP) {
        this.DSCP = dSCP;
    }

    /**
     * Returns the DSCP field
     *
     * @return the DSCP field
     * @since 1.0
     */
    public int getDSCP() {
        return this.DSCP;
    }

    /**
     * Returns the trace ID for this packet
     *
     * @return the trace ID for this packet
     * @since 1.0
     */
    public int getTraceID() {
        return this.traceID;
    }

    /**
     * Gets if this packet contains OAM information or data
     *
     * @return true if this packet contains OAM
     * @since 1.0
     */
    public boolean getIsOAM() {
        return this.OAM;
    }

    /**
     * Sets if this cell contains OAM information or data
     *
     * @param isOAM if true the cell contains OAM
     * @since 1.0
     */
    public void setIsOAM(boolean isOAM) {
        this.OAM = isOAM;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int classifyDSCP() {
        if (this.DSCP == 46)
            return GlobalVariables.PHB_EF;
        else if (this.DSCP >= 10 && this.DSCP <= 40) {
            return GlobalVariables.PHB_AF;
        } else if (this.DSCP == 0)
            return GlobalVariables.PHB_BE;
        return -1;
    }

    public int getAFClass() {

        if (this.DSCP == GlobalVariables.DSCP_AF11 || this.DSCP == GlobalVariables.DSCP_AF12 || this.DSCP == GlobalVariables.DSCP_AF13)
            return 1;
        else if (this.DSCP == GlobalVariables.DSCP_AF21 || this.DSCP == GlobalVariables.DSCP_AF22 || this.DSCP == GlobalVariables.DSCP_AF23)
            return 2;
        else if (this.DSCP == GlobalVariables.DSCP_AF31 || this.DSCP == GlobalVariables.DSCP_AF32 || this.DSCP == GlobalVariables.DSCP_AF33)
            return 3;
        else if (this.DSCP == GlobalVariables.DSCP_AF41 || this.DSCP == GlobalVariables.DSCP_AF42 || this.DSCP == GlobalVariables.DSCP_AF43)
            return 4;
        return 0;
    }

    public int getDropPriority() {
        int mask = 0x06;
        int maskedDSCP = mask & DSCP;
        return maskedDSCP >> 1;
    }


}

