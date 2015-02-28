package DataTypes;

import Dijkstra.Node;

import java.util.HashMap;

/**
 * Created by gs on 11/16/14.
 */
public class GlobalVariables {

    public static HashMap<Integer, Node> graph = new HashMap<Integer, Node>();
    public static int DSCP_BE = 0;
    public static final int PHB_BE = 2;
    public static final int PHB_EF = 0;
    public static final int PHB_AF = 1;
    public static final int DSCP_AF11 = 10;
    public static final int DSCP_AF12 = 12;
    public static final int DSCP_AF13 = 14;
    public static final int DSCP_AF21 = 18;
    public static final int DSCP_AF22 = 20;
    public static final int DSCP_AF23 = 22;
    public static final int DSCP_AF31 = 26;
    public static final int DSCP_AF32 = 28;
    public static final int DSCP_AF33 = 30;
    public static final int DSCP_AF41 = 34;
    public static final int DSCP_AF42 = 36;
    public static final int DSCP_AF43 = 38;
    public static final int DSCP_EF = 46;
}
