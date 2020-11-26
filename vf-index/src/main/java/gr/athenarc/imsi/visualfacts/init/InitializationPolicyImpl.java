package gr.athenarc.imsi.visualfacts.init;

import gr.athenarc.imsi.visualfacts.Schema;
import gr.athenarc.imsi.visualfacts.Tile;
import gr.athenarc.imsi.visualfacts.query.Query;

import java.util.List;

public class InitializationPolicyImpl extends InitializationPolicy {
    public InitializationPolicyImpl(Query q0, int noOfSubTiles, Schema schema, Integer categoricalNodeBudget) {
        super(q0, noOfSubTiles, schema, categoricalNodeBudget);
    }

    @Override
    public double initTileTreeCategoricalAttrs(List<Tile> leafTiles) {
        return 0;
    }
}
