package DataTypes;

import NetworkElements.LSRNIC;

/**
 * Created by gs on 11/16/14.
 */
public class NICLabelPair {
    private LSRNIC nic; // The nic of the pair
    private int label; // the label of the pair

    /**
     * Constructor for a pair of (nic, label)
     *
     * @param nic   the nic that is in the pair
     * @param label the label that is in the pair
     * @since 1.0
     */
    public NICLabelPair(LSRNIC nic, int label) {
        this.nic = nic;
        this.label = label;
    }

    /**
     * Returns the nic that makes up half of the pair
     *
     * @return the nic that makes up half of the pair
     * @since 1.0
     */
    public LSRNIC getNIC() {
        return this.nic;
    }

    /**
     * Returns the nic that makes up half of the pair
     *
     * @return the nic that makes up half of the pair
     * @since 1.0
     */
    public int getlabel() {
        return this.label;
    }

    /**
     * Returns whether or not a given object is the same as this pair. I.e. it is a pair containing the same nic and label
     *
     * @return true/false the given object of the same as this object
     * @since 1.0
     */
    public boolean equals(Object o) {
        if (o instanceof NICLabelPair) {
            NICLabelPair other = (NICLabelPair) o;

            if (other.getNIC() == this.getNIC() && other.getlabel() == this.getlabel())
                return true;
        }

        return false;
    }

    /**
     * Allows this object to be used in a TreeMap
     *
     * @returns if this object is less than, equal to, or greater than a given object
     * @since 1.0
     */
    public int compareTo(NICLabelPair o) {
        return this.getlabel() - o.getlabel();
    }
}
