package gr.athenarc.imsi.visualfacts.init;

import gr.athenarc.imsi.visualfacts.query.Query;
import gr.athenarc.imsi.visualfacts.query.QueryResults;

public class InitializationResults extends QueryResults {

    double totalUtil;

    public InitializationResults(Query query) {
        super(query);
    }

    public double getTotalUtil() {
        return totalUtil;
    }

    public void setTotalUtil(double totalUtil) {
        this.totalUtil = totalUtil;
    }

    @Override
    public String toString() {
        return "InitializationResults{" +
                "totalUtil=" + totalUtil +
                "} " + super.toString();
    }
}
